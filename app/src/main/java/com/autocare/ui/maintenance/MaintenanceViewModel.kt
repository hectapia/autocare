package com.autocare.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autocare.data.entity.Maintenance
import com.autocare.data.entity.Reminder
import com.autocare.data.repository.MaintenanceRepository
import com.autocare.data.repository.ReminderRepository
import com.autocare.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MaintenanceViewModel(
    private val maintenanceRepository: MaintenanceRepository,
    private val vehicleRepository: VehicleRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _maintenance = MutableStateFlow<Maintenance?>(null)
    val maintenance: StateFlow<Maintenance?> = _maintenance

    fun loadMaintenance(id: Int) {
        viewModelScope.launch {
            _maintenance.value = maintenanceRepository.getMaintenanceById(id)
        }
    }

    fun saveMaintenance(
        id: Int?,
        vehicleId: Int,
        type: String,
        km: String,
        cost: String,
        notes: String,
        date: Long,
        applyReminder: Boolean,
        reminderKm: String?,
        reminderDate: Long?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Determine the correct vehicleId and preserve UUID if editing
                val existing = if (id != null && id != 0) maintenanceRepository.getMaintenanceById(id) else null
                val targetVehicleId = when {
                    vehicleId != -1 -> vehicleId
                    existing != null -> existing.vehicleId
                    else -> -1
                }

                if (targetVehicleId == -1) {
                    // Logic error: we don't have a vehicle to associate this with
                    return@launch
                }

                val kmValue = km.toIntOrNull() ?: 0
                val costValue = cost.toDoubleOrNull() ?: 0.0

                val maintenanceEntity = Maintenance(
                    id = id ?: 0,
                    vehicleId = targetVehicleId,
                    type = type,
                    date = date,
                    km = kmValue,
                    cost = costValue,
                    notes = notes,
                    uuid = existing?.uuid ?: java.util.UUID.randomUUID().toString()
                )
                
                if (id == null || id == 0) {
                    maintenanceRepository.insert(maintenanceEntity)
                } else {
                    maintenanceRepository.update(maintenanceEntity)
                }

                if (applyReminder) {
                    val reminder = Reminder(
                        vehicleId = targetVehicleId,
                        title = type,
                        description = "Próximo mantenimiento: $type",
                        type = "MAINTENANCE",
                        targetKm = reminderKm?.toIntOrNull(),
                        targetDate = reminderDate,
                        isCompleted = false
                    )
                    reminderRepository.insert(reminder)
                }

                // Update vehicle odometer if this maintenance is more recent
                val vehicle = vehicleRepository.getVehicleById(targetVehicleId)
                vehicle?.let {
                    if (kmValue > it.currentKm) {
                        vehicleRepository.update(it.copy(currentKm = kmValue))
                    }
                }
                
                onSuccess()
            } catch (e: Exception) {
                // Prevent app from freezing/crashing on DB errors
                e.printStackTrace()
            }
        }
    }

    fun deleteMaintenance(maintenance: Maintenance, onSuccess: () -> Unit) {
        viewModelScope.launch {
            maintenanceRepository.delete(maintenance)
            onSuccess()
        }
    }
}

class MaintenanceViewModelFactory(
    private val maintenanceRepository: MaintenanceRepository,
    private val vehicleRepository: VehicleRepository,
    private val reminderRepository: ReminderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaintenanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MaintenanceViewModel(maintenanceRepository, vehicleRepository, reminderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
