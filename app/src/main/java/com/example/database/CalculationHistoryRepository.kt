package com.example.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CalculationHistoryRepository(private val dao: CalculationHistoryDao) {

    val allHistory: Flow<List<CalculationHistoryEntity>> = dao.getAllHistory()

    fun getHistoryByMetal(metalType: String): Flow<List<CalculationHistoryEntity>> {
        return dao.getHistoryByMetal(metalType)
    }

    suspend fun insert(entity: CalculationHistoryEntity): Long = withContext(Dispatchers.IO) {
        dao.insertCalculation(entity)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAllHistory()
    }
}
