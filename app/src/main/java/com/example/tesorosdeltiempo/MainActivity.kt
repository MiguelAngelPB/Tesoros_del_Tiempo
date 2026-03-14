package com.example.tesorosdeltiempo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.FileProvider
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.adaptador.GaleriaFotosAdap
import com.example.tesorosdeltiempo.ui.RecuerdosViewModel
import com.example.tesorosdeltiempo.ui.RecuerdosViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: RecuerdosViewModel by viewModels {
        RecuerdosViewModelFactory(this)
    }

    private lateinit var gridView: GridView
    private lateinit var fab: FloatingActionButton
    private lateinit var galeriaAdapter: GaleriaFotosAdap

    private var pendingPhotoPath: String? = null

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val path = pendingPhotoPath
            pendingPhotoPath = null
            if (success && !path.isNullOrEmpty()) {
                lifecycleScope.launch {
                    val recuerdo = RecuerdosEntity(
                        title = generarTituloAutomatico("Foto"),
                        filePath = path,
                        type = "FOTO"
                    )
                    viewModel.guardarRecuerdo(recuerdo)
                    Toast.makeText(
                        this@MainActivity,
                        "Foto guardada en la base de datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Foto cancelada", Toast.LENGTH_SHORT).show()
            }
        }

    private val captureVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    lifecycleScope.launch {
                        val path = copiarUriAAlmacenamientoInterno(uri, "video")
                        val recuerdo = RecuerdosEntity(
                            title = generarTituloAutomatico("Video"),
                            filePath = path,
                            type = "VIDEO"
                        )
                        viewModel.guardarRecuerdo(recuerdo)
                        Toast.makeText(
                            this@MainActivity,
                            "Vídeo guardado en la base de datos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "No se pudo obtener el vídeo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vídeo cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    private val audioLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    lifecycleScope.launch {
                        val path = copiarUriAAlmacenamientoInterno(uri, "audio")
                        val recuerdo = RecuerdosEntity(
                            title = generarTituloAutomatico("Audio"),
                            filePath = path,
                            type = "AUDIO"
                        )
                        viewModel.guardarRecuerdo(recuerdo)
                        Toast.makeText(
                            this@MainActivity,
                            "Audio guardado en la base de datos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "No se pudo obtener el audio", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val mimeType = contentResolver.getType(uri) ?: ""
                    val tipo = when {
                        mimeType.startsWith("image") -> "FOTO"
                        mimeType.startsWith("video") -> "VIDEO"
                        mimeType.startsWith("audio") -> "AUDIO"
                        else -> "DESCONOCIDO"
                    }

                    val path = copiarUriAAlmacenamientoInterno(uri, "archivo")

                    val recuerdo = RecuerdosEntity(
                        title = generarTituloAutomatico(tipo),
                        filePath = path,
                        type = tipo
                    )
                    viewModel.guardarRecuerdo(recuerdo)

                    Toast.makeText(
                        this@MainActivity,
                        "Archivo guardado en la base de datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById(R.id.Id_Galeria)
        fab = findViewById(R.id.BotonA)

        galeriaAdapter = GaleriaFotosAdap(this)
        gridView.adapter = galeriaAdapter

        gridView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val recuerdo = galeriaAdapter.getItem(position) as RecuerdosEntity
                val intent = Intent(this, ImagenCompleta::class.java)
                intent.putExtra("filePath", recuerdo.filePath)
                intent.putExtra("type", recuerdo.type)
                startActivity(intent)
            }

        fab.setOnClickListener {
            mostrarOpcionesNuevoRecuerdo()
        }

        lifecycleScope.launch {
            viewModel.recuerdos.collect { lista ->
                galeriaAdapter.submitList(lista)
            }
        }
    }

    private fun mostrarOpcionesNuevoRecuerdo() {
        val opciones = arrayOf(
            "Hacer foto / vídeo",
            "Grabar audio",
            "Elegir desde archivos"
        )

        AlertDialog.Builder(this)
            .setTitle("Añadir recuerdo")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarOpcionesCamara()
                    1 -> grabarAudio()
                    2 -> seleccionarDesdeArchivos()
                }
            }
            .show()
    }

    private fun mostrarOpcionesCamara() {
        val opciones = arrayOf("Foto", "Vídeo")
        AlertDialog.Builder(this)
            .setTitle("Cámara")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> hacerFoto()
                    1 -> grabarVideo()
                }
            }
            .show()
    }

    private fun hacerFoto() {
        val photoFile = File(filesDir, "foto_${System.currentTimeMillis()}.jpg")
        pendingPhotoPath = photoFile.absolutePath
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(uri)
    }

    private fun grabarVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        captureVideoLauncher.launch(intent)
    }

    private fun grabarAudio() {
        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        audioLauncher.launch(intent)
    }

    private fun seleccionarDesdeArchivos() {
        filePickerLauncher.launch(arrayOf("image/*", "video/*", "audio/*"))
    }

    private suspend fun copiarUriAAlmacenamientoInterno(uri: Uri, prefijo: String): String =
        withContext(Dispatchers.IO) {
            val extension = when {
                contentResolver.getType(uri)?.startsWith("image") == true -> ".jpg"
                contentResolver.getType(uri)?.startsWith("video") == true -> ".mp4"
                contentResolver.getType(uri)?.startsWith("audio") == true -> ".m4a"
                else -> ""
            }
            val nombre = "${prefijo}_${System.currentTimeMillis()}$extension"
            val ficheroDestino = File(filesDir, nombre)

            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream.use { input ->
                FileOutputStream(ficheroDestino).use { output ->
                    if (input != null) {
                        input.copyTo(output)
                    }
                }
            }

            ficheroDestino.absolutePath
        }

    private fun generarTituloAutomatico(tipo: String): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = formato.format(Date())
        return "$tipo - $fecha"
    }
}

