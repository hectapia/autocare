package com.autocare.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocare.data.repository.ExpenseRepository
import com.autocare.data.repository.FuelLoadRepository
import com.autocare.data.repository.MaintenanceRepository
import com.autocare.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class HistoryViewModel(
    private val fuelLoadRepository: FuelLoadRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val expenseRepository: ExpenseRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    fun getHistory(vehicleId: Int): Flow<HistoryUiState> {
        return combine(
            fuelLoadRepository.getFuelLoadsForVehicle(vehicleId),
            maintenanceRepository.getMaintenancesForVehicle(vehicleId),
            expenseRepository.getExpensesForVehicle(vehicleId),
            reminderRepository.getActiveReminders(vehicleId)
        ) { fuel, maint, exp, reminders ->
            val items = mutableListOf<HistoryItem>()
            items.addAll(fuel.map { HistoryItem.Fuel(it) })
            items.addAll(maint.map { m ->
                val matchingReminder = reminders.find { r -> 
                    !r.isCompleted && r.type == "MAINTENANCE" && r.title.equals(m.type, ignoreCase = true)
                }
                HistoryItem.Maint(m, matchingReminder?.id)
            })
            items.addAll(exp.map { HistoryItem.Exp(it) })
            HistoryUiState(items.sortedByDescending { it.date })
        }
    }
}

class HistoryViewModelFactory(
    private val fuelLoadRepository: FuelLoadRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val expenseRepository: ExpenseRepository,
    private val reminderRepository: ReminderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                fuelLoadRepository,
                maintenanceRepository,
                expenseRepository,
                reminderRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
