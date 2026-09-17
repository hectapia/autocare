package com.autocare.data.repository

import com.autocare.data.dao.FuelLoadDao
import com.autocare.data.entity.FuelLoad
import kotlinx.coroutines.flow.Flow

class FuelLoadRepository(private val fuelLoadDao: FuelLoadDao) {
    fun getFuelLoadsForVehicle(vehicleId: Int): Flow<List<FuelLoad>> {
        return fuelLoadDao.getFuelLoadsForVehicle(vehicleId)
    }

    suspend fun getFuelLoadById(id: Int): FuelLoad? {
        return fuelLoadDao.getFuelLoadById(id)
    }

    suspend fun insert(fuelLoad: FuelLoad) {
        fuelLoadDao.insertFuelLoad(fuelLoad)
    }

    suspend fun update(fuelLoad: FuelLoad) {
        fuelLoadDao.updateFuelLoad(fuelLoad)
    }

    suspend fun delete(fuelLoad: FuelLoad) {
        fuelLoadDao.deleteFuelLoad(fuelLoad)
    }
}
