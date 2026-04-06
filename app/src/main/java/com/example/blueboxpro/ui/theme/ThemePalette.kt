package com.example.blueboxpro.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemePalette(val displayName: String) {
    OCEAN("Ocean Blue"),
    SUNSET("Sunset Orange"),
    FOREST("Forest Green");

    fun getLightColorScheme(): ColorScheme {
        return when (this) {
            OCEAN -> lightColorScheme(
                primary = OceanPrimaryLight,
                secondary = OceanSecondaryLight,
                tertiary = OceanTertiaryLight,
                background = OceanBackgroundLight,
                surface = OceanSurfaceLight,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onTertiary = Color.White,
                onBackground = OceanOnSurfaceLight,
                onSurface = OceanOnSurfaceLight,
                primaryContainer = NautixBlueOcean.copy(alpha = 0.1f),
                secondaryContainer = NautixBlueAzure.copy(alpha = 0.15f),
                tertiaryContainer = NautixOrangeCoral.copy(alpha = 0.1f),
                onPrimaryContainer = OceanPrimaryLight,
                onSecondaryContainer = OceanSecondaryLight,
                onTertiaryContainer = OceanTertiaryLight,
                surfaceVariant = Color(0xFFF2F4F5) // Light gray variant
            )
            SUNSET -> lightColorScheme(
                primary = SunsetPrimaryLight,
                secondary = SunsetSecondaryLight,
                tertiary = SunsetTertiaryLight,
                background = SunsetBackgroundLight,
                surface = SunsetSurfaceLight,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onTertiary = Color.White,
                onBackground = SunsetOnBackgroundLight,
                onSurface = SunsetOnSurfaceLight
            )
            FOREST -> lightColorScheme(
                primary = ForestPrimaryLight,
                secondary = ForestSecondaryLight,
                tertiary = ForestTertiaryLight,
                background = ForestBackgroundLight,
                surface = ForestSurfaceLight,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onTertiary = Color.White,
                onBackground = ForestOnBackgroundLight,
                onSurface = ForestOnSurfaceLight
            )
        }
    }

    fun getDarkColorScheme(): ColorScheme {
        return when (this) {
            OCEAN -> darkColorScheme(
                primary = OceanPrimaryDark,
                secondary = OceanSecondaryDark,
                tertiary = OceanTertiaryDark,
                background = OceanBackgroundDark,
                surface = OceanSurfaceDark,
                onPrimary = OceanOnPrimaryDark,
                onSecondary = OceanOnSecondaryDark,
                onTertiary = OceanOnTertiaryDark,
                onBackground = OceanOnBackgroundDark,
                onSurface = OceanOnSurfaceDark,
                primaryContainer = NautixBlueOcean,
                secondaryContainer = NautixBlueAzure.copy(alpha = 0.3f),
                tertiaryContainer = NautixOrangeCoral.copy(alpha = 0.3f),
                onPrimaryContainer = Color.White,
                onSecondaryContainer = Color.White,
                onTertiaryContainer = Color.White
            )
            SUNSET -> darkColorScheme(
                primary = SunsetPrimaryDark,
                secondary = SunsetSecondaryDark,
                tertiary = SunsetTertiaryDark,
                background = SunsetBackgroundDark,
                surface = SunsetSurfaceDark,
                onPrimary = SunsetOnPrimaryDark,
                onSecondary = SunsetOnSecondaryDark,
                onTertiary = SunsetOnTertiaryDark,
                onBackground = SunsetOnBackgroundDark,
                onSurface = SunsetOnSurfaceDark
            )
            FOREST -> darkColorScheme(
                primary = ForestPrimaryDark,
                secondary = ForestSecondaryDark,
                tertiary = ForestTertiaryDark,
                background = ForestBackgroundDark,
                surface = ForestSurfaceDark,
                onPrimary = ForestOnPrimaryDark,
                onSecondary = ForestOnSecondaryDark,
                onTertiary = ForestOnTertiaryDark,
                onBackground = ForestOnBackgroundDark,
                onSurface = ForestOnSurfaceDark
            )
        }
    }
}
