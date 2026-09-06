package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnatomyExploreScreen
import com.example.ui.screens.BrainGymScreen
import com.example.ui.screens.NeuroHabitsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ActiveGame
import com.example.viewmodel.BrainViewModel

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

@Composable
fun BrainApp(viewModel: BrainViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val activeGame by viewModel.activeGame.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem("Explore", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_item_explore"),
        NavItem("Brain Gym", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "nav_item_gym"),
        NavItem("Habits", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_item_habits"),
        NavItem("Analytics", Icons.Filled.Insights, Icons.Outlined.Insights, "nav_item_stats")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Only display bottom navigation bar when not playing a workout mini-game
            if (activeGame == ActiveGame.NONE) {
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
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .testTag(item.testTag)
                                .height(56.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = if (activeGame != ActiveGame.NONE) -1 else selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    -1 -> {
                        // Game in progress
                        BrainGymScreen(viewModel = viewModel)
                    }
                    0 -> AnatomyExploreScreen(viewModel = viewModel)
                    1 -> BrainGymScreen(viewModel = viewModel)
                    2 -> NeuroHabitsScreen(viewModel = viewModel)
                    3 -> StatsScreen(viewModel = viewModel)
                    else -> AnatomyExploreScreen(viewModel = viewModel)
                }
            }
        }
    }
}
