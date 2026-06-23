package com.example.tesorosdeltiempo.BD.datos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecuerdosEntityTest {

    @Test
    fun valores_por_defecto() {
        val r = RecuerdosEntity(title = "Viaje", type = "FOTO")

        assertEquals(0L, r.id)
        assertEquals("", r.filePath)
        assertEquals("", r.tags)
        assertNull(r.description)
        assertNull(r.descriptionPath)
        assertFalse(r.enPapelera)
        assertNull(r.papeleraAt)
    }

    @Test
    fun copy_mantiene_campos_modificados() {
        val original = RecuerdosEntity(title = "Boda", type = "VIDEO", tags = "familia")
        val copia = original.copy(enPapelera = true, tags = "viaje")

        assertEquals("Boda", copia.title)
        assertTrue(copia.enPapelera)
        assertEquals("viaje", copia.tags)
    }
}