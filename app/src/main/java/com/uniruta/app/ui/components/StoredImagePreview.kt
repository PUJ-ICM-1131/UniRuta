package com.uniruta.app.ui.components

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

private const val THUMBNAIL_MAX_PIXELS = 900

private sealed interface PreviewState {
    data object Loading : PreviewState
    data class Loaded(val bitmap: ImageBitmap) : PreviewState
    data object Unavailable : PreviewState
}

@Composable
fun StoredImagePreview(
    path: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val preview by produceState<PreviewState>(initialValue = PreviewState.Loading, path) {
        value = withContext(Dispatchers.IO) {
            loadThumbnail(path)?.let { PreviewState.Loaded(it) } ?: PreviewState.Unavailable
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        when (val current = preview) {
            PreviewState.Loading -> CircularProgressIndicator()
            is PreviewState.Loaded -> Image(
                bitmap = current.bitmap,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            PreviewState.Unavailable -> Text(
                text = "La vista previa de la imagen no está disponible.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private fun loadThumbnail(path: String): ImageBitmap? {
    val file = File(path)
    if (!file.exists() || file.length() == 0L) return null
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
                val largest = max(info.size.width, info.size.height)
                if (largest > THUMBNAIL_MAX_PIXELS) {
                    val scale = THUMBNAIL_MAX_PIXELS / largest.toFloat()
                    decoder.setTargetSize(
                        (info.size.width * scale).roundToInt().coerceAtLeast(1),
                        (info.size.height * scale).roundToInt().coerceAtLeast(1)
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }.asImageBitmap()
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sampleSize = 1
            while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= THUMBNAIL_MAX_PIXELS) {
                sampleSize *= 2
            }
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
                ?.asImageBitmap()
        }
    } catch (error: IOException) {
        null
    } catch (error: IllegalArgumentException) {
        null
    } catch (error: OutOfMemoryError) {
        null
    }
}
