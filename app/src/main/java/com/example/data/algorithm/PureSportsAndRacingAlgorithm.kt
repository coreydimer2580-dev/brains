package com.example.data.algorithm

import com.example.data.RacingResultEntity
import com.example.model.NextToJumpRace
import com.example.model.RaceRunner
import com.example.model.StreakTrend
import com.example.model.WinnerNumberFrequencyItem
import kotlin.math.roundToInt

object PureSportsAndRacingAlgorithm {

    // Standard baseline empirical priors for Dog boxes (1 to 8) based on Australian track statistics
    // Box 1 (inside rail) has natural turn advantage; Box 5 (traffic) is lower.
    private val DEFAULT_DOG_BOX_PRIORS = mapOf(
        1 to 21.5, // Box 1 - Red (Rail)
        2 to 16.2, // Box 2 - Black/White
        3 to 12.8, // Box 3 - White
        4 to 11.0, // Box 4 - Blue
        5 to 9.8,  // Box 5 - Yellow
        6 to 10.5, // Box 6 - Green
        7 to 11.2, // Box 7 - Black
        8 to 14.5  // Box 8 - Pink (Wide sweep)
    )

    // Standard baseline empirical priors for Horse saddlecloths/barriers (1 to 14)
    private val DEFAULT_HORSE_NUMBER_PRIORS = mapOf(
        1 to 14.2,
        2 to 13.5,
        3 to 12.8,
        4 to 11.5,
        5 to 10.2,
        6 to 9.4,
        7 to 8.2,
        8 to 7.8,
        9 to 6.2,
        10 to 5.4,
        11 to 4.8,
        12 to 4.2
    )

    /**
     * Calculates empirical win-rate probabilities for horse and dog racing runners
     * based strictly on the winning number frequency from the sequence of jump results.
     * Zero dependency on bookmaker odds, payouts, or betting market margins.
     */
    fun calculateRacingNumberProbabilities(
        runners: List<RaceRunner>,
        raceType: String,
        historicalResults: List<RacingResultEntity>,
        lastWinningNumber: Int? = null
    ): List<RaceRunner> {
        if (runners.isEmpty()) return emptyList()

        val isDogRace = raceType.contains("Greyhound", ignoreCase = true) || raceType.contains("Dog", ignoreCase = true)
        val filteredResults = historicalResults.filter {
            if (isDogRace) it.raceType.contains("Greyhound", ignoreCase = true)
            else !it.raceType.contains("Greyhound", ignoreCase = true)
        }

        val totalRaces = filteredResults.size

        // Count historical wins per number
        val winCounts = mutableMapOf<Int, Int>()
        filteredResults.forEach { result ->
            winCounts[result.winningNumber] = (winCounts[result.winningNumber] ?: 0) + 1
        }

        // Check transition sequence: when lastWinningNumber won, what number won the NEXT jump?
        val transitionCounts = mutableMapOf<Int, Int>()
        if (lastWinningNumber != null && filteredResults.size >= 2) {
            for (i in 0 until filteredResults.size - 1) {
                val current = filteredResults[i + 1]
                val next = filteredResults[i] // newer is at lower index
                if (current.winningNumber == lastWinningNumber) {
                    transitionCounts[next.winningNumber] = (transitionCounts[next.winningNumber] ?: 0) + 1
                }
            }
        }

        // Calculate raw score for each runner number
        val rawScores = runners.map { runner ->
            val num = runner.number
            val historicalWins = winCounts[num] ?: 0

            // Baseline prior
            val prior = if (isDogRace) {
                DEFAULT_DOG_BOX_PRIORS[num] ?: 10.0
            } else {
                DEFAULT_HORSE_NUMBER_PRIORS[num] ?: (100.0 / runners.size)
            }

            // Empirical rate from database
            val empiricalRate = if (totalRaces >= 5) {
                // Weight between database results and long-term prior
                val dbRate = (historicalWins.toDouble() / totalRaces.toDouble()) * 100.0
                val weight = (totalRaces.coerceAtMost(50).toDouble() / 50.0)
                (dbRate * weight) + (prior * (1.0 - weight))
            } else {
                prior
            }

            // Sequence jump transition bonus
            val transitionBonus = if (transitionCounts.isNotEmpty() && transitionCounts.containsKey(num)) {
                val totalTransitions = transitionCounts.values.sum().coerceAtLeast(1)
                (transitionCounts[num]!!.toDouble() / totalTransitions.toDouble()) * 8.0
            } else {
                0.0
            }

            // Number hot streak bonus: check recent 10 results
            val recentTen = filteredResults.take(10)
            val recentWinsForNumber = recentTen.count { it.winningNumber == num }
            val streakBonus = recentWinsForNumber * 2.5

            val finalScore = (empiricalRate + transitionBonus + streakBonus).coerceAtLeast(1.0)
            Triple(runner, finalScore, empiricalRate to historicalWins)
        }

        // Normalize raw scores to sum to 100%
        val sumScores = rawScores.sumOf { it.second }.coerceAtLeast(1.0)

        return rawScores.map { (runner, score, empiricalPair) ->
            val (empiricalRate, historicalWins) = empiricalPair
            val normalizedProb = ((score / sumScores) * 1000).roundToInt() / 10.0
            val fairOdds = if (normalizedProb > 0) ((100.0 / normalizedProb) * 100).roundToInt() / 100.0 else 99.0

            val isFavorite = normalizedProb == rawScores.maxOf { (it.second / sumScores) * 100.0 }

            val streakTrend = when {
                normalizedProb >= 20.0 -> StreakTrend.HOT_STREAK
                normalizedProb >= 12.0 -> StreakTrend.STABLE_FAVORITE
                normalizedProb <= 6.0 -> StreakTrend.COLD_STREAK
                else -> StreakTrend.RECOVERY_FORM
            }

            val basisText = if (isDogRace) {
                "Box #$runner.number empirical rate: ${String.format("%.1f", empiricalRate)}% ($historicalWins wins in track history). Zero bookmaker odds influence."
            } else {
                "Number #$runner.number historical frequency: ${String.format("%.1f", empiricalRate)}% across jump sequence. Zero bookmaker odds influence."
            }

            runner.copy(
                isFavorite = isFavorite,
                easyBetCalculatedProbability = normalizedProb,
                easyBetFairOdds = fairOdds,
                streakTrend = streakTrend,
                momentumScore = normalizedProb.toInt().coerceIn(5, 99),
                empiricalNumberWinRate = (empiricalRate * 10).roundToInt() / 10.0,
                empiricalNumberWins = historicalWins,
                algorithmCalculationBasis = basisText
            )
        }
    }

    /**
     * Calculates team sports win-rate percentages based purely on Home and Away
     * win-loss records and streaks from the next to play fixtures.
     * Zero reliance on sportsbet payouts or betting lines.
     */
    fun calculateHomeAwayMatchProbabilities(
        homeTeam: String,
        awayTeam: String,
        homeWins: Int,
        homeLosses: Int,
        awayWins: Int,
        awayLosses: Int,
        homeStreakWins: Int = 0,
        awayStreakLosses: Int = 0,
        currentHomeScore: Int? = null,
        currentAwayScore: Int? = null
    ): Pair<Double, Double> {
        val totalHomeGames = (homeWins + homeLosses).coerceAtLeast(1)
        val totalAwayGames = (awayWins + awayLosses).coerceAtLeast(1)

        val rawHomeWinRate = homeWins.toDouble() / totalHomeGames.toDouble()
        val rawAwayWinRate = awayWins.toDouble() / totalAwayGames.toDouble()

        // Home court historical empirical advantage (standard 6-10% in AFL/Premier League/NBA)
        val homeAdvantage = 1.08

        // Streak adjustments
        val homeStreakBonus = (homeStreakWins * 0.04).coerceAtMost(0.16)
        val awayStreakPenalty = (awayStreakLosses * 0.03).coerceAtMost(0.12)

        // In-play score momentum adjustment if game has commenced
        val inPlayShift = if (currentHomeScore != null && currentAwayScore != null) {
            val diff = currentHomeScore - currentAwayScore
            (diff * 0.08).coerceIn(-0.40, 0.40)
        } else {
            0.0
        }

        var homeFormRating = ((rawHomeWinRate * homeAdvantage) + homeStreakBonus + inPlayShift).coerceAtLeast(0.05)
        var awayFormRating = ((rawAwayWinRate * 0.96) - awayStreakPenalty - inPlayShift).coerceAtLeast(0.05)

        val totalRating = homeFormRating + awayFormRating
        val homeProb = ((homeFormRating / totalRating) * 1000).roundToInt() / 10.0
        val awayProb = ((100.0 - homeProb) * 10).roundToInt() / 10.0

        return homeProb to awayProb
    }

    /**
     * Generates a full frequency distribution of winning numbers for Greyhound boxes (1-8)
     * or Horse numbers (1-12) to power color-coded visual charts in the UI.
     */
    fun getWinningNumberFrequencies(
        results: List<RacingResultEntity>,
        isDogRace: Boolean
    ): List<WinnerNumberFrequencyItem> {
        val maxNumber = if (isDogRace) 8 else 12
        val filtered = results.filter {
            if (isDogRace) it.raceType.contains("Greyhound", ignoreCase = true)
            else !it.raceType.contains("Greyhound", ignoreCase = true)
        }

        val totalRaces = filtered.size
        val counts = mutableMapOf<Int, Int>()
        filtered.forEach {
            counts[it.winningNumber] = (counts[it.winningNumber] ?: 0) + 1
        }

        return (1..maxNumber).map { num ->
            val winCount = counts[num] ?: 0
            val pct = if (totalRaces > 0) {
                ((winCount.toDouble() / totalRaces.toDouble()) * 1000).roundToInt() / 10.0
            } else {
                if (isDogRace) DEFAULT_DOG_BOX_PRIORS[num] ?: 12.5 else DEFAULT_HORSE_NUMBER_PRIORS[num] ?: 8.3
            }

            val label = if (isDogRace) {
                when (num) {
                    1 -> "Box 1 (Red/Rail)"
                    2 -> "Box 2 (Black/White)"
                    3 -> "Box 3 (White)"
                    4 -> "Box 4 (Blue)"
                    5 -> "Box 5 (Yellow)"
                    6 -> "Box 6 (Green)"
                    7 -> "Box 7 (Black)"
                    8 -> "Box 8 (Pink/Wide)"
                    else -> "Box $num"
                }
            } else {
                "Saddlecloth #$num"
            }

            val hotStreak = pct >= (if (isDogRace) 16.0 else 12.0)

            WinnerNumberFrequencyItem(
                number = num,
                label = label,
                winCount = winCount,
                winPercentage = pct,
                hotStreak = hotStreak
            )
        }
    }
}
