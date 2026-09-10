package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.viewmodel.EasyBettingViewModel
import com.example.viewmodel.PayoutTreasuryViewModel
import java.util.Locale

// Custom Dual-Color Reference Palette
val WinStreakGreen = Color(0xFF00C853) // Vibrant Emerald Green for wins in a row / high win rate
val WinStreakBg = Color(0xFFE8F5E9)
val LossStreakRed = Color(0xFFD50000)  // Bold Crimson Red for consecutive losses / cold streaks
val LossStreakBg = Color(0xFFFFEBEE)
val ModerateCyan = Color(0xFF00B0FF)   // Cyan for balanced / moderate probability
val FavoriteGold = Color(0xFFFFAB00)

// Official Australian Greyhound Box Rug Colors
fun getBoxRugColor(boxNumber: Int): Pair<Color, Color> {
    return when (boxNumber) {
        1 -> Color(0xFFD32F2F) to Color.White      // Box 1: Red
        2 -> Color(0xFF212121) to Color.White      // Box 2: Black & White
        3 -> Color(0xFFF5F5F5) to Color.Black      // Box 3: White
        4 -> Color(0xFF1976D2) to Color.White      // Box 4: Blue
        5 -> Color(0xFFFBC02D) to Color.Black      // Box 5: Yellow
        6 -> Color(0xFF388E3C) to Color.White      // Box 6: Green
        7 -> Color(0xFF424242) to Color.White      // Box 7: Black
        8 -> Color(0xFFE91E63) to Color.White      // Box 8: Pink
        else -> Color(0xFF616161) to Color.White
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyBettingScreen(
    easyBetViewModel: EasyBettingViewModel,
    treasuryViewModel: PayoutTreasuryViewModel,
    onNavigateToTreasury: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val races by easyBetViewModel.races.collectAsStateWithLifecycle()
    val selectedRace by easyBetViewModel.selectedRace.collectAsStateWithLifecycle()
    val topSignals by easyBetViewModel.topSignals.collectAsStateWithLifecycle()
    val isVipUnlocked by easyBetViewModel.isVipUnlocked.collectAsStateWithLifecycle()
    val activeTier by easyBetViewModel.activeSubscriptionTier.collectAsStateWithLifecycle()
    val showPaywall by easyBetViewModel.showPaywallModal.collectAsStateWithLifecycle()
    val notification by easyBetViewModel.notification.collectAsStateWithLifecycle()
    val heldCents by treasuryViewModel.availableFundCents.collectAsStateWithLifecycle()

    // Live Feed States
    val selectedSource by easyBetViewModel.selectedDataSource.collectAsStateWithLifecycle()
    val isLiveFetching by easyBetViewModel.isLiveFetching.collectAsStateWithLifecycle()
    val networkStatus by easyBetViewModel.networkStatus.collectAsStateWithLifecycle()
    val lastUpdated by easyBetViewModel.lastUpdated.collectAsStateWithLifecycle()
    val customUrl by easyBetViewModel.customApiUrl.collectAsStateWithLifecycle()
    val customKey by easyBetViewModel.customApiKey.collectAsStateWithLifecycle()

    // Empirical Number Algorithm States
    val numberFrequencies by easyBetViewModel.numberFrequencies.collectAsStateWithLifecycle()
    val lastWinningNumber by easyBetViewModel.lastWinningNumber.collectAsStateWithLifecycle()
    val historicalResults by easyBetViewModel.historicalResults.collectAsStateWithLifecycle()

    var showCustomApiDialog by remember { mutableStateOf(false) }
    var raceToResultDialog by remember { mutableStateOf<NextToJumpRace?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notification) {
        notification?.let {
            snackbarHostState.showSnackbar(it)
            easyBetViewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QueryStats,
                            contentDescription = "EasyBetting",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "EasyBetting Live",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isVipUnlocked) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "PRO VIP",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Pure Number Frequency & Home/Away Algorithms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Treasury Link Badge
                    FilledTonalButton(
                        onClick = onNavigateToTreasury,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "$%,.2f", heldCents / 100.0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Live Refresh Button
                    IconButton(
                        onClick = { easyBetViewModel.fetchLiveData() },
                        enabled = !isLiveFetching,
                        modifier = Modifier.testTag("refresh_live_data_btn")
                    ) {
                        if (isLiveFetching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Live Data")
                        }
                    }

                    if (!isVipUnlocked) {
                        Button(
                            onClick = { easyBetViewModel.openPaywall() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp).testTag("vip_unlock_topbar_btn")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VIP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
        ) {
            // Live Server Status & Source Selection Card
            item {
                LiveNetworkBanner(
                    source = selectedSource,
                    isFetching = isLiveFetching,
                    statusText = networkStatus,
                    lastUpdated = lastUpdated,
                    onConfigureCustomApi = { showCustomApiDialog = true }
                )
            }

            // Live Feed Category Selector Row
            item {
                LiveSourceSelectorRow(
                    selectedSource = selectedSource,
                    onSelectSource = { source ->
                        if (source == LiveDataSource.CUSTOM_FEED) {
                            showCustomApiDialog = true
                        }
                        easyBetViewModel.selectDataSource(source)
                    }
                )
            }

            // Empirical Winning Number Frequency Card (for Racing feeds)
            if (selectedSource == LiveDataSource.AU_GREYHOUNDS ||
                selectedSource == LiveDataSource.AU_THOROUGHBRED ||
                selectedSource == LiveDataSource.AU_HARNESS
            ) {
                item {
                    WinningNumberMatrixCard(
                        frequencies = numberFrequencies,
                        isDogRace = selectedSource == LiveDataSource.AU_GREYHOUNDS,
                        lastWinningNumber = lastWinningNumber,
                        totalHistoricalRaces = historicalResults.size
                    )
                }
            }

            // High Conviction Streak Signals Horizontal Carousel
            if (topSignals.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(WinStreakGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Algorithm Conviction Signals",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${topSignals.size} Live Edges",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(topSignals) { signal ->
                                SignalCard(
                                    signal = signal,
                                    isUnlocked = isVipUnlocked || !signal.isVipExclusive,
                                    onUnlockClick = { easyBetViewModel.openPaywall() }
                                )
                            }
                        }
                    }
                }
            }

            // Next To Jump / Live Fixtures Carousel
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedSource == LiveDataSource.LIVESCORE_GLOBAL) "Live Games In-Play" else "Next Events To Jump (Sequence)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${races.size} Events",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (races.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isLiveFetching) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Connecting to live sports servers...", textAlign = TextAlign.Center)
                                } else {
                                    Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No active events found for this feed. Tap refresh to poll the live servers.",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(onClick = { easyBetViewModel.fetchLiveData() }) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Poll Live Feed Now")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(races) { race ->
                                val isSelected = selectedRace?.id == race.id
                                NextToJumpChip(
                                    race = race,
                                    isSelected = isSelected,
                                    onClick = { easyBetViewModel.selectRace(race) }
                                )
                            }
                        }
                    }
                }
            }

            // Selected Race Deep-Dive & Runner Grid
            selectedRace?.let { race ->
                item {
                    RaceDetailHeaderCard(
                        race = race,
                        onRecordResultClick = { raceToResultDialog = race }
                    )
                }

                // If Team Sports Home/Away data is present, show the Home/Away Card
                if (race.homeTeamName != null && race.awayTeamName != null) {
                    item {
                        HomeAwayAlgorithmCard(race = race)
                    }
                }

                item {
                    Text(
                        text = if (race.homeTeamName != null) "Competitors & Win-Rate Probabilities" else "Runners & Number Frequency Probabilities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(race.runners) { runner ->
                    RunnerStreakRow(
                        runner = runner,
                        isRaceClosed = race.isClosed,
                        isDogRace = race.raceType.contains("Greyhound", ignoreCase = true)
                    )
                }
            }

            // Commercial SaaS Monetization Card
            item {
                MonetizationBanner(
                    isVipUnlocked = isVipUnlocked,
                    activeTier = activeTier,
                    onOpenPaywall = { easyBetViewModel.openPaywall() },
                    onGoToTreasury = onNavigateToTreasury
                )
            }
        }
    }

    // VIP Subscription Paywall Modal
    if (showPaywall) {
        PaywallDialog(
            onDismiss = { easyBetViewModel.closePaywall() },
            onSelectTier = { tier ->
                easyBetViewModel.unlockTier(tier, treasuryViewModel)
            }
        )
    }

    // Custom Live API Configuration Dialog
    if (showCustomApiDialog) {
        CustomApiConfigDialog(
            initialUrl = customUrl,
            initialKey = customKey,
            onSaveAndTest = { url, key ->
                easyBetViewModel.setCustomApiConfig(url, key)
                showCustomApiDialog = false
            },
            onDismiss = { showCustomApiDialog = false }
        )
    }

    // Race Result Declaration Dialog (Next-to-Jump -> Closed -> Resulted)
    raceToResultDialog?.let { targetRace ->
        DeclareWinnerDialog(
            race = targetRace,
            onWinnerSelected = { winNum, winName ->
                easyBetViewModel.recordRaceResult(targetRace.id, winNum, winName)
                raceToResultDialog = null
            },
            onDismiss = { raceToResultDialog = null }
        )
    }
}

@Composable
fun WinningNumberMatrixCard(
    frequencies: List<WinnerNumberFrequencyItem>,
    isDogRace: Boolean,
    lastWinningNumber: Int?,
    totalHistoricalRaces: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDogRace) "Empirical Box 1-8 Frequency Matrix" else "Winning Saddlecloth Probability",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                }

                if (lastWinningNumber != null) {
                    Surface(
                        color = WinStreakGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, WinStreakGreen)
                    ) {
                        Text(
                            text = "Last Winner: #$lastWinningNumber",
                            color = WinStreakGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pure winning number frequency from jump sequence ($totalHistoricalRaces races). Excludes $ bookmaker payouts.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Number Win Rate Frequency Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                frequencies.forEach { item ->
                    val (boxBg, boxFg) = if (isDogRace) getBoxRugColor(item.number) else (MaterialTheme.colorScheme.primary to Color.White)
                    val progressFraction = (item.winPercentage / 30.0).coerceIn(0.05, 1.0).toFloat()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Box/Saddlecloth Badge
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(boxBg)
                                .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${item.number}",
                                color = boxFg,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Label
                        Text(
                            text = item.label,
                            modifier = Modifier.width(110.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )

                        // Visual Frequency Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        if (item.hotStreak) WinStreakGreen
                                        else if (item.winPercentage >= 12.0) ModerateCyan
                                        else LossStreakRed.copy(alpha = 0.7f)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Percentage Text
                        Text(
                            text = "${item.winPercentage}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.hotStreak) WinStreakGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeAwayAlgorithmCard(race: NextToJumpRace) {
    val homeName = race.homeTeamName ?: "Home Team"
    val awayName = race.awayTeamName ?: "Away Team"
    val homeRecord = race.homeWinLossRecord ?: "Record N/A"
    val awayRecord = race.awayWinLossRecord ?: "Record N/A"
    val homeProb = race.algorithmHomeWinProb ?: 50.0
    val awayProb = race.algorithmAwayWinProb ?: 50.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SportsScore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Home & Away Win/Loss Algorithm",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ZERO ODDS INFLUENCE",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Home vs Away Stats Split
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = homeName,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = homeRecord,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = if (homeProb >= 50.0) WinStreakBg else LossStreakBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${homeProb}% Algorithm Win Prob",
                            color = if (homeProb >= 50.0) WinStreakGreen else LossStreakRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "VS",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = awayName,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = awayRecord,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = if (awayProb >= 50.0) WinStreakBg else LossStreakBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${awayProb}% Algorithm Win Prob",
                            color = if (awayProb >= 50.0) WinStreakGreen else LossStreakRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comparative Ratio Bar
            val homeFraction = (homeProb / 100.0).toFloat().coerceIn(0.05f, 0.95f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(homeFraction)
                        .fillMaxHeight()
                        .background(WinStreakGreen)
                )
                Box(
                    modifier = Modifier
                        .weight(1f - homeFraction)
                        .fillMaxHeight()
                        .background(LossStreakRed)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = race.algorithmBasis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun RaceDetailHeaderCard(
    race: NextToJumpRace,
    onRecordResultClick: () -> Unit
) {
    val isRacing = race.raceType.contains("Greyhound", ignoreCase = true) ||
            race.raceType.contains("Thoroughbred", ignoreCase = true) ||
            race.raceType.contains("Harness", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = race.meetingName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${race.raceType} · Venue: ${race.trackCondition}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Source: ${race.sourceApi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = when (race.stage) {
                        NextToJumpStage.NEXT_TO_JUMP -> MaterialTheme.colorScheme.primary
                        NextToJumpStage.CLOSED_BET -> FavoriteGold
                        NextToJumpStage.RESULTED -> WinStreakGreen
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (race.stage) {
                                NextToJumpStage.NEXT_TO_JUMP -> if (race.secondsToJump > 0) "${race.secondsToJump}s" else "TO JUMP"
                                NextToJumpStage.CLOSED_BET -> "CLOSED"
                                NextToJumpStage.RESULTED -> "RESULTED"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Text(
                            text = race.stage.label,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper: Next to Jump -> Closed Bet -> Resulted
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StageStepItem(
                    title = "1. Next to Jump",
                    isActive = race.stage == NextToJumpStage.NEXT_TO_JUMP,
                    isDone = race.stage != NextToJumpStage.NEXT_TO_JUMP
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
                StageStepItem(
                    title = "2. Closed Bet",
                    isActive = race.stage == NextToJumpStage.CLOSED_BET,
                    isDone = race.stage == NextToJumpStage.RESULTED
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
                StageStepItem(
                    title = "3. Resulted",
                    isActive = race.stage == NextToJumpStage.RESULTED,
                    isDone = race.stage == NextToJumpStage.RESULTED
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button: Declare / Record Result
            if (isRacing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (race.winningRunnerNumber != null) {
                        Surface(
                            color = WinStreakBg,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, WinStreakGreen)
                        ) {
                            Text(
                                text = "🏆 Winner: #${race.winningRunnerNumber} ${race.winningRunnerName ?: ""}",
                                color = WinStreakGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Model Confidence: ${race.algorithmConfidence}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FilledTonalButton(
                        onClick = onRecordResultClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (race.stage == NextToJumpStage.RESULTED) "Re-declare Result" else "Record Race Result", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StageStepItem(
    title: String,
    isActive: Boolean,
    isDone: Boolean
) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primary
        else if (isDone) WinStreakGreen.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary
            else if (isDone) WinStreakGreen
            else Color.Transparent
        )
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary
            else if (isDone) WinStreakGreen
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RunnerStreakRow(
    runner: RaceRunner,
    isRaceClosed: Boolean,
    isDogRace: Boolean = false
) {
    val prob = runner.easyBetCalculatedProbability
    val (rugBg, rugFg) = if (isDogRace) getBoxRugColor(runner.number) else (MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant)

    val isHighProb = prob >= (if (isDogRace) 18.0 else 55.0)
    val isModerateProb = prob >= (if (isDogRace) 12.0 else 40.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (runner.isFavorite) FavoriteGold
            else if (isHighProb) WinStreakGreen.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Runner Number & Name
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(rugBg)
                            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${runner.number}",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = rugFg
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = runner.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (runner.isFavorite) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = FavoriteGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "TOP PROB",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        color = Color(0xFFE65100),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        Text(
                            text = runner.jockeyOrDriver,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Win % Badge
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = if (isHighProb) WinStreakBg else if (isModerateProb) MaterialTheme.colorScheme.secondaryContainer else LossStreakBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (isHighProb) WinStreakGreen else if (isModerateProb) ModerateCyan else LossStreakRed)
                    ) {
                        Text(
                            text = "${runner.easyBetCalculatedProbability}% Win Rate",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isHighProb) WinStreakGreen else if (isModerateProb) MaterialTheme.colorScheme.onSecondaryContainer else LossStreakRed
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Fair Odds: $${runner.easyBetFairOdds}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Explanation basis banner
            if (runner.algorithmCalculationBasis.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = runner.algorithmCalculationBasis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Streak Badges & Dual Color Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Form History Dots (Green for Win, Red for Loss)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Form: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    runner.lastFiveRaces.forEach { isWin ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isWin) WinStreakGreen else LossStreakRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isWin) "W" else "L",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Dual Color Streak Counter
                if (runner.consecutiveWins > 0) {
                    Surface(
                        color = WinStreakBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, WinStreakGreen.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = WinStreakGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${runner.consecutiveWins} Wins in a Row",
                                color = WinStreakGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        color = LossStreakBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, LossStreakRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = LossStreakRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${runner.consecutiveLosses} Behind / Losses",
                                color = LossStreakRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeclareWinnerDialog(
    race: NextToJumpRace,
    onWinnerSelected: (winningNumber: Int, winnerName: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Declare Winner / Record Result", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select the winning number for ${race.meetingName} (Race ${race.raceNumber}). This will record the official result into Room and instantly recalculate subsequent jump sequence probabilities!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                race.runners.forEach { runner ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWinnerSelected(runner.number, runner.name) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${runner.number}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(runner.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("Prob: ${runner.easyBetCalculatedProbability}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Icon(Icons.Default.Check, contentDescription = "Select Winner", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LiveNetworkBanner(
    source: LiveDataSource,
    isFetching: Boolean,
    statusText: String,
    lastUpdated: String,
    onConfigureCustomApi: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isFetching) FavoriteGold else WinStreakGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFetching) "SYNCING LIVE FEED..." else "LIVE FEED ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isFetching) Color(0xFFE65100) else WinStreakGreen
                    )
                }

                if (source == LiveDataSource.CUSTOM_FEED) {
                    TextButton(
                        onClick = onConfigureCustomApi,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Configure API", fontSize = 11.sp)
                    }
                } else {
                    Text(
                        text = "Updated: $lastUpdated",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LiveSourceSelectorRow(
    selectedSource: LiveDataSource,
    onSelectSource: (LiveDataSource) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(LiveDataSource.values()) { source ->
            val isSelected = selectedSource == source
            FilterChip(
                selected = isSelected,
                onClick = { onSelectSource(source) },
                label = {
                    Text(
                        text = source.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
fun NextToJumpChip(
    race: NextToJumpRace,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val mins = race.secondsToJump / 60
    val secs = race.secondsToJump % 60
    val countdownText = if (race.stage == NextToJumpStage.RESULTED) "RESULTED"
    else if (race.stage == NextToJumpStage.CLOSED_BET || race.secondsToJump <= 0) "CLOSED"
    else String.format(Locale.US, "%dm %02ds", mins, secs)

    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("race_chip_${race.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = race.meetingName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            if (race.homeScore != null && race.awayScore != null) {
                Text(
                    text = "${race.homeScore} - ${race.awayScore}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = race.raceType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = when (race.stage) {
                    NextToJumpStage.RESULTED -> WinStreakGreen
                    NextToJumpStage.CLOSED_BET -> FavoriteGold
                    NextToJumpStage.NEXT_TO_JUMP -> if (race.secondsToJump < 120) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = countdownText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = when (race.stage) {
                        NextToJumpStage.RESULTED -> Color.White
                        NextToJumpStage.CLOSED_BET -> Color.Black
                        NextToJumpStage.NEXT_TO_JUMP -> if (race.secondsToJump < 120) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}

@Composable
fun SignalCard(
    signal: BettingSignal,
    isUnlocked: Boolean,
    onUnlockClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (signal.isWinStreak) WinStreakGreen.copy(alpha = 0.5f) else LossStreakRed.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (signal.isWinStreak) WinStreakBg else LossStreakBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = signal.signalType,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = if (signal.isWinStreak) WinStreakGreen else LossStreakRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${signal.confidencePct}% Score",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = signal.runnerName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = signal.meetingName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dual color streak indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (signal.isWinStreak) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (signal.isWinStreak) WinStreakGreen else LossStreakRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = signal.winLossSequenceText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (signal.isWinStreak) WinStreakGreen else LossStreakRed,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isUnlocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Live Fair", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${signal.fairOdds}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Market", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${signal.marketOdds}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Edge", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${signal.edgePercentage}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WinStreakGreen)
                    }
                }
            } else {
                Button(
                    onClick = onUnlockClick,
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Unlock VIP Odds", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun MonetizationBanner(
    isVipUnlocked: Boolean,
    activeTier: SubscriptionTier?,
    onOpenPaywall: () -> Unit,
    onGoToTreasury: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Commercial App Monetization",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = onGoToTreasury) {
                    Text("Treasury Vault", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isVipUnlocked)
                    "Active Membership: ${activeTier?.title ?: "VIP Pro"}. Subscription proceeds are credited to Corey & Sarah's Payout Treasury."
                else
                    "Monetize this app cleanly on Google Play via Pro VIP subscriptions ($19.99/mo). All purchases automatically deposit into Corey & Sarah's payout fund.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            if (!isVipUnlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onOpenPaywall,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Explore VIP Subscriptions & Pricing")
                }
            }
        }
    }
}

@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onSelectTier: (SubscriptionTier) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("EasyBet Pro VIP Upgrade", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Unlock live proprietary algorithmic streak calculations, mathematical edge percentages, and favorite trackers until jump.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SubscriptionTier.values().forEach { tier ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTier(tier) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (tier.popular) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (tier.popular) 2.dp else 1.dp,
                            color = if (tier.popular) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tier.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tier.priceDisplay,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            tier.features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = WinStreakGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = feature,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CustomApiConfigDialog(
    initialUrl: String,
    initialKey: String,
    onSaveAndTest: (url: String, key: String) -> Unit,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }
    var keyText by remember { mutableStateOf(initialKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Configure Custom Live Sports API", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Connect any live JSON sports feed or The Odds API endpoint. Enter your endpoint URL and optional API key below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Live API Endpoint URL *") },
                    placeholder = { Text("https://api.the-odds-api.com/v4/sports/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("API Key (Optional)") },
                    placeholder = { Text("Enter your provider API key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveAndTest(urlText, keyText) },
                enabled = urlText.isNotBlank()
            ) {
                Text("Save & Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
