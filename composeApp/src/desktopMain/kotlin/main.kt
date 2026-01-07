import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.example.freegameradar.App
import com.example.freegameradar.data.auth.createAuthRepository
import com.example.freegameradar.initializeDatabase
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.AuthViewModel
import com.example.freegameradar.firebase.testFirebaseConfig
import com.example.freegameradar.firebase.runAllHttpClientTests
import com.example.freegameradar.firebase.runAllModelTests
import com.example.freegameradar.firebase.runAllAuthServiceTests  // ADD THIS
import com.example.freegameradar.firebase.FirebaseHttpClient
import com.example.freegameradar.firebase.runAllTokenStorageTests
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Thread

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        println("Uncaught exception in thread '${thread.name}':")
        throwable.printStackTrace()
    }

//    CoroutineScope(Dispatchers.IO).launch {
//        runAllAuthServiceTests()
//    }

    CoroutineScope(Dispatchers.IO).launch {
        runAllTokenStorageTests()
    }

    try {
        initializeDatabase()
    } catch (e: Exception) {
        println("Fatal error during database initialization: ${e.message}")
        e.printStackTrace()
        return
    }

    application {
        Window(
            onCloseRequest = {
                FirebaseHttpClient.close()
                exitApplication()
            },
            title = "Free Game Radar",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            setSingletonImageLoaderFactory { context ->
                ImageLoader.Builder(context)
                    .components {
                        add(KtorNetworkFetcherFactory())
                    }
                    .build()
            }

            val authRepository = remember { createAuthRepository() }
            val authViewModel = remember { AuthViewModel(authRepository) }
            App(authViewModel = authViewModel, startRoute = Screen.Home.route)
        }
    }
}
