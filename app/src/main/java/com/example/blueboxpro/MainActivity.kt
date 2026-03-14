/**
 * Main entry point of the BlueBoxPro application.
 * This activity handles the high-level navigation (Scaffold, TopAppBar, BottomBar),
 * permissions, and initializes core components like the MovementProcessor and CaptorListener.
 */
package com.example.blueboxpro

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.blueboxpro.Save.SessionManager
import com.example.blueboxpro.pages.*
import com.example.blueboxpro.ui.theme.BlueBoxProTheme
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

/**
 * The primary Activity for the application.
 * Responsible for setting up the UI theme, navigation graph, and lifecycle-bound sensor processing.
 */
class MainActivity : AppCompatActivity() {
    companion object {
        private const val ROUTE_MAIN = "main"
        private const val ROUTE_FULL_MAP = "full_map"
        private const val ROUTE_ADVANCED_SETTINGS = "advanced_settings"
        private const val ROUTE_SESSION_DETAIL_BASE = "session_detail"
        private const val ARG_SESSION_ID = "sessionId"
        private const val ROUTE_SESSION_DETAIL = "$ROUTE_SESSION_DETAIL_BASE/{$ARG_SESSION_ID}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load global options first to ensure UI is initialized correctly
        Option.load(this)

        // Load osmdroid configuration
        Configuration.getInstance().load(
            applicationContext,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        // Load saved sessions from local storage
        SessionManager.loadSessions(this)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val context = LocalContext.current
            
            // Initialize UI states from persistent Options
            var isDarkMode by remember { mutableStateOf(Option.UI.isDarkMode) }
            var unitSystemKey by remember { mutableStateOf(Option.UI.unitSystem) }
            var language by remember { mutableStateOf(Option.UI.language) }

            val processor = remember { MovementProcessor() }
            var lastLocationState by remember { mutableStateOf<GeoPoint?>(null) }
            var refreshTrigger by remember { mutableStateOf(0) }

            // Initialize sensor listener
            val captorListener = remember {
                CaptorListener(context, processor) {
                    lastLocationState = processor.lastLocation
                    refreshTrigger++
                }
            }

            // Sync recording with processor updates
            LaunchedEffect(refreshTrigger) {
                SessionManager.updateRecording(processor)
            }

            // Manage sensor listener lifecycle
            DisposableEffect(Unit) {
                captorListener.start()
                onDispose {
                    captorListener.stop()
                    SessionManager.saveSessions(this@MainActivity)
                }
            }

            // Permission handling
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { /* Permission results could be handled here */ }

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
                
                NavHost(navController = rootNavController, startDestination = ROUTE_MAIN) {
                    composable(ROUTE_MAIN) {
                        MainScreen(
                            processor = processor,
                            lastLocationState = lastLocationState,
                            refreshTrigger = refreshTrigger,
                            unitSystem = unitSystemKey,
                            language = language,
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { 
                                isDarkMode = it
                                Option.UI.isDarkMode = it
                                Option.save(this@MainActivity)
                            },
                            onUnitSystemChange = { 
                                unitSystemKey = it
                                Option.UI.unitSystem = it
                                Option.save(this@MainActivity)
                            },
                            onLanguageChange = { newLang ->
                                language = newLang
                                Option.UI.language = newLang
                                Option.save(this@MainActivity)
                                
                                val tag = if (newLang == Option.App.LANG_NAME_FR) Option.App.LANG_FR else Option.App.LANG_EN
                                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(tag)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                            },
                            onOpenFullScreenMap = { rootNavController.navigate(ROUTE_FULL_MAP) },
                            onNavigateToAdvancedSettings = { rootNavController.navigate(ROUTE_ADVANCED_SETTINGS) },
                            onNavigateToSessionDetail = { sessionId ->
                                rootNavController.navigate("$ROUTE_SESSION_DETAIL_BASE/$sessionId")
                            }
                        )
                    }
                    composable(ROUTE_FULL_MAP) {
                        Page4(
                            location = lastLocationState,
                            onBack = { rootNavController.popBackStack() }
                        )
                    }
                    composable(ROUTE_ADVANCED_SETTINGS) {
                        AdvancedSettingsPage(
                            processor = processor,
                            refreshTrigger = refreshTrigger,
                            unitSystem = unitSystemKey,
                            onBack = { rootNavController.popBackStack() }
                        )
                    }
                    composable(
                        route = ROUTE_SESSION_DETAIL,
                        arguments = listOf(navArgument(ARG_SESSION_ID) { type = NavType.IntType })
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getInt(ARG_SESSION_ID)
                        val session = SessionManager.sessions.find { it.id == sessionId }
                        SessionDetailPage(
                            session = session,
                            onBack = { rootNavController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        SessionManager.saveSessions(this)
        Option.save(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SessionManager.saveSessions(this)
        Option.save(this)
    }
}

/**
 * The main screen containing the bottom navigation and horizontal pager for top-level pages.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    onNavigateToAdvancedSettings: () -> Unit,
    onNavigateToSessionDetail: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    
    val tabs = listOf(
        TabItem(Icons.Default.Home, R.string.tab_home),
        TabItem(Icons.Default.LocationOn, R.string.tab_map),
        TabItem(Icons.Default.PlayArrow, R.string.tab_sessions),
        TabItem(Icons.Default.Settings, R.string.tab_settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(tabs[pagerState.currentPage].labelRes)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = { 
                            Icon(
                                imageVector = tab.icon, 
                                contentDescription = stringResource(tab.labelRes),
                                modifier = Modifier.size(28.dp)
                            ) 
                        },
                        label = { Text(stringResource(tab.labelRes)) },
                        alwaysShowLabel = true
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
                    refreshTrigger = refreshTrigger,
                    unitSystem = unitSystem,
                    onOpenFullScreenMap = onOpenFullScreenMap
                )
                2 -> Page3(
                    processor = processor,
                    refreshTrigger = refreshTrigger,
                    onSessionClick = onNavigateToSessionDetail
                )
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

/**
 * Data class representing an item in the bottom navigation bar.
 */
data class TabItem(val icon: ImageVector, val labelRes: Int)
