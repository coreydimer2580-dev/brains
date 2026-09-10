package com.example.model

enum class NextToJumpStage(val label: String) {
    NEXT_TO_JUMP("🟢 Next to Jump"),
    CLOSED_BET("🟡 Closed Bet (In Running)"),
    RESULTED("🏁 Resulted (Winner Declared)")
}

data class RaceRunner(
    val id: String,
    val number: Int,
    val name: String,
    val jockeyOrDriver: String,
    val barrier: Int,
    val isFavorite: Boolean,
    val consecutiveWins: Int,
    val consecutiveLosses: Int,
    val lastFiveRaces: List<Boolean>, // true for win (1st), false for loss
    val fixedOdds: Double, // Market reference (informational only)
    val easyBetCalculatedProbability: Double, // The custom algorithm win % (derived purely from winning number history or home/away records)
    val easyBetFairOdds: Double, // 100 / probability
    val streakTrend: StreakTrend,
    val momentumScore: Int, // 0 - 100
    val score: String? = null, // Live score if match in progress
    val empiricalNumberWinRate: Double = 0.0, // Historical win rate % of this saddlecloth/box number
    val empiricalNumberWins: Int = 0, // Total times this number has won
    val algorithmCalculationBasis: String = "" // Human-readable explanation of pure number/record basis
)

enum class LiveDataSource(val label: String, val category: String) {
    AU_THOROUGHBRED("🏇 Horses (Thoroughbred)", "Racing"),
    AU_GREYHOUNDS("🐕 Dogs (Greyhound Box 1-8)", "Racing"),
    AU_HARNESS("🐎 Harness Racing", "Racing"),
    ESPN_AFL("🏉 Australian AFL", "Sports"),
    ESPN_PREMIER_LEAGUE("⚽ Premier League", "Sports"),
    ESPN_NBA("🏀 NBA Basketball", "Sports"),
    LIVESCORE_GLOBAL("🔴 Global Live In-Play", "Sports"),
    CUSTOM_FEED("⚙️ Custom Live API", "Custom Endpoint")
}

enum class StreakTrend {
    HOT_STREAK,      // 2+ consecutive wins (Emerald Green)
    RECOVERY_FORM,   // Recent win after losses
    COLD_STREAK,     // 3+ consecutive losses (Crimson Red)
    STABLE_FAVORITE  // Empirical leader trending steady
}

data class NextToJumpRace(
    val id: String,
    val meetingName: String,
    val raceNumber: Int,
    val raceType: String, // Thoroughbred, Greyhound, Harness, AFL, Soccer, NBA
    val distanceMeters: Int,
    val trackCondition: String, // Good 4, Soft 5, Heavy 8, Fast, Synthetic, Home Stadium
    val secondsToJump: Int,
    val isClosed: Boolean = false,
    val stage: NextToJumpStage = NextToJumpStage.NEXT_TO_JUMP,
    val winningRunnerNumber: Int? = null,
    val winningRunnerName: String? = null,
    val runners: List<RaceRunner>,
    val algorithmConfidence: Int, // e.g. 94%
    val liveStatus: String = "Upcoming",
    val homeScore: String? = null,
    val awayScore: String? = null,
    val sourceApi: String = "Live Network Feed",
    // Team sports Home / Away win-loss empirical records
    val homeTeamName: String? = null,
    val awayTeamName: String? = null,
    val homeWinLossRecord: String? = null, // e.g. "11W - 3L Home (78.6%)"
    val awayWinLossRecord: String? = null, // e.g. "4W - 9L Away (30.8%)"
    val homeWinRate: Double? = null,
    val awayWinRate: Double? = null,
    val algorithmHomeWinProb: Double? = null,
    val algorithmAwayWinProb: Double? = null,
    val algorithmBasis: String = "Empirical winner number frequency & home/away record model"
)

data class WinnerNumberFrequencyItem(
    val number: Int,
    val label: String, // e.g. "Number 1" or "Box 1 (Red)"
    val winCount: Int,
    val winPercentage: Double,
    val hotStreak: Boolean
)

data class BettingSignal(
    val id: String,
    val raceId: String,
    val meetingName: String,
    val runnerName: String,
    val runnerNumber: Int,
    val signalType: String, // "PURE NUMBER EDGE", "HOME RECORD EDGE", "HOT STREAK"
    val confidencePct: Int,
    val winLossSequenceText: String, // e.g. "Empirical 24.5% win rate across 53 races"
    val isWinStreak: Boolean,
    val fairOdds: Double,
    val marketOdds: Double,
    val edgePercentage: Double,
    val isVipExclusive: Boolean
)

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val priceDisplay: String,
    val priceCents: Long,
    val billingInterval: String,
    val popular: Boolean = false,
    val features: List<String>
) {
    DAILY_PASS(
        id = "pass_daily",
        title = "24-Hour Race Pass",
        priceDisplay = "$4.99",
        priceCents = 499L,
        billingInterval = "single day",
        features = listOf(
            "Access live Next-to-Jump races & dogs",
            "Box 1-8 winning number probabilities",
            "Home/Away win-loss algorithm model"
        )
    ),
    VIP_MONTHLY(
        id = "vip_monthly",
        title = "EasyBet Pro VIP",
        priceDisplay = "$19.99/mo",
        priceCents = 1999L,
        billingInterval = "monthly recurring",
        popular = true,
        features = listOf(
            "Full empirical winner number probability matrix",
            "Zero payout dependency algorithm",
            "Next-to-Jump -> Closed -> Resulted tracker",
            "Automated home/away team record model",
            "Funds routed to Corey & Sarah Treasury"
        )
    ),
    ANNUAL_FOUNDER(
        id = "annual_founder",
        title = "Founder Syndicate Tier",
        priceDisplay = "$149.00/yr",
        priceCents = 14900L,
        billingInterval = "yearly (save 38%)",
        features = listOf(
            "All VIP Pro benefits included",
            "Continuous multi-track result recording",
            "Priority AI prediction feeds",
            "Direct Admin communication channel"
        )
    )
}
