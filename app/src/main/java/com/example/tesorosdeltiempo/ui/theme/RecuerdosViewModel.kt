package com.example.tesorosdeltiempo.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tesorosdeltiempo.BD.datos.AppDatabase
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.BD.datos.RecuerdosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecuerdosViewModel(
    private val repository: RecuerdosRepository
) : ViewModel() {

    val recuerdos = repository.observeRecuerdos()
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
}

class RecuerdosViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(context)
        val repo = RecuerdosRepository(db.recuerdosDao())
        @Suppress("UNCHECKED_CAST")
        return RecuerdosViewModel(repo) as T
    }
}

