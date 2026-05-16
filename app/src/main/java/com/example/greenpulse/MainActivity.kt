package com.example.greenpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.greenpulse.ui.navigation.Screen
import com.example.greenpulse.ui.screens.AddMedicineScreen
import com.example.greenpulse.ui.screens.ReportsScreen
import com.example.greenpulse.ui.screens.SettingsScreen
import com.example.greenpulse.ui.screens.SMSLogsScreen
import com.example.greenpulse.ui.screens.ScheduleScreen
import com.example.greenpulse.ui.theme.GreenPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreenPulseTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                Screen.bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Schedule.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Schedule.route) { ScheduleScreen() }
            composable(Screen.Reports.route) { ReportsScreen() }
            composable(Screen.Action.route) { 
                AddMedicineScreen(onSave = { 
                    navController.navigate(Screen.Schedule.route) 
                }) 
            }
            composable(Screen.SMSLogs.route) { SMSLogsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

