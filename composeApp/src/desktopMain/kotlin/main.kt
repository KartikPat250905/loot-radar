import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.freegameradar.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Free Game Radar",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        App()  // This is your main composable from commonMain
    }
}
