package com.example.tesorosdeltiempo.adaptador

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.R

// Una fila de la papelera con título y restaurar o eliminar todo
class PapeleraListaAdap(
    private val contexto: Context,
    private var items: List<RecuerdosEntity>,
    private val alRestaurar: (Long) -> Unit,
    private val alEliminarDefinitivo: (Long) -> Unit
) : BaseAdapter() {

    fun actualizarLista(nueva: List<RecuerdosEntity>) {
        items = nueva
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = items[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vista = convertView ?: LayoutInflater.from(contexto)
            .inflate(R.layout.item_papelera_fila, parent, false)

        val recuerdo = items[position]
        val tvTitulo = vista.findViewById<TextView>(R.id.tvTituloPapeleraItem)
        val btnRestaurar = vista.findViewById<Button>(R.id.btnRestaurarItem)
        val btnEliminar = vista.findViewById<Button>(R.id.btnEliminarDefinitivoItem)

        tvTitulo.text = recuerdo.title.ifBlank { "Sin título" }

        val id = recuerdo.id
        btnRestaurar.setOnClickListener { alRestaurar(id) }
        btnEliminar.setOnClickListener { alEliminarDefinitivo(id) }

        return vista
    }
}