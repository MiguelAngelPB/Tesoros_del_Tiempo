package com.example.tesorosdeltiempo.BD.datos

import androidx.room.Entity
import androidx.room.PrimaryKey

// Fila de la tabla Room, un recuerdo multimedia y metadatos con opción de estar en papelera
@Entity(tableName = "recuerdos")
data class RecuerdosEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String? = null,
    val filePath: String = "",
    val type: String,
    val tags: String = "",
    val descriptionType: String? = null, // "TEXTO", "FOTO", "VIDEO", "AUDIO"
    val descriptionContent: String? = null, // texto libre
    val descriptionPath: String? = null, // ruta archivo descripción multimedia
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val enPapelera: Boolean = false
)