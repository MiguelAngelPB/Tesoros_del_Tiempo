package com.example.tesorosdeltiempo.BD.datos

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

class RecuerdosRepositoryTest {

    private val dao = mockk<RecuerdosDao>(relaxed = true)
    private lateinit var repo: RecuerdosRepository

    @Before
    fun setUp() {
        repo = RecuerdosRepository(dao)
    }

    @Test
    fun guardar_y_borrar_recuerdo() = runTest {
        val r = recuerdo(id = 1L)
        coEvery { dao.upsert(r) } returns Unit
        coEvery { dao.delete(r) } returns Unit

        repo.guardarRecuerdo(r)
        repo.borrarRecuerdo(r)

        coVerify { dao.upsert(r) }
        coVerify { dao.delete(r) }
    }

    @Test
    fun papelera_mover_restaurar_y_borrar() = runTest {
        coEvery { dao.moverAPapeleraPorId(2L, any()) } returns 1
        coEvery { dao.restaurarDesdePapeleraPorId(2L) } returns 1

        repo.moverRecuerdoAPapeleraPorId(2L)
        repo.restaurarDesdePapeleraPorId(2L)

        coVerify { dao.moverAPapeleraPorId(2L, any()) }
        coVerify { dao.restaurarDesdePapeleraPorId(2L) }
    }

    @Test
    fun eliminar_definitivo_borra_ficheros() = runTest {
        val f1 = File.createTempFile("main", ".enc")
        val f2 = File.createTempFile("desc", ".enc")
        val r = recuerdo(id = 3L, path = f1.absolutePath, descPath = f2.absolutePath, enPapelera = true)

        coEvery { dao.obtenerPorId(3L) } returns r
        coEvery { dao.eliminarDefinitivoPorId(3L) } returns 1

        repo.eliminarDefinitivoDesdePapeleraPorId(3L)

        assertFalse(f1.exists())
        assertFalse(f2.exists())
        coVerify { dao.eliminarDefinitivoPorId(3L) }
    }

    @Test
    fun no_elimina_si_no_esta_en_papelera() = runTest {
        coEvery { dao.obtenerPorId(4L) } returns recuerdo(id = 4L, enPapelera = false)

        repo.eliminarDefinitivoDesdePapeleraPorId(4L)

        coVerify(exactly = 0) { dao.eliminarDefinitivoPorId(any()) }
    }

    @Test
    fun purga_recuerdos_de_hace_mas_de_seis_meses() = runTest {
        val fichero = File.createTempFile("viejo", ".enc")
        val antiguo = Instant.now().minus(220, ChronoUnit.DAYS).toEpochMilli()
        val r = recuerdo(id = 5L, path = fichero.absolutePath, enPapelera = true, papeleraAt = antiguo)

        coEvery { dao.listarEnPapelera() } returns listOf(r)
        coEvery { dao.eliminarDefinitivoPorId(5L) } returns 1

        repo.eliminarPapeleraCaducada()

        assertFalse(fichero.exists())
        coVerify { dao.eliminarDefinitivoPorId(5L) }
    }

    @Test
    fun no_purga_recuerdos_recientes() = runTest {
        val reciente = Instant.now().minus(20, ChronoUnit.DAYS).toEpochMilli()
        val r = recuerdo(id = 6L, enPapelera = true, papeleraAt = reciente)
        coEvery { dao.listarEnPapelera() } returns listOf(r)

        repo.eliminarPapeleraCaducada()

        coVerify(exactly = 0) { dao.eliminarDefinitivoPorId(any()) }
    }

    @Test
    fun vaciar_papelera_borra_todo() = runTest {
        val f = File.createTempFile("pap", ".enc")
        val r = recuerdo(id = 7L, path = f.absolutePath, enPapelera = true)
        coEvery { dao.listarEnPapelera() } returns listOf(r)
        coEvery { dao.eliminarTodoEnPapeleraDefinitivo() } returns 1

        repo.vaciarPapeleraDefinitivo()

        assertFalse(f.exists())
        coVerify { dao.eliminarTodoEnPapeleraDefinitivo() }
    }

    @Test
    fun borrar_por_id_y_toda_la_tabla() = runTest {
        coEvery { dao.deleteById(8L) } returns Unit
        coEvery { dao.clearAll() } returns Unit

        repo.borrarRecuerdoPorId(8L)
        repo.borrarTodo()

        coVerify { dao.deleteById(8L) }
        coVerify { dao.clearAll() }
    }

    @Test
    fun restaurar_toda_la_papelera() = runTest {
        coEvery { dao.restaurarTodoDesdePapelera() } returns 2

        repo.restaurarTodoDesdePapelera()

        coVerify { dao.restaurarTodoDesdePapelera() }
    }

    @Test
    fun eliminar_definitivo_si_id_no_existe() = runTest {
        coEvery { dao.obtenerPorId(99L) } returns null

        repo.eliminarDefinitivoDesdePapeleraPorId(99L)

        coVerify(exactly = 0) { dao.eliminarDefinitivoPorId(any()) }
    }

    @Test
    fun purga_usa_createdAt_si_no_hay_papeleraAt() = runTest {
        val fichero = File.createTempFile("ant", ".enc")
        val antiguo = Instant.now().minus(220, ChronoUnit.DAYS).toEpochMilli()
        val r = recuerdo(
            id = 10L,
            path = fichero.absolutePath,
            enPapelera = true,
            createdAt = antiguo
        )

        coEvery { dao.listarEnPapelera() } returns listOf(r)
        coEvery { dao.eliminarDefinitivoPorId(10L) } returns 1

        repo.eliminarPapeleraCaducada()

        assertFalse(fichero.exists())
        coVerify { dao.eliminarDefinitivoPorId(10L) }
    }

    @Test
    fun eliminar_definitivo_sin_ficheros_en_disco() = runTest {
        val r = recuerdo(id = 11L, enPapelera = true)
        coEvery { dao.obtenerPorId(11L) } returns r
        coEvery { dao.eliminarDefinitivoPorId(11L) } returns 1

        repo.eliminarDefinitivoDesdePapeleraPorId(11L)

        coVerify { dao.eliminarDefinitivoPorId(11L) }
    }

    private fun recuerdo(
        id: Long,
        path: String = "",
        descPath: String? = null,
        enPapelera: Boolean = false,
        papeleraAt: Long? = null,
        createdAt: Long = System.currentTimeMillis()
    ) = RecuerdosEntity(
        id = id,
        title = "Prueba",
        type = "FOTO",
        filePath = path,
        descriptionPath = descPath,
        createdAt = createdAt,
        enPapelera = enPapelera,
        papeleraAt = papeleraAt
    )
}