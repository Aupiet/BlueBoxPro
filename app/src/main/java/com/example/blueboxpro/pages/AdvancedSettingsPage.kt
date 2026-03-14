/**
 * This page provides advanced configuration options for sensor thresholds, 
 * filtering parameters, and system-level settings.
 * It allows direct modification of the global Option singleton.
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.blueboxpro.Option
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.R

/**
 * The Advanced Settings screen.
 * 
 * Provides granular control over the application's processing logic.
 * 
 * @param processor The movement processor instance (used for reset action).
 * @param refreshTrigger Trigger used to fetch latest snapshot of processed data.
 * @param unitSystem The current unit system.
 * @param onBack Callback for back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsPage(
    processor: MovementProcessor,
    refreshTrigger: Int,
    unitSystem: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val result = remember(refreshTrigger) { processor.getResult(unitSystem) }

    // State for all configurable variables (bound to UI text fields)
    var gpsTimeout by remember { mutableStateOf(Option.Process.GPS_TIMEOUT_MS.toString()) }
    var minGpsAccuracy by remember { mutableStateOf(Option.Process.MIN_GPS_ACCURACY.toString()) }
    var maxAcceptableAccuracy by remember { mutableStateOf(Option.Process.MAX_ACCEPTABLE_ACCURACY.toString()) }
    var azimuthAlpha by remember { mutableStateOf(Option.Process.AZIMUTH_ALPHA.toString()) }
    var speedHistorySize by remember { mutableStateOf(Option.Process.SPEED_HISTORY_SIZE.toString()) }
    var lpfAccelAlpha by remember { mutableStateOf(Option.Process.LPF_ACCEL_ALPHA.toString()) }
    var zuptSpeedThreshold by remember { mutableStateOf(Option.Process.ZUPT_SPEED_THRESHOLD.toString()) }
    var maxDtBackground by remember { mutableStateOf(Option.Process.MAX_DT_BACKGROUND.toString()) }
    
    var roundingFactor by remember { mutableStateOf(Option.Movement.ROUNDING_FACTOR.toString()) }
    
    var fileName by remember { mutableStateOf(Option.Save.FILE_NAME) }
    var distanceThreshold by remember { mutableStateOf(Option.Save.DISTANCE_THRESHOLD_METERS.toString()) }
    var recordingFrequency by remember { mutableStateOf(Option.Save.RECORDING_FREQUENCY_HZ.toString()) }
    var maxLogicalSpeed by remember { mutableStateOf(Option.Save.MAX_LOGICAL_SPEED_KMH.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.advanced_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.content_desc_back)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        try {
                            // Update global options
                            Option.Process.GPS_TIMEOUT_MS = gpsTimeout.toLong()
                            Option.Process.MIN_GPS_ACCURACY = minGpsAccuracy.toFloat()
                            Option.Process.MAX_ACCEPTABLE_ACCURACY = maxAcceptableAccuracy.toFloat()
                            Option.Process.AZIMUTH_ALPHA = azimuthAlpha.toFloat()
                            Option.Process.SPEED_HISTORY_SIZE = speedHistorySize.toInt()
                            Option.Process.LPF_ACCEL_ALPHA = lpfAccelAlpha.toFloat()
                            Option.Process.ZUPT_SPEED_THRESHOLD = zuptSpeedThreshold.toFloat()
                            Option.Process.MAX_DT_BACKGROUND = maxDtBackground.toFloat()
                            
                            Option.Movement.ROUNDING_FACTOR = roundingFactor.toFloat()
                            
                            Option.Save.FILE_NAME = fileName
                            Option.Save.DISTANCE_THRESHOLD_METERS = distanceThreshold.toDouble()
                            Option.Save.RECORDING_FREQUENCY_HZ = recordingFrequency.toFloat()
                            Option.Save.MAX_LOGICAL_SPEED_KMH = maxLogicalSpeed.toDouble()
                            
                            // Persist to storage
                            Option.save(context)
                            onBack()
                        } catch (e: Exception) {
                            // Invalid input handling could be added here
                        }
                    }) {
                        Text(stringResource(R.string.button_save), color = MaterialTheme.colorScheme.primary)
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
            // --- Maintenance ---
            SectionHeader(stringResource(R.string.maintenance_actions))
            Button(
                onClick = { processor.reset() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.reset_button))
            }
            Text(
                text = stringResource(R.string.reset_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = SPACING_MEDIUM))

            // --- Real-time debug info ---
            SectionHeader(stringResource(R.string.header_realtime_tech))
            TechnicalInfoRow(stringResource(R.string.label_filtered_avg), "${SOG_FORMAT.format(result.getMoyspeed())} ${result.getSpeedUnit()}")
            TechnicalInfoRow(stringResource(R.string.label_gps_source), "${SOG_FORMAT.format(result.getSpeedGPS())} ${result.getSpeedUnit()}")
            TechnicalInfoRow(stringResource(R.string.label_imu_source), "${SOG_FORMAT.format(result.getSpeedIMU())} ${result.getSpeedUnit()}")
            TechnicalInfoRow(stringResource(R.string.accuracy_label), "${SOG_FORMAT.format(result.getAccuracy())} ${result.getAccuracyUnit()}")

            HorizontalDivider(modifier = Modifier.padding(vertical = SPACING_MEDIUM))

            // --- GPS & Processing Params ---
            SectionHeader(stringResource(R.string.header_processing_gps))
            SettingField(
                label = stringResource(R.string.label_gps_timeout),
                value = gpsTimeout,
                onValueChange = { gpsTimeout = it },
                description = stringResource(R.string.desc_gps_timeout)
            )
            SettingField(
                label = stringResource(R.string.label_min_gps_accuracy),
                value = minGpsAccuracy,
                onValueChange = { minGpsAccuracy = it },
                description = stringResource(R.string.desc_min_gps_accuracy)
            )
            SettingField(
                label = stringResource(R.string.label_azimuth_alpha),
                value = azimuthAlpha,
                onValueChange = { azimuthAlpha = it },
                description = stringResource(R.string.desc_azimuth_alpha)
            )
            SettingField(
                label = stringResource(R.string.label_speed_hist_size),
                value = speedHistorySize,
                onValueChange = { speedHistorySize = it },
                description = stringResource(R.string.desc_speed_hist_size),
                isInteger = true
            )
            SettingField(
                label = stringResource(R.string.label_zupt_speed),
                value = zuptSpeedThreshold,
                onValueChange = { zuptSpeedThreshold = it },
                description = stringResource(R.string.desc_zupt_speed)
            )
            SettingField(
                label = stringResource(R.string.label_max_dt),
                value = maxDtBackground,
                onValueChange = { maxDtBackground = it },
                description = stringResource(R.string.desc_max_dt)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = SPACING_MEDIUM))

            // --- Calculation Params ---
            SectionHeader(stringResource(R.string.header_calc_display))
            SettingField(
                label = stringResource(R.string.label_rounding_factor),
                value = roundingFactor,
                onValueChange = { roundingFactor = it },
                description = stringResource(R.string.desc_rounding_factor)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = SPACING_MEDIUM))

            // --- Storage Params ---
            SectionHeader(stringResource(R.string.header_save_files))
            SettingField(
                label = stringResource(R.string.label_json_filename),
                value = fileName,
                onValueChange = { fileName = it },
                description = stringResource(R.string.desc_json_filename),
                keyboardType = KeyboardType.Text
            )
            SettingField(
                label = stringResource(R.string.label_dist_threshold),
                value = distanceThreshold,
                onValueChange = { distanceThreshold = it },
                description = stringResource(R.string.desc_dist_threshold)
            )
            SettingField(
                label = stringResource(R.string.label_recording_freq),
                value = recordingFrequency,
                onValueChange = { recordingFrequency = it },
                description = stringResource(R.string.desc_recording_freq)
            )
            SettingField(
                label = stringResource(R.string.label_max_logical_speed),
                value = maxLogicalSpeed,
                onValueChange = { maxLogicalSpeed = it },
                description = stringResource(R.string.desc_max_logical_speed)
            )
            
            Spacer(modifier = Modifier.height(SPACING_LARGE))
        }
    }
}

/**
 * Renders a bold section header.
 */
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

/**
 * Displays a technical key-value pair.
 */
@Composable
private fun TechnicalInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

/**
 * A specialized text field for settings input with a label and help description.
 */
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
private const val SOG_FORMAT = "%.2f"
