import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.example.freegameradar.App
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.initializeDatabase
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.AuthViewModel
import com.example.freegameradar.firebase.testFirebaseConfig
import com.example.freegameradar.firebase.runAllHttpClientTests  // ADD THIS
import com.example.freegameradar.firebase.FirebaseHttpClient      // ADD THIS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Thread

fun main() {
    // Set a global uncaught exception handler to see UI thread crashes
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        println("Uncaught exception in thread '${thread.name}':")
        throwable.printStackTrace()
    }

    // Phase 3 Test: Firebase config loading
    testFirebaseConfig()

    // ADD THIS - Phase 4 Test: HTTP client
    CoroutineScope(Dispatchers.IO).launch {
        runAllHttpClientTests()
    }

    // Synchronously initialize the database before starting the UI
    try {
        initializeDatabase()
    } catch (e: Exception) {
        println("Fatal error during database initialization: ${e.message}")
        e.printStackTrace()
        return // Exit if the database can't be initialized
    }

    // Start the Compose application
    application {
        Window(
            onCloseRequest = {
                // ADD THIS - Clean up HTTP client on exit
                FirebaseHttpClient.close()
                exitApplication()
            },
            title = "Free Game Radar",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            // Initialize Coil ImageLoader for Desktop
            setSingletonImageLoaderFactory { context ->
                ImageLoader.Builder(context)
                    .components {
                        add(KtorNetworkFetcherFactory())
                    }
                    .build()
            }

            val authRepository = remember { AuthRepositoryImpl() }
            val authViewModel = remember { AuthViewModel(authRepository) }
            App(authViewModel = authViewModel, startRoute = Screen.Home.route)
        }
    }
}
