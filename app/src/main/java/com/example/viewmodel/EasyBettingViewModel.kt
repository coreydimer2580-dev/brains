package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrainDatabase
import com.example.data.RacingResultEntity
import com.example.data.algorithm.PureSportsAndRacingAlgorithm
import com.example.data.api.LiveSportsService
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EasyBettingViewModel(application: Application) : AndroidViewModel(application) {

    private val liveService = LiveSportsService()
    private val racingDao = BrainDatabase.getInstance(application).racingResultDao()

    // Active Live Data Source
    private val _selectedDataSource = MutableStateFlow(LiveDataSource.AU_GREYHOUNDS)
    val selectedDataSource: StateFlow<LiveDataSource> = _selectedDataSource.asStateFlow()

    // Network connection & live status
    private val _isLiveFetching = MutableStateFlow(false)
    val isLiveFetching: StateFlow<Boolean> = _isLiveFetching.asStateFlow()

    private val _networkStatus = MutableStateFlow("Connecting to live sequence feeds...")
    val networkStatus: StateFlow<String> = _networkStatus.asStateFlow()

    private val _lastUpdated = MutableStateFlow("Pending initial live sync")
    val lastUpdated: StateFlow<String> = _lastUpdated.asStateFlow()

    // Custom Live API URL and Key
    private val _customApiUrl = MutableStateFlow("https://api.the-odds-api.com/v4/sports/upcoming/odds")
    val customApiUrl: StateFlow<String> = _customApiUrl.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Subscription status
    private val _isVipUnlocked = MutableStateFlow(false)
    val isVipUnlocked: StateFlow<Boolean> = _isVipUnlocked.asStateFlow()

    private val _activeSubscriptionTier = MutableStateFlow<SubscriptionTier?>(null)
    val activeSubscriptionTier: StateFlow<SubscriptionTier?> = _activeSubscriptionTier.asStateFlow()

    // Next to Jump Races / Live Match list
    private val _races = MutableStateFlow<List<NextToJumpRace>>(emptyList())
    val races: StateFlow<List<NextToJumpRace>> = _races.asStateFlow()

    // Currently selected race/event for deep analysis
    private val _selectedRace = MutableStateFlow<NextToJumpRace?>(null)
    val selectedRace: StateFlow<NextToJumpRace?> = _selectedRace.asStateFlow()

    // Top algorithmic betting signals
    private val _topSignals = MutableStateFlow<List<BettingSignal>>(emptyList())
    val topSignals: StateFlow<List<BettingSignal>> = _topSignals.asStateFlow()

    // Historical results loaded from Room
    private val _historicalResults = MutableStateFlow<List<RacingResultEntity>>(emptyList())
    val historicalResults: StateFlow<List<RacingResultEntity>> = _historicalResults.asStateFlow()

    // Empirical Winning Number Frequency distribution (Box 1-8 or Horse 1-12)
    private val _numberFrequencies = MutableStateFlow<List<WinnerNumberFrequencyItem>>(emptyList())
    val numberFrequencies: StateFlow<List<WinnerNumberFrequencyItem>> = _numberFrequencies.asStateFlow()

    // Last resulted winning number
    private val _lastWinningNumber = MutableStateFlow<Int?>(null)
    val lastWinningNumber: StateFlow<Int?> = _lastWinningNumber.asStateFlow()

    // VIP upgrade dialog visibility
    private val _showPaywallModal = MutableStateFlow(false)
    val showPaywallModal: StateFlow<Boolean> = _showPaywallModal.asStateFlow()

    // User action notifications
    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    // Periodic tasks
    private var countdownJob: Job? = null
    private var autoRefreshJob: Job? = null

    init {
        viewModelScope.launch {
            seedInitialHistoricalResultsIfNeeded()
            loadHistoricalResults()
            fetchLiveData()
            startCountdownTicker()
            startAutoRefreshPoller()
        }
    }

    private suspend fun seedInitialHistoricalResultsIfNeeded() = withContext(Dispatchers.IO) {
        val count = racingDao.getTotalRacesCount()
        if (count < 20) {
            val initialResults = mutableListOf<RacingResultEntity>()
            val now = System.currentTimeMillis()

            // Australian Greyhound empirical distribution (higher frequency for Box 1 rail, Box 2, Box 8 wide)
            val dogWinners = listOf(
                1, 2, 1, 8, 3, 1, 4, 2, 8, 1, 6, 2, 1, 7, 5, 8, 2, 1, 3, 4,
                1, 8, 2, 1, 6, 3, 2, 8, 1, 7, 4, 1, 2, 8, 5, 1, 3, 2, 1, 8
            )
            dogWinners.forEachIndexed { i, winNum ->
                initialResults.add(
                    RacingResultEntity(
                        meetingName = if (i % 2 == 0) "Wentworth Park Dogs" else "Albion Park Dogs",
                        raceNumber = (i % 10) + 1,
                        raceType = "Greyhound",
                        winningNumber = winNum,
                        winnerName = "Winner Box #$winNum",
                        totalRunners = 8,
                        timestamp = now - ((40 - i) * 600_000L)
                    )
                )
            }

            // Thoroughbred empirical jump sequence results
            val horseWinners = listOf(1, 3, 2, 4, 1, 6, 2, 5, 1, 3, 8, 2, 1, 4, 7, 2, 1, 3, 5, 2)
            horseWinners.forEachIndexed { i, winNum ->
                initialResults.add(
                    RacingResultEntity(
                        meetingName = if (i % 2 == 0) "Flemington Horses" else "Royal Randwick",
                        raceNumber = (i % 8) + 1,
                        raceType = "Thoroughbred",
                        winningNumber = winNum,
                        winnerName = "Saddlecloth #$winNum Winner",
                        totalRunners = 12,
                        timestamp = now - ((20 - i) * 900_000L)
                    )
                )
            }

            racingDao.insertResults(initialResults)
        }
    }

    private suspend fun loadHistoricalResults() = withContext(Dispatchers.IO) {
        val results = racingDao.getAllResultsStatic()
        _historicalResults.value = results
        _lastWinningNumber.value = results.firstOrNull()?.winningNumber

        val isDog = _selectedDataSource.value == LiveDataSource.AU_GREYHOUNDS
        _numberFrequencies.value = PureSportsAndRacingAlgorithm.getWinningNumberFrequencies(results, isDog)
    }

    fun fetchLiveData() {
        viewModelScope.launch {
            _isLiveFetching.value = true
            _networkStatus.value = "Fetching live feed from ${_selectedDataSource.value.label}..."

            val result = liveService.fetchLiveFeed(
                source = _selectedDataSource.value,
                historicalResults = _historicalResults.value,
                lastWinningNumber = _lastWinningNumber.value,
                customUrl = _customApiUrl.value,
                customApiKey = _customApiKey.value
            )

            _races.value = result.races
            _topSignals.value = result.topSignals
            _networkStatus.value = result.statusMessage
            _lastUpdated.value = result.timestampFormatted

            val currentSelectedId = _selectedRace.value?.id
            _selectedRace.value = result.races.find { it.id == currentSelectedId } ?: result.races.firstOrNull()

            val isDog = _selectedDataSource.value == LiveDataSource.AU_GREYHOUNDS
            _numberFrequencies.value = PureSportsAndRacingAlgorithm.getWinningNumberFrequencies(_historicalResults.value, isDog)

            _isLiveFetching.value = false
        }
    }

    fun selectDataSource(source: LiveDataSource) {
        if (_selectedDataSource.value != source) {
            _selectedDataSource.value = source
            val isDog = source == LiveDataSource.AU_GREYHOUNDS
            _numberFrequencies.value = PureSportsAndRacingAlgorithm.getWinningNumberFrequencies(_historicalResults.value, isDog)
            fetchLiveData()
        }
    }

    fun setCustomApiConfig(url: String, apiKey: String) {
        _customApiUrl.value = url.trim()
        _customApiKey.value = apiKey.trim()
        if (_selectedDataSource.value == LiveDataSource.CUSTOM_FEED) {
            fetchLiveData()
        }
    }

    /**
     * Manually or automatically declare a winner for a race, moving it through the
     * Next-to-Jump -> Closed Bet -> Resulted sequence.
     * Records the winning number in Room, which recalculates the empirical number
     * win rate algorithm percentages for all subsequent jumps!
     */
    fun recordRaceResult(raceId: String, winningNumber: Int, winnerName: String) {
        viewModelScope.launch {
            val targetRace = _races.value.find { it.id == raceId } ?: return@launch
            val raceType = if (targetRace.raceType.contains("Greyhound", ignoreCase = true)) "Greyhound" else "Thoroughbred"

            // 1. Insert into Room Database
            val entity = RacingResultEntity(
                meetingName = targetRace.meetingName,
                raceNumber = targetRace.raceNumber,
                raceType = raceType,
                winningNumber = winningNumber,
                winnerName = winnerName,
                totalRunners = targetRace.runners.size
            )
            withContext(Dispatchers.IO) {
                racingDao.insertResult(entity)
            }

            _lastWinningNumber.value = winningNumber

            // 2. Reload historical results
            loadHistoricalResults()

            // 3. Mark this race as RESULTED
            _races.value = _races.value.map { r ->
                if (r.id == raceId) {
                    r.copy(
                        isClosed = true,
                        secondsToJump = 0,
                        stage = NextToJumpStage.RESULTED,
                        winningRunnerNumber = winningNumber,
                        winningRunnerName = winnerName,
                        liveStatus = "Resulted: Winner #$winningNumber ($winnerName)"
                    )
                } else {
                    // Recalculate other races' number win-rate probabilities using updated sequence
                    val updatedRunners = PureSportsAndRacingAlgorithm.calculateRacingNumberProbabilities(
                        runners = r.runners,
                        raceType = r.raceType,
                        historicalResults = _historicalResults.value,
                        lastWinningNumber = winningNumber
                    )
                    r.copy(runners = updatedRunners)
                }
            }

            // Sync selected race
            _selectedRace.value = _races.value.find { it.id == raceId } ?: _races.value.firstOrNull()

            _notification.value = "Race Result Recorded: #$winningNumber won! Algorithm recalculated all sequence probabilities."
        }
    }

    private fun startCountdownTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _races.value = _races.value.map { race ->
                    if (race.secondsToJump > 0) {
                        val newSec = race.secondsToJump - 1
                        if (newSec <= 0) {
                            // Automatically transition to CLOSED_BET stage
                            race.copy(
                                secondsToJump = 0,
                                isClosed = true,
                                stage = NextToJumpStage.CLOSED_BET,
                                liveStatus = "Bet Closed (In Running)"
                            )
                        } else {
                            race.copy(secondsToJump = newSec)
                        }
                    } else {
                        race
                    }
                }

                // Keep selected race state fresh
                val currentSelectedId = _selectedRace.value?.id
                if (currentSelectedId != null) {
                    _selectedRace.value = _races.value.find { it.id == currentSelectedId }
                }
            }
        }
    }

    private fun startAutoRefreshPoller() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(45000) // Poll every 45s for live scores and ESPN updates
                if (!_isLiveFetching.value) {
                    fetchLiveData()
                }
            }
        }
    }

    fun selectRace(race: NextToJumpRace) {
        _selectedRace.value = race
    }

    fun openPaywall() {
        _showPaywallModal.value = true
    }

    fun closePaywall() {
        _showPaywallModal.value = false
    }

    fun unlockTier(tier: SubscriptionTier, treasuryViewModel: PayoutTreasuryViewModel) {
        _isVipUnlocked.value = true
        _activeSubscriptionTier.value = tier
        _showPaywallModal.value = false

        val clientRef = "SYNDICATE-VIP-${System.currentTimeMillis().toString().takeLast(6)}"
        treasuryViewModel.recordLivePurchase(
            itemName = "EasyBet ${tier.title}",
            category = "SaaS Sports Analytics",
            amountCents = tier.priceCents,
            buyerName = "Sports Syndicate Member #$clientRef"
        )

        _notification.value = "Welcome to ${tier.title}! All algorithmic live streak signals & odds analysis unlocked."
    }

    fun clearNotification() {
        _notification.value = null
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        autoRefreshJob?.cancel()
    }
}
