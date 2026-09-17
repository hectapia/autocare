package com.autocare.ui.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autocare.data.entity.FuelLoad
import com.autocare.data.repository.FuelLoadRepository
import com.autocare.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class FuelViewModel(
    private val fuelLoadRepository: FuelLoadRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _fuelLoad = MutableStateFlow<FuelLoad?>(null)
    val fuelLoad: StateFlow<FuelLoad?> = _fuelLoad

    fun loadFuelLoad(id: Int) {
        viewModelScope.launch {
            _fuelLoad.value = fuelLoadRepository.getFuelLoadById(id)
        }
    }

    fun saveFuelLoad(
        id: Int?,
        vehicleId: Int,
        liters: String,
        totalCost: String,
        odometer: String,
        date: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val litersValue = liters.toDoubleOrNull() ?: 0.0
            val costValue = totalCost.toDoubleOrNull() ?: 0.0
            val odoValue = odometer.toIntOrNull() ?: 0

            val fuelLoad = FuelLoad(
                id = id ?: 0,
                vehicleId = vehicleId,
                liters = litersValue,
                totalCost = costValue,
                odometer = odoValue,
                date = date
            )
            
            if (id == null || id == 0) {
                fuelLoadRepository.insert(fuelLoad)
            } else {
                fuelLoadRepository.update(fuelLoad)
            }

            // Optionally update vehicle's current odometer
            val vehicle = vehicleRepository.getVehicleById(vehicleId)
            vehicle?.let {
                if (odoValue > it.currentKm) {
                    vehicleRepository.update(it.copy(currentKm = odoValue))
                }
            }
            
            onSuccess()
        }
    }

    fun deleteFuelLoad(fuelLoad: FuelLoad, onSuccess: () -> Unit) {
        viewModelScope.launch {
            fuelLoadRepository.delete(fuelLoad)
            onSuccess()
        }
    }
}

class FuelViewModelFactory(
    private val fuelLoadRepository: FuelLoadRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FuelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FuelViewModel(fuelLoadRepository, vehicleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
