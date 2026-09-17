package com.autocare.ui.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autocare.data.entity.Vehicle
import com.autocare.data.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VehicleViewModel(private val repository: VehicleRepository) : ViewModel() {

    val allVehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insert(brand: String, model: String, year: String, plate: String, km: String, imageUri: String?, type: String) {
        viewModelScope.launch {
            val vehicle = Vehicle(
                brand = brand,
                model = model,
                year = year.toIntOrNull() ?: 0,
                plate = plate,
                currentKm = km.toIntOrNull() ?: 0,
                type = type,
                imageUri = imageUri
            )
            repository.insert(vehicle)
        }
    }

    fun update(id: Int, brand: String, model: String, year: String, plate: String, km: String, imageUri: String?, type: String) {
        viewModelScope.launch {
            val vehicle = Vehicle(
                id = id,
                brand = brand,
                model = model,
                year = year.toIntOrNull() ?: 0,
                plate = plate,
                currentKm = km.toIntOrNull() ?: 0,
                type = type,
                imageUri = imageUri
            )
            repository.update(vehicle)
        }
    }

    fun delete(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.delete(vehicle)
        }
    }

    suspend fun getVehicleById(id: Int): Vehicle? {
        return repository.getVehicleById(id)
    }
}

class VehicleViewModelFactory(private val repository: VehicleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VehicleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
