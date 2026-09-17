package com.autocare.data.repository

import com.autocare.data.dao.ReminderDao
import com.autocare.data.entity.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    fun getActiveReminders(vehicleId: Int): Flow<List<Reminder>> = 
        reminderDao.getActiveRemindersForVehicle(vehicleId)

    suspend fun insert(reminder: Reminder) = reminderDao.insert(reminder)
    suspend fun update(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun delete(reminder: Reminder) = reminderDao.delete(reminder)
}
