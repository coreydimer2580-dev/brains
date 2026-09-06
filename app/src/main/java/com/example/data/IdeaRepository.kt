package com.example.data

import kotlinx.coroutines.flow.Flow

class IdeaRepository(private val ideaDao: IdeaDao) {
    val allIdeas: Flow<List<IdeaEntity>> = ideaDao.getAllIdeas()

    fun searchIdeas(query: String): Flow<List<IdeaEntity>> {
        return ideaDao.searchIdeas(query)
    }

    suspend fun saveIdea(idea: IdeaEntity) {
        ideaDao.insertIdea(idea)
    }
}
