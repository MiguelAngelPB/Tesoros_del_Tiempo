package com.example.tesorosdeltiempo

import android.content.Context
import android.net.Uri
import com.example.tesorosdeltiempo.BD.datos.AppDatabase
import com.example.tesorosdeltiempo.BD.datos.RecuerdosEntity
import com.example.tesorosdeltiempo.seguridad.AyArchivoSeguro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

// Exportar / importar la galería en un .zip
object GaleriaExportImport {
    // Nombre del índice dentro del zip
    private const val MANIFEST = "manifest.json"
    private const val VERSION = 2
    // Carpeta virtual dentro del zip para los .bin
    private const val PREFIX = "f/"

    // Crea un zip en destinoUri: lista Room, mete bytes en claro de cada adjunto y luego manifest.json
    suspend fun exportarZipPortatil(context: Context, destinoUri: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val dao = AppDatabase.getInstance(context).recuerdosDao()
                val lista = dao.listarTodosRecuerdos()
                val items = JSONArray()
                BufferedOutputStream(
                    context.contentResolver.openOutputStream(destinoUri)
                        ?: error(context.getString(R.string.no_se_pudo_crear_el_archivo))
                ).use { out ->
                    ZipOutputStream(out).use { zos ->
                        for (r in lista) {
                            val main = zipNombreYBytes(context, r.filePath, "${PREFIX}${r.id}_main.bin")
                            if (main != null) {
                                zos.putNextEntry(ZipEntry(main.first))
                                zos.write(main.second)
                                zos.closeEntry()
                            }
                            val desc = if (!r.descriptionPath.isNullOrBlank()) {
                                zipNombreYBytes(context, r.descriptionPath!!, "${PREFIX}${r.id}_desc.bin")
                            } else null
                            if (desc != null) {
                                zos.putNextEntry(ZipEntry(desc.first))
                                zos.write(desc.second)
                                zos.closeEntry()
                            }
                            items.put(recuerdoAJson(r, main?.first, desc?.first))
                        }
                        val manifest = JSONObject()
                            .put("v", VERSION)
                            .put("items", items)
                            .toString()
                            .toByteArray(Charsets.UTF_8)
                        zos.putNextEntry(ZipEntry(MANIFEST))
                        zos.write(manifest)
                        zos.closeEntry()
                    }
                }
                Result.success(Unit)
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

    // Lee el zip elegido por el usuario, recrea .enc en filesDir e inserta las filas nuevas en Room
    suspend fun importarZipPortatil(context: Context, origenUri: Uri): Result<Int> =
        withContext(Dispatchers.IO) {
            val temp = File(context.cacheDir, "import_${System.currentTimeMillis()}.zip")
            try {
                context.contentResolver.openInputStream(origenUri)?.use { input ->
                    FileOutputStream(temp).use { input.copyTo(it) }
                } ?: error(context.getString(R.string.no_se_pudo_leer_el_archivo))

                val db = AppDatabase.getInstance(context)
                val dao = db.recuerdosDao()
                val n = ZipFile(temp).use { zip ->
                    val texto = zip.getInputStream(zip.getEntry(MANIFEST) ?: error(
                        context.getString(
                            R.string.copia_inv_lida
                        )))
                        .bufferedReader().use { it.readText() }
                    val raiz = JSONObject(texto)
                    if (raiz.getInt("v") != VERSION) error(context.getString(R.string.copia_no_compatible))
                    val arr = raiz.getJSONArray("items")
                    val filas = ArrayList<RecuerdosEntity>(arr.length())
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        val m = o.optString("mainEntry", "")
                        val d = o.optString("descEntry", "")
                        val rutaMain = if (m.isNotBlank() && entradaOk(m)) {
                            val raw = zip.getInputStream(zip.getEntry(m)!!).use { it.readBytes() }
                            AyArchivoSeguro.guardarBytesComoArchivoCifrado(context, raw, "port_main")
                        } else ""
                        val rutaDesc = if (d.isNotBlank() && entradaOk(d)) {
                            val raw = zip.getInputStream(zip.getEntry(d)!!).use { it.readBytes() }
                            AyArchivoSeguro.guardarBytesComoArchivoCifrado(context, raw, "port_desc")
                        } else null
                        filas.add(jsonARecuerdo(o, rutaMain, rutaDesc))
                    }
                    db.runInTransaction {
                        for (fila in filas) {
                            dao.insertar(fila)
                        }
                    }
                    filas.size
                }
                Result.success(n)
            } catch (e: Throwable) {
                Result.failure(e)
            } finally {
                if (!temp.delete()) {
                    temp.deleteOnExit()
                }
            }
        }

    private fun zipNombreYBytes(context: Context, ruta: String, nombreEnZip: String): Pair<String, ByteArray>? {
        if (ruta.isBlank()) return null
        val bytes = try {
            if (AyArchivoSeguro.esRutaArchivoCifrado(ruta)) {
                AyArchivoSeguro.leerArchivoCifradoABytes(context, ruta)
            } else {
                File(ruta).takeIf { it.isFile }?.readBytes()
            }
        } catch (_: Exception) {
            null
        }
        return if (bytes != null) Pair(nombreEnZip, bytes) else null
    }

    private fun entradaOk(nombre: String): Boolean =
        nombre.startsWith(PREFIX) && !nombre.contains("..") && nombre != MANIFEST

    private fun recuerdoAJson(r: RecuerdosEntity, main: String?, desc: String?): JSONObject =
        JSONObject().apply {
            put("title", r.title)
            put("description", r.description ?: JSONObject.NULL)
            put("type", r.type)
            put("tags", r.tags)
            put("descriptionType", r.descriptionType ?: JSONObject.NULL)
            put("descriptionContent", r.descriptionContent ?: JSONObject.NULL)
            put("createdAt", r.createdAt)
            put("enPapelera", r.enPapelera)
            put("mainEntry", main ?: "")
            put("descEntry", desc ?: "")
            put("papeleraAt", r.papeleraAt ?: JSONObject.NULL)
        }

    private fun jsonARecuerdo(o: JSONObject, filePath: String, descriptionPath: String?): RecuerdosEntity =
        RecuerdosEntity(
            id = 0L,
            title = o.getString("title"),
            description = if (o.isNull("description")) null else o.getString("description"),
            filePath = filePath,
            type = o.getString("type"),
            tags = o.optString("tags", ""),
            descriptionType = if (o.isNull("descriptionType")) null else o.getString("descriptionType"),
            descriptionContent = if (o.isNull("descriptionContent")) null else o.getString("descriptionContent"),
            descriptionPath = descriptionPath,
            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
            enPapelera = o.optBoolean("enPapelera", false),
            papeleraAt = if (o.isNull("papeleraAt")) null else o.getLong("papeleraAt")        )

    fun nombreCopiaSugerido(): String {
        val f = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        return "tesoros_portable_${f.format(Date())}.zip"
    }
}