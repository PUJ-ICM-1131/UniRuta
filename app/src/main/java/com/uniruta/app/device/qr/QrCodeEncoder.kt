package com.uniruta.app.device.qr

import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder

class QrMatrix(
    val size: Int,
    private val modules: BooleanArray
) {
    fun isDark(row: Int, column: Int): Boolean = modules[row * size + column]
}

object QrCodeEncoder {

    fun encode(content: String): QrMatrix? {
        if (content.isBlank()) return null
        val code = try {
            Encoder.encode(
                content,
                ErrorCorrectionLevel.M,
                mapOf(EncodeHintType.CHARACTER_SET to Charsets.UTF_8.name())
            )
        } catch (error: WriterException) {
            return null
        }
        val matrix = code.matrix ?: return null
        val size = matrix.width
        val modules = BooleanArray(size * size) { index ->
            matrix.get(index % size, index / size).toInt() == 1
        }
        return QrMatrix(size = size, modules = modules)
    }
}
