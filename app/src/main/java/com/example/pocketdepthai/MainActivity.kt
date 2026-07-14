package com.example.pocketdepthai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pocketdepthai.ui.*
import com.example.pocketdepthai.ui.theme.PocketDepthAITheme
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dynamically request microphone permission on startup
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        }

        setContent {
            PocketDepthAITheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val toothViewModel: ToothViewModel = viewModel()

    val navigateToNotifications = { navController.navigate("notifications") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppLogo(
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF8C4BFF)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Pocket Depth AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Clinical Edition", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Divider()
                NavigationDrawerItem(
                    label = { Text("Main Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("dashboard") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Patient Registration") },
                    selected = currentRoute == "patient_registration",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("patient_registration") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Notifications") },
                    selected = currentRoute == "notifications",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("notifications") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Search Patients") },
                    selected = currentRoute == "search_patients",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("search_patients") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Filter Dashboard") },
                    selected = currentRoute == "filter_dashboard",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("filter_dashboard") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.FilterList, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Reports") },
                    selected = currentRoute == "reports_list",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("reports_list") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Description, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Manual Input") },
                    selected = currentRoute?.startsWith("manual_input") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("manual_input/Patient") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                Divider()
                NavigationDrawerItem(
                    label = { Text("User Profile") },
                    selected = currentRoute == "user_profile",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("user_profile") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings") { launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                )
            }
        },
        gesturesEnabled = currentRoute != "splash" && currentRoute != "onboarding" && currentRoute != "login" && currentRoute != "signup"
    ) {
        Scaffold { innerPadding ->
            val contentModifier = if (currentRoute == "splash") Modifier.fillMaxSize() else Modifier.padding(innerPadding)
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = contentModifier
            ) {
                composable("splash") {
                    SplashScreen(onTimeout = {
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    })
                }
                composable("onboarding") {
                    OnboardingScreen(onGetStarted = {
                        navController.navigate("login")
                    })
                }
                composable("login") {
                    LoginScreen(
                        onSignIn = { email, password, onError ->
                            toothViewModel.loginUser(email, password,
                                onSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onError = { errorMsg ->
                                    onError(errorMsg)
                                }
                            )
                        },
                        onForgotPassword = {
                            navController.navigate("forgot_password")
                        },
                        onNavigateToSignup = {
                            navController.navigate("signup")
                        }
                    )
                }
                composable("signup") {
                    SignupScreen(
                        onSignUp = { name, email, phone, password, onError ->
                            toothViewModel.registerUser(name, email, phone, password,
                                onSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                },
                                onError = { errorMsg ->
                                    onError(errorMsg)
                                }
                            )
                        },
                        onBackToLogin = {
                            navController.popBackStack()
                        }
                    )
                }
                composable("dashboard") {
                    val doctorName by toothViewModel.clinicalUserName.collectAsState()
                    DashboardScreen(
                        viewModel = toothViewModel,
                        doctorName = doctorName,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNavigateToAnalytics = { /* Analytics module removed */ },
                        onNavigateToNotifications = navigateToNotifications,
                        onNavigateToRegistration = { navController.navigate("patient_registration") },
                        onNavigateToFilter = { navController.navigate("filter_dashboard") }
                    )
                }
                composable("patient_registration") {
                    PatientRegistrationScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onCancel = { navController.popBackStack() },
                        onRegisterSuccess = { navController.navigate("registration_success") },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("registration_success") {
                    RegistrationSuccessScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onViewList = { navController.navigate("search_patients") },
                        onStartExamination = { navController.navigate("start_examination") },
                        onAddAnother = { navController.navigate("patient_registration") { popUpTo("dashboard") } },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("start_examination") {
                    StartExaminationScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onAddPatientClick = { navController.navigate("patient_registration") },
                        onPatientClick = { patientName ->
                            toothViewModel.setActivePatient(patientName)
                            navController.navigate("examination_instructions/$patientName")
                        },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("examination_instructions/{patientName}") { backStackEntry ->
                    val patientName = backStackEntry.arguments?.getString("patientName") ?: "Patient"
                    ExaminationInstructionsScreen(
                        patientName = patientName,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onStartExamination = { navController.navigate("voice_input/$patientName") },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("voice_input/{patientName}") { backStackEntry ->
                    val patientName = backStackEntry.arguments?.getString("patientName") ?: "Patient"
                    VoiceInputScreen(
                        patientName = patientName,
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onContinue = { 
                            toothViewModel.analyzeAndStoreMetrics()
                            navController.navigate("processing_examination") 
                        },
                        onSkip = { navController.navigate("manual_input/$patientName") },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("manual_input/{patientName}") { backStackEntry ->
                    val patientName = backStackEntry.arguments?.getString("patientName") ?: "Patient"
                    ManualInputScreen(
                        patientName = patientName,
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onGenerateCharts = { navController.navigate("processing_examination") },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("processing_examination") {
                    ProcessingExaminationScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNavigateToNotifications = navigateToNotifications,
                        onProcessingComplete = { navController.navigate("statistics_summary") }
                    )
                }
                composable("statistics_summary") {
                    StatisticsSummaryScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onViewAIClassification = { navController.navigate("ai_classification") },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("ai_classification") {
                    AIClassificationScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onGenerateReport = { navController.navigate("probability_breakdown") },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("probability_breakdown") {
                    ProbabilityBreakdownScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onGenerateReport = { navController.navigate("generate_report") },
                        onNewExamination = { navController.navigate("start_examination") { popUpTo("dashboard") } },
                        onBackToDashboard = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("generate_report") {
                    GenerateReportScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onGenerateSuccess = { navController.navigate("report_preview") },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("report_preview") {
                    ReportPreviewScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onDownload = { navController.navigate("download_report") },
                        onShare = { navController.navigate("share_report") },
                        onDone = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } } },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("share_report") {
                    ShareReportScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("download_report") {
                    DownloadReportScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("reports_list") {
                    ReportsScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNavigateToNotifications = navigateToNotifications,
                        onStartExamination = { navController.navigate("start_examination") }
                    )
                }
                composable("user_profile") {
                    UserProfileScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBack = { navController.popBackStack() },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("notifications") {
                    NotificationsScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("search_patients") {
                    SearchPatientsScreen(
                        viewModel = toothViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNewPatientClick = { navController.navigate("patient_registration") },
                        onPatientClick = { patientName ->
                            toothViewModel.setActivePatient(patientName)
                            navController.navigate("examination_instructions/$patientName")
                        },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("filter_dashboard") {
                    FilterDashboardScreen(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNavigateToNotifications = navigateToNotifications
                    )
                }
                composable("forgot_password") {
                    ForgotPasswordScreen(
                        onSendLink = { navController.navigate("success") },
                        onBackToLogin = { navController.popBackStack() }
                    )
                }
                composable("success") {
                    SuccessScreen(onBackToLogin = {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    })
                }
            }
        }
    }
}
