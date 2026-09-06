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
    NEURO_QUIZ
}

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
    init {
        val db = BrainDatabase.getInstance(application)
        repository = BrainRepository(db.workoutDao())
    }

    val workoutHistory: StateFlow<List<WorkoutEntity>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    // MEMORY GRID GAME STATE
    // ----------------------------------------------------
    private val _gridSequence = MutableStateFlow<List<Int>>(emptyList())
    val gridSequence: StateFlow<List<Int>> = _gridSequence.asStateFlow()

    private val _activeHighlightedTile = MutableStateFlow<Int?>(null)
    val activeHighlightedTile: StateFlow<Int?> = _activeHighlightedTile.asStateFlow()

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
            ActiveGame.NONE -> {}
        }
    }

    fun exitCurrentGame() {
        cancelAllGameJobs()
        _activeGame.value = ActiveGame.NONE
    }

    private fun cancelAllGameJobs() {
        memoryPlaybackJob?.cancel()
        stroopTimerJob?.cancel()
        mathTimerJob?.cancel()
    }

    // ====================================================
    // MEMORY GRID LOGIC
    // ====================================================
    fun startMemoryGridGame() {
        cancelAllGameJobs()
        _memoryLevel.value = 1
        _memoryScore.value = 0
        _isMemoryGameOver.value = false
        _userMemoryInput.value = emptyList()
        generateNewMemorySequence(1)
    }

    private fun generateNewMemorySequence(level: Int) {
        val length = 2 + level
        val sequence = List(length) { Random.nextInt(0, 9) } // 3x3 grid (indices 0..8)
        _gridSequence.value = sequence
        _userMemoryInput.value = emptyList()
        playSequencePreview(sequence)
    }

    private fun playSequencePreview(sequence: List<Int>) {
        memoryPlaybackJob?.cancel()
        memoryPlaybackJob = viewModelScope.launch {
            _isShowingSequence.value = true
            delay(500)
            for (tile in sequence) {
                _activeHighlightedTile.value = tile
                delay(480)
                _activeHighlightedTile.value = null
                delay(220)
            }
            _isShowingSequence.value = false
        }
    }

    fun onMemoryTileTapped(index: Int) {
        if (_isShowingSequence.value || _isMemoryGameOver.value) return

        val currentInput = _userMemoryInput.value + index
        _userMemoryInput.value = currentInput

        val currentIndex = currentInput.size - 1
        val expected = _gridSequence.value.getOrNull(currentIndex)

        if (expected != null && expected == index) {
            // Correct tile
            if (currentInput.size == _gridSequence.value.size) {
                // Completed the round!
                _memoryScore.value += 100 * _memoryLevel.value
                val nextLevel = _memoryLevel.value + 1
                _memoryLevel.value = nextLevel
                viewModelScope.launch {
                    delay(400)
                    generateNewMemorySequence(nextLevel)
                }
            }
        } else {
            // Mistake - Game Over
            _isMemoryGameOver.value = true
            val finalScore = _memoryScore.value
            val level = _memoryLevel.value
            val accuracy = if (level > 1) 0.85f else 0.5f
            saveWorkoutResult(
                gameType = "MEMORY_GRID",
                score = finalScore,
                accuracy = accuracy,
                avgReactionTime = 520L,
                level = level
            )
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

    fun clearStatsHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
