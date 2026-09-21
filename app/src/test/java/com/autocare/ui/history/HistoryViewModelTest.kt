package com.autocare.ui.history

import com.autocare.data.entity.Expense
import com.autocare.data.entity.FuelLoad
import com.autocare.data.entity.Maintenance
import com.autocare.data.entity.Reminder
import com.autocare.data.repository.ExpenseRepository
import com.autocare.data.repository.FuelLoadRepository
import com.autocare.data.repository.MaintenanceRepository
import com.autocare.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class HistoryViewModelTest {

    private lateinit var fuelLoadRepository: FuelLoadRepository
    private lateinit var maintenanceRepository: MaintenanceRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var viewModel: HistoryViewModel

    private var mockFuelLoads = emptyList<FuelLoad>()
    private var mockMaintenances = emptyList<Maintenance>()
    private var mockExpenses = emptyList<Expense>()
    private var mockReminders = emptyList<Reminder>()

    @Before
    fun setUp() {
        val fuelLoadDao = object : com.autocare.data.dao.FuelLoadDao {
            override fun getFuelLoadsForVehicle(vehicleId: Int): Flow<List<FuelLoad>> = flowOf(mockFuelLoads)
            override suspend fun getAllFuelLoads(): List<FuelLoad> = emptyList()
            override suspend fun getFuelLoadByUuid(uuid: String): FuelLoad? = null
            override suspend fun getFuelLoadById(id: Int): FuelLoad? = null
            override suspend fun insertFuelLoad(fuelLoad: FuelLoad): Long = 0L
            override suspend fun updateFuelLoad(fuelLoad: FuelLoad) {}
            override suspend fun deleteFuelLoad(fuelLoad: FuelLoad) {}
        }

        val maintenanceDao = object : com.autocare.data.dao.MaintenanceDao {
            override fun getMaintenancesForVehicle(vehicleId: Int): Flow<List<Maintenance>> = flowOf(mockMaintenances)
            override suspend fun getAllMaintenances(): List<Maintenance> = emptyList()
            override suspend fun getMaintenanceByUuid(uuid: String): Maintenance? = null
            override suspend fun getMaintenanceById(id: Int): Maintenance? = null
            override suspend fun insertMaintenance(maintenance: Maintenance): Long = 0L
            override suspend fun updateMaintenance(maintenance: Maintenance) {}
            override suspend fun deleteMaintenance(maintenance: Maintenance) {}
        }

        val expenseDao = object : com.autocare.data.dao.ExpenseDao {
            override fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>> = flowOf(mockExpenses)
            override suspend fun getAllExpenses(): List<Expense> = emptyList()
            override suspend fun getExpenseByUuid(uuid: String): Expense? = null
            override suspend fun getExpenseById(id: Int): Expense? = null
            override suspend fun insertExpense(expense: Expense): Long = 0L
            override suspend fun updateExpense(expense: Expense) {}
            override suspend fun deleteExpense(expense: Expense) {}
        }

        val reminderDao = object : com.autocare.data.dao.ReminderDao {
            override fun getActiveRemindersForVehicle(vehicleId: Int): Flow<List<Reminder>> = flowOf(mockReminders)
            override fun getAllActiveReminders(): Flow<List<Reminder>> = flowOf(emptyList())
            override suspend fun getAllReminders(): List<Reminder> = emptyList()
            override suspend fun getReminderByUuid(uuid: String): Reminder? = null
            override suspend fun getReminderById(id: Int): Reminder? = null
            override suspend fun insert(reminder: Reminder): Long = 0L
            override suspend fun update(reminder: Reminder) {}
            override suspend fun delete(reminder: Reminder) {}
        }

        fuelLoadRepository = FuelLoadRepository(fuelLoadDao)
        maintenanceRepository = MaintenanceRepository(maintenanceDao)
        expenseRepository = ExpenseRepository(expenseDao)
        reminderRepository = ReminderRepository(reminderDao)
        
        viewModel = HistoryViewModel(
            fuelLoadRepository,
            maintenanceRepository,
            expenseRepository,
            reminderRepository
        )
    }

    @Test
    fun getHistory_matchesMaintenanceWithActiveReminder() = runTest {
        val vehicleId = 1
        
        mockFuelLoads = listOf(
            FuelLoad(id = 1, vehicleId = vehicleId, liters = 40.0, totalCost = 60.0, odometer = 10000, date = 1000L)
        )
        mockMaintenances = listOf(
            Maintenance(id = 2, vehicleId = vehicleId, type = "Cambio de Aceite", date = 2000L, km = 10000, cost = 80.0, notes = "Filtro incluido")
        )
        mockExpenses = listOf(
            Expense(id = 3, vehicleId = vehicleId, category = "Seguro", amount = 120.0, date = 3000L, notes = "")
        )
        mockReminders = listOf(
            Reminder(id = 10, vehicleId = vehicleId, title = "Cambio de Aceite", description = "Toca ya", type = "MAINTENANCE", isCompleted = false)
        )

        val uiState = viewModel.getHistory(vehicleId).first()
        
        assertEquals(3, uiState.items.size)
        
        val firstItem = uiState.items[0] as HistoryItem.Exp
        assertEquals(3, firstItem.data.id)

        val secondItem = uiState.items[1] as HistoryItem.Maint
        assertEquals(2, secondItem.data.id)
        assertEquals(10, secondItem.reminderId)

        val thirdItem = uiState.items[2] as HistoryItem.Fuel
        assertEquals(1, thirdItem.data.id)
    }

    @Test
    fun getHistory_doesNotMatchMaintenanceWithDifferentTitleOrType() = runTest {
        val vehicleId = 1
        
        mockFuelLoads = emptyList()
        mockMaintenances = listOf(
            Maintenance(id = 2, vehicleId = vehicleId, type = "Cambio de Aceite", date = 2000L, km = 10000, cost = 80.0, notes = "")
        )
        mockExpenses = emptyList()
        mockReminders = listOf(
            Reminder(id = 11, vehicleId = vehicleId, title = "Frenos", description = "Chequear", type = "MAINTENANCE", isCompleted = false),
            Reminder(id = 12, vehicleId = vehicleId, title = "Cambio de Aceite", description = "Papeles", type = "DOCUMENT", isCompleted = false)
        )

        val uiState = viewModel.getHistory(vehicleId).first()
        
        assertEquals(1, uiState.items.size)
        val item = uiState.items[0] as HistoryItem.Maint
        assertNull(item.reminderId)
    }
}
