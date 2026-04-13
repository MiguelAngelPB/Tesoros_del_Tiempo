package com.example.tesorosdeltiempo.BD.datos

import kotlinx.coroutines.flow.Flow
import java.io.File

// Acceso al Dao
class RecuerdosRepository(
    private val dao: RecuerdosDao
) {

    fun observeRecuerdos(): Flow<List<RecuerdosEntity>> =
        dao.observeRecuerdos()

    fun observeRecuerdosByTag(query: String): Flow<List<RecuerdosEntity>> =
        dao.observeRecuerdosByTag(query)

    fun observePapelera(): Flow<List<RecuerdosEntity>> =
        dao.observePapelera()

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

    suspend fun moverRecuerdoAPapeleraPorId(id: Long) {
        dao.moverAPapeleraPorId(id)
    }

    suspend fun restaurarDesdePapeleraPorId(id: Long) {
        dao.restaurarDesdePapeleraPorId(id)
    }

    suspend fun restaurarTodoDesdePapelera() {
        dao.restaurarTodoDesdePapelera()
    }

    suspend fun eliminarDefinitivoDesdePapeleraPorId(id: Long) {
        val recuerdo = dao.obtenerPorId(id) ?: return
        if (!recuerdo.enPapelera) return
        eliminarArchivosEnDisco(recuerdo)
        dao.eliminarDefinitivoPorId(id)
    }

    suspend fun vaciarPapeleraDefinitivo() {
        val enPapelera = dao.listarEnPapelera()
        enPapelera.forEach { eliminarArchivosEnDisco(it) }
        dao.eliminarTodoEnPapeleraDefinitivo()
    }

    // Rutas principal y descripción guardadas
    private fun eliminarArchivosEnDisco(recuerdo: RecuerdosEntity) {
        try {
            if (recuerdo.filePath.isNotBlank()) File(recuerdo.filePath).delete()
        } catch (_: Exception) { }
        try {
            val desc = recuerdo.descriptionPath
            if (!desc.isNullOrBlank()) File(desc).delete()
        } catch (_: Exception) { }
    }
}