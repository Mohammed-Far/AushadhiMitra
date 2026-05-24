package com.example.greenpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.greenpulse.ui.navigation.Screen
import com.example.greenpulse.ui.screens.*
import com.example.greenpulse.ui.theme.AushadhiMitraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AushadhiMitraTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator(
    authViewModel: AuthViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel(),
) {
    val authState by authViewModel.authState

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            mainViewModel.onUserAuthenticated()
        }
    }

    when (authState) {
        is AuthState.Authenticated -> {
            val isProfileLoaded by mainViewModel.isProfileLoaded
            val profile by mainViewModel.patientProfile

            if (!isProfileLoaded) {
                // ✅ Show loading screen until we confirm if profile exists
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!profile.isSetupComplete) {
                ProfileSetupScreen(mainViewModel)
            } else {
                MainScreen(mainViewModel) {
                    mainViewModel.clearData()
                    authViewModel.signOut()
                }
            }
        }
        else -> {
            AuthScreen(authViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val isBuzzerActive by viewModel.isBuzzerActive
    val alertingMedicine by viewModel.currentAlertMedicine

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontWeight = FontWeight.Bold) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Schedule.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Screen.Schedule.route) { ScheduleScreen(viewModel) }
                composable(Screen.Tablets.route) { TabletsScreen(viewModel) }
                composable(Screen.Reports.route) { ReportsScreen(viewModel) }
                composable(Screen.SMSLogs.route) { SMSLogsScreen(viewModel) }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel, onLogout = onSignOut)
                }
            }
        }

        AnimatedVisibility(
            visible = isBuzzerActive && (alertingMedicine != null),
            enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
            exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.errorContainer,
                                        MaterialTheme.colorScheme.surface
                                    ),
                                ),
                            )
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "ATTENTION",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.error,
                            letterSpacing = 2.sp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Medicine Due Now",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = alertingMedicine?.userMedicineName ?: "",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = "Slot ${alertingMedicine?.slot?.name ?: ""}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.dispenseMedicine() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        ) {
                            Text("DISPENSE MEDICINE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}