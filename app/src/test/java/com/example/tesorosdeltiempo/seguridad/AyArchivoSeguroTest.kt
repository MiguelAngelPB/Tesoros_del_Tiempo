package com.example.tesorosdeltiempo.seguridad

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AyArchivoSeguroTest {

    @Test
    fun rutaEnc_detectaFicheroCifrado() {
        assertTrue(AyArchivoSeguro.esRutaArchivoCifrado("/data/foto_1.enc"))
        assertTrue(AyArchivoSeguro.esRutaArchivoCifrado("/data/video.ENC"))
    }

    @Test
    fun rutaEnc_rechazaOtrosFormatos() {
        assertFalse(AyArchivoSeguro.esRutaArchivoCifrado("/data/foto.jpg"))
        assertFalse(AyArchivoSeguro.esRutaArchivoCifrado(null))
        assertFalse(AyArchivoSeguro.esRutaArchivoCifrado(""))
        assertFalse(AyArchivoSeguro.esRutaArchivoCifrado("   "))
        assertFalse(AyArchivoSeguro.esRutaArchivoCifrado("/data/foto.enc.txt"))
    }

    @Test
    fun rutaEnc_acepta_sufijo_en_medio_de_ruta_larga() {
        assertTrue(AyArchivoSeguro.esRutaArchivoCifrado("/storage/emulated/0/Android/data/com.app/files/port_main_123.enc"))
    }
}