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
    fun usuario_rechaza_espacios() {
        assertFalse(auth.usuarioValido("correo @mail.com"))
        assertFalse(auth.usuarioValido("  "))
    }

    @Test
    fun contrasena_pideMayusculaNumeroYSimbolo() {
        assertTrue(auth.contrasenaSegura("MiClave123!"))
        assertFalse(auth.contrasenaSegura("miclave123"))
        assertFalse(auth.contrasenaSegura("Corta1!"))
    }

    @Test
    fun contrasena_rechaza_sin_mayuscula_numero_o_simbolo() {
        assertFalse(auth.contrasenaSegura("clave123!"))
        assertFalse(auth.contrasenaSegura("ClaveSegura!"))
        assertFalse(auth.contrasenaSegura("Clave1234"))
    }

    @Test
    fun contrasena_rechaza_espacios() {
        assertFalse(auth.contrasenaSegura("Clave 123!"))
    }

    @Test
    fun fraseRecuperacion_entre8y20Caracteres() {
        assertTrue(auth.recuerdoBonitoValido("primer viaje"))
        assertFalse(auth.recuerdoBonitoValido("corta"))
        assertFalse(auth.recuerdoBonitoValido("esta frase es demasiado larga ya"))
    }

    @Test
    fun frase_rechaza_vacia_y_con_salto_de_linea() {
        assertFalse(auth.recuerdoBonitoValido(""))
        assertFalse(auth.recuerdoBonitoValido("frase\nmala"))
    }

    @Test
    fun frase_acepta_limites_8_y_20() {
        assertTrue(auth.recuerdoBonitoValido("12345678"))
        assertTrue(auth.recuerdoBonitoValido("12345678901234567890"))
    }
}