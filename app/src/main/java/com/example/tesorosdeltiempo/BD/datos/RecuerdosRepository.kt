package com.example.tesorosdeltiempo.BD.datos

import kotlinx.coroutines.flow.Flow

class RecuerdosRepository(
    private val dao: RecuerdosDao
) {

    fun observeRecuerdos(): Flow<List<RecuerdosEntity>> =
        dao.observeRecuerdos()

    fun observeRecuerdosByTag(query: String): Flow<List<RecuerdosEntity>> =
        dao.observeRecuerdosByTag(query)

    suspend fun guardarRecuerdo(recuerdo: RecuerdosEntity) {
        dao.upsert(recuerdo)
    }

    suspend fun borrarRecuerdo(recuerdo: RecuerdosEntity) {
        dao.delete(recuerdo)
    }

    suspend fun borrarRecuerdoPorId(id: Long) {
        dao.deleteById(id)
    }

    suspend fun borrarTodo() {
        dao.clearAll()
    }
}