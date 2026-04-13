package com.example.tesorosdeltiempo.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tesorosdeltiempo.BD.datos.AppDatabase
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.BD.datos.RecuerdosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Capa entre las pantallas yel  repositorio con operaciones en segundo plano.
class RecuerdosViewModel(
    private val repository: RecuerdosRepository
) : ViewModel() {

    private val filtroTags = MutableStateFlow("")

    // Galería principal
    val recuerdos = filtroTags
        .flatMapLatest { filtro ->
            if (filtro.isBlank()) {
                repository.observeRecuerdos()
            } else {
                repository.observeRecuerdosByTag(filtro.trim())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Solo ítems con enPapelera = true
    val papelera = repository.observePapelera()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun guardarRecuerdo(recuerdo: RecuerdosEntity) {
        viewModelScope.launch {
            repository.guardarRecuerdo(recuerdo)
        }
    }

    fun borrarRecuerdo(recuerdo: RecuerdosEntity) {
        viewModelScope.launch {
            repository.borrarRecuerdo(recuerdo)
        }
    }

    fun borrarRecuerdoPorId(id: Long) {
        viewModelScope.launch {
            repository.borrarRecuerdoPorId(id)
        }
    }

    fun moverRecuerdoAPapeleraPorId(id: Long) {
        viewModelScope.launch {
            repository.moverRecuerdoAPapeleraPorId(id)
        }
    }

    fun restaurarDesdePapeleraPorId(id: Long) {
        viewModelScope.launch {
            repository.restaurarDesdePapeleraPorId(id)
        }
    }

    fun eliminarDefinitivoDesdePapeleraPorId(id: Long) {
        viewModelScope.launch {
            repository.eliminarDefinitivoDesdePapeleraPorId(id)
        }
    }

    fun restaurarTodoDesdePapelera() {
        viewModelScope.launch {
            repository.restaurarTodoDesdePapelera()
        }
    }

    fun vaciarPapeleraDefinitivo() {
        viewModelScope.launch {
            repository.vaciarPapeleraDefinitivo()
        }
    }

    fun filtrarPorEtiqueta(query: String) {
        filtroTags.value = query
    }
}

// Crea el ViewModel con el repositorio ya montado con Room
class RecuerdosViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(context)
        val repo = RecuerdosRepository(db.recuerdosDao())
        @Suppress("UNCHECKED_CAST")
        return RecuerdosViewModel(repo) as T
    }
}