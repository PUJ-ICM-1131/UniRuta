package com.uniruta.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class UniRutaColors(
    val headerContainer: Color,
    val headerSubtitle: Color,
    val sectionLabel: Color,
    val mutedIcon: Color,
    val divider: Color,
    val connector: Color,
    val outlineStrong: Color,
    val infoTile: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val successHalo: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val warningBorder: Color,
    val errorBorder: Color,
    val progressTrack: Color,
    val disabledFill: Color,
    val disabledText: Color
)

private val LightExtended = UniRutaColors(
    headerContainer = NavyContainer,
    headerSubtitle = InkHeaderLight,
    sectionLabel = SectionLabelLight,
    mutedIcon = MutedIconLight,
    divider = DividerLight,
    connector = ConnectorLight,
    outlineStrong = OutlineStrongLight,
    infoTile = CanvasLight,
    successContainer = SuccessContainerLight,
    onSuccessContainer = SuccessTextLight,
    successHalo = SuccessHaloLight,
    warningContainer = AmberContainerLight,
    onWarningContainer = AmberTextLight,
    warningBorder = AmberBorderLight,
    errorBorder = ErrorBorderLight,
    progressTrack = ProgressTrackLight,
    disabledFill = DisabledFillLight,
    disabledText = DisabledTextLight
)

private val DarkExtended = UniRutaColors(
    headerContainer = NavyDarkContainer,
    headerSubtitle = InkSecondaryDark,
    sectionLabel = SectionLabelDark,
    mutedIcon = MutedIconDark,
    divider = DividerDark,
    connector = ConnectorDark,
    outlineStrong = OutlineStrongDark,
    infoTile = SurfaceRaisedDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = SuccessTextDark,
    successHalo = SuccessHaloDark,
    warningContainer = AmberContainerDark,
    onWarningContainer = AmberTextDark,
    warningBorder = AmberBorderDark,
    errorBorder = ErrorBorderDark,
    progressTrack = ProgressTrackDark,
    disabledFill = DisabledFillDark,
    disabledText = DisabledTextDark
)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = SurfaceWhite,
    primaryContainer = NavyContainer,
    onPrimaryContainer = Navy,
    inversePrimary = NavyDarkPrimary,
    secondary = AmberTextLight,
    onSecondary = SurfaceWhite,
    secondaryContainer = AmberContainerLight,
    onSecondaryContainer = AmberTextLight,
    tertiary = SuccessTextLight,
    onTertiary = SurfaceWhite,
    tertiaryContainer = SuccessContainerLight,
    onTertiaryContainer = SuccessTextLight,
    background = CanvasLight,
    onBackground = InkLight,
    surface = SurfaceWhite,
    onSurface = InkLight,
    surfaceVariant = CanvasLight,
    onSurfaceVariant = InkSecondaryLight,
    surfaceTint = Navy,
    surfaceBright = SurfaceWhite,
    surfaceDim = Color(0xFFE3E8EF),
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = Color(0xFFFAFBFC),
    surfaceContainer = Color(0xFFF2F5F9),
    surfaceContainerHigh = SurfaceWhite,
    surfaceContainerHighest = Color(0xFFE9EEF4),
    inverseSurface = InkLight,
    inverseOnSurface = CanvasLight,
    outline = InputOutlineLight,
    outlineVariant = OutlineSoft,
    error = ErrorTextLight,
    onError = SurfaceWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorTextLight,
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = NavyDarkPrimary,
    onPrimary = NavyDarkOnPrimary,
    primaryContainer = NavyDarkContainer,
    onPrimaryContainer = NavyDarkOnContainer,
    inversePrimary = Navy,
    secondary = AmberTextDark,
    onSecondary = Color(0xFF2E1F00),
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = AmberTextDark,
    tertiary = SuccessTextDark,
    onTertiary = Color(0xFF00391B),
    tertiaryContainer = SuccessContainerDark,
    onTertiaryContainer = SuccessTextDark,
    background = CanvasDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceRaisedDark,
    onSurfaceVariant = InkSecondaryDark,
    surfaceTint = NavyDarkPrimary,
    surfaceBright = Color(0xFF2A3442),
    surfaceDim = CanvasDark,
    surfaceContainerLowest = Color(0xFF0A0F15),
    surfaceContainerLow = Color(0xFF131A24),
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceRaisedDark,
    surfaceContainerHighest = Color(0xFF253042),
    inverseSurface = InkDark,
    inverseOnSurface = CanvasDark,
    outline = InputOutlineDark,
    outlineVariant = OutlineSoftDark,
    error = ErrorTextDark,
    onError = Color(0xFF5C1510),
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorTextDark,
    scrim = Color(0xFF000000)
)

val UniRutaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val LocalUniRutaColors = staticCompositionLocalOf { LightExtended }

object UniRutaTheme {
    val colors: UniRutaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalUniRutaColors.current
}

@Composable
fun UniRutaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalUniRutaColors provides if (darkTheme) DarkExtended else LightExtended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = UniRutaShapes,
            content = content
        )
    }
}
