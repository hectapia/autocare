package com.autocare.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenances",
    indices = [Index(value = ["uuid"], unique = true)]
)
data class Maintenance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: Int,
    val type: String,
    val date: Long,
    val km: Int,
    val cost: Double,
    val notes: String,
    val uuid: String = java.util.UUID.randomUUID().toString()
)
