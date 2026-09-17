package com.autocare.data.dao

import androidx.room.*
import com.autocare.data.entity.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles")
    suspend fun getAllVehiclesList(): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE uuid = :uuid LIMIT 1")
    suspend fun getVehicleByUuid(uuid: String): Vehicle?

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Int): Vehicle?

    @Query("SELECT * FROM vehicles WHERE plate = :plate LIMIT 1")
    suspend fun getVehicleByPlate(plate: String): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)
}
