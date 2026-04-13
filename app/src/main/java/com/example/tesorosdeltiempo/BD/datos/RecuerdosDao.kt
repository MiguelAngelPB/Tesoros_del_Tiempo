package com.example.tesorosdeltiempo.BD.datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// Consultas y cambios en SQLite vía Room
@Dao
interface RecuerdosDao {

    @Query("SELECT * FROM recuerdos WHERE enPapelera = 0 ORDER BY createdAt DESC")
    fun observeRecuerdos(): Flow<List<RecuerdosEntity>>

    @Query(
        "SELECT * FROM recuerdos WHERE enPapelera = 0 AND tags LIKE '%' || :query || '%' ORDER BY createdAt DESC"
    )
    fun observeRecuerdosByTag(query: String): Flow<List<RecuerdosEntity>>

    @Query("SELECT * FROM recuerdos WHERE enPapelera = 1 ORDER BY createdAt DESC")
    fun observePapelera(): Flow<List<RecuerdosEntity>>

    @Query("SELECT * FROM recuerdos WHERE id = :id")
    fun observeRecuerdo(id: Long): Flow<RecuerdosEntity?>

    @Query("SELECT * FROM recuerdos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): RecuerdosEntity?

    @Query("SELECT * FROM recuerdos WHERE enPapelera = 1")
    suspend fun listarEnPapelera(): List<RecuerdosEntity>

    @Upsert
    suspend fun upsert(recuerdo: RecuerdosEntity)

    @Delete
    suspend fun delete(recuerdo: RecuerdosEntity)

    @Query("DELETE FROM recuerdos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recuerdos")
    suspend fun clearAll()

    @Query("UPDATE recuerdos SET enPapelera = 1 WHERE id = :id AND enPapelera = 0")
    suspend fun moverAPapeleraPorId(id: Long): Int

    @Query("UPDATE recuerdos SET enPapelera = 0 WHERE id = :id AND enPapelera = 1")
    suspend fun restaurarDesdePapeleraPorId(id: Long): Int

    @Query("UPDATE recuerdos SET enPapelera = 0 WHERE enPapelera = 1")
    suspend fun restaurarTodoDesdePapelera(): Int

    @Query("DELETE FROM recuerdos WHERE id = :id AND enPapelera = 1")
    suspend fun eliminarDefinitivoPorId(id: Long): Int

    @Query("DELETE FROM recuerdos WHERE enPapelera = 1")
    suspend fun eliminarTodoEnPapeleraDefinitivo(): Int
}