package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnatomyExploreScreen
import com.example.ui.screens.BrainExplorerScreen
import com.example.ui.screens.BrainGymScreen
import com.example.ui.screens.NeuroHabitsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ActiveGame
import com.example.viewmodel.BrainViewModel
import com.example.viewmodel.ChatbotViewModel
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.VoiceSessionScreen
import com.example.ui.screens.ProfileAuthScreen
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Close
import com.example.viewmodel.WellnessViewModel
import com.example.ui.screens.WellnessSurveyScreen
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import com.example.viewmodel.IdeaViewModel
import com.example.ui.screens.IdeaVaultScreen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Lightbulb

import com.example.ui.screens.MindsetStarterScreen
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AutoAwesome

import com.example.viewmodel.TruthAnalysisViewModel
import com.example.ui.screens.TruthAnalysisScreen
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.outlined.Policy

import com.example.viewmodel.LiveBookViewModel
import com.example.ui.screens.LiveBookScreen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.MenuBook

import com.example.viewmodel.CommandViewModel
import com.example.ui.screens.CommandCenterScreen
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Terminal

import com.example.viewmodel.CommunityViewModel
import com.example.ui.screens.CommunityTasksScreen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.Group

import com.example.viewmodel.UserProgressViewModel
import com.example.ui.screens.UserProgressScreen
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.TrendingUp

import com.example.ui.screens.EvolutionBuilderScreen
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import com.example.viewmodel.AdminAuthViewModel
import com.example.viewmodel.PayoutTreasuryViewModel
import com.example.ui.screens.AdminPayoutScreen
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BrainApp()
            }
        }
    }
}

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainApp(
    viewModel: BrainViewModel = viewModel(), 
    chatbotViewModel: ChatbotViewModel = viewModel(),
    wellnessViewModel: WellnessViewModel = viewModel(),
    ideaViewModel: IdeaViewModel = viewModel(),
    truthAnalysisViewModel: TruthAnalysisViewModel = viewModel(),
    liveBookViewModel: LiveBookViewModel = viewModel(),
    commandViewModel: CommandViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel(),
    userProgressViewModel: UserProgressViewModel = viewModel(),
    adminAuthViewModel: AdminAuthViewModel = viewModel(),
    treasuryViewModel: PayoutTreasuryViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeGame by viewModel.activeGame.collectAsStateWithLifecycle()
    val currentUser by adminAuthViewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthedAdmin by adminAuthViewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val heldFundCents by treasuryViewModel.availableFundCents.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem("Starter", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_item_starter"),
        NavItem("Terminal", Icons.Filled.Terminal, Icons.Outlined.Terminal, "nav_item_terminal"),
        NavItem("Vault", Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, "nav_item_vault"),
        NavItem("Book", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_item_book"),
        NavItem("Progress", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp, "nav_item_progress"),
        NavItem("Payouts", Icons.Filled.Savings, Icons.Outlined.Savings, "nav_item_payouts"),
        NavItem("Map", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_item_map"),
        NavItem("Evolve", Icons.Filled.Group, Icons.Outlined.Group, "nav_item_evolve"),
        NavItem("Builder", Icons.Filled.Build, Icons.Outlined.Build, "nav_item_builder"),
        NavItem("Coach", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubble, "nav_item_coach"),
        NavItem("Analyzer", Icons.Filled.Policy, Icons.Outlined.Policy, "nav_item_analyzer"),
        NavItem("Wellness", Icons.Filled.Favorite, Icons.Outlined.Favorite, "nav_item_wellness"),
        NavItem("Account", Icons.Filled.Person, Icons.Outlined.Person, "nav_item_account")
    )

    var showVoiceSession by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showTreasuryScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showProfileScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (activeGame == ActiveGame.NONE && !showVoiceSession && !showTreasuryScreen && !showProfileScreen) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "corey++sarah++",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isAuthedAdmin) "Admin: ${currentUser.displayName}" else "Learner Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        // Quick-access Payout Treasury button
                        FilledTonalButton(
                            onClick = { showTreasuryScreen = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .testTag("topbar_payout_treasury_btn")
                        ) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = "Payout Treasury",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val heldFormatted = String.format(Locale.US, "$%,.2f", heldFundCents / 100.0)
                            Text(
                                text = "$heldFormatted Held",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // User profile / switcher avatar
                        IconButton(
                            onClick = { showProfileScreen = true },
                            modifier = Modifier.testTag("topbar_user_profile_btn")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentUser == com.example.viewmodel.UserRole.COREY) MaterialTheme.colorScheme.primary
                                        else if (currentUser == com.example.viewmodel.UserRole.SARAH) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.outline
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentUser == com.example.viewmodel.UserRole.COREY) "C"
                                    else if (currentUser == com.example.viewmodel.UserRole.SARAH) "S" else "L",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (activeGame == ActiveGame.NONE && !showVoiceSession && !showTreasuryScreen && !showProfileScreen) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(index) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .testTag(item.testTag)
                                .height(64.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 9 && activeGame == ActiveGame.NONE && !showVoiceSession && !showTreasuryScreen && !showProfileScreen) {
                FloatingActionButton(onClick = { showVoiceSession = true }) {
                    Icon(androidx.compose.material.icons.Icons.Default.Mic, contentDescription = "Voice Session")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showTreasuryScreen) {
                AdminPayoutScreen(
                    authViewModel = adminAuthViewModel,
                    treasuryViewModel = treasuryViewModel,
                    onBack = { showTreasuryScreen = false }
                )
            } else if (showProfileScreen) {
                ProfileAuthScreen(
                    authViewModel = adminAuthViewModel,
                    treasuryViewModel = treasuryViewModel,
                    onNavigateToTreasury = {
                        showProfileScreen = false
                        showTreasuryScreen = true
                    },
                    onBack = { showProfileScreen = false }
                )
            } else if (showVoiceSession) {
                Box(modifier = Modifier.fillMaxSize()) {
                    VoiceSessionScreen()
                    IconButton(
                        onClick = { showVoiceSession = false },
                        modifier = Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 16.dp)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close")
                    }
                }
            } else {
                AnimatedContent(
                    targetState = if (activeGame != ActiveGame.NONE) -1 else selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        -1 -> BrainGymScreen(viewModel = viewModel)
                        0 -> MindsetStarterScreen()
                        1 -> CommandCenterScreen(viewModel = commandViewModel)
                        2 -> IdeaVaultScreen(viewModel = ideaViewModel)
                        3 -> LiveBookScreen(viewModel = liveBookViewModel)
                        4 -> UserProgressScreen(viewModel = userProgressViewModel, brainViewModel = viewModel)
                        5 -> AdminPayoutScreen(
                            authViewModel = adminAuthViewModel,
                            treasuryViewModel = treasuryViewModel
                        )
                        6 -> com.example.ui.screens.BrainMapScreen()
                        7 -> CommunityTasksScreen(viewModel = communityViewModel)
                        8 -> EvolutionBuilderScreen()
                        9 -> ChatbotScreen(viewModel = chatbotViewModel)
                        10 -> TruthAnalysisScreen(viewModel = truthAnalysisViewModel)
                        11 -> WellnessSurveyScreen(viewModel = wellnessViewModel)
                        12 -> ProfileAuthScreen(
                            authViewModel = adminAuthViewModel,
                            treasuryViewModel = treasuryViewModel,
                            onNavigateToTreasury = { viewModel.selectTab(5) }
                        )
                        else -> MindsetStarterScreen()
                    }
                }
            }
        }
    }
}
