package com.example.data

import com.example.model.*
import kotlin.math.roundToInt

object AlgorithmEngine {

    // =========================================================================
    // 1. TEAM SPORTS ALGORITHM: PURE HOME & AWAY WIN/LOSS MODEL
    // Built exclusively on home vs away empirical records, 0% bookmaker odds
    // =========================================================================

    data class SportsMatchProbabilities(
        val homeWinRatePct: Double,
        val awayWinRatePct: Double,
        val homeAlgorithmProbPct: Double,
        val awayAlgorithmProbPct: Double,
        val drawProbPct: Double = 0.0,
        val algorithmBasis: String
    )

    fun calculateSportsWinProbability(
        homeWins: Int,
        homeLosses: Int,
        awayWins: Int,
        awayLosses: Int,
        sportCategory: String,
        currentHomeScore: Int = 0,
        currentAwayScore: Int = 0,
        isInPlay: Boolean = false
    ): SportsMatchProbabilities {
        val totalHomeGames = (homeWins + homeLosses).coerceAtLeast(1)
        val totalAwayGames = (awayWins + awayLosses).coerceAtLeast(1)

        val rawHomeWinRate = homeWins.toDouble() / totalHomeGames.toDouble()
        val rawAwayWinRate = awayWins.toDouble() / totalAwayGames.toDouble()

        // Home pitch / court advantage factor (empirically ~1.15 in AFL, NBA, Soccer)
        val homeAdvantageWeight = 1.15
        val weightedHomeStrength = (rawHomeWinRate * homeAdvantageWeight).coerceAtLeast(0.05)
        val weightedAwayStrength = rawAwayWinRate.coerceAtLeast(0.05)

        val totalStrength = weightedHomeStrength + weightedAwayStrength

        var homeProb = weightedHomeStrength / totalStrength
        var awayProb = weightedAwayStrength / totalStrength
        var drawProb = 0.0

        val isSoccer = sportCategory.contains("Soccer", ignoreCase = true) ||
                sportCategory.contains("Premier", ignoreCase = true)

        if (isSoccer) {
            drawProb = 0.25 // Standard soccer empirical draw baseline
            homeProb *= (1.0 - drawProb)
            awayProb *= (1.0 - drawProb)
        }

        // If in-play, modulate by actual live score differential without bookmaker odds
        if (isInPlay) {
            val scoreDiff = currentHomeScore - currentAwayScore
            when {
                scoreDiff >= 2 -> {
                    homeProb = (homeProb * 1.35).coerceAtMost(0.92)
                    awayProb = (1.0 - homeProb - drawProb).coerceAtLeast(0.04)
                }
                scoreDiff == 1 -> {
                    homeProb = (homeProb * 1.18).coerceAtMost(0.80)
                    awayProb = (1.0 - homeProb - drawProb).coerceAtLeast(0.08)
                }
                scoreDiff == -1 -> {
                    awayProb = (awayProb * 1.18).coerceAtMost(0.80)
                    homeProb = (1.0 - awayProb - drawProb).coerceAtLeast(0.08)
                }
                scoreDiff <= -2 -> {
                    awayProb = (awayProb * 1.35).coerceAtMost(0.92)
                    homeProb = (1.0 - awayProb - drawProb).coerceAtLeast(0.04)
                }
            }
        }

        val homeProbPct = ((homeProb * 1000).roundToInt()) / 10.0
        val awayProbPct = ((awayProb * 1000).roundToInt()) / 10.0
        val drawProbPct = ((drawProb * 1000).roundToInt()) / 10.0

        val homeWinRatePct = ((rawHomeWinRate * 1000).roundToInt()) / 10.0
        val awayWinRatePct = ((rawAwayWinRate * 1000).roundToInt()) / 10.0

        val basis = "Built from Home ($homeWins W - $homeLosses L, $homeWinRatePct%) vs Away ($awayWins W - $awayLosses L, $awayWinRatePct%) Win/Loss record. Zero bookmaker dependency."

        return SportsMatchProbabilities(
            homeWinRatePct = homeWinRatePct,
            awayWinRatePct = awayWinRatePct,
            homeAlgorithmProbPct = homeProbPct,
            awayAlgorithmProbPct = awayProbPct,
            drawProbPct = drawProbPct,
            algorithmBasis = basis
        )
    }

    // =========================================================================
    // 2. HORSES & DOGS ALGORITHM: PURE WINNER NUMBER FREQUENCY ENGINE
    // Tracks Next to Jump -> Closed Bet -> Resulted
    // 100% computed from the Number of the Winner, ZERO $ payout dependence
    // =========================================================================

    // Baseline statistical distributions for Box Numbers (Greyhounds 1-8)
    // Box 1 (Red rail) has the highest statistical frequency in greyhound racing
    val GREYHOUND_BENCHMARK_WINS = mapOf(
        1 to 28, // Box 1: inside rail bias (~20%)
        2 to 21, // Box 2: inside lane (~15%)
        3 to 17, // Box 3: middle inside (~12%)
        4 to 14, // Box 4: middle (~10%)
        5 to 13, // Box 5: middle (~9%)
        6 to 15, // Box 6: middle wide (~11%)
        7 to 16, // Box 7: wide (~11%)
        8 to 20  // Box 8: outside sweep (~14%)
    )

    // Baseline statistical distributions for Horse Saddlecloth Numbers (1-12)
    val THOROUGHBRED_BENCHMARK_WINS = mapOf(
        1 to 24, // Number 1: top handicap class
        2 to 21,
        3 to 19,
        4 to 18,
        5 to 16,
        6 to 15,
        7 to 14,
        8 to 13,
        9 to 11,
        10 to 10,
        11 to 9,
        12 to 8
    )

    /**
     * Calculates the win rate percentage probability for each runner purely
     * from the historical frequency of its number across resulted races.
     *
     * @param runnerNumbers List of runner numbers (e.g. [1, 2, 3, 4, 5, 6, 7, 8])
     * @param historicalResultWinnerNumbers Count of each winning number recorded from Next-to-Jump -> Closed -> Result
     * @param isGreyhound Whether this is dog racing or horse racing
     */
    fun calculateWinnerNumberProbabilities(
        runnerNumbers: List<Int>,
        historicalResultWinnerNumbers: Map<Int, Int>,
        isGreyhound: Boolean
    ): Map<Int, Double> {
        val benchmark = if (isGreyhound) GREYHOUND_BENCHMARK_WINS else THOROUGHBRED_BENCHMARK_WINS

        // Sum empirical wins from resulted races + baseline prior weights
        val weightedWins = mutableMapOf<Int, Double>()
        var totalWeightedWins = 0.0

        runnerNumbers.forEach { num ->
            val empiricalRecordedWins = historicalResultWinnerNumbers[num] ?: 0
            val priorBenchmarkWins = (benchmark[num] ?: 12).toDouble()

            // Empirical recorded wins carry heavy weight (each resulted race adds strong weight)
            val combinedScore = priorBenchmarkWins + (empiricalRecordedWins * 3.5)
            weightedWins[num] = combinedScore
            totalWeightedWins += combinedScore
        }

        val probabilities = mutableMapOf<Int, Double>()
        if (totalWeightedWins <= 0.0) {
            val uniform = 100.0 / runnerNumbers.size
            runnerNumbers.forEach { probabilities[it] = uniform }
            return probabilities
        }

        var sumPct = 0.0
        runnerNumbers.forEach { num ->
            val score = weightedWins[num] ?: 1.0
            val pct = ((score / totalWeightedWins) * 1000).roundToInt() / 10.0
            probabilities[num] = pct
            sumPct += pct
        }

        // Normalize rounding
        val diff = ((100.0 - sumPct) * 10).roundToInt() / 10.0
        val topNum = runnerNumbers.maxByOrNull { probabilities[it] ?: 0.0 } ?: runnerNumbers.first()
        probabilities[topNum] = ((probabilities[topNum] ?: 0.0) + diff * 10).roundToInt() / 10.0

        return probabilities
    }

    /**
     * Builds the human-readable explanation of why this runner has this win probability,
     * highlighting the pure winner number frequency and zero $ payout reliance.
     */
    fun buildWinnerNumberExplanation(
        number: Int,
        winProbability: Double,
        winCount: Int,
        totalRacesResulted: Int,
        isGreyhound: Boolean
    ): String {
        return if (isGreyhound) {
            "Box #$number empirical rate: $winProbability% based on $winCount winner results across $totalRacesResulted races (Next to Jump ➔ Closed ➔ Resulted). 0% bookmaker payout dependence."
        } else {
            "Saddlecloth #$number empirical rate: $winProbability% based on $winCount winner results across $totalRacesResulted races. Pure winning number statistical distribution."
        }
    }
}
