package app.ar5as.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Ar5as v0.1 palette: white + green (saving / fresh), amber for promo badges.
// Follows the phone's light / dark mode.
val Ar5asGreen = Color(0xFF1B7A43)
val Ar5asGreenDark = Color(0xFF145C33)
val Ar5asGreenLight = Color(0xFF2FA866)
val Ar5asAmber = Color(0xFFFFB300)
val Ar5asAmberDark = Color(0xFFB27A00)

private val LightColors = lightColorScheme(
    primary = Ar5asGreen,
    onPrimary = Color.White,
    secondary = Ar5asGreenLight,
    onSecondary = Color.White,
    tertiary = Ar5asAmberDark,
    background = Color(0xFFF6F8F6),
    onBackground = Color(0xFF14201A),
    surface = Color.White,
    onSurface = Color(0xFF14201A),
    surfaceVariant = Color(0xFFE8F2EC),
    onSurfaceVariant = Color(0xFF4A5A50),
    outline = Color(0xFFCBD8CF)
)

private val DarkColors = darkColorScheme(
    primary = Ar5asGreenLight,
    onPrimary = Color(0xFF062B16),
    secondary = Ar5asGreenLight,
    onSecondary = Color(0xFF062B16),
    tertiary = Ar5asAmber,
    background = Color(0xFF0B1A11),
    onBackground = Color(0xFFF0F5F1),
    surface = Color(0xFF142B1C),
    onSurface = Color(0xFFF0F5F1),
    surfaceVariant = Color(0xFF1E3A28),
    onSurfaceVariant = Color(0xFFB9C9BE),
    outline = Color(0xFF33503F)
)

@Composable
fun Ar5asTheme(
    // v0.2: always LIGHT, like the web preview — ignore the phone's dark mode.
    // The dark palette stays defined below for a later version.
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
