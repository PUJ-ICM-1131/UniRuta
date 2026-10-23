package com.uniruta.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Roboto = FontFamily.Default

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

val SectionLabelStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 1.sp
)

val BadgeStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.8.sp
)

val ValueStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp
)

val PriceStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp
)

val TotalStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 32.sp
)

val MetricStyle = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.Bold,
    fontSize = 30.sp,
    lineHeight = 36.sp
)

val TokenStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.3.sp
)
