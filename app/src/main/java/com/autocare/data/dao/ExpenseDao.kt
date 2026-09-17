package com.autocare.data.dao

import androidx.room.*
import com.autocare.data.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE uuid = :uuid LIMIT 1")
    suspend fun getExpenseByUuid(uuid: String): Expense?

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
