package com.autocare.data.repository

import com.autocare.data.dao.MaintenanceDao
import com.autocare.data.entity.Maintenance
import kotlinx.coroutines.flow.Flow

class MaintenanceRepository(private val maintenanceDao: MaintenanceDao) {
    fun getMaintenancesForVehicle(vehicleId: Int): Flow<List<Maintenance>> {
        return maintenanceDao.getMaintenancesForVehicle(vehicleId)
    }

    suspend fun getMaintenanceById(id: Int): Maintenance? {
        return maintenanceDao.getMaintenanceById(id)
    }

    suspend fun insert(maintenance: Maintenance) {
        maintenanceDao.insertMaintenance(maintenance)
    }

    suspend fun update(maintenance: Maintenance) {
        maintenanceDao.updateMaintenance(maintenance)
    }

    suspend fun delete(maintenance: Maintenance) {
        maintenanceDao.deleteMaintenance(maintenance)
    }
}
