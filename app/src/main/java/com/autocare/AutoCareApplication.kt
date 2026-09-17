package com.autocare

import android.app.Application
import com.autocare.data.AppDatabase
import com.autocare.data.repository.ExpenseRepository
import com.autocare.data.repository.FuelLoadRepository
import com.autocare.data.repository.MaintenanceRepository
import com.autocare.data.repository.ReminderRepository
import com.autocare.data.repository.VehicleRepository

class AutoCareApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val vehicleRepository by lazy { VehicleRepository(database.vehicleDao()) }
    val maintenanceRepository by lazy { MaintenanceRepository(database.maintenanceDao()) }
    val fuelLoadRepository by lazy { FuelLoadRepository(database.fuelLoadDao()) }
    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val reminderRepository by lazy { ReminderRepository(database.reminderDao()) }
    val importLogRepository by lazy { com.autocare.data.repository.ImportLogRepository(database.importLogDao()) }
}
