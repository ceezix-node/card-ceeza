package com.example.cardceeza.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cardceeza.data.local.entity.BankAccountEntity
import com.example.cardceeza.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Flow<Int>
}

@Dao
interface BankAccountDao {
    @Query("SELECT * FROM bank_accounts WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun getBankAccountsForUser(userId: String): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM bank_accounts WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultBankAccount(userId: String): BankAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccount(account: BankAccountEntity)

    @Query("UPDATE bank_accounts SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultFlags(userId: String)

    @Query("DELETE FROM bank_accounts WHERE id = :id")
    suspend fun deleteBankAccount(id: String)
}
