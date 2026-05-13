package com.example.nammasantheledger

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // --- Existing Transaction Logic ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomerSummaries(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE name = :name")
    suspend fun getCustomerByName(name: String): Customer?

    @Upsert
    suspend fun upsertCustomer(customer: Customer)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    // --- Auth (User) ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert
    suspend fun registerUser(user: User)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
