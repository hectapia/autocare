package com.autocare.data.dao

import androidx.room.*
import com.autocare.data.entity.Maintenance
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenances WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getMaintenancesForVehicle(vehicleId: Int): Flow<List<Maintenance>>

    @Query("SELECT * FROM maintenances")
    suspend fun getAllMaintenances(): List<Maintenance>

    @Query("SELECT * FROM maintenances WHERE uuid = :uuid LIMIT 1")
    suspend fun getMaintenanceByUuid(uuid: String): Maintenance?

    @Query("SELECT * FROM maintenances WHERE id = :id")
    suspend fun getMaintenanceById(id: Int): Maintenance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(maintenance: Maintenance): Long

    @Update
    suspend fun updateMaintenance(maintenance: Maintenance)

    @Delete
    suspend fun deleteMaintenance(maintenance: Maintenance)
}
