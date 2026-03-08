package com.example.tesorosdeltiempo.BD.datos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recuerdos")
data class RecuerdosEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val filePath: String,          // ruta del archivo (foto / vídeo / audio)
    val type: String,              // "FOTO", "VIDEO", "AUDIO"
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)