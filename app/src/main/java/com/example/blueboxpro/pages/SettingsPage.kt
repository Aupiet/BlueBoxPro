/**
 * This page allows the user to configure general application settings,
 * such as theme, unit system, and language.
 * Changes are propagated back to the MainActivity via callbacks.
 */
package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.R

/**
 * The settings screen (Page 4).
 * 
 * Provides UI controls for global app preferences.
 * 
 * @param isDarkMode Whether the dark theme is currently enabled.
 * @param onDarkModeChange Callback to toggle dark mode.
 * @param unitSystem The current unit system display string.
 * @param onUnitSystemChange Callback when a new unit system is selected.
 * @param language The current language key.
 * @param onLanguageChange Callback when a new language is selected.
 * @param onNavigateToAdvancedSettings Callback to open the advanced settings screen.
 */
@Composable
fun SettingsPage(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    unitSystem: String,
    onUnitSystemChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    onNavigateToAdvancedSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(PADDING_MEDIUM)
    ) {
        // Dark Mode Toggle
        ListItem(
            headlineContent = { Text(stringResource(R.string.dark_mode)) },
            trailingContent = {
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeChange
                )
            }
        )
        
        HorizontalDivider()

        // Unit System Selection
        Text(
            text = stringResource(R.string.unit_system_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = SPACING_MEDIUM, bottom = SPACING_SMALL)
        )
        
        val unitOptions = listOf(
            stringResource(R.string.unit_metric_kmh),
            stringResource(R.string.unit_metric_ms),
            stringResource(R.string.unit_imperial),
            stringResource(R.string.unit_nautical)
        )
        
        Column(Modifier.selectableGroup()) {
            unitOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(OPTION_HEIGHT)
                        .selectable(
                            selected = (text == unitSystem),
                            onClick = { onUnitSystemChange(text) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = PADDING_MEDIUM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == unitSystem),
                        onClick = null 
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = PADDING_MEDIUM)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = SPACING_SMALL))

        // Language Selection
        Text(
            text = stringResource(R.string.language_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = SPACING_SMALL, bottom = SPACING_SMALL)
        )
        
        val langOptionsMap = mapOf(
            LANG_KEY_FRENCH to stringResource(R.string.lang_french),
            LANG_KEY_ENGLISH to stringResource(R.string.lang_english)
        )
        
        Column(Modifier.selectableGroup()) {
            langOptionsMap.forEach { (key, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(OPTION_HEIGHT)
                        .selectable(
                            selected = (key == language),
                            onClick = { onLanguageChange(key) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = PADDING_MEDIUM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (key == language),
                        onClick = null
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = PADDING_MEDIUM)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SPACING_XLARGE))

        // Advanced Settings Navigation
        Button(
            onClick = onNavigateToAdvancedSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.advanced_settings))
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = stringResource(R.string.version_pre_alpha),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = SPACING_SMALL)
        )
    }
}

private val PADDING_MEDIUM = 16.dp
private val SPACING_SMALL = 8.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_LARGE = 24.dp
private val SPACING_XLARGE = 32.dp
private val OPTION_HEIGHT = 48.dp

private const val LANG_KEY_FRENCH = "Français"
private const val LANG_KEY_ENGLISH = "English"
