package com.uniruta.app.device.qr

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.uniruta.app.data.mock.MockDriverData
import com.uniruta.app.data.mock.MockStudentData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val MODULE_PIXELS = 6
private const val QUIET_ZONE = 4

class QrCodeEncoderTest {

    private fun decode(matrix: QrMatrix): String {
        val side = (matrix.size + QUIET_ZONE * 2) * MODULE_PIXELS
        val pixels = IntArray(side * side) { index ->
            val row = index / side / MODULE_PIXELS - QUIET_ZONE
            val column = index % side / MODULE_PIXELS - QUIET_ZONE
            val dark = row in 0 until matrix.size && column in 0 until matrix.size && matrix.isDark(row, column)
            if (dark) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(side, side, pixels)))
        return QRCodeReader().decode(bitmap, mapOf(DecodeHintType.TRY_HARDER to true)).text
    }

    @Test
    fun studentPassQrDecodesToTheExactToken() {
        val token = MockStudentData.passes.single().token

        val matrix = QrCodeEncoder.encode(token)!!

        assertEquals(token, decode(matrix))
    }

    @Test
    fun studentAndDriverShareTheSameDemoToken() {
        val studentToken = MockStudentData.passes.single().token
        val driverToken = MockDriverData.demoScans.first().token

        assertEquals(studentToken, driverToken)
    }

    @Test
    fun qrContainsOnlyTheOpaqueToken() {
        val token = "c47d2a91-5b60-4f0e-9a72-3ed418c6b075"

        val decoded = decode(QrCodeEncoder.encode(token)!!)

        assertEquals(token, decoded)
        assertEquals(36, decoded.length)
    }

    @Test
    fun blankTokenProducesNoCode() {
        assertNull(QrCodeEncoder.encode(" "))
    }
}
