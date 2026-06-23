package com.example.tesorosdeltiempo.tutorial

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialPreferenciasTest {

    @Test
    fun marca_como_visto_y_lo_recuerda() {
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        val prefs = mockk<SharedPreferences>()
        val context = mockk<Context>()

        every { context.getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.getBoolean("tutorial_visto", false) } returnsMany listOf(false, true)
        every { prefs.edit() } returns editor
        every { editor.putBoolean("tutorial_visto", true) } returns editor

        assertFalse(TutorialPreferencias.yaSeMostro(context))
        TutorialPreferencias.marcarComoVisto(context)
        assertTrue(TutorialPreferencias.yaSeMostro(context))

        verify { editor.apply() }
    }
}