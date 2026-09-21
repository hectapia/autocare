package com.autocare.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autocare.data.entity.Vehicle
import com.autocare.data.repository.ExpenseRepository
import com.autocare.data.repository.FuelLoadRepository
import com.autocare.data.repository.MaintenanceRepository
import com.autocare.data.repository.ReminderRepository
import com.autocare.data.repository.VehicleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class DashboardUiState(
    val vehicle: Vehicle? = null,
    val allVehicles: List<Vehicle> = emptyList(),
    val monthlyExpense: Double = 0.0,
    val nextOilChangeKm: Int? = null,
    val fuelPerformance: Double = 0.0,
    val reminders: List<com.autocare.data.entity.Reminder> = emptyList(),
    val otherVehiclesWithReminders: List<Vehicle> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val fuelLoadRepository: FuelLoadRepository,
    private val expenseRepository: ExpenseRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _selectedVehicleId = MutableStateFlow<Int?>(null)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun selectVehicle(vehicleId: Int) {
        _selectedVehicleId.value = vehicleId
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                vehicleRepository.allVehicles,
                _selectedVehicleId
            ) { vehicles, selectedId ->
                vehicles to selectedId
            }.flatMapLatest { (vehicles, selectedId) ->
                if (vehicles.isEmpty()) {
                    flowOf(DashboardUiState(vehicle = null, allVehicles = emptyList(), isLoading = false))
                } else {
                    val currentVehicle = vehicles.find { it.id == selectedId } ?: vehicles.first()
                    combine(
                        fuelLoadRepository.getFuelLoadsForVehicle(currentVehicle.id),
                        expenseRepository.getExpensesForVehicle(currentVehicle.id),
                        maintenanceRepository.getMaintenancesForVehicle(currentVehicle.id),
                        reminderRepository.getActiveReminders(currentVehicle.id),
                        reminderRepository.getAllActiveReminders()
                    ) { fuel, exp, maint, reminders, allActiveReminders ->
                        val otherVehiclesWithReminders = vehicles.filter { v ->
                            v.id != currentVehicle.id && allActiveReminders.any { r -> r.vehicleId == v.id }
                        }
                        calculateUiState(currentVehicle, vehicles, fuel, exp, maint, reminders, otherVehiclesWithReminders)
                    }
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun calculateUiState(
        vehicle: Vehicle,
        allVehicles: List<Vehicle>,
        fuelLoads: List<com.autocare.data.entity.FuelLoad>,
        expenses: List<com.autocare.data.entity.Expense>,
        maintenances: List<com.autocare.data.entity.Maintenance>,
        reminders: List<com.autocare.data.entity.Reminder>,
        otherVehiclesWithReminders: List<Vehicle>
    ): DashboardUiState {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = calendar.timeInMillis

        val monthlyFuel = fuelLoads.filter { it.date >= startOfMonth }.sumOf { it.totalCost }
        val monthlyOther = expenses.filter { it.date >= startOfMonth }.sumOf { it.amount }
        val monthlyMaint = maintenances.filter { it.date >= startOfMonth }.sumOf { it.cost }

        // Cálculo de Rendimiento (Simplificado: km totales / litros totales)
        val totalLiters = fuelLoads.sumOf { it.liters }
        val totalKm = if (fuelLoads.size > 1) {
            fuelLoads.maxOf { it.odometer } - fuelLoads.minOf { it.odometer }
        } else 0
        val avgPerformance = if (totalLiters > 0) totalKm / totalLiters else 0.0

        // Próximo servicio desde Reminders
        val nextService = reminders.firstOrNull { it.type == "MAINTENANCE" }

        return DashboardUiState(
            vehicle = vehicle,
            allVehicles = allVehicles,
            monthlyExpense = monthlyFuel + monthlyOther + monthlyMaint,
            nextOilChangeKm = nextService?.targetKm?.let { it - vehicle.currentKm },
            fuelPerformance = avgPerformance,
            reminders = reminders,
            otherVehiclesWithReminders = otherVehiclesWithReminders,
            isLoading = false
        )
    }
}

class DashboardViewModelFactory(
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val fuelLoadRepository: FuelLoadRepository,
    private val expenseRepository: ExpenseRepository,
    private val reminderRepository: ReminderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                vehicleRepository,
                maintenanceRepository,
                fuelLoadRepository,
                expenseRepository,
                reminderRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
