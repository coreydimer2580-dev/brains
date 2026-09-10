package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PayoutDao {
    // --- Purchases ---
    @Query("SELECT * FROM app_purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<PurchaseEntity>)

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM app_purchases")
    fun getTotalGrossRevenueCents(): Flow<Long>

    @Query("SELECT COALESCE(SUM(netHeldCents), 0) FROM app_purchases")
    fun getTotalNetHeldCents(): Flow<Long>

    // --- Destinations ---
    @Query("SELECT * FROM payout_destinations ORDER BY isSelected DESC, dateAdded ASC")
    fun getAllDestinations(): Flow<List<PayoutDestinationEntity>>

    @Query("SELECT * FROM payout_destinations WHERE isSelected = 1 LIMIT 1")
    fun getSelectedDestination(): Flow<PayoutDestinationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: PayoutDestinationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestinations(destinations: List<PayoutDestinationEntity>)

    @Query("UPDATE payout_destinations SET isSelected = 0")
    suspend fun unselectAllDestinations()

    @Query("UPDATE payout_destinations SET isSelected = 1 WHERE id = :destinationId")
    suspend fun setSelectedDestination(destinationId: String)

    @Query("DELETE FROM payout_destinations WHERE id = :destinationId")
    suspend fun deleteDestination(destinationId: String)

    // --- Disbursements ---
    @Query("SELECT * FROM payout_disbursements ORDER BY timestamp DESC")
    fun getAllDisbursements(): Flow<List<PayoutDisbursementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisbursement(disbursement: PayoutDisbursementEntity): Long

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM payout_disbursements WHERE status = 'COMPLETED'")
    fun getTotalDisbursedCents(): Flow<Long>
}
