package com.autocare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.autocare.data.dao.ExpenseDao
import com.autocare.data.dao.FuelLoadDao
import com.autocare.data.dao.MaintenanceDao
import com.autocare.data.dao.ReminderDao
import com.autocare.data.dao.VehicleDao
import com.autocare.data.dao.ImportLogDao
import com.autocare.data.entity.Expense
import com.autocare.data.entity.FuelLoad
import com.autocare.data.entity.ImportLog
import com.autocare.data.entity.Maintenance
import com.autocare.data.entity.Reminder
import com.autocare.data.entity.Vehicle

@Database(
    entities = [Vehicle::class, Maintenance::class, FuelLoad::class, Expense::class, Reminder::class, ImportLog::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun fuelLoadDao(): FuelLoadDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun reminderDao(): ReminderDao
    abstract fun importLogDao(): ImportLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                val tables = listOf("vehicles", "fuel_loads", "maintenances", "expenses", "reminders")
                
                for (table in tables) {
                    // Verificar si la columna 'uuid' ya existe para evitar el error 'duplicate column'
                    val cursor = db.query("PRAGMA table_info(`$table`)")
                    var columnExists = false
                    try {
                        val nameIndex = cursor.getColumnIndex("name")
                        while (cursor.moveToNext()) {
                            val columnName = if (nameIndex != -1) cursor.getString(nameIndex) else cursor.getString(1)
                            if (columnName.equals("uuid", ignoreCase = true)) {
                                columnExists = true
                                break
                            }
                        }
                    } finally {
                        cursor.close()
                    }

                    if (!columnExists) {
                        db.execSQL("ALTER TABLE `$table` ADD COLUMN `uuid` TEXT NOT NULL DEFAULT ''")
                    }

                    // Generar UUIDs únicos para registros existentes antes de crear el índice único
                    val cursorRows = db.query("SELECT rowid FROM `$table` WHERE `uuid` = ''")
                    try {
                        while (cursorRows.moveToNext()) {
                            val rowId = cursorRows.getLong(0)
                            val newUuid = java.util.UUID.randomUUID().toString()
                            db.execSQL("UPDATE `$table` SET `uuid` = '$newUuid' WHERE rowid = $rowId")
                        }
                    } finally {
                        cursorRows.close()
                    }
                    
                    // Asegurar que existan los índices
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_${table}_uuid` ON `$table`(`uuid`)")
                }
                
                // Asegurar que la placa sea única (específico de vehículos)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_vehicles_plate` ON `vehicles`(`plate`)")

                // Crear la tabla ImportLog si no existe (nueva en v6)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `import_logs` (
                        `uuid` TEXT NOT NULL PRIMARY KEY,
                        `vehiclePlate` TEXT NOT NULL,
                        `recordType` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autocare_database"
                )
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
