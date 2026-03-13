/**
 * This page provides advanced configuration options for sensor thresholds and filtering parameters.
 */
package com.example.blueboxpro.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Option

/**
 * Composable for the advanced settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State for all configurable variables
    var gpsTimeout by remember { mutableStateOf(Option.Process.GPS_TIMEOUT_MS.toString()) }
    var minGpsAccuracy by remember { mutableStateOf(Option.Process.MIN_GPS_ACCURACY.toString()) }
    var maxAcceptableAccuracy by remember { mutableStateOf(Option.Process.MAX_ACCEPTABLE_ACCURACY.toString()) }
    var azimuthAlpha by remember { mutableStateOf(Option.Process.AZIMUTH_ALPHA.toString()) }
    var speedHistorySize by remember { mutableStateOf(Option.Process.SPEED_HISTORY_SIZE.toString()) }
    var lpfAccelAlpha by remember { mutableStateOf(Option.Process.LPF_ACCEL_ALPHA.toString()) }
    
    var roundingFactor by remember { mutableStateOf(Option.Movement.ROUNDING_FACTOR.toString()) }
    
    var fileName by remember { mutableStateOf(Option.Save.FILE_NAME) }
    var distanceThreshold by remember { mutableStateOf(Option.Save.DISTANCE_THRESHOLD_METERS.toString()) }
    var recordingFrequency by remember { mutableStateOf(Option.Save.RECORDING_FREQUENCY_HZ.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres Avancés") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // Apply and Save
                        try {
                            Option.Process.GPS_TIMEOUT_MS = gpsTimeout.toLong()
                            Option.Process.MIN_GPS_ACCURACY = minGpsAccuracy.toFloat()
                            Option.Process.MAX_ACCEPTABLE_ACCURACY = maxAcceptableAccuracy.toFloat()
                            Option.Process.AZIMUTH_ALPHA = azimuthAlpha.toFloat()
                            Option.Process.SPEED_HISTORY_SIZE = speedHistorySize.toInt()
                            Option.Process.LPF_ACCEL_ALPHA = lpfAccelAlpha.toFloat()
                            
                            Option.Movement.ROUNDING_FACTOR = roundingFactor.toFloat()
                            
                            Option.Save.FILE_NAME = fileName
                            Option.Save.DISTANCE_THRESHOLD_METERS = distanceThreshold.toDouble()
                            Option.Save.RECORDING_FREQUENCY_HZ = recordingFrequency.toFloat()
                            
                            Option.save(context)
                            onBack()
                        } catch (e: Exception) {
                            // Handle parsing error (could show a Toast)
                        }
                    }) {
                        Text("ENREGISTRER", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = PADDING_MEDIUM)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            SectionHeader("Traitement & GPS")
            SettingField(
                label = "Timeout GPS (ms)",
                value = gpsTimeout,
                onValueChange = { gpsTimeout = it },
                description = "Temps avant de considérer le signal GPS comme perdu (par défaut 5000ms)."
            )
            SettingField(
                label = "Précision GPS Min (m)",
                value = minGpsAccuracy,
                onValueChange = { minGpsAccuracy = it },
                description = "Seuil au-delà duquel les données GPS sont ignorées car trop imprécises."
            )
            SettingField(
                label = "Lissage Azimut (Alpha)",
                value = azimuthAlpha,
                onValueChange = { azimuthAlpha = it },
                description = "Facteur de lissage pour la boussole (0.0 à 1.0). Plus bas = plus stable mais plus lent."
            )
            SettingField(
                label = "Taille historique vitesse",
                value = speedHistorySize,
                onValueChange = { speedHistorySize = it },
                description = "Nombre de points utilisés pour calculer la vitesse moyenne en temps réel.",
                isInteger = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = SPACING_MEDIUM))

            SectionHeader("Calculs & Affichage")
            SettingField(
                label = "Facteur d'arrondi",
                value = roundingFactor,
                onValueChange = { roundingFactor = it },
                description = "10 = 1 décale, 100 = 2 décimales. Définit la précision de l'affichage."
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = SPACING_MEDIUM))

            SectionHeader("Sauvegarde & Fichiers")
            SettingField(
                label = "Nom du fichier JSON",
                value = fileName,
                onValueChange = { fileName = it },
                description = "Nom du fichier utilisé pour stocker l'historique des sessions.",
                keyboardType = KeyboardType.Text
            )
            SettingField(
                label = "Seuil de distance (m)",
                value = distanceThreshold,
                onValueChange = { distanceThreshold = it },
                description = "Distance minimale entre deux points pour qu'ils soient comptabilisés dans le trajet."
            )
            SettingField(
                label = "Fréquence d'enregistrement (Hz)",
                value = recordingFrequency,
                onValueChange = { recordingFrequency = it },
                description = "Nombre de points enregistrés par seconde (ex: 1.0 = 1 point/sec, 5.0 = 5 points/sec)."
            )
            
            Spacer(modifier = Modifier.height(SPACING_LARGE))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = SPACING_SMALL)
    )
}

@Composable
private fun SettingField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    description: String,
    isInteger: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    Column(modifier = Modifier.padding(vertical = SPACING_SMALL)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
    }
}

private val PADDING_MEDIUM = 16.dp
private val SPACING_SMALL = 8.dp
private val SPACING_MEDIUM = 16.dp
private val SPACING_LARGE = 24.dp
