package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrainDatabase
import com.example.data.BrainRepository
import com.example.data.WorkoutEntity
import com.example.model.BrainDataRepository
import com.example.model.BrainLobe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.max
import kotlin.random.Random

enum class ActiveGame {
    NONE,
    MEMORY_GRID,
    STROOP_SPEED,
    SYNAPSE_MATH,
    NEURO_QUIZ,
    FOCUS_TRAINER
}

enum class MemoryGameMode {
    CARD_MATCH,       // Simple, un-confusing pair match (universally understood)
    PATTERN_SEQUENCE  // Visual pattern recall (with step badges, replay, and 3 lives)
}

data class MemoryCardItem(
    val id: Int,
    val pairId: Int,
    val title: String,
    val symbol: String,
    val color: Color,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

data class StroopChallenge(
    val wordText: String,
    val textColor: Color,
    val colorName: String,
    val options: List<Pair<String, Color>>
)

data class MathChallenge(
    val expression: String,
    val options: List<Int>,
    val correctIndex: Int
)

data class GameResult(
    val gameType: String,
    val score: Int,
    val accuracy: Float,
    val avgReactionTimeMs: Long,
    val levelReached: Int,
    val bqGain: Int
)

class BrainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrainRepository
    private val insightsRepository = com.example.data.repository.BrainInsightsRepository()

    init {
        val db = BrainDatabase.getInstance(application)
        repository = BrainRepository(db.workoutDao())
    }

    val workoutHistory: StateFlow<List<WorkoutEntity>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _brainInsight = MutableStateFlow<String?>(null)
    val brainInsight: StateFlow<String?> = _brainInsight.asStateFlow()

    private val _isFetchingInsight = MutableStateFlow(false)
    val isFetchingInsight: StateFlow<Boolean> = _isFetchingInsight.asStateFlow()

    fun fetchPersonalizedInsight() {
        if (_isFetchingInsight.value) return
        
        viewModelScope.launch {
            _isFetchingInsight.value = true
            try {
                val workouts = workoutHistory.value
                val bq = calculateBrainQuotient(workouts)
                val summary = generateWorkoutSummary(workouts)
                
                val insight = insightsRepository.getPersonalizedInsights(summary, bq)
                _brainInsight.value = insight
            } finally {
                _isFetchingInsight.value = false
            }
        }
    }

    private fun generateWorkoutSummary(workouts: List<WorkoutEntity>): String {
        if (workouts.isEmpty()) return "No workouts completed yet."
        val recent = workouts.take(5)
        return recent.joinToString("\n") { 
            "- ${it.gameType}: Score ${it.score}, Accuracy ${(it.accuracy * 100).toInt()}%" 
        }
    }

    // Navigation & Inspection State
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedLobe = MutableStateFlow<BrainLobe>(BrainDataRepository.brainLobes[0])
    val selectedLobe: StateFlow<BrainLobe> = _selectedLobe.asStateFlow()

    private val _activeGame = MutableStateFlow(ActiveGame.NONE)
    val activeGame: StateFlow<ActiveGame> = _activeGame.asStateFlow()

    private val _lastGameResult = MutableStateFlow<GameResult?>(null)
    val lastGameResult: StateFlow<GameResult?> = _lastGameResult.asStateFlow()

    // ----------------------------------------------------
    // SIMPLE MEMORY GAME STATE (Card Pairs + Pattern Sequence)
    // ----------------------------------------------------
    private val _memoryMode = MutableStateFlow(MemoryGameMode.CARD_MATCH)
    val memoryMode: StateFlow<MemoryGameMode> = _memoryMode.asStateFlow()

    // Card Match State (Zero confusion, classic pairs)
    private val _cardList = MutableStateFlow<List<MemoryCardItem>>(emptyList())
    val cardList: StateFlow<List<MemoryCardItem>> = _cardList.asStateFlow()

    private val _cardMoves = MutableStateFlow(0)
    val cardMoves: StateFlow<Int> = _cardMoves.asStateFlow()

    private val _cardMatchedPairs = MutableStateFlow(0)
    val cardMatchedPairs: StateFlow<Int> = _cardMatchedPairs.asStateFlow()

    private val _isCardMatchWon = MutableStateFlow(false)
    val isCardMatchWon: StateFlow<Boolean> = _isCardMatchWon.asStateFlow()

    private val _isCheckingCards = MutableStateFlow(false)
    val isCheckingCards: StateFlow<Boolean> = _isCheckingCards.asStateFlow()

    private val _cardElapsedTimeSec = MutableStateFlow(0)
    val cardElapsedTimeSec: StateFlow<Int> = _cardElapsedTimeSec.asStateFlow()

    private var cardTimerJob: Job? = null
    private val flippedCardIndices = mutableListOf<Int>()

    // Pattern Sequence State (Un-confusing: step badges, replay, 3 lives)
    private val _gridSequence = MutableStateFlow<List<Int>>(emptyList())
    val gridSequence: StateFlow<List<Int>> = _gridSequence.asStateFlow()

    private val _activeHighlightedTile = MutableStateFlow<Int?>(null)
    val activeHighlightedTile: StateFlow<Int?> = _activeHighlightedTile.asStateFlow()

    private val _patternStepNumber = MutableStateFlow<Int?>(null)
    val patternStepNumber: StateFlow<Int?> = _patternStepNumber.asStateFlow()

    private val _patternFeedback = MutableStateFlow("Tap any card to begin")
    val patternFeedback: StateFlow<String> = _patternFeedback.asStateFlow()

    private val _lastTappedMistakeTile = MutableStateFlow<Int?>(null)
    val lastTappedMistakeTile: StateFlow<Int?> = _lastTappedMistakeTile.asStateFlow()

    private val _memoryLives = MutableStateFlow(3)
    val memoryLives: StateFlow<Int> = _memoryLives.asStateFlow()

    private val _userMemoryInput = MutableStateFlow<List<Int>>(emptyList())
    val userMemoryInput: StateFlow<List<Int>> = _userMemoryInput.asStateFlow()

    private val _isShowingSequence = MutableStateFlow(false)
    val isShowingSequence: StateFlow<Boolean> = _isShowingSequence.asStateFlow()

    private val _memoryLevel = MutableStateFlow(1)
    val memoryLevel: StateFlow<Int> = _memoryLevel.asStateFlow()

    private val _memoryScore = MutableStateFlow(0)
    val memoryScore: StateFlow<Int> = _memoryScore.asStateFlow()

    private val _isMemoryGameOver = MutableStateFlow(false)
    val isMemoryGameOver: StateFlow<Boolean> = _isMemoryGameOver.asStateFlow()

    private var memoryPlaybackJob: Job? = null

    // ----------------------------------------------------
    // STROOP SPEED GAME STATE
    // ----------------------------------------------------
    private val _stroopRound = MutableStateFlow(1)
    val stroopRound: StateFlow<Int> = _stroopRound.asStateFlow()

    private val _stroopChallenge = MutableStateFlow<StroopChallenge?>(null)
    val stroopChallenge: StateFlow<StroopChallenge?> = _stroopChallenge.asStateFlow()

    private val _stroopScore = MutableStateFlow(0)
    val stroopScore: StateFlow<Int> = _stroopScore.asStateFlow()

    private val _stroopTimeLeft = MutableStateFlow(1.0f) // 1.0 down to 0.0
    val stroopTimeLeft: StateFlow<Float> = _stroopTimeLeft.asStateFlow()

    private val _isStroopGameOver = MutableStateFlow(false)
    val isStroopGameOver: StateFlow<Boolean> = _isStroopGameOver.asStateFlow()

    private var stroopRoundStartTime = 0L
    private val stroopReactionTimes = mutableListOf<Long>()
    private var stroopCorrectAnswers = 0
    private var stroopTimerJob: Job? = null

    // ----------------------------------------------------
    // SYNAPSE MATH GAME STATE
    // ----------------------------------------------------
    private val _mathRound = MutableStateFlow(1)
    val mathRound: StateFlow<Int> = _mathRound.asStateFlow()

    private val _mathChallenge = MutableStateFlow<MathChallenge?>(null)
    val mathChallenge: StateFlow<MathChallenge?> = _mathChallenge.asStateFlow()

    private val _mathScore = MutableStateFlow(0)
    val mathScore: StateFlow<Int> = _mathScore.asStateFlow()

    private val _mathTimeLeft = MutableStateFlow(1.0f)
    val mathTimeLeft: StateFlow<Float> = _mathTimeLeft.asStateFlow()

    private val _isMathGameOver = MutableStateFlow(false)
    val isMathGameOver: StateFlow<Boolean> = _isMathGameOver.asStateFlow()

    private var mathRoundStartTime = 0L
    private val mathReactionTimes = mutableListOf<Long>()
    private var mathCorrectAnswers = 0
    private var mathTimerJob: Job? = null

    // ----------------------------------------------------
    // NEURO QUIZ STATE
    // ----------------------------------------------------
    private val _quizQuestionIndex = MutableStateFlow(0)
    val quizQuestionIndex: StateFlow<Int> = _quizQuestionIndex.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    private val _quizIsAnswered = MutableStateFlow(false)
    val quizIsAnswered: StateFlow<Boolean> = _quizIsAnswered.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _isQuizGameOver = MutableStateFlow(false)
    val isQuizGameOver: StateFlow<Boolean> = _isQuizGameOver.asStateFlow()

    // ----------------------------------------------------
    // FLASHCARD STATE
    // ----------------------------------------------------
    private val _currentFlashcardIndex = MutableStateFlow(0)
    val currentFlashcardIndex: StateFlow<Int> = _currentFlashcardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectLobe(lobe: BrainLobe) {
        _selectedLobe.value = lobe
    }

    fun openGame(game: ActiveGame) {
        _activeGame.value = game
        _lastGameResult.value = null
        when (game) {
            ActiveGame.MEMORY_GRID -> startMemoryGridGame()
            ActiveGame.STROOP_SPEED -> startStroopGame()
            ActiveGame.SYNAPSE_MATH -> startMathGame()
            ActiveGame.NEURO_QUIZ -> startQuizGame()
            ActiveGame.FOCUS_TRAINER -> cancelAllGameJobs()
            ActiveGame.NONE -> {}
        }
    }

    fun exitCurrentGame() {
        cancelAllGameJobs()
        _activeGame.value = ActiveGame.NONE
    }

    private fun cancelAllGameJobs() {
        memoryPlaybackJob?.cancel()
        cardTimerJob?.cancel()
        stroopTimerJob?.cancel()
        mathTimerJob?.cancel()
    }

    // ====================================================
    // SIMPLE MEMORY LOGIC (Card Match & Pattern Sequence)
    // ====================================================

    fun setMemoryMode(mode: MemoryGameMode) {
        _memoryMode.value = mode
        if (mode == MemoryGameMode.CARD_MATCH) {
            if (_cardList.value.isEmpty() || _isCardMatchWon.value) {
                startCardMatchGame()
            }
        } else {
            if (_gridSequence.value.isEmpty() || _isMemoryGameOver.value) {
                startPatternSequenceGame()
            }
        }
    }

    /**
     * Card Match (Pair Matching):
     * The classic, universally understood memory game.
     * Flip 2 cards to find matching neuroscience icons.
     * Zero confusion, pure spatial working memory.
     */
    fun startCardMatchGame() {
        cancelAllGameJobs()
        _isCardMatchWon.value = false
        _cardMoves.value = 0
        _cardMatchedPairs.value = 0
        _cardElapsedTimeSec.value = 0
        _isCheckingCards.value = false
        flippedCardIndices.clear()

        val templates = listOf(
            Triple("Neuron", "⚡", Color(0xFF38BDF8)),
            Triple("Cortex", "🧠", Color(0xFFA855F7)),
            Triple("Synapse", "💡", Color(0xFFF59E0B)),
            Triple("Vision", "👁️", Color(0xFF10B981)),
            Triple("Helix", "🧬", Color(0xFFEC4899)),
            Triple("Emotion", "❤️", Color(0xFFEF4444))
        )

        val cards = mutableListOf<MemoryCardItem>()
        var idCounter = 0
        templates.forEachIndexed { pairId, (title, symbol, color) ->
            cards.add(MemoryCardItem(id = idCounter++, pairId = pairId, title = title, symbol = symbol, color = color))
            cards.add(MemoryCardItem(id = idCounter++, pairId = pairId, title = title, symbol = symbol, color = color))
        }
        _cardList.value = cards.shuffled()

        // Elapsed time counter
        cardTimerJob = viewModelScope.launch {
            while (!_isCardMatchWon.value) {
                delay(1000)
                _cardElapsedTimeSec.value += 1
            }
        }
    }

    fun onCardTapped(cardId: Int) {
        if (_isCheckingCards.value || _isCardMatchWon.value) return
        val currentCards = _cardList.value
        val clickedIndex = currentCards.indexOfFirst { it.id == cardId }
        if (clickedIndex == -1) return

        val card = currentCards[clickedIndex]
        if (card.isFaceUp || card.isMatched) return

        // Flip card face up
        val updated = currentCards.toMutableList()
        updated[clickedIndex] = card.copy(isFaceUp = true)
        _cardList.value = updated
        flippedCardIndices.add(clickedIndex)

        if (flippedCardIndices.size == 2) {
            _cardMoves.value += 1
            val idx1 = flippedCardIndices[0]
            val idx2 = flippedCardIndices[1]
            val c1 = updated[idx1]
            val c2 = updated[idx2]

            if (c1.pairId == c2.pairId) {
                // Match found!
                val matchedList = updated.toMutableList()
                matchedList[idx1] = c1.copy(isMatched = true)
                matchedList[idx2] = c2.copy(isMatched = true)
                _cardList.value = matchedList
                _cardMatchedPairs.value += 1
                flippedCardIndices.clear()

                if (_cardMatchedPairs.value >= 6) {
                    _isCardMatchWon.value = true
                    cardTimerJob?.cancel()
                    val moves = _cardMoves.value
                    val timeSec = _cardElapsedTimeSec.value
                    val score = max(100, 1200 - (moves * 30) - (timeSec * 4))
                    val accuracy = (6f / moves.coerceAtLeast(6)).coerceIn(0.5f, 1.0f)

                    saveWorkoutResult(
                        gameType = "MEMORY_GRID",
                        score = score,
                        accuracy = accuracy,
                        avgReactionTime = (timeSec * 1000L / moves.coerceAtLeast(1)),
                        level = 1
                    )
                }
            } else {
                // Mismatch: show for 850ms then flip back
                _isCheckingCards.value = true
                viewModelScope.launch {
                    delay(850)
                    val resetList = _cardList.value.toMutableList()
                    resetList[idx1] = resetList[idx1].copy(isFaceUp = false)
                    resetList[idx2] = resetList[idx2].copy(isFaceUp = false)
                    _cardList.value = resetList
                    flippedCardIndices.clear()
                    _isCheckingCards.value = false
                }
            }
        }
    }

    /**
     * Peeks at cards for 1.2 seconds so users never feel stuck or confused.
     */
    fun peekCards() {
        if (_isCheckingCards.value || _isCardMatchWon.value) return
        viewModelScope.launch {
            _isCheckingCards.value = true
            val prev = _cardList.value
            _cardList.value = prev.map { it.copy(isFaceUp = true) }
            delay(1200)
            _cardList.value = prev.map { if (it.isMatched) it else it.copy(isFaceUp = false) }
            _isCheckingCards.value = false
        }
    }

    /**
     * Pattern Sequence Recall:
     * Overhauled to prevent confusion:
     * - Numbered step badges on each flashing tile (Step #1, #2...)
     * - "Replay Pattern" button so user can re-watch without penalty
     * - 3 Lives instead of abrupt game over on a single misclick
     * - Friendly real-time instructions
     */
    fun startMemoryGridGame() {
        cancelAllGameJobs()
        startCardMatchGame()
        startPatternSequenceGame()
    }

    fun startPatternSequenceGame() {
        cancelAllGameJobs()
        _memoryLevel.value = 1
        _memoryScore.value = 0
        _memoryLives.value = 3
        _isMemoryGameOver.value = false
        _userMemoryInput.value = emptyList()
        _lastTappedMistakeTile.value = null
        generateNewMemorySequence(1)
    }

    private fun generateNewMemorySequence(level: Int) {
        val length = (1 + level).coerceAtMost(7)
        val sequence = List(length) { Random.nextInt(0, 9) }
        _gridSequence.value = sequence
        _userMemoryInput.value = emptyList()
        playSequencePreview(sequence)
    }

    private fun playSequencePreview(sequence: List<Int>) {
        memoryPlaybackJob?.cancel()
        memoryPlaybackJob = viewModelScope.launch {
            _isShowingSequence.value = true
            _patternFeedback.value = "👀 Watch carefully! (${sequence.size} steps)"
            _lastTappedMistakeTile.value = null
            delay(500)
            for ((stepIdx, tile) in sequence.withIndex()) {
                _activeHighlightedTile.value = tile
                _patternStepNumber.value = stepIdx + 1
                _patternFeedback.value = "Step ${stepIdx + 1} of ${sequence.size}"
                delay(650)
                _activeHighlightedTile.value = null
                _patternStepNumber.value = null
                delay(260)
            }
            _isShowingSequence.value = false
            _patternFeedback.value = "👉 Your turn! Tap step 1 of ${sequence.size}"
        }
    }

    fun replayPattern() {
        if (_isShowingSequence.value || _isMemoryGameOver.value) return
        _userMemoryInput.value = emptyList()
        _lastTappedMistakeTile.value = null
        playSequencePreview(_gridSequence.value)
    }

    fun onMemoryTileTapped(index: Int) {
        if (_isShowingSequence.value || _isMemoryGameOver.value) return

        val currentInput = _userMemoryInput.value + index
        val currentIndex = currentInput.size - 1
        val expected = _gridSequence.value.getOrNull(currentIndex)

        if (expected != null && expected == index) {
            _userMemoryInput.value = currentInput
            _lastTappedMistakeTile.value = null

            if (currentInput.size == _gridSequence.value.size) {
                // Completed the round!
                _patternFeedback.value = "🎉 Great job! Level ${_memoryLevel.value} cleared."
                _memoryScore.value += 120 * _memoryLevel.value
                val nextLevel = _memoryLevel.value + 1
                _memoryLevel.value = nextLevel
                viewModelScope.launch {
                    delay(700)
                    generateNewMemorySequence(nextLevel)
                }
            } else {
                val nextStep = currentInput.size + 1
                _patternFeedback.value = "✓ Step ${currentInput.size} matched! Tap step $nextStep of ${_gridSequence.value.size}"
            }
        } else {
            // Mistake made
            _lastTappedMistakeTile.value = index
            val newLives = _memoryLives.value - 1
            _memoryLives.value = newLives

            if (newLives > 0) {
                _patternFeedback.value = "Oops! You have $newLives ${if (newLives == 1) "life" else "lives"} left. Tap 'Replay Pattern' to watch again!"
                _userMemoryInput.value = emptyList()
            } else {
                _patternFeedback.value = "Pattern finished! Great cognitive exercise."
                _isMemoryGameOver.value = true
                val finalScore = _memoryScore.value
                val level = _memoryLevel.value
                val accuracy = (level.toFloat() / (level + 2)).coerceIn(0.5f, 0.95f)
                saveWorkoutResult(
                    gameType = "MEMORY_GRID",
                    score = finalScore,
                    accuracy = accuracy,
                    avgReactionTime = 520L,
                    level = level
                )
            }
        }
    }

    // ====================================================
    // STROOP SPEED LOGIC
    // ====================================================
    private val stroopColors = listOf(
        Pair("RED", Color(0xFFEF4444)),
        Pair("BLUE", Color(0xFF3B82F6)),
        Pair("GREEN", Color(0xFF10B981)),
        Pair("YELLOW", Color(0xFFF59E0B)),
        Pair("PURPLE", Color(0xFF8B5CF6))
    )

    fun startStroopGame() {
        cancelAllGameJobs()
        _stroopRound.value = 1
        _stroopScore.value = 0
        _isStroopGameOver.value = false
        stroopReactionTimes.clear()
        stroopCorrectAnswers = 0
        nextStroopRound()
    }

    private fun nextStroopRound() {
        if (_stroopRound.value > 12) {
            finishStroopGame()
            return
        }

        val textItem = stroopColors.random()
        // Sometimes match, sometimes conflict
        val colorItem = if (Random.nextBoolean()) {
            stroopColors.filter { it.first != textItem.first }.random()
        } else {
            textItem
        }

        val options = stroopColors.shuffled().take(4)
        val finalOptions = if (options.none { it.second == colorItem.second }) {
            (options.take(3) + colorItem).shuffled()
        } else {
            options
        }

        _stroopChallenge.value = StroopChallenge(
            wordText = textItem.first,
            textColor = colorItem.second,
            colorName = colorItem.first,
            options = finalOptions
        )

        stroopRoundStartTime = System.currentTimeMillis()
        startStroopTimer()
    }

    private fun startStroopTimer() {
        stroopTimerJob?.cancel()
        stroopTimerJob = viewModelScope.launch {
            val totalDurationMs = 2800L
            val stepMs = 30L
            var elapsed = 0L
            while (elapsed < totalDurationMs) {
                delay(stepMs)
                elapsed += stepMs
                _stroopTimeLeft.value = (1f - (elapsed.toFloat() / totalDurationMs)).coerceIn(0f, 1f)
            }
            // Timeout counts as incorrect
            onStroopOptionSelected(null)
        }
    }

    fun onStroopOptionSelected(selectedColor: Color?) {
        stroopTimerJob?.cancel()
        val challenge = _stroopChallenge.value ?: return
        val reactionTime = System.currentTimeMillis() - stroopRoundStartTime
        stroopReactionTimes.add(reactionTime)

        if (selectedColor != null && selectedColor == challenge.textColor) {
            stroopCorrectAnswers++
            val timeBonus = max(10, (2800 - reactionTime).toInt() / 25)
            _stroopScore.value += 100 + timeBonus
        }

        _stroopRound.value += 1
        nextStroopRound()
    }

    private fun finishStroopGame() {
        _isStroopGameOver.value = true
        val totalRounds = 12
        val accuracy = stroopCorrectAnswers.toFloat() / totalRounds
        val avgReactionTime = if (stroopReactionTimes.isNotEmpty()) {
            stroopReactionTimes.average().toLong()
        } else 1200L

        saveWorkoutResult(
            gameType = "STROOP_SPEED",
            score = _stroopScore.value,
            accuracy = accuracy,
            avgReactionTime = avgReactionTime,
            level = 1
        )
    }

    // ====================================================
    // SYNAPSE MATH LOGIC
    // ====================================================
    fun startMathGame() {
        cancelAllGameJobs()
        _mathRound.value = 1
        _mathScore.value = 0
        _isMathGameOver.value = false
        mathReactionTimes.clear()
        mathCorrectAnswers = 0
        nextMathRound()
    }

    private fun nextMathRound() {
        if (_mathRound.value > 10) {
            finishMathGame()
            return
        }

        val operators = listOf("+", "-", "*")
        val op = operators.random()
        val a: Int
        val b: Int
        val correctVal: Int

        when (op) {
            "+" -> {
                a = Random.nextInt(12, 60)
                b = Random.nextInt(11, 45)
                correctVal = a + b
            }
            "-" -> {
                a = Random.nextInt(25, 99)
                b = Random.nextInt(12, a)
                correctVal = a - b
            }
            else -> {
                a = Random.nextInt(3, 12)
                b = Random.nextInt(4, 15)
                correctVal = a * b
            }
        }

        val distractors = mutableSetOf<Int>()
        while (distractors.size < 3) {
            val delta = listOf(-10, -5, -2, -1, 1, 2, 5, 10).random()
            val candidate = correctVal + delta
            if (candidate != correctVal && candidate > 0) {
                distractors.add(candidate)
            }
        }

        val optionsList = (distractors.toList() + correctVal).shuffled()
        val correctIndex = optionsList.indexOf(correctVal)

        _mathChallenge.value = MathChallenge(
            expression = "$a $op $b",
            options = optionsList,
            correctIndex = correctIndex
        )

        mathRoundStartTime = System.currentTimeMillis()
        startMathTimer()
    }

    private fun startMathTimer() {
        mathTimerJob?.cancel()
        mathTimerJob = viewModelScope.launch {
            val totalDurationMs = 3500L
            val stepMs = 35L
            var elapsed = 0L
            while (elapsed < totalDurationMs) {
                delay(stepMs)
                elapsed += stepMs
                _mathTimeLeft.value = (1f - (elapsed.toFloat() / totalDurationMs)).coerceIn(0f, 1f)
            }
            onMathOptionSelected(-1)
        }
    }

    fun onMathOptionSelected(selectedIndex: Int) {
        mathTimerJob?.cancel()
        val challenge = _mathChallenge.value ?: return
        val reactionTime = System.currentTimeMillis() - mathRoundStartTime
        mathReactionTimes.add(reactionTime)

        if (selectedIndex == challenge.correctIndex) {
            mathCorrectAnswers++
            val timeBonus = max(10, (3500 - reactionTime).toInt() / 30)
            _mathScore.value += 120 + timeBonus
        }

        _mathRound.value += 1
        nextMathRound()
    }

    private fun finishMathGame() {
        _isMathGameOver.value = true
        val totalRounds = 10
        val accuracy = mathCorrectAnswers.toFloat() / totalRounds
        val avgReactionTime = if (mathReactionTimes.isNotEmpty()) {
            mathReactionTimes.average().toLong()
        } else 1500L

        saveWorkoutResult(
            gameType = "SYNAPSE_MATH",
            score = _mathScore.value,
            accuracy = accuracy,
            avgReactionTime = avgReactionTime,
            level = 1
        )
    }

    // ====================================================
    // NEURO QUIZ LOGIC
    // ====================================================
    fun startQuizGame() {
        _quizQuestionIndex.value = 0
        _quizScore.value = 0
        _quizSelectedOption.value = null
        _quizIsAnswered.value = false
        _isQuizGameOver.value = false
    }

    fun answerQuizQuestion(optionIndex: Int) {
        if (_quizIsAnswered.value) return
        _quizSelectedOption.value = optionIndex
        _quizIsAnswered.value = true

        val currentQ = BrainDataRepository.quizQuestions.getOrNull(_quizQuestionIndex.value)
        if (currentQ != null && optionIndex == currentQ.correctIndex) {
            _quizScore.value += 100
        }
    }

    fun advanceQuiz() {
        val nextIndex = _quizQuestionIndex.value + 1
        if (nextIndex < BrainDataRepository.quizQuestions.size) {
            _quizQuestionIndex.value = nextIndex
            _quizSelectedOption.value = null
            _quizIsAnswered.value = false
        } else {
            // Quiz finished
            _isQuizGameOver.value = true
            val totalQuestions = BrainDataRepository.quizQuestions.size
            val accuracy = (_quizScore.value.toFloat() / (totalQuestions * 100)).coerceIn(0f, 1f)
            saveWorkoutResult(
                gameType = "NEURO_QUIZ",
                score = _quizScore.value,
                accuracy = accuracy,
                avgReactionTime = 2500L,
                level = 1
            )
        }
    }

    // ====================================================
    // FLASHCARD NAVIGATION
    // ====================================================
    fun nextFlashcard() {
        _isCardFlipped.value = false
        val next = (_currentFlashcardIndex.value + 1) % BrainDataRepository.flashcards.size
        _currentFlashcardIndex.value = next
    }

    fun previousFlashcard() {
        _isCardFlipped.value = false
        val count = BrainDataRepository.flashcards.size
        val prev = (_currentFlashcardIndex.value - 1 + count) % count
        _currentFlashcardIndex.value = prev
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    // ====================================================
    // DATA PERSISTENCE & METRICS CALCULATION
    // ====================================================
    private fun saveWorkoutResult(
        gameType: String,
        score: Int,
        accuracy: Float,
        avgReactionTime: Long,
        level: Int
    ) {
        val bqGain = (score / 150) + (accuracy * 8).toInt()
        val result = GameResult(
            gameType = gameType,
            score = score,
            accuracy = accuracy,
            avgReactionTimeMs = avgReactionTime,
            levelReached = level,
            bqGain = bqGain
        )
        _lastGameResult.value = result

        viewModelScope.launch {
            repository.recordWorkout(
                WorkoutEntity(
                    gameType = gameType,
                    score = score,
                    accuracy = accuracy,
                    reactionTimeMs = avgReactionTime,
                    levelReached = level
                )
            )
        }
    }

    fun calculateBrainQuotient(workouts: List<WorkoutEntity>): Int {
        if (workouts.isEmpty()) return 100 // baseline BQ
        val recent = workouts.take(20)
        val avgScore = recent.map { it.score }.average().toInt()
        val avgAcc = recent.map { it.accuracy }.average().toFloat()
        val bq = 100 + (avgScore / 80) + ((avgAcc - 0.5f) * 40).toInt()
        return bq.coerceIn(80, 165)
    }

    fun calculateStreak(workouts: List<WorkoutEntity>): Int {
        if (workouts.isEmpty()) return 0
        val days = workouts.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            cal.get(Calendar.YEAR) * 366 + cal.get(Calendar.DAY_OF_YEAR)
        }.distinct().sortedDescending()

        val todayCal = Calendar.getInstance()
        val todayDay = todayCal.get(Calendar.YEAR) * 366 + todayCal.get(Calendar.DAY_OF_YEAR)

        var streak = 0
        var expectedDay = todayDay

        for (day in days) {
            if (day == expectedDay) {
                streak++
                expectedDay--
            } else if (day == expectedDay - 1 && streak == 0) {
                // Completed yesterday, keep streak alive
                streak++
                expectedDay = day - 1
            } else {
                break
            }
        }
        return max(streak, 1)
    }

    fun recordFocusSession(durationSec: Int = 30) {
        saveWorkoutResult(
            gameType = "FOCUS_TRAINING",
            score = durationSec * 10,
            accuracy = 1.0f,
            avgReactionTime = durationSec * 1000L,
            level = 1
        )
    }

    fun clearStatsHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
