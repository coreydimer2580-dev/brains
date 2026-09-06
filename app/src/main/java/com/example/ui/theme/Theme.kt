package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrainPrimaryDark,
    onPrimary = BrainOnPrimaryDark,
    primaryContainer = BrainPrimaryContainerDark,
    onPrimaryContainer = BrainOnPrimaryContainerDark,
    secondary = BrainSecondaryDark,
    onSecondary = BrainOnSecondaryDark,
    secondaryContainer = BrainSecondaryContainerDark,
    onSecondaryContainer = BrainOnSecondaryContainerDark,
    tertiary = BrainTertiaryDark,
    onTertiary = BrainOnTertiaryDark,
    background = BrainBackgroundDark,
    surface = BrainSurfaceDark,
    surfaceVariant = BrainSurfaceVariantDark,
    outline = BrainOutlineDark,
    onSurface = BrainOnSurfaceDark,
    onSurfaceVariant = BrainOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrainPrimaryLight,
    onPrimary = BrainOnPrimaryLight,
    primaryContainer = BrainPrimaryContainerLight,
    onPrimaryContainer = BrainOnPrimaryContainerLight,
    secondary = BrainSecondaryLight,
    onSecondary = BrainOnSecondaryLight,
    secondaryContainer = BrainSecondaryContainerLight,
    onSecondaryContainer = BrainOnSecondaryContainerLight,
    tertiary = BrainTertiaryLight,
    onTertiary = BrainOnTertiaryLight,
    background = BrainBackgroundLight,
    surface = BrainSurfaceLight,
    surfaceVariant = BrainSurfaceVariantLight,
    outline = BrainOutlineLight,
    onSurface = BrainOnSurfaceLight,
    onSurfaceVariant = BrainOnSurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our custom neuroscience palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
