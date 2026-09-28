package com.autocare.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocare.data.entity.Reminder
import com.autocare.data.repository.ExpenseRepository
import com.autocare.data.repository.FuelLoadRepository
import com.autocare.data.repository.MaintenanceRepository
import com.autocare.data.repository.ReminderRepository
import com.autocare.data.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class FullHistoryState(
    val vehicle: com.autocare.data.entity.Vehicle? = null,
    val fuelLoads: List<com.autocare.data.entity.FuelLoad> = emptyList(),
    val maintenances: List<com.autocare.data.entity.Maintenance> = emptyList(),
    val expenses: List<com.autocare.data.entity.Expense> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val historyItems: List<HistoryItem> = emptyList()
)

class HistoryViewModel(
    private val fuelLoadRepository: FuelLoadRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val expenseRepository: ExpenseRepository,
    private val reminderRepository: ReminderRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    fun getFullHistory(vehicleId: Int): Flow<FullHistoryState> {
        return combine(
            fuelLoadRepository.getFuelLoadsForVehicle(vehicleId),
            maintenanceRepository.getMaintenancesForVehicle(vehicleId),
            expenseRepository.getExpensesForVehicle(vehicleId),
            reminderRepository.getActiveReminders(vehicleId),
            combine(
                fuelLoadRepository.getFuelLoadsForVehicle(vehicleId),
                maintenanceRepository.getMaintenancesForVehicle(vehicleId)
            ) { _, _ ->  // Just to trigger combine when vehicle repository is separate
                vehicleRepository.getVehicleById(vehicleId)
            }
        ) { fuel, maint, exp, reminders, vehicle ->
            val items = mutableListOf<HistoryItem>()
            items.addAll(fuel.map { HistoryItem.Fuel(it) })
            items.addAll(maint.map { m ->
                val matchingReminder = reminders.find { r -> 
                    !r.isCompleted && r.type == "MAINTENANCE" && r.title.equals(m.type, ignoreCase = true)
                }
                HistoryItem.Maint(m, matchingReminder?.id)
            })
            items.addAll(exp.map { HistoryItem.Exp(it) })
            
            FullHistoryState(
                vehicle = vehicle,
                fuelLoads = fuel,
                maintenances = maint,
                expenses = exp,
                reminders = reminders,
                historyItems = items.sortedByDescending { it.date }
            )
        }
    }

    fun getHistory(vehicleId: Int): Flow<HistoryUiState> {
        return getFullHistory(vehicleId).map { HistoryUiState(it.historyItems) }
    }
}

class HistoryViewModelFactory(
    private val fuelLoadRepository: FuelLoadRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val expenseRepository: ExpenseRepository,
    private val reminderRepository: ReminderRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(
                fuelLoadRepository,
                maintenanceRepository,
                expenseRepository,
                reminderRepository,
                vehicleRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
