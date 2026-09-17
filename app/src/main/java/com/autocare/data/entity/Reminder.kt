package com.autocare.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [Index(value = ["uuid"], unique = true)]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val vehicleId: Int,
    val title: String,
    val description: String,
    val type: String, // MAINTENANCE, DOCUMENT, OTHER
    val targetKm: Int? = null,
    val targetDate: Long? = null,
    val isCompleted: Boolean = false,
    val uuid: String = java.util.UUID.randomUUID().toString()
)

enum class AlertSeverity { NONE, WARNING, CRITICAL }

fun Reminder.calculateSeverity(currentKm: Int): AlertSeverity {
    val kmSeverity = targetKm?.let {
        val diff = it - currentKm
        when {
            diff <= 0 -> AlertSeverity.CRITICAL
            diff <= 500 -> AlertSeverity.WARNING
            else -> AlertSeverity.NONE
        }
    } ?: AlertSeverity.NONE

    val timeSeverity = targetDate?.let {
        val diff = it - System.currentTimeMillis()
        val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
        when {
            diff <= 0 -> AlertSeverity.CRITICAL
            diff <= sevenDaysMs -> AlertSeverity.WARNING
            else -> AlertSeverity.NONE
        }
    } ?: AlertSeverity.NONE

    return maxOf(kmSeverity, timeSeverity)
}
