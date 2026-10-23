package com.uniruta.app.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val MAX_IMAGE_BYTES = 15L * 1024 * 1024
private const val COPY_BUFFER_BYTES = 16 * 1024
private const val DEFAULT_EXTENSION = "jpg"
private val fileStamp: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")

class LocalImageStore(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageStore {

    private val appContext = context.applicationContext
    private val authority = "${appContext.packageName}.fileprovider"

    override suspend fun importImage(sourceUri: String, folder: String, baseName: String): ImageResult =
        withContext(dispatcher) {
            val resolver = appContext.contentResolver
            val uri = Uri.parse(sourceUri)
            try {
                val mimeType = resolver.getType(uri)
                if (mimeType != null && !mimeType.startsWith("image/")) {
                    return@withContext ImageResult.Failure("El archivo seleccionado no es una imagen.")
                }
                val (displayName, declaredSize) = describe(uri)
                if (declaredSize != null && declaredSize > MAX_IMAGE_BYTES) {
                    return@withContext ImageResult.Failure(SIZE_MESSAGE)
                }

                val extension = mimeType
                    ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
                    ?: DEFAULT_EXTENSION
                val directory = directory(folder)
                val prefix = "${baseName}_"
                val target = File(directory, "$prefix${stamp()}.$extension")
                val temporary = File(directory, "${target.name}.tmp")

                val input = resolver.openInputStream(uri)
                    ?: return@withContext ImageResult.Failure(MISSING_MESSAGE)
                val copied = input.use { source ->
                    temporary.outputStream().use { sink -> copyLimited(source, sink) }
                }
                if (copied == null) {
                    temporary.delete()
                    return@withContext ImageResult.Failure(SIZE_MESSAGE)
                }
                if (copied == 0L || !temporary.renameTo(target)) {
                    temporary.delete()
                    return@withContext ImageResult.Failure(MISSING_MESSAGE)
                }

                directory.listFiles()
                    ?.filter { it.name.startsWith(prefix) && it != target }
                    ?.forEach { it.delete() }

                ImageResult.Success(
                    StoredImage(
                        displayName = displayName ?: target.name,
                        path = target.absolutePath,
                        sizeBytes = target.length()
                    )
                )
            } catch (error: FileNotFoundException) {
                ImageResult.Failure(MISSING_MESSAGE)
            } catch (error: SecurityException) {
                ImageResult.Failure("No hay permiso para leer la imagen seleccionada.")
            } catch (error: IOException) {
                ImageResult.Failure(SAVE_MESSAGE)
            }
        }

    override fun createCaptureTarget(folder: String, baseName: String): CaptureTarget? = try {
        val file = File(directory(folder), "${baseName}_${stamp()}.$DEFAULT_EXTENSION")
        CaptureTarget(
            path = file.absolutePath,
            contentUri = FileProvider.getUriForFile(appContext, authority, file).toString()
        )
    } catch (error: IllegalArgumentException) {
        null
    } catch (error: IOException) {
        null
    }

    override suspend fun confirmCapture(target: CaptureTarget): ImageResult = withContext(dispatcher) {
        val file = File(target.path)
        when {
            !file.exists() || file.length() == 0L -> {
                file.delete()
                ImageResult.Failure("No se recibió ninguna fotografía de la cámara.")
            }
            file.length() > MAX_IMAGE_BYTES -> {
                file.delete()
                ImageResult.Failure(SIZE_MESSAGE)
            }
            else -> ImageResult.Success(
                StoredImage(displayName = file.name, path = file.absolutePath, sizeBytes = file.length())
            )
        }
    }

    override suspend fun delete(path: String) {
        withContext(dispatcher) {
            val file = File(path)
            if (file.isInside(appContext.filesDir)) file.delete()
        }
    }

    override suspend fun clear(folder: String) {
        withContext(dispatcher) {
            File(appContext.filesDir, folder).deleteRecursively()
        }
    }

    private fun directory(folder: String): File {
        val directory = File(appContext.filesDir, folder)
        if (!directory.exists() && !directory.mkdirs()) throw IOException("Cannot create $folder")
        return directory
    }

    private fun describe(uri: Uri): Pair<String?, Long?> {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        appContext.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val name = if (nameIndex >= 0 && !cursor.isNull(nameIndex)) cursor.getString(nameIndex) else null
                val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null
                return name to size
            }
        }
        return null to null
    }

    private fun copyLimited(source: InputStream, sink: OutputStream): Long? {
        val buffer = ByteArray(COPY_BUFFER_BYTES)
        var total = 0L
        while (true) {
            val read = source.read(buffer)
            if (read < 0) return total
            total += read
            if (total > MAX_IMAGE_BYTES) return null
            sink.write(buffer, 0, read)
        }
    }

    private fun stamp(): String = LocalDateTime.now().format(fileStamp)

    private fun File.isInside(root: File): Boolean =
        canonicalPath.startsWith(root.canonicalPath + File.separator)

    private companion object {
        const val SIZE_MESSAGE = "La imagen supera el tamaño máximo permitido de 15 MB."
        const val MISSING_MESSAGE = "La imagen seleccionada ya no está disponible."
        const val SAVE_MESSAGE = "No fue posible guardar la imagen en el almacenamiento de la aplicación."
    }
}
