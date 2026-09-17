package com.autocare.data.dao

import androidx.room.*
import com.autocare.data.entity.ImportLog

@Dao
interface ImportLogDao {
    @Query("SELECT uuid FROM import_logs")
    suspend fun getAllImportedUuids(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImportLog(importLog: ImportLog)
}
