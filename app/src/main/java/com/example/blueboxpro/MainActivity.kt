package com.example.blueboxpro

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blueboxpro.Process.CaptorListener
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.pages.AdvancedSettingsPage
import com.example.blueboxpro.pages.Page1
import com.example.blueboxpro.pages.Page2
import com.example.blueboxpro.pages.Page3
import com.example.blueboxpro.pages.Page4
import com.example.blueboxpro.pages.SettingsPage
import com.example.blueboxpro.ui.theme.BlueBoxProTheme
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        // Configuration pour cacher uniquement la barre de navigation système
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // On cache la navigation bar mais on laisse la status bar (heure, batterie)
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            
            var isDarkMode by remember { mutableStateOf(false) }
            var unitSystem by remember { mutableStateOf("Métrique (m/s, km/h)") }
            var language by remember { mutableStateOf("Français") }

            val processor = remember { MovementProcessor() }
            var lastLocationState by remember { mutableStateOf<GeoPoint?>(null) }
            var refreshTrigger by remember { mutableStateOf(0) }

            val captorListener = remember {
                CaptorListener(context, processor) {
                    lastLocationState = processor.lastLocation
                    refreshTrigger++
                }
            }

            DisposableEffect(Unit) {
                captorListener.start()
                onDispose {
                    captorListener.stop()
                }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { }

            LaunchedEffect(Unit) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }

            BlueBoxProTheme(darkTheme = isDarkMode) {
                val rootNavController = rememberNavController()
                
                NavHost(navController = rootNavController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            processor = processor,
                            lastLocationState = lastLocationState,
                            refreshTrigger = refreshTrigger,
                            unitSystem = unitSystem,
                            language = language,
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { isDarkMode = it },
                            onUnitSystemChange = { unitSystem = it },
                            onLanguageChange = { language = it },
                            onOpenFullScreenMap = { rootNavController.navigate("full_map") },
                            onNavigateToAdvancedSettings = { rootNavController.navigate("advanced_settings") }
                        )
                    }
                    composable("full_map") {
                        Page4(
                            location = lastLocationState,
                            //refreshTrigger = refreshTrigger,
                            onBack = { rootNavController.popBackStack() }
                        )
                    }
                    composable("advanced_settings") {
                        AdvancedSettingsPage(
                            onBack = { rootNavController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    processor: MovementProcessor,
    lastLocationState: GeoPoint?,
    refreshTrigger: Int,
    unitSystem: String,
    language: String,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onUnitSystemChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onOpenFullScreenMap: () -> Unit,
    onNavigateToAdvancedSettings: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    
    val tabs = listOf(
        TabItem("Analyse", Icons.Default.Home),
        TabItem("Carte", Icons.Default.LocationOn),
        TabItem("Cours", Icons.Default.PlayArrow),
        TabItem("Réglages", Icons.Default.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        label = { Text(tab.title) },
                        icon = { Icon(tab.icon, contentDescription = null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            beyondViewportPageCount = 1
        ) { pageIndex ->
            when (pageIndex) {
                0 -> Page1(
                    processor = processor,
                    refreshTrigger = refreshTrigger,
                    unitSystem = unitSystem,
                    onNavigateToMap = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onNavigateToSettings = { scope.launch { pagerState.animateScrollToPage(3) } }
                )
                1 -> Page2(
                    location = lastLocationState,
                    processor = processor,
                    unitSystem = unitSystem,
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                    onOpenFullScreenMap = onOpenFullScreenMap
                )
                2 -> Page3()
                3 -> SettingsPage(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    unitSystem = unitSystem,
                    onUnitSystemChange = onUnitSystemChange,
                    language = language,
                    onLanguageChange = onLanguageChange,
                    onNavigateToAdvancedSettings = onNavigateToAdvancedSettings
                )
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)
