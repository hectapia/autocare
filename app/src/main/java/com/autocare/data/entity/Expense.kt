package com.autocare.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["uuid"], unique = true)]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: Int,
    val category: String,
    val amount: Double,
    val date: Long,
    val notes: String = "",
    val uuid: String = java.util.UUID.randomUUID().toString()
)
