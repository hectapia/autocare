package com.autocare.data.repository

import com.autocare.data.dao.ExpenseDao
import com.autocare.data.entity.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>> {
        return expenseDao.getExpensesForVehicle(vehicleId)
    }

    suspend fun getExpenseById(id: Int): Expense? {
        return expenseDao.getExpenseById(id)
    }

    suspend fun insert(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun update(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
}
