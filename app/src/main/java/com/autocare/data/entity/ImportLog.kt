package com.autocare.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "import_logs")
data class ImportLog(
    @PrimaryKey
    val uuid: String,
    val vehiclePlate: String,
    val recordType: String,
    val timestamp: Long
)
