/**
 * Main entry point of the BlueBoxPro application.
 * This activity sets up the navigation, theme, and global sensor listeners.
 */
package com.example.blueboxpro

import android.Manifest
import android.content.res.Configuration as AndroidConfiguration
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.blueboxpro.Process.CaptorListener
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.Process.WeatherData
import com.example.blueboxpro.Process.WeatherManager
import com.example.blueboxpro.Save.SessionManager
import com.example.blueboxpro.pages.*
import com.example.blueboxpro.ui.theme.BlueBoxProTheme
import com.example.blueboxpro.ui.theme.ThemePalette
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

class MainActivity : AppCompatActivity() {
    companion object {
        private const val ROUTE_MAIN = "main"
        private const val ROUTE_FULL_MAP = "full_map"
        private const val ROUTE_ADVANCED_SETTINGS = "advanced_settings"
        private const val ROUTE_SESSION_DETAIL_BASE = "session_detail"
        private const val ARG_SESSION_ID = "sessionId"
        private const val ROUTE_SESSION_DETAIL = "$ROUTE_SESSION_DETAIL_BASE/{$ARG_SESSION_ID}"
        private const val INITIAL_REFRESH_TRIGGER = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Option.load(this)
        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))
        SessionManager.loadSessions(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val context = LocalContext.current
            var isDarkMode by rememberSaveable { mutableStateOf(Option.UI.isDarkMode) }
            var themePaletteKey by rememberSaveable { mutableStateOf(Option.UI.themePalette) }
            var language by rememberSaveable { mutableStateOf(Option.UI.language) }
            
            var unitSpeed by rememberSaveable { mutableStateOf(Option.UI.unitSpeed) }
            var unitDistance by rememberSaveable { mutableStateOf(Option.UI.unitDistance) }
            var unitAltitude by rememberSaveable { mutableStateOf(Option.UI.unitAltitude) }
            var unitAngle by rememberSaveable { mutableStateOf(Option.UI.unitAngle) }

            val processor = remember { MovementProcessor() }
            var lastLocationState by remember { mutableStateOf<GeoPoint?>(null) }
            var refreshTrigger by remember { mutableStateOf(INITIAL_REFRESH_TRIGGER) }
            
            // Global Weather State
            var weatherData by remember { mutableStateOf<WeatherData?>(null) }

            val captorListener = remember {
                CaptorListener(context, processor) {
                    lastLocationState = processor.lastLocation
                    refreshTrigger++
                }
            }

            LaunchedEffect(refreshTrigger) {
                SessionManager.updateRecording(processor)
            }
            
            // Fetch weather when location changes
            LaunchedEffect(lastLocationState) {
                lastLocationState?.let { geoPoint ->
                    if (weatherData == null) { // Fetch once or update occasionally
                        weatherData = WeatherManager.fetchWeather(geoPoint.latitude, geoPoint.longitude)
                    }
                }
            }

            DisposableEffect(Unit) {
                captorListener.start()
                onDispose { captorListener.stop() }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { }

            LaunchedEffect(Unit) {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }

            val currentPalette = try { ThemePalette.valueOf(themePaletteKey) } catch(e: Exception) { ThemePalette.OCEAN }

            BlueBoxProTheme(darkTheme = isDarkMode, palette = currentPalette) {
                val rootNavController = rememberNavController()
                NavHost(navController = rootNavController, startDestination = ROUTE_MAIN) {
                    composable(ROUTE_MAIN) {
                        MainScreen(
                            processor = processor,
                            lastLocationState = lastLocationState,
                            weatherData = weatherData,
                            refreshTrigger = refreshTrigger,
                            unitSpeed = unitSpeed,
                            unitDistance = unitDistance,
                            unitAltitude = unitAltitude,
                            unitAngle = unitAngle,
                            language = language,
                            isDarkMode = isDarkMode,
                            themePalette = themePaletteKey,
                            onDarkModeChange = { 
                                isDarkMode = it
                                Option.UI.isDarkMode = it
                                Option.save(this@MainActivity) 
                            },
                            onThemePaletteChange = {
                                themePaletteKey = it
                                Option.UI.themePalette = it
                                Option.save(this@MainActivity)
                            },
                            onUnitSpeedChange = { newUnit ->
                                unitSpeed = newUnit
                                Option.UI.unitSpeed = newUnit
                                Option.UI.unitSystem = when(newUnit) {
                                    "km/h" -> "METRIC_KMH"
                                    "m/s" -> "METRIC_MS"
                                    "mph" -> "IMPERIAL"
                                    "kn" -> "NAUTICAL"
                                    else -> "METRIC_KMH"
                                }
                                Option.save(this@MainActivity) 
                            },
                            onUnitDistanceChange = { unitDistance = it; Option.UI.unitDistance = it; Option.save(this@MainActivity) },
                            onUnitAltitudeChange = { unitAltitude = it; Option.UI.unitAltitude = it; Option.save(this@MainActivity) },
                            onUnitAngleChange = { unitAngle = it; Option.UI.unitAngle = it; Option.save(this@MainActivity) },
                            onLanguageChange = { newLang ->
                                language = newLang
                                Option.UI.language = newLang
                                Option.save(this@MainActivity)
                                val tag = if (newLang == Option.App.LANG_NAME_FR) Option.App.LANG_FR else Option.App.LANG_EN
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                            },
                            onOpenFullScreenMap = { rootNavController.navigate(ROUTE_FULL_MAP) },
                            onNavigateToAdvancedSettings = { rootNavController.navigate(ROUTE_ADVANCED_SETTINGS) },
                            onNavigateToSessionDetail = { sessionId -> rootNavController.navigate("$ROUTE_SESSION_DETAIL_BASE/$sessionId") }
                        )
                    }
                    composable(ROUTE_FULL_MAP) {
                        Page4(
                            location = lastLocationState,
                            processor = processor,
                            weatherData = weatherData,
                            unitSystem = Option.UI.unitSystem,
                            onBack = { rootNavController.popBackStack() }
                        )
                    }
                    composable(ROUTE_ADVANCED_SETTINGS) {
                        AdvancedSettingsPage(processor = processor, refreshTrigger = refreshTrigger, unitSystem = Option.UI.unitSystem, onBack = { rootNavController.popBackStack() })
                    }
                    composable(ROUTE_SESSION_DETAIL, arguments = listOf(navArgument(ARG_SESSION_ID) { type = NavType.IntType })) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getInt(ARG_SESSION_ID)
                        val session = SessionManager.sessions.find { it.id == sessionId }
                        SessionDetailPage(session = session, onBack = { rootNavController.popBackStack() })
                    }
                }
            }
        }
    }

    override fun onStop() { super.onStop(); SessionManager.saveSessions(this); Option.save(this) }
    override fun onDestroy() { super.onDestroy(); SessionManager.saveSessions(this); Option.save(this) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    processor: MovementProcessor,
    lastLocationState: GeoPoint?,
    weatherData: WeatherData?,
    refreshTrigger: Int,
    unitSpeed: String,
    unitDistance: String,
    unitAltitude: String,
    unitAngle: String,
    language: String,
    isDarkMode: Boolean,
    themePalette: String,
    onDarkModeChange: (Boolean) -> Unit,
    onThemePaletteChange: (String) -> Unit,
    onUnitSpeedChange: (String) -> Unit,
    onUnitDistanceChange: (String) -> Unit,
    onUnitAltitudeChange: (String) -> Unit,
    onUnitAngleChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onOpenFullScreenMap: () -> Unit,
    onNavigateToAdvancedSettings: () -> Unit,
    onNavigateToSessionDetail: (Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == AndroidConfiguration.ORIENTATION_LANDSCAPE
    val pagerState = rememberPagerState(pageCount = { MainScreenConstants.PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val isRecording = SessionManager.activeRecording != null
    
    val tabs = listOf(
        TabItem(Icons.Default.Home, R.string.tab_home), 
        TabItem(Icons.Default.LocationOn, R.string.tab_map), 
        TabItem(Icons.Default.PlayArrow, R.string.tab_sessions), 
        TabItem(Icons.Default.Settings, R.string.tab_settings)
    )

    val onTabSelected: (Int) -> Unit = { index ->
        scope.launch { pagerState.animateScrollToPage(index) }
    }

    val topBar = @Composable {
        TopAppBar(
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info, 
                        contentDescription = "Logo",
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(stringResource(tabs[pagerState.currentPage].labelRes)) 
                }
            }
        )
    }

    val pagerContent = @Composable { padding: PaddingValues ->
        HorizontalPager(
            state = pagerState, 
            modifier = Modifier.padding(padding).fillMaxSize(),
            userScrollEnabled = false
        ) { pageIndex ->
            when (pageIndex) {
                0 -> Page1(processor, refreshTrigger, Option.UI.unitSystem, weatherData, { onTabSelected(1) }, { onTabSelected(3) })
                1 -> Page2(lastLocationState, processor, refreshTrigger, Option.UI.unitSystem, weatherData, onOpenFullScreenMap)
                2 -> Page3(processor, refreshTrigger, onNavigateToSessionDetail)
                3 -> SettingsPage(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    themePalette = themePalette,
                    onThemePaletteChange = onThemePaletteChange,
                    unitSpeed = unitSpeed,
                    onUnitSpeedChange = onUnitSpeedChange,
                    unitDistance = unitDistance,
                    onUnitDistanceChange = onUnitDistanceChange,
                    unitAltitude = unitAltitude,
                    onUnitAltitudeChange = onUnitAltitudeChange,
                    unitAngle = unitAngle,
                    onUnitAngleChange = onUnitAngleChange,
                    language = language,
                    onLanguageChange = onLanguageChange,
                    onNavigateToAdvancedSettings = onNavigateToAdvancedSettings
                )
            }
        }
    }

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = topBar
            ) { innerPadding ->
                pagerContent(innerPadding)
            }

            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                header = {
                    // your header (currently empty)
                }
            ) {
                // Add a spacer above the items to push them down to the center
                Spacer(Modifier.weight(1f))

                tabs.forEachIndexed { index, tab ->
                    val isSessionTab = index == MainScreenConstants.SESSION_TAB_INDEX
                    NavigationRailItem(
                        selected = pagerState.currentPage == index,
                        onClick = { onTabSelected(index) },
                        icon = {
                            if (isSessionTab && isRecording) {
                                Surface(
                                    modifier = Modifier
                                        .size(MainScreenConstants.ICON_SIZE_ACTIVE.dp)
                                        .clip(RoundedCornerShape(MainScreenConstants.ICON_CORNER_RADIUS.dp)),
                                    color = Color.Red
                                ) { }
                            } else {
                                Icon(
                                    tab.icon,
                                    contentDescription = stringResource(tab.labelRes),
                                    modifier = Modifier.size(MainScreenConstants.ICON_SIZE_DEFAULT.dp)
                                )
                            }
                        },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }

                // Add a spacer below the items to balance the layout
                Spacer(Modifier.weight(1f))
            }

        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        val isSessionTab = index == MainScreenConstants.SESSION_TAB_INDEX
                        NavigationBarItem(
                            selected = pagerState.currentPage == index,
                            onClick = { onTabSelected(index) },
                            icon = { 
                                if (isSessionTab && isRecording) {
                                    Surface(
                                        modifier = Modifier
                                            .size(MainScreenConstants.ICON_SIZE_ACTIVE.dp)
                                            .clip(RoundedCornerShape(MainScreenConstants.ICON_CORNER_RADIUS.dp)),
                                        color = Color.Red
                                    ) { }
                                } else {
                                    Icon(tab.icon, contentDescription = stringResource(tab.labelRes), modifier = Modifier.size(MainScreenConstants.ICON_SIZE_DEFAULT.dp))
                                }
                            },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            pagerContent(innerPadding)
        }
    }
}

private object MainScreenConstants {
    const val PAGE_COUNT = 4
    const val SESSION_TAB_INDEX = 2
    const val ICON_SIZE_ACTIVE = 24
    const val ICON_SIZE_DEFAULT = 28
    const val ICON_CORNER_RADIUS = 4
    const val LOGO_SIZE = 40
}

data class TabItem(val icon: ImageVector, val labelRes: Int)
