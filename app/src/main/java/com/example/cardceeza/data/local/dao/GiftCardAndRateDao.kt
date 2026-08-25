package com.example.cardceeza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cardceeza.data.local.entity.GiftCardEntity
import com.example.cardceeza.data.local.entity.RateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftCardDao {
    @Query("SELECT * FROM gift_card_types WHERE active = 1 ORDER BY category ASC, name ASC")
    fun getActiveGiftCards(): Flow<List<GiftCardEntity>>

    @Query("SELECT * FROM gift_card_types ORDER BY name ASC")
    fun getAllGiftCards(): Flow<List<GiftCardEntity>>

    @Query("SELECT * FROM gift_card_types WHERE id = :id LIMIT 1")
    suspend fun getGiftCardById(id: String): GiftCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGiftCards(cards: List<GiftCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGiftCard(card: GiftCardEntity)

    @Update
    suspend fun updateGiftCard(card: GiftCardEntity)
}

@Dao
interface RateDao {
    @Query("SELECT * FROM gift_card_rates WHERE status = 'ACTIVE' ORDER BY cardName ASC")
    fun getActiveRates(): Flow<List<RateEntity>>

    @Query("SELECT * FROM gift_card_rates ORDER BY updatedAt DESC")
    fun getAllRates(): Flow<List<RateEntity>>

    @Query("SELECT * FROM gift_card_rates WHERE cardId = :cardId AND region = :region LIMIT 1")
    suspend fun getRateByCardAndRegion(cardId: String, region: String): RateEntity?

    @Query("SELECT * FROM gift_card_rates WHERE id = :id LIMIT 1")
    suspend fun getRateById(id: String): RateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<RateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: RateEntity)

    @Update
    suspend fun updateRate(rate: RateEntity)
}
