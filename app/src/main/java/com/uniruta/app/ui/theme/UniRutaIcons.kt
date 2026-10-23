package com.uniruta.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

object UniRutaIcons {

    val Bus: ImageVector by lazy { strokeIcon("Bus", "M7 3h10a3 3 0 0 1 3 3v9a3 3 0 0 1 -3 3h-10a3 3 0 0 1 -3 -3v-9a3 3 0 0 1 3 -3zM4 11h16M7.4 14.5a0.6 0.6 0 1 0 1.2 0a0.6 0.6 0 1 0 -1.2 0zM15.4 14.5a0.6 0.6 0 1 0 1.2 0a0.6 0.6 0 1 0 -1.2 0zM7 18v2.5M17 18v2.5") }

    val Mail: ImageVector by lazy { strokeIcon("Mail", "M5 4h14a3 3 0 0 1 3 3v10a3 3 0 0 1 -3 3h-14a3 3 0 0 1 -3 -3v-10a3 3 0 0 1 3 -3zM22 7l-10 6L2 7") }

    val Lock: ImageVector by lazy { strokeIcon("Lock", "M6 11h12a2 2 0 0 1 2 2v6a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2v-6a2 2 0 0 1 2 -2zM8 11V7a4 4 0 0 1 8 0v4") }

    val Eye: ImageVector by lazy { strokeIcon("Eye", "M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12zM9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0z") }

    val EyeOff: ImageVector by lazy { strokeIcon("EyeOff", "M3 3l18 18M10.6 10.6a2 2 0 0 0 2.8 2.8M9.9 5.1A10 10 0 0 1 12 5c6.5 0 10 7 10 7a17.6 17.6 0 0 1-3.2 4.1M6.6 6.6C3.9 8.4 2 12 2 12s3.5 7 10 7c1.9 0 3.6-.5 5-1.4") }

    val GraduationCap: ImageVector by lazy { strokeIcon("GraduationCap", "M22 10 12 5 2 10l10 5 10-5zM6 12v5c3 2 9 2 12 0v-5") }

    val SteeringWheel: ImageVector by lazy { strokeIcon("SteeringWheel", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0zM9.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0zM12 14.5V21M9.6 11.3 3.4 9.5M14.4 11.3l6.2-1.8") }

    val ShieldCheck: ImageVector by lazy { strokeIcon("ShieldCheck", "M12 3 4 6v6c0 4.5 3.4 8.3 8 9 4.6-.7 8-4.5 8-9V6l-8-3zM9 12l2 2 4-4") }

    val Route: ImageVector by lazy { strokeIcon("Route", "M3.5 19a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0zM18 11.5c1.9 0 3.5-1.6 3.5-3.5S19.9 4.5 18 4.5 14.5 6.1 14.5 8c0 2.2 3.5 6 3.5 6M8.5 19H16a3 3 0 0 0 0-6h-6a3 3 0 0 1 0-6h3") }

    val ChevronRight: ImageVector by lazy { strokeIcon("ChevronRight", "M9 6l6 6-6 6") }

    val ChevronUp: ImageVector by lazy { strokeIcon("ChevronUp", "M6 15l6-6 6 6") }

    val ChevronDown: ImageVector by lazy { strokeIcon("ChevronDown", "M6 9l6 6 6-6") }

    val ArrowBack: ImageVector by lazy { strokeIcon("ArrowBack", "M19 12H5M12 19l-7-7 7-7") }

    val CalendarCheck: ImageVector by lazy { strokeIcon("CalendarCheck", "M6 5h12a3 3 0 0 1 3 3v10a3 3 0 0 1 -3 3h-12a3 3 0 0 1 -3 -3v-10a3 3 0 0 1 3 -3zM3 10h18M8 3v4M16 3v4M9 15.5l2 2 4-4") }

    val Calendar: ImageVector by lazy { strokeIcon("Calendar", "M6 5h12a3 3 0 0 1 3 3v10a3 3 0 0 1 -3 3h-12a3 3 0 0 1 -3 -3v-10a3 3 0 0 1 3 -3zM3 10h18M8 3v4M16 3v4") }

    val QrCode: ImageVector by lazy { strokeIcon("QrCode", "M4.5 3h4a1.5 1.5 0 0 1 1.5 1.5v4a1.5 1.5 0 0 1 -1.5 1.5h-4a1.5 1.5 0 0 1 -1.5 -1.5v-4a1.5 1.5 0 0 1 1.5 -1.5zM15.5 3h4a1.5 1.5 0 0 1 1.5 1.5v4a1.5 1.5 0 0 1 -1.5 1.5h-4a1.5 1.5 0 0 1 -1.5 -1.5v-4a1.5 1.5 0 0 1 1.5 -1.5zM4.5 14h4a1.5 1.5 0 0 1 1.5 1.5v4a1.5 1.5 0 0 1 -1.5 1.5h-4a1.5 1.5 0 0 1 -1.5 -1.5v-4a1.5 1.5 0 0 1 1.5 -1.5zM14 14h3v3h-3zM17 21h4v-4M14 20v1") }

    val Logout: ImageVector by lazy { strokeIcon("Logout", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9") }

    val Users: ImageVector by lazy { strokeIcon("Users", "M5.5 8a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0zM3 20c0-3.3 2.7-6 6-6s6 2.7 6 6M16 4.5a3.5 3.5 0 0 1 0 7M18 14.5c2 .8 3 2.8 3 5.5") }

    val User: ImageVector by lazy { strokeIcon("User", "M8 8a4 4 0 1 0 8 0a4 4 0 1 0 -8 0zM4 21c0-4.4 3.6-8 8-8s8 3.6 8 8") }

    val UserCheck: ImageVector by lazy { strokeIcon("UserCheck", "M6 8a4 4 0 1 0 8 0a4 4 0 1 0 -8 0zM3 21c0-4 3-7 7-7 1.5 0 2.8.4 4 1M15 19l2 2 4-4") }

    val UserPlus: ImageVector by lazy { strokeIcon("UserPlus", "M5 8a4 4 0 1 0 8 0a4 4 0 1 0 -8 0zM2 21c0-4 3.1-7 7-7s7 3 7 7M19 8v6M16 11h6") }

    val Timer: ImageVector by lazy { strokeIcon("Timer", "M4 13a8 8 0 1 0 16 0a8 8 0 1 0 -16 0zM12 9v4l2 2M9 2h6") }

    val Clock: ImageVector by lazy { strokeIcon("Clock", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0zM12 7v5l3 2") }

    val ArrowsHorizontal: ImageVector by lazy { strokeIcon("ArrowsHorizontal", "M3 12h18M7 8l-4 4 4 4M17 8l4 4-4 4") }

    val Truck: ImageVector by lazy { strokeIcon("Truck", "M4 6h10a2 2 0 0 1 2 2v7a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-7a2 2 0 0 1 2 -2zM16 9h3.5l2.5 3.5V17h-6M4.7 17.5a1.8 1.8 0 1 0 3.6 0a1.8 1.8 0 1 0 -3.6 0zM15.7 17.5a1.8 1.8 0 1 0 3.6 0a1.8 1.8 0 1 0 -3.6 0z") }

    val Cash: ImageVector by lazy { strokeIcon("Cash", "M5 6h14a3 3 0 0 1 3 3v6a3 3 0 0 1 -3 3h-14a3 3 0 0 1 -3 -3v-6a3 3 0 0 1 3 -3zM9.5 12a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0zM6 12h.01M18 12h.01") }

    val Info: ImageVector by lazy { strokeIcon("Info", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0zM12 11v5M12 8h.01") }

    val Bank: ImageVector by lazy { strokeIcon("Bank", "M3 10 12 4l9 6M5 10v8M9.5 10v8M14.5 10v8M19 10v8M3 20h18") }

    val Store: ImageVector by lazy { strokeIcon("Store", "M3 9l1.5-5h15L21 9M3 9h18M5 9v11h14V9M10 20v-5h4v5") }

    val Hash: ImageVector by lazy { strokeIcon("Hash", "M4 9h16M4 15h16M10 3 8 21M16 3l-2 18") }

    val Image: ImageVector by lazy { strokeIcon("Image", "M6 3h12a3 3 0 0 1 3 3v12a3 3 0 0 1 -3 3h-12a3 3 0 0 1 -3 -3v-12a3 3 0 0 1 3 -3zM7 9a2 2 0 1 0 4 0a2 2 0 1 0 -4 0zM21 15l-5-5L5 21") }

    val CheckCircle: ImageVector by lazy { strokeIcon("CheckCircle", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0zM8 12l3 3 5-6") }

    val Upload: ImageVector by lazy { strokeIcon("Upload", "M12 15V3M7 8l5-5 5 5M5 15v4a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-4") }

    val Send: ImageVector by lazy { strokeIcon("Send", "M22 2 11 13M22 2l-7 20-4-9-9-4 20-7z") }

    val Check: ImageVector by lazy { strokeIcon("Check", "M5 12.5 10 17l9-10") }

    val Close: ImageVector by lazy { strokeIcon("Close", "M18 6 6 18M6 6l12 12") }

    val Play: ImageVector by lazy { strokeIcon("Play", "M7 4.5v15l12.5-7.5z") }

    val Activity: ImageVector by lazy { strokeIcon("Activity", "M3 12h4l3-8 4 16 3-8h4") }

    val MapPin: ImageVector by lazy { strokeIcon("MapPin", "M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21zM9.5 9.5a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0z") }

    val Crosshair: ImageVector by lazy { strokeIcon("Crosshair", "M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0zM4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0zM12 2v2M12 20v2M2 12h2M20 12h2") }

    val Warning: ImageVector by lazy { strokeIcon("Warning", "M12 3 2 20h20L12 3zM12 10v4.5M12 17.5h.01") }

    val AlertCircle: ImageVector by lazy { strokeIcon("AlertCircle", "M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0zM12 7v6M12 16.5h.01") }

    val Turn: ImageVector by lazy { strokeIcon("Turn", "M4 20v-7a5 5 0 0 1 5-5h11M16 4l4 4-4 4") }

    val Note: ImageVector by lazy { strokeIcon("Note", "M4 4h16v12l-4 4H4zM16 20v-4h4M8 9h8M8 13h5") }

    val Stop: ImageVector by lazy { strokeIcon("Stop", "M8 5h8a3 3 0 0 1 3 3v8a3 3 0 0 1 -3 3h-8a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3z") }

    val Camera: ImageVector by lazy { strokeIcon("Camera", "M4 8h3l2-3h6l2 3h3a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1zM8.5 13.5a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0z") }

    val Plus: ImageVector by lazy { strokeIcon("Plus", "M12 5v14M5 12h14") }

    val Trash: ImageVector by lazy { strokeIcon("Trash", "M4 7h16M10 11v6M14 11v6M6 7l1 13a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1l1-13M9 7V4h6v3") }

    val Flag: ImageVector by lazy { strokeIcon("Flag", "M5 21V4M5 4h11l-2 4 2 4H5") }

    val Phone: ImageVector by lazy { strokeIcon("Phone", "M5 3h3.5l2 5-2.5 1.5a11 11 0 0 0 6.5 6.5l1.5-2.5 5 2V19a2 2 0 0 1-2 2A17 17 0 0 1 3 5a2 2 0 0 1 2-2z") }

    val Bell: ImageVector by lazy { strokeIcon("Bell", "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.94 1.94 0 0 0 3.4 0") }

    val Refresh: ImageVector by lazy { strokeIcon("Refresh", "M3 12a9 9 0 0 1 15.5-6.2L21 8M21 3v5h-5M21 12a9 9 0 0 1-15.5 6.2L3 16M3 21v-5h5") }

    val FileCheck: ImageVector by lazy { strokeIcon("FileCheck", "M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8zM14 3v5h5M9 15l2 2 4-4") }

    val Map: ImageVector by lazy { strokeIcon("Map", "M9 4 3 6v14l6-2 6 2 6-2V4l-6 2-6-2zM9 4v14M15 6v14") }
}

private fun strokeIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(pathData),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ).build()
