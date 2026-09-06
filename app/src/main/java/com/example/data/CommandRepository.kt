package com.example.data

import kotlinx.coroutines.flow.Flow

class CommandRepository(private val commandDao: CommandDao) {
    val allCommands: Flow<List<CommandEntity>> = commandDao.getAllCommands()

    suspend fun insert(command: CommandEntity) = commandDao.insertCommand(command)

    suspend fun deleteById(id: Int) = commandDao.deleteCommandById(id)
}
