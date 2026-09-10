package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrainDatabase
import com.example.data.PayoutDestinationEntity
import com.example.data.PayoutDisbursementEntity
import com.example.data.PayoutRepository
import com.example.data.PurchaseEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PayoutTreasuryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PayoutRepository

    init {
        val db = BrainDatabase.getInstance(application)
        repository = PayoutRepository(db.payoutDao())
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    val allPurchases: StateFlow<List<PurchaseEntity>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGrossRevenueCents: StateFlow<Long> = repository.totalGrossRevenueCents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalNetHeldCents: StateFlow<Long> = repository.totalNetHeldCents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalDisbursedCents: StateFlow<Long> = repository.totalDisbursedCents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Held in Treasury = Net Revenue - Total Already Disbursed
    val availableFundCents: StateFlow<Long> = combine(totalNetHeldCents, totalDisbursedCents) { net, disbursed ->
        (net - disbursed).coerceAtLeast(0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val allDestinations: StateFlow<List<PayoutDestinationEntity>> = repository.allDestinations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDestination: StateFlow<PayoutDestinationEntity?> = repository.selectedDestination
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allDisbursements: StateFlow<List<PayoutDisbursementEntity>> = repository.allDisbursements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Message (e.g., "Payout of $500.00 successfully transferred to Chase Business Vault")
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun selectDestination(destinationId: String) {
        viewModelScope.launch {
            repository.selectDestination(destinationId)
            _statusMessage.value = "Payout destination updated successfully."
        }
    }

    fun addNewDestination(
        name: String,
        type: String,
        holder: String,
        identifier: String,
        routing: String,
        adminName: String,
        setAsActive: Boolean = true
    ) {
        viewModelScope.launch {
            repository.addDestination(
                name = name,
                destinationType = type,
                accountHolder = holder,
                identifierMasked = identifier,
                routingOrCode = routing,
                addedByAdmin = adminName,
                setAsActive = setAsActive
            )
            _statusMessage.value = "New payout destination '$name' added."
        }
    }

    fun deleteDestination(destinationId: String) {
        viewModelScope.launch {
            repository.deleteDestination(destinationId)
            _statusMessage.value = "Destination removed."
        }
    }

    fun requestDisbursement(amountCents: Long, adminName: String, note: String) {
        viewModelScope.launch {
            val dest = selectedDestination.value
            if (dest == null) {
                _statusMessage.value = "Error: Please select a payout destination first."
                return@launch
            }

            val available = availableFundCents.value
            if (amountCents <= 0 || amountCents > available) {
                _statusMessage.value = "Error: Invalid payout amount. Available held fund is $${available / 100.0}."
                return@launch
            }

            repository.disburseFunds(
                amountCents = amountCents,
                destination = dest,
                adminName = adminName,
                note = note.ifBlank { "Fund disbursement to ${dest.name}" }
            )

            val formatted = String.format("$%.2f", amountCents / 100.0)
            _statusMessage.value = "Success! $formatted disburse initiated to ${dest.name} by $adminName."
        }
    }

    fun recordLivePurchase(
        itemName: String,
        category: String,
        amountCents: Long,
        buyerName: String,
        buyerEmail: String? = null
    ) {
        viewModelScope.launch {
            val email = buyerEmail?.takeIf { it.isNotBlank() } ?: (buyerName.lowercase().replace(" ", ".") + "@client.org")
            repository.recordNewPurchase(
                itemName = itemName,
                itemCategory = category,
                buyerName = buyerName,
                buyerEmail = email,
                amountCents = amountCents
            )
            val addedNet = String.format("$%.2f", (amountCents * 85) / 10000.0)
            _statusMessage.value = "Payment recorded! $addedNet added to Corey & Sarah's Held Payout Fund."
        }
    }

    fun recordSimulatedPurchase(
        itemName: String,
        category: String,
        amountCents: Long,
        buyerName: String
    ) {
        recordLivePurchase(itemName, category, amountCents, buyerName)
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
