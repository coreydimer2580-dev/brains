package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrainDatabase
import com.example.data.WorkoutEntity
import com.example.data.repository.BrainInsightsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class TimeframeFilter(val label: String) {
    ALL_TIME("All Time"),
    LAST_7_DAYS("7 Days"),
    LAST_30_DAYS("30 Days")
}

enum class ProgressMetric(val label: String) {
    SCORE("Score (Pts)"),
    ACCURACY("Accuracy (%)")
}

enum class TopicMasteryStatus(val label: String, val badgeColor: Color) {
    MASTERED("Mastered", Color(0xFF10B981)),
    PROFICIENT("Proficient", Color(0xFF0EA5E9)),
    DEVELOPING("Developing", Color(0xFF8B5CF6)),
    NEEDS_FOCUS("Needs Focus", Color(0xFFF59E0B))
}

enum class RecommendationPriority(val label: String, val color: Color) {
    HIGH("Urgent Focus", Color(0xFFEF4444)),
    MEDIUM("Recommended", Color(0xFFF59E0B)),
    MAINTENANCE("Reinforce", Color(0xFF10B981))
}

data class TopicMastery(
    val id: String,
    val title: String,
    val brainRegion: String,
    val gameType: String,
    val masteryScore: Int, // 0 - 100
    val status: TopicMasteryStatus,
    val sessionsCount: Int,
    val bestScore: Int,
    val avgAccuracy: Int, // 0 - 100
    val color: Color,
    val activeGame: ActiveGame,
    val description: String
)

data class TopicRecommendation(
    val id: String,
    val topicTitle: String,
    val priority: RecommendationPriority,
    val reason: String,
    val actionDescription: String,
    val activeGame: ActiveGame,
    val color: Color,
    val estimatedBoost: String
)

data class DayActivity(
    val dayLabel: String,
    val count: Int,
    val dateString: String
)

class UserProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BrainDatabase.getInstance(application)
    private val workoutDao = db.workoutDao()
    private val insightsRepository = BrainInsightsRepository()

    val workouts: StateFlow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _timeframeFilter = MutableStateFlow(TimeframeFilter.ALL_TIME)
    val timeframeFilter: StateFlow<TimeframeFilter> = _timeframeFilter.asStateFlow()

    private val _topicFilter = MutableStateFlow<String?>(null) // null = All
    val topicFilter: StateFlow<String?> = _topicFilter.asStateFlow()

    private val _metricType = MutableStateFlow(ProgressMetric.SCORE)
    val metricType: StateFlow<ProgressMetric> = _metricType.asStateFlow()

    private val _aiRecommendation = MutableStateFlow<String?>(null)
    val aiRecommendation: StateFlow<String?> = _aiRecommendation.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        // Automatically seed rich initial calibration data if user has 0 workouts
        viewModelScope.launch {
            workoutDao.getAllWorkouts().first().let { currentList ->
                if (currentList.isEmpty()) {
                    seedSampleData()
                }
            }
        }
    }

    fun setTimeframeFilter(filter: TimeframeFilter) {
        _timeframeFilter.value = filter
    }

    fun setTopicFilter(topic: String?) {
        _topicFilter.value = topic
    }

    fun setMetricType(metric: ProgressMetric) {
        _metricType.value = metric
    }

    // Filtered workouts chronologically ordered for charts
    val filteredWorkouts: StateFlow<List<WorkoutEntity>> = combine(
        workouts,
        _timeframeFilter,
        _topicFilter
    ) { allWorkouts, timeframe, topic ->
        val now = System.currentTimeMillis()
        val cutoff = when (timeframe) {
            TimeframeFilter.ALL_TIME -> 0L
            TimeframeFilter.LAST_7_DAYS -> now - (7L * 24 * 60 * 60 * 1000)
            TimeframeFilter.LAST_30_DAYS -> now - (30L * 24 * 60 * 60 * 1000)
        }

        allWorkouts
            .filter { it.timestamp >= cutoff }
            .filter { topic == null || it.gameType == topic }
            .sortedBy { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Topic Masteries across the 5 cognitive disciplines
    val topicMasteries: StateFlow<List<TopicMastery>> = workouts.map { allWorkouts ->
        calculateTopicMasteries(allWorkouts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), calculateTopicMasteries(emptyList()))

    // Overall average cognitive mastery index
    val overallMasteryScore: StateFlow<Int> = topicMasteries.map { masteries ->
        if (masteries.isEmpty()) 0
        else (masteries.map { it.masteryScore }.average()).roundToInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Recommendations for areas to focus on next
    val recommendations: StateFlow<List<TopicRecommendation>> = topicMasteries.map { masteries ->
        generateRecommendations(masteries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), generateRecommendations(calculateTopicMasteries(emptyList())))

    // Weekly activity distribution (Mon-Sun)
    val weeklyActivity: StateFlow<List<DayActivity>> = workouts.map { allWorkouts ->
        calculateWeeklyActivity(allWorkouts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), calculateWeeklyActivity(emptyList()))

    // Total Workouts Count
    val totalWorkoutsCount: StateFlow<Int> = workouts.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Average Accuracy
    val averageAccuracyPercent: StateFlow<Int> = workouts.map { list ->
        if (list.isEmpty()) 0
        else (list.map { it.accuracy }.average() * 100).roundToInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Highest Score
    val highestScore: StateFlow<Int> = workouts.map { list ->
        if (list.isEmpty()) 0
        else list.maxOf { it.score }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active learning streak
    val learningStreakDays: StateFlow<Int> = workouts.map { list ->
        calculateStreak(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addManualWorkout(gameType: String, score: Int, accuracy: Float, reactionTimeMs: Long = 420L) {
        viewModelScope.launch {
            val entity = WorkoutEntity(
                gameType = gameType,
                score = score,
                accuracy = accuracy.coerceIn(0f, 1f),
                reactionTimeMs = reactionTimeMs,
                levelReached = (score / 150).coerceAtLeast(1),
                timestamp = System.currentTimeMillis()
            )
            workoutDao.insertWorkout(entity)
        }
    }

    fun clearAllWorkouts() {
        viewModelScope.launch {
            workoutDao.clearAllWorkouts()
            _aiRecommendation.value = null
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            val sampleWorkouts = listOf(
                WorkoutEntity(gameType = "MEMORY_GRID", score = 380, accuracy = 0.85f, reactionTimeMs = 640, levelReached = 3, timestamp = now - (6 * dayMs) + (2 * 3600 * 1000)),
                WorkoutEntity(gameType = "STROOP_SPEED", score = 420, accuracy = 0.90f, reactionTimeMs = 380, levelReached = 4, timestamp = now - (5 * dayMs) + (4 * 3600 * 1000)),
                WorkoutEntity(gameType = "SYNAPSE_MATH", score = 310, accuracy = 0.70f, reactionTimeMs = 780, levelReached = 2, timestamp = now - (4 * dayMs) + (1 * 3600 * 1000)),
                WorkoutEntity(gameType = "NEURO_QUIZ", score = 500, accuracy = 0.95f, reactionTimeMs = 450, levelReached = 5, timestamp = now - (3 * dayMs) + (6 * 3600 * 1000)),
                WorkoutEntity(gameType = "FOCUS_TRAINING", score = 350, accuracy = 0.80f, reactionTimeMs = 520, levelReached = 3, timestamp = now - (3 * dayMs) + (8 * 3600 * 1000)),
                WorkoutEntity(gameType = "MEMORY_GRID", score = 460, accuracy = 0.92f, reactionTimeMs = 560, levelReached = 4, timestamp = now - (2 * dayMs) + (3 * 3600 * 1000)),
                WorkoutEntity(gameType = "STROOP_SPEED", score = 490, accuracy = 0.94f, reactionTimeMs = 340, levelReached = 5, timestamp = now - (1 * dayMs) + (5 * 3600 * 1000)),
                WorkoutEntity(gameType = "SYNAPSE_MATH", score = 360, accuracy = 0.76f, reactionTimeMs = 710, levelReached = 3, timestamp = now - (12 * 3600 * 1000)),
                WorkoutEntity(gameType = "NEURO_QUIZ", score = 520, accuracy = 1.00f, reactionTimeMs = 410, levelReached = 5, timestamp = now - (2 * 3600 * 1000))
            )

            sampleWorkouts.forEach { workoutDao.insertWorkout(it) }
        }
    }

    fun fetchAiRecommendations() {
        if (_isAiLoading.value) return
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val currentMasteries = topicMasteries.value
                val summary = currentMasteries.joinToString("\n") {
                    "• ${it.title}: Mastery ${it.masteryScore}%, Best Score: ${it.bestScore}, Avg Accuracy: ${it.avgAccuracy}%, Status: ${it.status.label}"
                }
                val prompt = """
                    User's Cognitive Domain Masteries:
                    $summary
                    Overall Mastery: ${overallMasteryScore.value}%
                    
                    Identify their top strength, their primary growth area to focus on next, and prescribe 2 specific cognitive training actions they should take this week. Keep it concise, scientifically grounded, and motivating (3-4 sentences).
                """.trimIndent()
                
                val insight = withContext(Dispatchers.IO) {
                    insightsRepository.getPersonalizedInsights(prompt, overallMasteryScore.value)
                }
                _aiRecommendation.value = insight
            } catch (e: Exception) {
                _aiRecommendation.value = "To optimize your neuro-training: focus on your lowest-mastery topic first with 10-minute daily intervals to promote long-term potentiation."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    private fun calculateTopicMasteries(workouts: List<WorkoutEntity>): List<TopicMastery> {
        val topicDefs = listOf(
            Triple("MEMORY_GRID", "Working Memory", "Temporal Lobe / Hippocampus") to Pair(Color(0xFF38BDF8), ActiveGame.MEMORY_GRID),
            Triple("STROOP_SPEED", "Executive Inhibition", "Prefrontal Cortex") to Pair(Color(0xFFEF4444), ActiveGame.STROOP_SPEED),
            Triple("SYNAPSE_MATH", "Numerical Processing", "Parietal Lobe") to Pair(Color(0xFF10B981), ActiveGame.SYNAPSE_MATH),
            Triple("NEURO_QUIZ", "Neuroscience Literacy", "Cerebral Cortex") to Pair(Color(0xFF8B5CF6), ActiveGame.NEURO_QUIZ),
            Triple("FOCUS_TRAINING", "Visual Attention", "Occipital Lobe") to Pair(Color(0xFFF59E0B), ActiveGame.FOCUS_TRAINER)
        )

        return topicDefs.map { (info, gamePair) ->
            val (gameType, title, lobe) = info
            val (color, activeGame) = gamePair
            val matching = workouts.filter { it.gameType == gameType }

            val count = matching.size
            val best = if (matching.isEmpty()) 0 else matching.maxOf { it.score }
            val avgAcc = if (matching.isEmpty()) 0 else (matching.map { it.accuracy }.average() * 100).roundToInt()

            // Mastery calculation: combines count, average accuracy, and best score
            // Formula weighs accuracy (50%), score achievement (30%), and training volume (20%)
            val volumeComponent = (count * 15).coerceAtMost(25)
            val accuracyComponent = (avgAcc * 0.45f).roundToInt()
            val scoreComponent = ((best / 550f) * 30f).roundToInt().coerceAtMost(30)
            val rawMastery = (volumeComponent + accuracyComponent + scoreComponent).coerceIn(0, 100)

            val status = when {
                rawMastery >= 80 -> TopicMasteryStatus.MASTERED
                rawMastery >= 60 -> TopicMasteryStatus.PROFICIENT
                rawMastery >= 35 -> TopicMasteryStatus.DEVELOPING
                else -> TopicMasteryStatus.NEEDS_FOCUS
            }

            val desc = when (gameType) {
                "MEMORY_GRID" -> "Spatial pattern recall and hippocampal declarative indexing."
                "STROOP_SPEED" -> "Attentional control and cognitive interference suppression."
                "SYNAPSE_MATH" -> "Rapid quantitative logic and parietal angular gyrus pathways."
                "NEURO_QUIZ" -> "Theoretical knowledge of neuroanatomy and cellular synaptic plasticity."
                else -> "Sustained ocular fixation and visual retinotopic focus."
            }

            TopicMastery(
                id = gameType,
                title = title,
                brainRegion = lobe,
                gameType = gameType,
                masteryScore = rawMastery,
                status = status,
                sessionsCount = count,
                bestScore = best,
                avgAccuracy = avgAcc,
                color = color,
                activeGame = activeGame,
                description = desc
            )
        }
    }

    private fun generateRecommendations(masteries: List<TopicMastery>): List<TopicRecommendation> {
        if (masteries.isEmpty()) return emptyList()

        // Sort by mastery score ascending (lowest first)
        val sorted = masteries.sortedBy { it.masteryScore }
        val recommendations = mutableListOf<TopicRecommendation>()

        // 1. Primary Focus: Lowest mastery topic
        sorted.firstOrNull()?.let { lowest ->
            val priority = if (lowest.masteryScore < 50) RecommendationPriority.HIGH else RecommendationPriority.MEDIUM
            recommendations.add(
                TopicRecommendation(
                    id = "rec_${lowest.id}_priority",
                    topicTitle = lowest.title,
                    priority = priority,
                    reason = "Your mastery is currently at ${lowest.masteryScore}%. Strengthening this cognitive domain balances your neural architecture.",
                    actionDescription = "Complete 2 training rounds in ${lowest.title} (${lowest.brainRegion}) to boost synaptic connection density.",
                    activeGame = lowest.activeGame,
                    color = lowest.color,
                    estimatedBoost = "+12% Mastery"
                )
            )
        }

        // 2. Secondary Focus: Next lowest or topic with lower accuracy
        val secondary = sorted.getOrNull(1) ?: sorted.firstOrNull()
        secondary?.let { topic ->
            recommendations.add(
                TopicRecommendation(
                    id = "rec_${topic.id}_secondary",
                    topicTitle = topic.title,
                    priority = RecommendationPriority.MEDIUM,
                    reason = "Average accuracy is ${topic.avgAccuracy}%. Targeted drills can push this domain into the Proficient tier.",
                    actionDescription = "Practice with deliberate focus to enhance execution speed and reduce reaction latency.",
                    activeGame = topic.activeGame,
                    color = topic.color,
                    estimatedBoost = "+8% Accuracy"
                )
            )
        }

        // 3. Maintenance Focus: A mastered or highest domain
        val highest = masteries.maxByOrNull { it.masteryScore }
        if (highest != null && highest.masteryScore >= 60 && highest.id != sorted.firstOrNull()?.id) {
            recommendations.add(
                TopicRecommendation(
                    id = "rec_${highest.id}_maintain",
                    topicTitle = highest.title,
                    priority = RecommendationPriority.MAINTENANCE,
                    reason = "Strong baseline at ${highest.masteryScore}%! Maintenance workouts prevent synaptic pruning and preserve peak agility.",
                    actionDescription = "Run a high-speed benchmark round to test your all-time high score (${highest.bestScore} pts).",
                    activeGame = highest.activeGame,
                    color = highest.color,
                    estimatedBoost = "Maintain Peak"
                )
            )
        }

        return recommendations
    }

    private fun calculateWeeklyActivity(workouts: List<WorkoutEntity>): List<DayActivity> {
        val calendar = Calendar.getInstance()
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 7 = Sat

        // Last 7 days in order
        return (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            val startOfDay = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = startOfDay + (24 * 60 * 60 * 1000)

            val dayWorkouts = workouts.count { it.timestamp in startOfDay until endOfDay }
            val dayName = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(startOfDay))

            DayActivity(
                dayLabel = dayName,
                count = dayWorkouts,
                dateString = dateFmt
            )
        }
    }

    private fun calculateStreak(workouts: List<WorkoutEntity>): Int {
        if (workouts.isEmpty()) return 0
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val thisYear = calendar.get(Calendar.YEAR)

        val activeDays = workouts.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            Pair(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
        }.distinct().toSet()

        var streak = 0
        var currentCal = Calendar.getInstance()

        // Check today or yesterday
        val hasToday = activeDays.contains(Pair(thisYear, today))
        if (!hasToday) {
            currentCal.add(Calendar.DAY_OF_YEAR, -1)
            val hasYesterday = activeDays.contains(Pair(currentCal.get(Calendar.YEAR), currentCal.get(Calendar.DAY_OF_YEAR)))
            if (!hasYesterday) return 0
        }

        while (true) {
            val yr = currentCal.get(Calendar.YEAR)
            val dy = currentCal.get(Calendar.DAY_OF_YEAR)
            if (activeDays.contains(Pair(yr, dy))) {
                streak++
                currentCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }
}
