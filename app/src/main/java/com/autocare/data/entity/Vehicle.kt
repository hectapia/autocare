package com.autocare.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    indices = [
        Index(value = ["plate"], unique = true),
        Index(value = ["uuid"], unique = true)
    ]
)
data class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brand: String,
    val model: String,
    val year: Int,
    val plate: String,
    val currentKm: Int,
    val type: String = "Auto",
    val imageUri: String? = null,
    val uuid: String = java.util.UUID.randomUUID().toString()
)
