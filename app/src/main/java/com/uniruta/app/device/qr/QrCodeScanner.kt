package com.uniruta.app.device.qr

import android.content.Context
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

sealed interface QrScanResult {
    data class Success(val content: String) : QrScanResult
    data object Cancelled : QrScanResult
    data class Failure(val message: String) : QrScanResult
}

class QrCodeScanner(private val context: Context) {

    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .enableAutoZoom()
        .build()

    fun scan(onResult: (QrScanResult) -> Unit) {
        val task = try {
            GmsBarcodeScanning.getClient(context, options).startScan()
        } catch (error: IllegalStateException) {
            onResult(QrScanResult.Failure(UNAVAILABLE_MESSAGE))
            return
        }
        task
            .addOnSuccessListener { barcode ->
                val content = barcode.rawValue?.trim()
                onResult(
                    if (content.isNullOrEmpty()) {
                        QrScanResult.Failure("El código leído no contiene información.")
                    } else {
                        QrScanResult.Success(content)
                    }
                )
            }
            .addOnCanceledListener { onResult(QrScanResult.Cancelled) }
            .addOnFailureListener { error ->
                val unavailable = error is MlKitException && error.errorCode == MlKitException.UNAVAILABLE
                onResult(
                    QrScanResult.Failure(
                        if (unavailable) {
                            "El escáner de Google Play services todavía se está descargando. " +
                                "Intenta de nuevo en unos segundos."
                        } else {
                            UNAVAILABLE_MESSAGE
                        }
                    )
                )
            }
    }

    private companion object {
        const val UNAVAILABLE_MESSAGE =
            "No fue posible abrir el escáner de la cámara. Verifica que Google Play services esté actualizado."
    }
}
