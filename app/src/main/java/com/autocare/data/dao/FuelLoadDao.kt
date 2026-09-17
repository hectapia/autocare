package com.autocare.data.dao

import androidx.room.*
import com.autocare.data.entity.FuelLoad
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLoadDao {
    @Query("SELECT * FROM fuel_loads WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getFuelLoadsForVehicle(vehicleId: Int): Flow<List<FuelLoad>>

    @Query("SELECT * FROM fuel_loads")
    suspend fun getAllFuelLoads(): List<FuelLoad>

    @Query("SELECT * FROM fuel_loads WHERE uuid = :uuid LIMIT 1")
    suspend fun getFuelLoadByUuid(uuid: String): FuelLoad?

    @Query("SELECT * FROM fuel_loads WHERE id = :id")
    suspend fun getFuelLoadById(id: Int): FuelLoad?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLoad(fuelLoad: FuelLoad): Long

    @Update
    suspend fun updateFuelLoad(fuelLoad: FuelLoad)

    @Delete
    suspend fun deleteFuelLoad(fuelLoad: FuelLoad)
}
