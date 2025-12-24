package com.example.blueboxpro.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.ui.components.MapComponents
import org.osmdroid.util.GeoPoint

@Composable
fun Page1(
    processor: MovementProcessor,
    refreshTrigger: Int,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    key(refreshTrigger) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Analyse de mouvement (Page 1)", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Accélération (m/s²):", style = MaterialTheme.typography.titleMedium)
            Text(text = "X: ${"%.2f".format(processor.accelX)} | Y: ${"%.2f".format(processor.accelY)} | Z: ${"%.2f".format(processor.accelZ)}")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = "Vitesse (m/s):", style = MaterialTheme.typography.titleMedium)
            Text(text = "IMU: ${"%.2f".format(processor.speedIMU)}")
            Text(text = "GPS: ${"%.2f".format(processor.speedGPS)}")
            Text(text = "Fusionnée: ${"%.2f".format(processor.speedFused)}", color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(onClick = { processor.reset() }) {
                Text("Réinitialiser")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onNavigateToMap, modifier = Modifier.fillMaxWidth()) {
                Text("Aller à la Carte (Page 2)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Paramètres")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Page2(
    location: GeoPoint?, 
    processor: MovementProcessor, 
    onBack: () -> Unit,
    onOpenFullScreenMap: () -> Unit
) {
    MapComponents.Page2Layout(
        location = location,
        processor = processor,
        onBack = onBack,
        onMapClick = onOpenFullScreenMap
    )
}

@Composable
fun Page4(location: GeoPoint?, onBack: () -> Unit) {
    // Variable pour forcer le recentrage quand on clique sur le bouton
    var recenterTrigger by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Carte interactive plein écran
        // On utilise le trigger pour forcer le recentrage manuel
        key(recenterTrigger) {
            MapComponents.MapContainer(
                location = location,
                modifier = Modifier.fillMaxSize(),
                isLocked = false,
                autoCenter = (recenterTrigger == 0) // Auto au début, puis manuel
            )
        }
        
        // Bouton de retour (en bas à gauche)
        FloatingActionButton(
            onClick = onBack,
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomStart),
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
        }

        // Bouton de recentrage (en bas à droite)
        FloatingActionButton(
            onClick = { recenterTrigger++ },
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Recentrer")
        }
    }
}

@Composable
fun SettingsPage(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    unitSystem: String,
    onUnitSystemChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Paramètres", 
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        ListItem(
            headlineContent = { Text("Mode Sombre") },
            trailingContent = {
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeChange
                )
            }
        )
        
        HorizontalDivider()

        Text(
            text = "Unités de mesure",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        val unitOptions = listOf("Métrique (m/s, km/h)", "Impérial (mph)")
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
            text = "Langue",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        val langOptions = listOf("Français", "English")
        Column(Modifier.selectableGroup()) {
            langOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .selectable(
                            selected = (text == language),
                            onClick = { onLanguageChange(text) },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (text == language),
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

        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Version Pre Alpha",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
        )
    }
}
