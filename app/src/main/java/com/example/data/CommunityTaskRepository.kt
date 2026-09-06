package com.example.data

import kotlinx.coroutines.flow.Flow

class CommunityTaskRepository(private val dao: CommunityTaskDao) {
    val allTasks: Flow<List<CommunityTaskEntity>> = dao.getAllTasks()

    suspend fun insert(task: CommunityTaskEntity) = dao.insertTask(task)

    suspend fun deleteById(id: Int) = dao.deleteTaskById(id)
}
