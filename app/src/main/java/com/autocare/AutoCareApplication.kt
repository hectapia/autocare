package com.autocare

import android.app.Application
import android.content.Context
import com.autocare.data.AppDatabase
import com.autocare.data.entity.calculateSeverity
import com.autocare.data.repository.ExpenseRepository
import com.autocare.data.repository.FuelLoadRepository
import com.autocare.data.repository.MaintenanceRepository
import com.autocare.data.repository.ReminderRepository
import com.autocare.data.repository.VehicleRepository
import com.autocare.data.preferences.UserPreferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AutoCareApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val userPreferences by lazy { UserPreferences(this) }
    val vehicleRepository by lazy { VehicleRepository(database.vehicleDao()) }
    val maintenanceRepository by lazy { MaintenanceRepository(database.maintenanceDao()) }
    val fuelLoadRepository by lazy { FuelLoadRepository(database.fuelLoadDao()) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val reminderRepository by lazy { ReminderRepository(database.reminderDao()) }
    val importLogRepository by lazy { com.autocare.data.repository.ImportLogRepository(database.importLogDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        checkCriticalReminders()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Recordatorios Críticos"
            val descriptionText = "Notificaciones para recordatorios vencidos o críticos"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("CRITICAL_REMINDERS_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkCriticalReminders() {
        MainScope().launch {
            combine(
                vehicleRepository.allVehicles,
                reminderRepository.getAllActiveReminders()
            ) { vehicles, reminders ->
                vehicles to reminders
            }.collect { (vehicles, reminders) ->
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                var notificationId = 1001
                
                reminders.forEach { reminder ->
                    val vehicle = vehicles.find { it.id == reminder.vehicleId }
                    if (vehicle != null) {
                        val severity = reminder.calculateSeverity(vehicle.currentKm)
                        if (severity == com.autocare.data.entity.AlertSeverity.CRITICAL) {
                            val builder = androidx.core.app.NotificationCompat.Builder(this@AutoCareApplication, "CRITICAL_REMINDERS_CHANNEL")
                                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                                .setContentTitle("¡Recordatorio Crítico!")
                                .setContentText("${reminder.title} para ${vehicle.brand} ${vehicle.model} (${vehicle.plate})")
                                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)

                            try {
                                notificationManager.notify(notificationId++, builder.build())
                            } catch (e: Exception) {
                                // Ignorar errores de permisos o de envío
                            }
                        }
                    }
                }
            }
        }
    }
}
