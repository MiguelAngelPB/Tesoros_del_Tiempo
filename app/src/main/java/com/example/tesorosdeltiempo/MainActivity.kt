package com.example.tesorosdeltiempo

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.GridView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.adaptador.GaleriaFotosAdap
import com.example.tesorosdeltiempo.ui.RecuerdosViewModel
import com.example.tesorosdeltiempo.ui.RecuerdosViewModelFactory
import com.example.tesorosdeltiempo.ui.BarraArribaAy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: RecuerdosViewModel by viewModels {
        RecuerdosViewModelFactory(this)
    }

    private lateinit var gridView: GridView
    private lateinit var fab: FloatingActionButton
    private lateinit var galeriaAdapter: GaleriaFotosAdap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Pongo la barra de arriba fija
        BarraArribaAy.ponerBarraArriba(this) { query ->
            viewModel.filtrarPorEtiqueta(query)
        }

        gridView = findViewById(R.id.Id_Galeria)
        fab = findViewById(R.id.BotonA)

        galeriaAdapter = GaleriaFotosAdap(this)
        gridView.adapter = galeriaAdapter

        gridView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val recuerdo = galeriaAdapter.getItem(position) as RecuerdosEntity
                val intent = Intent(this, ImagenCompleta::class.java)
                intent.putExtra("id", recuerdo.id)
                intent.putExtra("filePath", recuerdo.filePath)
                intent.putExtra("type", recuerdo.type)
                intent.putExtra("title", recuerdo.title)
                intent.putExtra("tags", recuerdo.tags)
                intent.putExtra("description", recuerdo.description)
                intent.putExtra("descriptionType", recuerdo.descriptionType)
                intent.putExtra("descriptionContent", recuerdo.descriptionContent)
                intent.putExtra("descriptionPath", recuerdo.descriptionPath)
                intent.putExtra("textContent", recuerdo.descriptionContent)
                startActivity(intent)
            }

        fab.setOnClickListener {
            startActivity(Intent(this, NuevoRecuerdoActivity::class.java))
        }

        lifecycleScope.launch {
            viewModel.recuerdos.collect { lista ->
                galeriaAdapter.submitList(lista)
            }
        }
    }
}