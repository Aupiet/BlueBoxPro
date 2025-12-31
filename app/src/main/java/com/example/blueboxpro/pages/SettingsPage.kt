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
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title), 
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

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

        Text(
            text = stringResource(R.string.unit_system_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
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
                        .height(48.dp)
                        .selectable(
                            selected = (text == unitSystem),
                            onClick = { onUnitSystemChange(text) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == unitSystem),
                        onClick = null 
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = stringResource(R.string.language_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        
        val langOptionsMap = mapOf(
            "Français" to stringResource(R.string.lang_french),
            "English" to stringResource(R.string.lang_english)
        )
        
        Column(Modifier.selectableGroup()) {
            langOptionsMap.forEach { (key, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .selectable(
                            selected = (key == language),
                            onClick = { onLanguageChange(key) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (key == language),
                        onClick = null
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
        )
    }
}
