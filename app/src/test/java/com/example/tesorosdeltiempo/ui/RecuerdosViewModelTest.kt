package com.example.tesorosdeltiempo.ui

import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.BD.datos.RecuerdosRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecuerdosViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = mockk<RecuerdosRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repo.observeRecuerdos() } returns flowOf(emptyList())
        every { repo.observeRecuerdosByTag(any()) } returns flowOf(emptyList())
        every { repo.observePapelera() } returns flowOf(emptyList())
        coEvery { repo.eliminarPapeleraCaducada() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = RecuerdosViewModel(repo)

    @Test
    fun al_crear_purga_papelera_caducada() = runTest {
        viewModel()
        advanceUntilIdle()
        coVerify { repo.eliminarPapeleraCaducada() }
    }

    @Test
    fun operaciones_delegan_en_repositorio() = runTest {
        val r = RecuerdosEntity(title = "Viaje", type = "FOTO")
        coEvery { repo.guardarRecuerdo(r) } returns Unit
        coEvery { repo.moverRecuerdoAPapeleraPorId(1L) } returns Unit
        coEvery { repo.restaurarDesdePapeleraPorId(1L) } returns Unit
        coEvery { repo.eliminarDefinitivoDesdePapeleraPorId(1L) } returns Unit
        coEvery { repo.vaciarPapeleraDefinitivo() } returns Unit

        val vm = viewModel()
        vm.guardarRecuerdo(r)
        vm.moverRecuerdoAPapeleraPorId(1L)
        vm.restaurarDesdePapeleraPorId(1L)
        vm.eliminarDefinitivoDesdePapeleraPorId(1L)
        vm.vaciarPapeleraDefinitivo()
        advanceUntilIdle()

        coVerify { repo.guardarRecuerdo(r) }
        coVerify { repo.moverRecuerdoAPapeleraPorId(1L) }
        coVerify { repo.restaurarDesdePapeleraPorId(1L) }
        coVerify { repo.eliminarDefinitivoDesdePapeleraPorId(1L) }
        coVerify { repo.vaciarPapeleraDefinitivo() }
    }

    @Test
    fun filtrar_por_etiqueta_usa_busqueda() = runTest {
        every { repo.observeRecuerdosByTag("familia") } returns flowOf(emptyList())

        val vm = viewModel()
        val job = launch { vm.recuerdos.collect { } }
        advanceUntilIdle()

        vm.filtrarPorEtiqueta("familia")
        advanceUntilIdle()

        verify { repo.observeRecuerdosByTag("familia") }
        job.cancel()
    }
}