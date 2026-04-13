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

// Lista de recuerdos en la papelera y restaurar o borrar (uno o todo) con confirmación
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
            Toast.makeText(this, "Inicia sesión como cuidador para ver la papelera", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Restaurado", Toast.LENGTH_SHORT).show()
            },
            alEliminarDefinitivo = { id ->
                AlertDialog.Builder(this)
                    .setTitle("Eliminar para siempre")
                    .setMessage("¿Seguro? No se puede deshacer.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.eliminarDefinitivoDesdePapeleraPorId(id)
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
        )
        listView.adapter = adaptador

        // Vuelven todos los recuerdos a la galería principal
        findViewById<Button>(R.id.btnRestaurarTodoPapelera).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Restaurar todo")
                .setMessage("¿Volver a poner todos los recuerdos en la galería principal?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restaurar todo") { _, _ ->
                    viewModel.restaurarTodoDesdePapelera()
                    Toast.makeText(this, "Todo restaurado", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        // Borra todos los recuerdos de la papelera
        findViewById<Button>(R.id.btnEliminarTodoPapelera).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Vaciar papelera")
                .setMessage("Se borrarán definitivamente todos los recuerdos de la papelera y sus archivos.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar todo") { _, _ ->
                    viewModel.vaciarPapeleraDefinitivo()
                    Toast.makeText(this, "Papelera vaciada", Toast.LENGTH_SHORT).show()
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