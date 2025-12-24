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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blueboxpro.Process.CaptorListener
import com.example.blueboxpro.Process.MovementProcessor
import com.example.blueboxpro.pages.Page1
import com.example.blueboxpro.pages.Page2
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

        enableEdgeToEdge()
        setContent {
            // États des paramètres globaux
            var isDarkMode by remember { mutableStateOf(false) }
            var unitSystem by remember { mutableStateOf("Métrique (m/s, km/h)") }
            var language by remember { mutableStateOf("Français") }

            // 1. Centralisation de l'état et du processeur au niveau de l'activité
            val processor = remember { MovementProcessor() }
            var lastLocationState by remember { mutableStateOf<GeoPoint?>(null) }
            var refreshTrigger by remember { mutableStateOf(0) }
            val context = LocalContext.current

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

            // Demande de permission
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
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { isDarkMode = it },
                            unitSystem = unitSystem,
                            onUnitSystemChange = { unitSystem = it },
                            language = language,
                            onLanguageChange = { language = it },
                            onOpenFullScreenMap = { rootNavController.navigate("full_map") }
                        )
                    }
                    composable("full_map") {
                        Page4(
                            location = lastLocationState,
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
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    unitSystem: String,
    onUnitSystemChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    onOpenFullScreenMap: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    val tabs = listOf(
        TabItem("Analyse", Icons.Default.Home),
        TabItem("Carte", Icons.Default.LocationOn),
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
                    onNavigateToMap = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onNavigateToSettings = { scope.launch { pagerState.animateScrollToPage(2) } }
                )
                1 -> Page2(
                    location = lastLocationState,
                    processor = processor,
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                    onOpenFullScreenMap = onOpenFullScreenMap
                )
                2 -> SettingsPage(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    unitSystem = unitSystem,
                    onUnitSystemChange = onUnitSystemChange,
                    language = language,
                    onLanguageChange = onLanguageChange
                )
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)
