package com.example.tesorosdeltiempo.seguridad

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CuidadorAuthManagerValidacionTest {

    private lateinit var auth: CuidadorAuthManager

    @Before
    fun setUp() {
        auth = CuidadorAuthManager(mockk<Context>(relaxed = true))
    }

    @Test
    fun usuario_necesitaArroba() {
        assertTrue(auth.usuarioValido("mama@correo.com"))
        assertFalse(auth.usuarioValido("mamacorreo"))
        assertFalse(auth.usuarioValido(""))
    }

    @Test
    fun contrasena_pideMayusculaNumeroYSimbolo() {
        assertTrue(auth.contrasenaSegura("MiClave123!"))
        assertFalse(auth.contrasenaSegura("miclave123"))
        assertFalse(auth.contrasenaSegura("Corta1!"))
    }

    @Test
    fun fraseRecuperacion_entre8y20Caracteres() {
        assertTrue(auth.recuerdoBonitoValido("primer viaje"))
        assertFalse(auth.recuerdoBonitoValido("corta"))
        assertFalse(auth.recuerdoBonitoValido("esta frase es demasiado larga ya"))
    }
}