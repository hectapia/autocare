package com.autocare.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_loads",
    indices = [Index(value = ["uuid"], unique = true)]
)
data class FuelLoad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: Int,
    val liters: Double,
    val totalCost: Double,
    val odometer: Int,
    val date: Long,
    val uuid: String = java.util.UUID.randomUUID().toString()
)
