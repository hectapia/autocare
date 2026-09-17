package com.autocare.data.repository

import com.autocare.data.dao.VehicleDao
import com.autocare.data.entity.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    suspend fun insert(vehicle: Vehicle) {
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun update(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun delete(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle)
    }

    suspend fun getVehicleById(id: Int): Vehicle? {
        return vehicleDao.getVehicleById(id)
    }

    suspend fun getVehicleByPlate(plate: String): Vehicle? {
        return vehicleDao.getVehicleByPlate(plate)
    }
}
