package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AdminAuthViewModel
import com.example.viewmodel.PayoutTreasuryViewModel
import com.example.viewmodel.UserRole
import java.util.Locale

@Composable
fun ProfileAuthScreen(
    authViewModel: AdminAuthViewModel,
    treasuryViewModel: PayoutTreasuryViewModel,
    onNavigateToTreasury: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthed by authViewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val heldFundCents by treasuryViewModel.availableFundCents.collectAsStateWithLifecycle()
    val selectedDest by treasuryViewModel.selectedDestination.collectAsStateWithLifecycle()

    var showPurchasedSnackbar by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        if (onBack != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Account & Administration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        // App & User Identity Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentUser == UserRole.COREY) MaterialTheme.colorScheme.primary
                                else if (currentUser == UserRole.SARAH) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outline
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentUser == UserRole.COREY) "C" else if (currentUser == UserRole.SARAH) "S" else "L",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAuthed) "Admin ${currentUser.displayName}" else "Learner Account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isAuthed) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ADMIN VERIFIED",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = currentUser.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentUser.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Account Switcher / Login Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Switch User / Admin Login",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Separate login credentials for Corey and Sarah to manage app treasury funds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRoleButton(
                            title = "Corey (Admin)",
                            isSelected = currentUser == UserRole.COREY,
                            onClick = { authViewModel.quickSwitch(UserRole.COREY) },
                            modifier = Modifier.weight(1f)
                        )
                        UserRoleButton(
                            title = "Sarah (Admin)",
                            isSelected = currentUser == UserRole.SARAH,
                            onClick = { authViewModel.quickSwitch(UserRole.SARAH) },
                            modifier = Modifier.weight(1f)
                        )
                        UserRoleButton(
                            title = "Learner",
                            isSelected = currentUser == UserRole.LEARNER,
                            onClick = { authViewModel.logoutToLearner() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Treasury Shortcut Card (Especially prominent for Corey & Sarah)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTreasury() }
                    .testTag("open_payout_treasury_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin Payout Treasury Vault",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val heldStr = String.format(Locale.US, "$%,.2f held in funds", heldFundCents / 100.0)
                        Text(
                            text = "$heldStr • Active: ${selectedDest?.name ?: "No destination selected"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Treasury",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // In-App Store Purchases (Show users what they can purchase, feeding Corey & Sarah's fund)
        item {
            Text(
                text = "In-App Store & Memberships",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "All purchases automatically settle into Corey & Sarah's Payout Fund",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val storeItems = listOf(
            Triple("Lifetime Neuro Masterclass Pass", "$149.99", 14999L),
            Triple("Synaptic Pro Annual Membership", "$89.99", 8999L),
            Triple("Cognitive Battery Assessment", "$29.99", 2999L),
            Triple("500 Synapse Quantum Credits", "$19.99", 1999L)
        )

        items(storeItems.size) { index ->
            val item = storeItems[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Instant unlock • Proceeds build app payout fund",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            val buyer = if (currentUser.isAdmin) "Learner Guest" else currentUser.displayName
                            treasuryViewModel.recordSimulatedPurchase(item.first, "STORE_PURCHASE", item.third, buyer)
                            showPurchasedSnackbar = "Purchased ${item.first}!"
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = item.second, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRoleButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Text(text = title, fontSize = 11.sp, maxLines = 1)
        }
    }
}
