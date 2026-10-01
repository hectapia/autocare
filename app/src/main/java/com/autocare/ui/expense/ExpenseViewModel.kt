package com.autocare.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autocare.data.entity.Expense
import com.autocare.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _expense = MutableStateFlow<Expense?>(null)
    val expense: StateFlow<Expense?> = _expense

    fun loadExpense(id: Int) {
        viewModelScope.launch {
            _expense.value = expenseRepository.getExpenseById(id)
        }
    }

    fun saveExpense(
        id: Int?,
        vehicleId: Int,
        category: String,
        amount: String,
        notes: String,
        date: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val existing = if (id != null && id != 0) expenseRepository.getExpenseById(id) else null
            val targetVehicleId = when {
                vehicleId != -1 -> vehicleId
                existing != null -> existing.vehicleId
                else -> -1
            }

            if (targetVehicleId == -1) return@launch

            val amountValue = amount.toDoubleOrNull() ?: 0.0

            val expense = Expense(
                id = id ?: 0,
                vehicleId = targetVehicleId,
                category = category,
                amount = amountValue,
                notes = notes,
                date = date,
                uuid = existing?.uuid ?: UUID.randomUUID().toString()
            )
            
            if (id == null || id == 0) {
                expenseRepository.insert(expense)
            } else {
                expenseRepository.update(expense)
            }
            onSuccess()
        }
    }

    fun deleteExpense(expense: Expense, onSuccess: () -> Unit) {
        viewModelScope.launch {
            expenseRepository.delete(expense)
            onSuccess()
        }
    }
}

class ExpenseViewModelFactory(
    private val expenseRepository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(expenseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
