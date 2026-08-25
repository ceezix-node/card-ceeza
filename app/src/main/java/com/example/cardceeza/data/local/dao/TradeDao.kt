package com.example.cardceeza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.data.local.entity.TradeEventEntity
import com.example.cardceeza.model.TradeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY createdAt DESC")
    fun getAllTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTradesForUser(userId: String): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE id = :id LIMIT 1")
    suspend fun getTradeById(id: String): TradeEntity?

    @Query("SELECT * FROM trades WHERE id = :id LIMIT 1")
    fun getTradeFlowById(id: String): Flow<TradeEntity?>

    @Query("SELECT * FROM trades WHERE status IN ('SUBMITTED', 'UNDER_REVIEW', 'VERIFICATION_REQUIRED') ORDER BY createdAt ASC")
    fun getPendingVerificationQueue(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trades WHERE status = 'APPROVED' ORDER BY createdAt ASC")
    fun getPendingPayoutsQueue(): Flow<List<TradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity)

    @Update
    suspend fun updateTrade(trade: TradeEntity)

    @Query("SELECT COUNT(*) FROM trades")
    fun getTotalTradesCount(): Flow<Int>

    @Query("SELECT SUM(netPayoutNgn) FROM trades WHERE status = 'PAID'")
    fun getTotalPaidVolumeNgn(): Flow<Double?>
}

@Dao
interface TradeEventDao {
    @Query("SELECT * FROM trade_events WHERE tradeId = :tradeId ORDER BY timestamp ASC")
    fun getEventsForTrade(tradeId: String): Flow<List<TradeEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTradeEvent(event: TradeEventEntity)
}
