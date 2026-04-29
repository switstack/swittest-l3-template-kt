package io.switstack.switcloud.swittestl3

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.switstack.switcloud.switcloudclt.domain.SwitcloudClt
import io.switstack.switcloud.swittestl3.ui.about.AboutScreen
import io.switstack.switcloud.swittestl3.ui.home.HomeScreen
import io.switstack.switcloud.swittestl3.ui.settings.SettingsScreen
import io.switstack.switcloud.swittestl3.ui.settings.SettingsViewModel
import io.switstack.switcloud.swittestl3.ui.theme.Swittestl3Theme

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

class MainActivity : AppCompatActivity() {

    private val l3Application: SwittestL3Application by lazy { application as SwittestL3Application }
    private val settingsViewModel: SettingsViewModel by lazy { l3Application.settingsViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Swittestl3Theme {
                AppNavigation(settingsViewModel)
            }
        }

        SwitcloudClt.setActivity(this, true)
    }
}

@Composable
fun AppNavigation(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(navController = navController, settingsViewModel = settingsViewModel)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController, viewModel = settingsViewModel)
        }
        composable(Routes.ABOUT) {
            AboutScreen(navController)
        }
    }
}