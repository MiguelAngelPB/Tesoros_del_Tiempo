package com.example.tesorosdeltiempo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tesorosdeltiempo.adaptador.PapeleraListaAdap
import com.example.tesorosdeltiempo.seguridad.CuidadorAuthManager
import com.example.tesorosdeltiempo.ui.BarraArribaAy
import com.example.tesorosdeltiempo.ui.RecuerdosViewModel
import com.example.tesorosdeltiempo.ui.RecuerdosViewModelFactory
import kotlinx.coroutines.launch

// Lista de recuerdos en la papelera y restaurar o borrar (uno o todos) con confirmación
class PapeleraActivity : AppCompatActivity() {

    private val viewModel: RecuerdosViewModel by viewModels {
        RecuerdosViewModelFactory(this)
    }

    private lateinit var listView: ListView
    private lateinit var tvVacia: TextView
    private lateinit var adaptador: PapeleraListaAdap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_papelera)

        // Pantalla solo para cuidador con sesión iniciada
        val auth = CuidadorAuthManager(this)
        if (!auth.estaConectado()) {
            Toast.makeText(this,
                getString(R.string.inicia_sesi_n_como_cuidador_para_ver_la_papelera), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        BarraArribaAy.ponerBarraArriba(this)

        listView = findViewById(R.id.listViewPapelera)
        tvVacia = findViewById(R.id.tvPapeleraVacia)

        adaptador = PapeleraListaAdap(
            contexto = this,
            items = emptyList(),
            alRestaurar = { id ->
                viewModel.restaurarDesdePapeleraPorId(id)
                Toast.makeText(this, getString(R.string.restaurado), Toast.LENGTH_SHORT).show()
            },
            alEliminarDefinitivo = { id ->
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.eliminar_para_siempre))
                    .setMessage(getString(R.string.seguro_no_se_puede_deshacer))
                    .setNegativeButton(getString(R.string.cancelar), null)
                    .setPositiveButton(getString(R.string.eliminar)) { _, _ ->
                        viewModel.eliminarDefinitivoDesdePapeleraPorId(id)
                        Toast.makeText(this, getString(R.string.eliminado), Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
        )
        listView.adapter = adaptador

        // Vuelven todos los recuerdos a la galería principal
        findViewById<Button>(R.id.btnRestaurarTodoPapelera).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.restaurar_todo))
                .setMessage(getString(R.string.volver_a_poner_todos_los_recuerdos_en_la_galer_a_principal))
                .setNegativeButton(getString(R.string.cancelar), null)
                .setPositiveButton(getString(R.string.restaurar_todo)) { _, _ ->
                    viewModel.restaurarTodoDesdePapelera()
                    Toast.makeText(this, getString(R.string.todo_restaurado), Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        // Borra todos los recuerdos de la papelera
        findViewById<Button>(R.id.btnEliminarTodoPapelera).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.vaciar_papelera))
                .setMessage(getString(R.string.se_borrar_n_definitivamente_todos_los_recuerdos_de_la_papelera_y_sus_archivos))
                .setNegativeButton(getString(R.string.cancelar), null)
                .setPositiveButton(getString(R.string.eliminar_todo)) { _, _ ->
                    viewModel.vaciarPapeleraDefinitivo()
                    Toast.makeText(this, getString(R.string.papelera_vaciada), Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        // Actualiza la lista
        lifecycleScope.launch {
            viewModel.papelera.collect { lista ->
                adaptador.actualizarLista(lista)
                val vacia = lista.isEmpty()
                tvVacia.visibility = if (vacia) View.VISIBLE else View.GONE
            }
        }
    }
}