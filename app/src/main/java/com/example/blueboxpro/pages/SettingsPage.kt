/**
 * This page allows the user to configure general application settings,
 * such as theme, unit systems for various metrics, and language.
 */
package com.example.blueboxpro.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.R
import com.example.blueboxpro.ui.components.SettingsCard
import com.example.blueboxpro.ui.components.SectionHeader
import com.example.blueboxpro.ui.theme.ThemePalette

/**
 * The reworked settings screen (Page 4).
 */
@Composable
fun SettingsPage(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    themePalette: String,
    onThemePaletteChange: (String) -> Unit,
    unitSpeed: String,
    onUnitSpeedChange: (String) -> Unit,
    unitDistance: String,
    onUnitDistanceChange: (String) -> Unit,
    unitAltitude: String,
    onUnitAltitudeChange: (String) -> Unit,
    unitAngle: String,
    onUnitAngleChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    onNavigateToAdvancedSettings: () -> Unit,
    @Suppress("UNUSED_PARAMETER") processor: MovementProcessor? = null
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(SETTINGS_PADDING)
    ) {
        // --- Appearance Section ---
        SectionHeader(stringResource(R.string.theme_section), modifier = Modifier.padding(top = 0.dp))
        SettingsCard {
            ListItem(
                headlineContent = { Text(stringResource(R.string.dark_mode)) },
                trailingContent = {
                    Switch(checked = isDarkMode, onCheckedChange = onDarkModeChange)
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            var showPaletteDialog by remember { mutableStateOf(false) }
            ListItem(
                modifier = Modifier.clickable { showPaletteDialog = true },
                headlineContent = { Text(stringResource(R.string.palette_label)) },
                supportingContent = { Text(themePalette) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            if (showPaletteDialog) {
                SettingsSelectionDialog(
                    title = stringResource(R.string.palette_label),
                    options = ThemePalette.entries.map { it.name },
                    selectedOption = themePalette,
                    onOptionSelected = { onThemePaletteChange(it); showPaletteDialog = false },
                    onDismiss = { showPaletteDialog = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(SETTINGS_SPACING))

        // --- Metrics Section ---
        SectionHeader(stringResource(R.string.metrics_section))
        SettingsCard {
            MetricSelectionRow(
                label = stringResource(R.string.unit_speed_label),
                currentValue = unitSpeed,
                options = listOf("km/h", "m/s", "mph", "kn"),
                onSelected = onUnitSpeedChange
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            MetricSelectionRow(
                label = stringResource(R.string.unit_distance_label),
                currentValue = unitDistance,
                options = listOf("km", "m", "mi", "nm"),
                onSelected = onUnitDistanceChange
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            MetricSelectionRow(
                label = stringResource(R.string.unit_altitude_label),
                currentValue = unitAltitude,
                options = listOf("m", "ft"),
                onSelected = onUnitAltitudeChange
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            MetricSelectionRow(
                label = stringResource(R.string.unit_angle_label),
                currentValue = unitAngle,
                options = listOf("°", "rad"),
                onSelected = onUnitAngleChange
            )
        }

        Spacer(modifier = Modifier.height(SETTINGS_SPACING))

        // --- Language Section ---
        SectionHeader(stringResource(R.string.language_title))
        SettingsCard {
            MetricSelectionRow(
                label = stringResource(R.string.language_title),
                currentValue = language,
                options = listOf("Français", "English"),
                onSelected = onLanguageChange
            )
        }

        Spacer(modifier = Modifier.height(SETTINGS_SPACING))

        // --- Advanced Settings Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToAdvancedSettings() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.advanced_settings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Expert options & Calibration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.version_pre_alpha),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun MetricSelectionRow(
    label: String,
    currentValue: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    ListItem(
        modifier = Modifier.clickable { showDialog = true },
        headlineContent = { Text(label) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )

    if (showDialog) {
        SettingsSelectionDialog(
            title = label,
            options = options,
            selectedOption = currentValue,
            onOptionSelected = { onSelected(it); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun SettingsSelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = null
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

private val SETTINGS_PADDING = 16.dp
private val SETTINGS_SPACING = 20.dp
