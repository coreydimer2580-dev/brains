package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks every purchase made in the app that feeds into the payout fund.
 */
@Entity(tableName = "app_purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: String,
    val itemName: String,
    val itemCategory: String, // "MASTERCLASS", "SUBSCRIPTION", "CREDITS", "ASSESSMENT"
    val buyerName: String,
    val buyerEmail: String,
    val amountCents: Long, // e.g. 14999 = $149.99
    val platformFeeCents: Long, // App store fee (e.g. 15%)
    val netHeldCents: Long, // Amount held in payout fund
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Google Play Billing",
    val status: String = "HELD_IN_FUND" // "HELD_IN_FUND", "DISBURSED"
)

/**
 * Payout destinations configured by Admin Corey and Sarah.
 */
@Entity(tableName = "payout_destinations")
data class PayoutDestinationEntity(
    @PrimaryKey val id: String,
    val name: String, // e.g. "Chase Business Checking"
    val destinationType: String, // "BANK_ACH", "STRIPE_CONNECT", "PAYPAL", "WIRE"
    val accountHolder: String, // "Corey & Sarah Partners LLC"
    val identifierMasked: String, // "JPMorgan Chase •••• 4821"
    val routingOrCode: String, // "021000021"
    val isSelected: Boolean = false,
    val addedByAdmin: String = "Admin Corey & Sarah",
    val dateAdded: Long = System.currentTimeMillis()
)

/**
 * Historical record of funds disbursed/transferred to Corey and Sarah's destination.
 */
@Entity(tableName = "payout_disbursements")
data class PayoutDisbursementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referenceCode: String,
    val amountCents: Long,
    val destinationId: String,
    val destinationName: String,
    val destinationDetails: String,
    val authorizedBy: String, // "Admin Corey", "Admin Sarah", "Corey & Sarah Joint"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED", // "COMPLETED", "PROCESSING"
    val note: String = ""
)
