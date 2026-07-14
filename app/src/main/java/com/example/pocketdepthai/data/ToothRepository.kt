package com.example.pocketdepthai.data

import kotlinx.coroutines.flow.Flow

class ToothRepository(private val toothDao: ToothDao) {
    val allToothData: Flow<List<ToothData>> = toothDao.getAllToothData()

    suspend fun insert(toothData: ToothData) {
        toothDao.insertToothData(toothData)
    }

    suspend fun deleteAll() {
        toothDao.deleteAll()
    }
}
