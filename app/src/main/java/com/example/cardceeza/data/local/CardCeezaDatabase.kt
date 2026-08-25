package com.example.cardceeza.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.cardceeza.data.local.dao.AuditDao
import com.example.cardceeza.data.local.dao.BankAccountDao
import com.example.cardceeza.data.local.dao.GiftCardDao
import com.example.cardceeza.data.local.dao.LedgerDao
import com.example.cardceeza.data.local.dao.NotificationDao
import com.example.cardceeza.data.local.dao.RateDao
import com.example.cardceeza.data.local.dao.SupportDao
import com.example.cardceeza.data.local.dao.TradeDao
import com.example.cardceeza.data.local.dao.TradeEventDao
import com.example.cardceeza.data.local.dao.UserDao
import com.example.cardceeza.data.local.entity.AuditLogEntity
import com.example.cardceeza.data.local.entity.BankAccountEntity
import com.example.cardceeza.data.local.entity.GiftCardEntity
import com.example.cardceeza.data.local.entity.LedgerEntryEntity
import com.example.cardceeza.data.local.entity.NotificationEntity
import com.example.cardceeza.data.local.entity.RateEntity
import com.example.cardceeza.data.local.entity.SupportTicketEntity
import com.example.cardceeza.data.local.entity.TradeEntity
import com.example.cardceeza.data.local.entity.TradeEventEntity
import com.example.cardceeza.data.local.entity.UserEntity
import com.example.cardceeza.model.KycStatus
import com.example.cardceeza.model.LedgerType
import com.example.cardceeza.model.RiskLevel
import com.example.cardceeza.model.TicketCategory
import com.example.cardceeza.model.TicketPriority
import com.example.cardceeza.model.TicketStatus
import com.example.cardceeza.model.TradeStatus
import com.example.cardceeza.model.UserRole

class RoomConverters {
    @TypeConverter fun fromUserRole(value: UserRole): String = value.name
    @TypeConverter fun toUserRole(value: String): UserRole = try { UserRole.valueOf(value) } catch (e: Exception) { UserRole.USER }

    @TypeConverter fun fromKycStatus(value: KycStatus): String = value.name
    @TypeConverter fun toKycStatus(value: String): KycStatus = try { KycStatus.valueOf(value) } catch (e: Exception) { KycStatus.KYC_NOT_STARTED }

    @TypeConverter fun fromTradeStatus(value: TradeStatus): String = value.name
    @TypeConverter fun toTradeStatus(value: String): TradeStatus = try { TradeStatus.valueOf(value) } catch (e: Exception) { TradeStatus.SUBMITTED }

    @TypeConverter fun fromRiskLevel(value: RiskLevel): String = value.name
    @TypeConverter fun toRiskLevel(value: String): RiskLevel = try { RiskLevel.valueOf(value) } catch (e: Exception) { RiskLevel.LOW }

    @TypeConverter fun fromLedgerType(value: LedgerType): String = value.name
    @TypeConverter fun toLedgerType(value: String): LedgerType = try { LedgerType.valueOf(value) } catch (e: Exception) { LedgerType.TRADE_CREDIT }

    @TypeConverter fun fromTicketCategory(value: TicketCategory): String = value.name
    @TypeConverter fun toTicketCategory(value: String): TicketCategory = try { TicketCategory.valueOf(value) } catch (e: Exception) { TicketCategory.OTHER }

    @TypeConverter fun fromTicketPriority(value: TicketPriority): String = value.name
    @TypeConverter fun toTicketPriority(value: String): TicketPriority = try { TicketPriority.valueOf(value) } catch (e: Exception) { TicketPriority.MEDIUM }

    @TypeConverter fun fromTicketStatus(value: TicketStatus): String = value.name
    @TypeConverter fun toTicketStatus(value: String): TicketStatus = try { TicketStatus.valueOf(value) } catch (e: Exception) { TicketStatus.OPEN }
}

@Database(
    entities = [
        UserEntity::class,
        BankAccountEntity::class,
        GiftCardEntity::class,
        RateEntity::class,
        TradeEntity::class,
        TradeEventEntity::class,
        LedgerEntryEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class CardCeezaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bankAccountDao(): BankAccountDao
    abstract fun giftCardDao(): GiftCardDao
    abstract fun rateDao(): RateDao
    abstract fun tradeDao(): TradeDao
    abstract fun tradeEventDao(): TradeEventDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supportDao(): SupportDao
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: CardCeezaDatabase? = null

        fun getInstance(context: Context): CardCeezaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardCeezaDatabase::class.java,
                    "cardceeza_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
