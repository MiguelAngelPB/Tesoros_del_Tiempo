package com.example.tesorosdeltiempo.BD.datos

import kotlinx.coroutines.flow.Flow

class RecuerdosRepository(
    private val dao: RecuerdosDao
) {

    fun observeRecuerdos(): Flow<List<RecuerdosEntity>> =
        dao.observeRecuerdos()

    suspend fun guardarRecuerdo(recuerdo: RecuerdosEntity) {
        dao.upsert(recuerdo)
    }

    suspend fun borrarRecuerdo(recuerdo: RecuerdosEntity) {
        dao.delete(recuerdo)
    }

    suspend fun borrarTodo() {
        dao.clearAll()
    }
}