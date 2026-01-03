import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.freegameradar.App
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.initializeDatabase
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.AuthViewModel
import java.lang.Thread

fun main() {
    // Set a global uncaught exception handler to see UI thread crashes
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        println("Uncaught exception in thread '${thread.name}':")
        throwable.printStackTrace()
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
            onCloseRequest = ::exitApplication,
            title = "Free Game Radar",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
        ) {
            val authRepository = remember { AuthRepositoryImpl() }
            val authViewModel = remember { AuthViewModel(authRepository) }
            App(authViewModel = authViewModel, startRoute = Screen.Home.route)
        }
    }
}
