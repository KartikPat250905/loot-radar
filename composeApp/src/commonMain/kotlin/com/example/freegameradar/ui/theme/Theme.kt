package com.example.freegameradar.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ---------- Dark Palette ----------
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),        // vivid accent purple
    onPrimary = Color.Black,
    secondary = Color(0xFF03DAC6),      // teal accent
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFC107),       // amber accent
    onTertiary = Color.Black,
    background = Color(0xFF121212),     // modern dark background
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),        // slightly lighter than background
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

// ---------- Light Palette (optional) ----------
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFC107),
    onTertiary = Color.Black,
    background = Color(0xFFF2F2F2),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White
)

// ---------- Typography ----------
private val ModernTypography = Typography(
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        color = Color.White
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        color = Color.White
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        color = Color.White,
        letterSpacing = 0.15.sp
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        color = Color.White
    )
)


// ---------- Theme Composable ----------
@Composable
fun ModernDarkTheme(
    darkTheme: Boolean = true,      // default to dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ModernTypography,
        content = content
    )
}
