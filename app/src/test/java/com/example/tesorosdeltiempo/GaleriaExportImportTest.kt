package com.example.tesorosdeltiempo

import org.junit.Assert.assertTrue
import org.junit.Test

class GaleriaExportImportTest {

    @Test
    fun nombre_copia_sugerido_tiene_formato_zip() {
        val nombre = GaleriaExportImport.nombreCopiaSugerido()
        assertTrue(nombre.startsWith("tesoros_portable_"))
        assertTrue(nombre.endsWith(".zip"))
        assertTrue(nombre.matches(Regex("tesoros_portable_\\d{8}_\\d{4}\\.zip")))
    }
}