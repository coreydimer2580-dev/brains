package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PayoutDestinationEntity
import com.example.data.PayoutDisbursementEntity
import com.example.data.PurchaseEntity
import com.example.viewmodel.AdminAuthViewModel
import com.example.viewmodel.PayoutTreasuryViewModel
import com.example.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPayoutScreen(
    authViewModel: AdminAuthViewModel,
    treasuryViewModel: PayoutTreasuryViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthed by authViewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val loginError by authViewModel.loginError.collectAsStateWithLifecycle()

    val heldFundCents by treasuryViewModel.availableFundCents.collectAsStateWithLifecycle()
    val grossRevenueCents by treasuryViewModel.totalGrossRevenueCents.collectAsStateWithLifecycle()
    val totalDisbursedCents by treasuryViewModel.totalDisbursedCents.collectAsStateWithLifecycle()
    val totalNetHeldCents by treasuryViewModel.totalNetHeldCents.collectAsStateWithLifecycle()

    val destinations by treasuryViewModel.allDestinations.collectAsStateWithLifecycle()
    val selectedDest by treasuryViewModel.selectedDestination.collectAsStateWithLifecycle()
    val purchases by treasuryViewModel.allPurchases.collectAsStateWithLifecycle()
    val disbursements by treasuryViewModel.allDisbursements.collectAsStateWithLifecycle()
    val statusMessage by treasuryViewModel.statusMessage.collectAsStateWithLifecycle()

    var showLoginDialog by remember { mutableStateOf(false) }
    var showAddDestDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview & Destinations, 1: App Purchases, 2: Payout Ledger

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            treasuryViewModel.clearStatusMessage()
        }
    }

    Scaffold(
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "corey++sarah++",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Payout Treasury",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isAuthed) "Logged in as ${currentUser.displayName} (${currentUser.title})" else "Guest / Learner Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Admin Switcher button
                    FilledTonalButton(
                        onClick = { showLoginDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("admin_user_switch_btn"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isAuthed) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                            contentDescription = "User Login",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAuthed) currentUser.displayName else "Admin Login",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            // Role & Access Banner
            item {
                AdminIdentityCard(
                    currentUser = currentUser,
                    isAuthed = isAuthed,
                    onSwitchUser = { showLoginDialog = true }
                )
            }

            // Main Treasury Fund Hero Card
            item {
                TreasuryFundCard(
                    heldFundCents = heldFundCents,
                    grossRevenueCents = grossRevenueCents,
                    totalNetHeldCents = totalNetHeldCents,
                    totalDisbursedCents = totalDisbursedCents,
                    selectedDestination = selectedDest,
                    isAuthed = isAuthed,
                    onWithdrawClick = { showWithdrawDialog = true },
                    onRecordLivePayment = { showRecordPaymentDialog = true }
                )
            }

            // Sub-navigation Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Payout Destinations", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("App Purchases (${purchases.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Disbursements", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
                    )
                }
            }

            // Tab 0: Payout Destinations Section
            if (selectedTab == 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Configured Payout Destinations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choose where built funds from all app purchases are held & routed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isAuthed) {
                            Button(
                                onClick = { showAddDestDialog = true },
                                modifier = Modifier.testTag("add_destination_btn"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Bank", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                if (destinations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "No payout destinations configured yet. Tap 'Add Bank' to set up an account.",
                                modifier = Modifier.padding(20.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(destinations, key = { it.id }) { dest ->
                        DestinationItemCard(
                            destination = dest,
                            isAuthed = isAuthed,
                            onSelect = {
                                treasuryViewModel.selectDestination(dest.id)
                            },
                            onDelete = {
                                treasuryViewModel.deleteDestination(dest.id)
                            }
                        )
                    }
                }
            }

            // Tab 1: App Purchases (Building the Fund)
            if (selectedTab == 1) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Incoming App Purchases",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All purchases across the app automatically accumulate into the payout pool",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { showRecordPaymentDialog = true },
                            modifier = Modifier.testTag("record_live_payment_btn"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Payment", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (purchases.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "No revenue transactions recorded yet. Tap 'Record Payment' or receive client payments to accumulate the held payout fund.",
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(purchases, key = { it.id }) { purchase ->
                        PurchaseItemRow(purchase = purchase)
                    }
                }
            }

            // Tab 2: Payout Ledger / Disbursements
            if (selectedTab == 2) {
                item {
                    Column {
                        Text(
                            text = "Payout Disbursements Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Audited record of funds released to Corey and Sarah's chosen payout destination",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (disbursements.isEmpty()) {
                    item {
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
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No payout disbursements recorded yet.",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap 'Withdraw / Send Payout' in the overview card to transfer available held funds.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(disbursements, key = { it.id }) { disburse ->
                        DisbursementItemCard(disbursement = disburse)
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Admin Login & User Switch Dialog
    if (showLoginDialog) {
        AdminUserLoginDialog(
            currentUser = currentUser,
            loginError = loginError,
            onSelectRole = { role, pin ->
                val success = authViewModel.login(role, pin)
                if (success) {
                    showLoginDialog = false
                }
            },
            onQuickSwitch = { role ->
                authViewModel.quickSwitch(role)
                showLoginDialog = false
            },
            onDismiss = {
                authViewModel.clearError()
                showLoginDialog = false
            }
        )
    }

    // 2. Add New Payout Destination Dialog
    if (showAddDestDialog) {
        AddDestinationDialog(
            adminName = currentUser.displayName,
            onAdd = { name, type, holder, identifier, routing, setActive ->
                treasuryViewModel.addNewDestination(
                    name = name,
                    type = type,
                    holder = holder,
                    identifier = identifier,
                    routing = routing,
                    adminName = "Admin ${currentUser.displayName}",
                    setAsActive = setActive
                )
                showAddDestDialog = false
            },
            onDismiss = { showAddDestDialog = false }
        )
    }

    // 3. Withdraw / Disburse Funds Dialog
    if (showWithdrawDialog) {
        DisburseFundsDialog(
            heldFundCents = heldFundCents,
            selectedDest = selectedDest,
            currentAdmin = currentUser.displayName,
            onConfirm = { amountCents, note ->
                treasuryViewModel.requestDisbursement(
                    amountCents = amountCents,
                    adminName = "Admin ${currentUser.displayName}",
                    note = note
                )
                showWithdrawDialog = false
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }

    // 4. Record Live Payment Dialog
    if (showRecordPaymentDialog) {
        RecordLivePaymentDialog(
            onRecordPayment = { name, category, amountCents, buyer, email ->
                treasuryViewModel.recordLivePurchase(name, category, amountCents, buyer, email)
                showRecordPaymentDialog = false
            },
            onDismiss = { showRecordPaymentDialog = false }
        )
    }
}

@Composable
private fun AdminIdentityCard(
    currentUser: UserRole,
    isAuthed: Boolean,
    onSwitchUser: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isAuthed) {
                if (currentUser == UserRole.COREY) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            } else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAuthed) "Admin ${currentUser.displayName}" else "Learner Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (isAuthed) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "VERIFIED ADMIN",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = currentUser.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentUser.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            OutlinedButton(
                onClick = onSwitchUser,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Switch", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun TreasuryFundCard(
    heldFundCents: Long,
    grossRevenueCents: Long,
    totalNetHeldCents: Long,
    totalDisbursedCents: Long,
    selectedDestination: PayoutDestinationEntity?,
    isAuthed: Boolean,
    onWithdrawClick: () -> Unit,
    onRecordLivePayment: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("treasury_fund_hero_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HELD PAYOUT TREASURY FUND",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Funds Accumulating",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Amount
                val heldDollars = String.format(Locale.US, "$%,.2f", heldFundCents / 100.0)
                Text(
                    text = heldDollars,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif
                )

                Text(
                    text = "Held in escrow for Corey & Sarah from all purchases",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Key metrics row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.18f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricColumn(
                        label = "Gross Purchases",
                        value = String.format(Locale.US, "$%,.2f", grossRevenueCents / 100.0)
                    )
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    MetricColumn(
                        label = "Total Disbursed",
                        value = String.format(Locale.US, "$%,.2f", totalDisbursedCents / 100.0)
                    )
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    MetricColumn(
                        label = "Active Routing",
                        value = selectedDestination?.name?.take(14) ?: "Not Selected"
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onWithdrawClick,
                        enabled = isAuthed && heldFundCents > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("disburse_funds_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send Payout",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onRecordLivePayment,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCard,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Record Payment",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DestinationItemCard(
    destination: PayoutDestinationEntity,
    isAuthed: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val isSelected = destination.isSelected
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isAuthed) { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { if (isAuthed) onSelect() },
                enabled = isAuthed
            )

            Spacer(modifier = Modifier.width(8.dp))

            val icon = when (destination.destinationType) {
                "STRIPE_CONNECT" -> Icons.Default.CloudSync
                "PAYPAL" -> Icons.Default.Payment
                "WIRE" -> Icons.Default.Public
                else -> Icons.Default.AccountBalance
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = destination.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ACTIVE PAYOUT",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = destination.accountHolder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${destination.identifierMasked} • Routing: ${destination.routingOrCode}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            if (isAuthed && !isSelected) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete destination",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PurchaseItemRow(purchase: PurchaseEntity) {
    val dateStr = remember(purchase.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()).format(Date(purchase.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = purchase.itemName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Buyer: ${purchase.buyerName} (${purchase.paymentMethod})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$dateStr • ${purchase.transactionId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val netHeldDollars = String.format(Locale.US, "+$%.2f", purchase.netHeldCents / 100.0)
                Text(
                    text = netHeldDollars,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                val grossDollars = String.format(Locale.US, "Gross: $%.2f", purchase.amountCents / 100.0)
                Text(
                    text = grossDollars,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Held in Fund",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DisbursementItemCard(disbursement: PayoutDisbursementEntity) {
    val dateStr = remember(disbursement.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()).format(Date(disbursement.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = disbursement.referenceCode,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val amountStr = String.format(Locale.US, "$%,.2f", disbursement.amountCents / 100.0)
                Text(
                    text = amountStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Destination: ${disbursement.destinationName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = disbursement.destinationDetails,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Authorized by: ${disbursement.authorizedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (disbursement.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${disbursement.note}",
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- DIALOG IMPLEMENTATIONS ---

@Composable
private fun AdminUserLoginDialog(
    currentUser: UserRole,
    loginError: String?,
    onSelectRole: (UserRole, String) -> Unit,
    onQuickSwitch: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentUser) }
    var pinText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Admin & User Authentication",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select account for Corey, Sarah, or Learner to access the Payout Treasury",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Corey Role Option
                UserRoleSelectCard(
                    role = UserRole.COREY,
                    isSelected = selectedRole == UserRole.COREY,
                    onClick = { selectedRole = UserRole.COREY }
                )

                // Sarah Role Option
                UserRoleSelectCard(
                    role = UserRole.SARAH,
                    isSelected = selectedRole == UserRole.SARAH,
                    onClick = { selectedRole = UserRole.SARAH }
                )

                // Learner / Student Option
                UserRoleSelectCard(
                    role = UserRole.LEARNER,
                    isSelected = selectedRole == UserRole.LEARNER,
                    onClick = { selectedRole = UserRole.LEARNER }
                )

                if (selectedRole.isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { pinText = it },
                        label = { Text("Security PIN (Optional / Default: 2580)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                loginError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSelectRole(selectedRole, pinText)
                }
            ) {
                Text("Log In as ${selectedRole.displayName}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun UserRoleSelectCard(
    role: UserRole,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = role.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (role.isAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = role.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = role.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun AddDestinationDialog(
    adminName: String,
    onAdd: (name: String, type: String, holder: String, identifier: String, routing: String, setActive: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("BANK_ACH") }
    var holder by remember { mutableStateOf("Corey & Sarah Partners LLC") }
    var identifier by remember { mutableStateOf("") }
    var routing by remember { mutableStateOf("") }
    var setAsActive by remember { mutableStateOf(true) }

    val typeOptions = listOf(
        "BANK_ACH" to "Bank Account (ACH / Checking)",
        "STRIPE_CONNECT" to "Stripe Express Connect",
        "PAYPAL" to "PayPal Business Merchant",
        "WIRE" to "Direct International Wire"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Payout Destination", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Label (e.g. Chase Business Checking)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = holder,
                    onValueChange = { holder = it },
                    label = { Text("Account Holder / Business Entity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("Account Number / Email / Stripe ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = routing,
                    onValueChange = { routing = it },
                    label = { Text("Routing Number / SWIFT / Branch Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { setAsActive = !setAsActive }
                ) {
                    Checkbox(checked = setAsActive, onCheckedChange = { setAsActive = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set as active payout destination immediately", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && identifier.isNotBlank()) {
                        onAdd(name, type, holder, identifier, routing, setAsActive)
                    }
                },
                enabled = name.isNotBlank() && identifier.isNotBlank()
            ) {
                Text("Save Destination")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DisburseFundsDialog(
    heldFundCents: Long,
    selectedDest: PayoutDestinationEntity?,
    currentAdmin: String,
    onConfirm: (amountCents: Long, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf(String.format(Locale.US, "%.2f", heldFundCents / 100.0)) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Send Payout from Held Funds", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Destination: ${selectedDest?.name ?: "No destination selected"}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${selectedDest?.accountHolder} • ${selectedDest?.identifierMasked}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Available Fund: " + String.format(Locale.US, "$%,.2f", heldFundCents / 100.0),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payout Amount ($ USD)") },
                    singleLine = true,
                    prefix = { Text("$") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Internal Reference / Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Funds will be settled to the selected destination and recorded under Admin $currentAdmin.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull() ?: 0.0
                    val cents = (parsed * 100).toLong()
                    if (cents in 1..heldFundCents) {
                        onConfirm(cents, note)
                    }
                }
            ) {
                Text("Confirm & Disburse")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RecordLivePaymentDialog(
    onRecordPayment: (name: String, category: String, amountCents: Long, buyer: String, email: String) -> Unit,
    onDismiss: () -> Unit
) {
    val liveProducts = listOf(
        Triple("EasyBet Pro VIP Annual Syndicate", "SUBSCRIPTION", 14900L),
        Triple("EasyBet VIP Monthly Intelligence", "SUBSCRIPTION", 1999L),
        Triple("24-Hour Next-to-Jump Day Pass", "DAY_PASS", 499L),
        Triple("Private Algorithmic Consulting & Setup", "CONSULTING", 25000L),
        Triple("API Live Data Integration License", "LICENSING", 9900L)
    )

    var selectedProduct by remember { mutableStateOf(liveProducts.first()) }
    var customAmountText by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }
    var buyerEmail by remember { mutableStateOf("") }
    var transactionRef by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record Live Revenue Transaction", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Record genuine incoming customer receipts, live subscription settlements, or syndicate payouts into the Corey & Sarah Treasury fund.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Select Product / Service:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                liveProducts.forEach { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProduct = product }
                            .border(
                                width = if (selectedProduct == product) 2.dp else 1.dp,
                                color = if (selectedProduct == product) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedProduct == product) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.first,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = product.second,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "$%.2f", product.third / 100.0),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { customAmountText = it },
                    label = { Text("Custom Amount ($) [Optional override]") },
                    placeholder = { Text(String.format(Locale.US, "%.2f", selectedProduct.third / 100.0)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Client / Subscriber Name *") },
                    placeholder = { Text("e.g. Johnathan Miller") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = buyerEmail,
                    onValueChange = { buyerEmail = it },
                    label = { Text("Client Email Address") },
                    placeholder = { Text("e.g. j.miller@syndicate.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("Transaction / Stripe Reference ID") },
                    placeholder = { Text("e.g. ch_3P7qR... or INV-8821") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountCents = customAmountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: selectedProduct.third
                    val name = buyerName.ifBlank { "Verified Client" }
                    val email = buyerEmail.ifBlank { "${name.lowercase().replace(" ", ".")}@client.org" }
                    onRecordPayment(selectedProduct.first, selectedProduct.second, amountCents, name, email)
                }
            ) {
                Text("Record to Live Ledger")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
