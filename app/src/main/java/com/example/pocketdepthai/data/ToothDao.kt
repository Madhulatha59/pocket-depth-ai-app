package com.example.pocketdepthai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ToothDao {
    @Query("SELECT * FROM tooth_data ORDER BY timestamp DESC")
    fun getAllToothData(): Flow<List<ToothData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToothData(toothData: ToothData)

    @Query("DELETE FROM tooth_data")
    suspend fun deleteAll()
}
