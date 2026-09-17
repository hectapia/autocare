package com.autocare.data.repository

import com.autocare.data.dao.ImportLogDao
import com.autocare.data.entity.ImportLog

class ImportLogRepository(private val importLogDao: ImportLogDao) {
    suspend fun getAllImportedUuids(): List<String> {
        return importLogDao.getAllImportedUuids()
    }

    suspend fun insertImportLog(importLog: ImportLog) {
        importLogDao.insertImportLog(importLog)
    }
}
