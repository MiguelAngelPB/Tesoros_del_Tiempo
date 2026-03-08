package com.example.tesorosdeltiempo.BD.datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecuerdosDao {

    @Query("SELECT * FROM recuerdos ORDER BY createdAt DESC")
    fun observeRecuerdos(): Flow<List<RecuerdosEntity>>

    @Query("SELECT * FROM recuerdos WHERE id = :id")
    fun observeRecuerdo(id: Long): Flow<RecuerdosEntity?>

    @Upsert
    suspend fun upsert(recuerdo: RecuerdosEntity)

    @Delete
    suspend fun delete(recuerdo: RecuerdosEntity)

    @Query("DELETE FROM recuerdos")
    suspend fun clearAll()
}