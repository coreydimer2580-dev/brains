package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class PayoutRepository(private val payoutDao: PayoutDao) {

    val allPurchases: Flow<List<PurchaseEntity>> = payoutDao.getAllPurchases()
    val totalGrossRevenueCents: Flow<Long> = payoutDao.getTotalGrossRevenueCents()
    val totalNetHeldCents: Flow<Long> = payoutDao.getTotalNetHeldCents()
    val allDestinations: Flow<List<PayoutDestinationEntity>> = payoutDao.getAllDestinations()
    val selectedDestination: Flow<PayoutDestinationEntity?> = payoutDao.getSelectedDestination()
    val allDisbursements: Flow<List<PayoutDisbursementEntity>> = payoutDao.getAllDisbursements()
    val totalDisbursedCents: Flow<Long> = payoutDao.getTotalDisbursedCents()

    suspend fun initializeDefaultsIfNeeded() {
        val existingDestinations = allDestinations.firstOrNull() ?: emptyList()
        if (existingDestinations.isEmpty()) {
            val defaultDestinations = listOf(
                PayoutDestinationEntity(
                    id = "dest_chase_main",
                    name = "JPMorgan Chase Business Vault",
                    destinationType = "BANK_ACH",
                    accountHolder = "Corey & Sarah Partners LLC",
                    identifierMasked = "Checking •••• 4821",
                    routingOrCode = "021000021",
                    isSelected = true,
                    addedByAdmin = "Admin Corey & Sarah"
                ),
                PayoutDestinationEntity(
                    id = "dest_wf_operating",
                    name = "Wells Fargo Joint Operating",
                    destinationType = "BANK_ACH",
                    accountHolder = "Corey & Sarah Holdings",
                    identifierMasked = "Operating •••• 9014",
                    routingOrCode = "121000241",
                    isSelected = false,
                    addedByAdmin = "Admin Sarah"
                ),
                PayoutDestinationEntity(
                    id = "dest_stripe_connect",
                    name = "Stripe Express Connect Hub",
                    destinationType = "STRIPE_CONNECT",
                    accountHolder = "Corey & Sarah Co-Treasury",
                    identifierMasked = "acct_cs_express_98a7f",
                    routingOrCode = "STRIPE-DIRECT",
                    isSelected = false,
                    addedByAdmin = "Admin Corey"
                ),
                PayoutDestinationEntity(
                    id = "dest_paypal_merchant",
                    name = "PayPal Business Merchant",
                    destinationType = "PAYPAL",
                    accountHolder = "Corey & Sarah Treasury",
                    identifierMasked = "payouts@coreysarah.org",
                    routingOrCode = "PAYPAL-MERCHANT",
                    isSelected = false,
                    addedByAdmin = "Admin Corey & Sarah"
                )
            )
            payoutDao.insertDestinations(defaultDestinations)
        }

        // Live production ledger starts clean. Only authentic recorded transactions accumulate.
    }

    private fun createPurchaseRecord(
        name: String,
        category: String,
        buyer: String,
        email: String,
        cents: Long,
        timestamp: Long
    ): PurchaseEntity {
        // Assume 15% platform processing fee, 85% holds in payout fund for Corey & Sarah
        val fee = (cents * 15) / 100
        val netHeld = cents - fee
        return PurchaseEntity(
            transactionId = "TXN-" + UUID.randomUUID().toString().take(8).uppercase(),
            itemName = name,
            itemCategory = category,
            buyerName = buyer,
            buyerEmail = email,
            amountCents = cents,
            platformFeeCents = fee,
            netHeldCents = netHeld,
            timestamp = timestamp,
            paymentMethod = "Google Play In-App Billing",
            status = "HELD_IN_FUND"
        )
    }

    suspend fun recordNewPurchase(
        itemName: String,
        itemCategory: String,
        buyerName: String,
        buyerEmail: String,
        amountCents: Long,
        paymentMethod: String = "Google Play In-App Billing"
    ): Long {
        val fee = (amountCents * 15) / 100
        val net = amountCents - fee
        val purchase = PurchaseEntity(
            transactionId = "TXN-" + UUID.randomUUID().toString().take(8).uppercase(),
            itemName = itemName,
            itemCategory = itemCategory,
            buyerName = buyerName,
            buyerEmail = buyerEmail,
            amountCents = amountCents,
            platformFeeCents = fee,
            netHeldCents = net,
            timestamp = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            status = "HELD_IN_FUND"
        )
        return payoutDao.insertPurchase(purchase)
    }

    suspend fun selectDestination(destinationId: String) {
        payoutDao.unselectAllDestinations()
        payoutDao.setSelectedDestination(destinationId)
    }

    suspend fun addDestination(
        name: String,
        destinationType: String,
        accountHolder: String,
        identifierMasked: String,
        routingOrCode: String,
        addedByAdmin: String,
        setAsActive: Boolean = true
    ) {
        val id = "dest_" + UUID.randomUUID().toString().take(8)
        if (setAsActive) {
            payoutDao.unselectAllDestinations()
        }
        val entity = PayoutDestinationEntity(
            id = id,
            name = name,
            destinationType = destinationType,
            accountHolder = accountHolder,
            identifierMasked = identifierMasked,
            routingOrCode = routingOrCode,
            isSelected = setAsActive,
            addedByAdmin = addedByAdmin,
            dateAdded = System.currentTimeMillis()
        )
        payoutDao.insertDestination(entity)
    }

    suspend fun deleteDestination(destinationId: String) {
        payoutDao.deleteDestination(destinationId)
    }

    suspend fun disburseFunds(
        amountCents: Long,
        destination: PayoutDestinationEntity,
        adminName: String,
        note: String
    ): Long {
        val refCode = "PAYOUT-" + UUID.randomUUID().toString().take(8).uppercase()
        val disbursement = PayoutDisbursementEntity(
            referenceCode = refCode,
            amountCents = amountCents,
            destinationId = destination.id,
            destinationName = destination.name,
            destinationDetails = "${destination.accountHolder} (${destination.identifierMasked})",
            authorizedBy = adminName,
            timestamp = System.currentTimeMillis(),
            status = "COMPLETED",
            note = note
        )
        return payoutDao.insertDisbursement(disbursement)
    }
}
