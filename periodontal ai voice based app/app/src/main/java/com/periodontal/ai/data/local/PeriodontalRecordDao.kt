package com.periodontal.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.periodontal.ai.data.model.PeriodontalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodontalRecordDao {
    @Query("SELECT * FROM periodontal_records WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getRecordsForPatient(patientId: String): Flow<List<PeriodontalRecord>>

    @Query("SELECT * FROM periodontal_records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<PeriodontalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PeriodontalRecord)

    @Query("DELETE FROM periodontal_records WHERE id = :id")
    suspend fun deleteRecord(id: String)
}
