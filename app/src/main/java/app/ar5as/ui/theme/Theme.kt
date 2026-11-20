package app.ar5as.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Ar5as v0.3 palette — strict 60/30/10 rule (approved modern preview):
//   60% Cloud white — backgrounds + cards
//   30% Cobalt      — the working color (headers, buttons, nav, cheapest price)
//   10% Sunshine    — accents ONLY (discount badges, savings pill, promo strips)
// The old green theme is gone: cheapest price is now cobalt, savings sunshine.
// Always LIGHT, like the web preview — the phone's dark mode is ignored.
val Ar5asCloud = Color(0xFFF6F8FB)
val Ar5asCobalt = Color(0xFF0047AB)
val Ar5asCobaltDark = Color(0xFF003A8C)
val Ar5asCobaltLight = Color(0xFF2F6FD0)
val Ar5asCobaltTint = Color(0xFFE3EDFB)
val Ar5asCobaltBar = Color(0xFFC7D6EF)
val Ar5asSunshine = Color(0xFFFFC21F)
val Ar5asSunshineDark = Color(0xFFB98A00)
val Ar5asInk = Color(0xFF101828)
val Ar5asInkSoft = Color(0xFF475467)

private val LightColors = lightColorScheme(
    primary = Ar5asCobalt,
    onPrimary = Color.White,
    secondary = Ar5asCobaltLight,
    onSecondary = Color.White,
    tertiary = Ar5asSunshine,
    onTertiary = Ar5asInk,
    background = Ar5asCloud,
    onBackground = Ar5asInk,
    surface = Color.White,
    onSurface = Ar5asInk,
    surfaceVariant = Color(0xFFEAF0F8),
    onSurfaceVariant = Ar5asInkSoft,
    outline = Color(0xFFD0D9E6)
)

@Composable
fun Ar5asTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // v0.3: always the light 60/30/10 scheme, whatever the phone's mode is.
    MaterialTheme(colorScheme = LightColors, content = content)
}
