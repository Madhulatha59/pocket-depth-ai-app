package com.example.pocketdepthai.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.pocketdepthai.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- SHARED COMPONENTS ---

@Composable
fun AppLogo(modifier: Modifier = Modifier, tint: Color = Color.Unspecified) {
    androidx.compose.foundation.Image(
        painter = painterResource(id = R.mipmap.app_logo),
        contentDescription = "App Logo",
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
    )
}

@Composable
fun FeatureCard(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color(0xFFF0F2FF), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF5D48D1), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, iconColor: Color, value: String, label: String, growth: String, growthColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).background(iconColor.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
                Text(text = label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Surface(color = growthColor.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (growth.startsWith("+")) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = growthColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = growth, color = growthColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun ChartSection(title: String, onViewDetails: (() -> Unit)? = null, content: @Composable (() -> Unit)) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (onViewDetails != null) {
                    IconButton(onClick = onViewDetails) { Icon(Icons.Default.OpenInNew, null, tint = Color(0xFF5D48D1), modifier = Modifier.size(20.dp)) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, backgroundBrush: Brush? = null, backgroundColor: Color = Color.Transparent, iconColor: Color = Color.White, onClick: () -> Unit = {}) {
    val containerModifier = if (backgroundBrush != null) Modifier.fillMaxWidth().background(backgroundBrush, RoundedCornerShape(20.dp)) else Modifier.fillMaxWidth().background(backgroundColor, RoundedCornerShape(20.dp))
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), onClick = onClick) {
        Row(modifier = containerModifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(if (backgroundBrush != null) Color.White.copy(alpha = 0.2f) else iconColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (backgroundBrush != null) Color.White else iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (backgroundBrush != null) Color.White else Color(0xFF1A1A1A))
                Text(text = subtitle, fontSize = 13.sp, color = if (backgroundBrush != null) Color.White.copy(alpha = 0.8f) else Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, null, tint = if (backgroundBrush != null) Color.White else Color.LightGray)
        }
    }
}

@Composable
fun InstructionCard(number: String, title: String, description: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(36.dp).background(Color(0xFF5D48D1), CircleShape), contentAlignment = Alignment.Center) { Text(number, color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun ClassificationRecommendationItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 15.sp, color = Color.DarkGray, lineHeight = 22.sp)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

// --- ONBOARDING & AUTH ---

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val backgroundBrush = Brush.verticalGradient(colors = listOf(Color(0xFF7B61FF), Color(0xFF5D48D1)))
    Column(modifier = Modifier.fillMaxSize().background(backgroundBrush).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(60.dp))
        AppLogo(modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Pocket Depth AI", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(text = "Advanced Periodontal Clinical System", fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f), letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(56.dp))
        FeatureCard(icon = Icons.Default.Psychology, title = "Clinical AI Engine", description = "High-precision classification based on AAP/EFP 2018 guidelines")
        FeatureCard(icon = Icons.Default.Mic, title = "Voice-First Entry", description = "Hands-free measurement recording with real-time waveform analysis")
        FeatureCard(icon = Icons.Default.Analytics, title = "Visual Analytics", description = "Dynamic bar and arc charts for clinical longitudinal monitoring")
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().height(68.dp),
            shape = RoundedCornerShape(34.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(text = "Get Started", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF5D48D1))
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF5D48D1), modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onSignIn: (String, String, (String) -> Unit) -> Unit, onForgotPassword: () -> Unit, onNavigateToSignup: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }

    if (showError != null) {
        AlertDialog(
            onDismissRequest = { showError = null },
            confirmButton = { TextButton(onClick = { showError = null }) { Text("OK") } },
            title = { Text("Login Error") },
            text = { Text(showError!!) }
        )
    }

    val backgroundBrush = Brush.verticalGradient(colors = listOf(Color(0xFF7B61FF), Color(0xFF5D48D1)))
    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush), contentAlignment = Alignment.Center) {
        // Subtle background decoration
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.05f), radius = 400f, center = Offset(size.width * 0.1f, size.height * 0.1f))
            drawCircle(Color.White.copy(alpha = 0.05f), radius = 600f, center = Offset(size.width * 0.9f, size.height * 0.9f))
        }

        Card(modifier = Modifier.fillMaxWidth(0.9f), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)), elevation = CardDefaults.cardElevation(12.dp)) {
            Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AppLogo(modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Clinical Gateway", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(text = "Authorized access only", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Clinic Email") }, leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF5D48D1)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1)))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Security Password") }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF5D48D1)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), visualTransformation = PasswordVisualTransformation(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1)))
                TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) { Text("Reset Credentials?", color = Color(0xFF5D48D1), fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (!email.contains("@")) {
                            showError = "Invalid clinical email format."
                        } else if (password.length < 8) {
                            showError = "Password must be at least 8 characters."
                        } else {
                            onSignIn(email, password) { errorMsg ->
                                showError = errorMsg
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
                ) {
                    Text("Secure Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Login, null)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onNavigateToSignup) {
                    Text(
                        text = "New Clinical Staff? Sign Up",
                        color = Color(0xFF5D48D1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(onSignUp: (String, String, String, String, (String) -> Unit) -> Unit, onBackToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf<String?>(null) }

    if (showError != null) {
        AlertDialog(
            onDismissRequest = { showError = null },
            confirmButton = { TextButton(onClick = { showError = null }) { Text("OK") } },
            title = { Text("Registration Error") },
            text = { Text(showError!!) }
        )
    }

    val backgroundBrush = Brush.verticalGradient(colors = listOf(Color(0xFF7B61FF), Color(0xFF5D48D1)))
    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.05f), radius = 400f, center = Offset(size.width * 0.1f, size.height * 0.1f))
            drawCircle(Color.White.copy(alpha = 0.05f), radius = 600f, center = Offset(size.width * 0.9f, size.height * 0.9f))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 28.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppLogo(modifier = Modifier.size(70.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Clinical Registration", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(text = "Create practitioner account", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Professional Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF5D48D1)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1))
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Clinic Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF5D48D1)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1))
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Contact") },
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF5D48D1)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1))
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Security Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF5D48D1)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1))
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF5D48D1)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1))
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            showError = "All fields are required."
                        } else if (!email.contains("@")) {
                            showError = "Invalid clinical email format."
                        } else if (password.length < 8) {
                            showError = "Password must be at least 8 characters."
                        } else if (password != confirmPassword) {
                            showError = "Passwords do not match."
                        } else {
                            onSignUp(name, email, phone, password) { errorMsg ->
                                showError = errorMsg
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
                ) {
                    Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.PersonAdd, null)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onBackToLogin) {
                    Text(
                        text = "Already registered? Log In",
                        color = Color(0xFF5D48D1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onSendLink: () -> Unit, onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF)), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(80.dp).background(Color(0xFFE8EBFF), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.LockReset, null, modifier = Modifier.size(40.dp), tint = Color(0xFF5D48D1)) }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Reset Password", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("We will send a recovery link to your inbox", color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Clinic Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onSendLink, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(30.dp)) { Text("Send Recovery Link", fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBackToLogin) { Text("Return to Login", color = Color(0xFF5D48D1), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun SuccessScreen(onBackToLogin: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(Color(0xFFE8F5E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Verified,
                null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF4CAF50)
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text("Credential Reset", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
        Text(
            "Instructions have been dispatched to your clinical email. Please follow the secure link to finalize.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(64.dp))
        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
        ) {
            Text("Return to Secure Login", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}


// --- DASHBOARDS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ToothViewModel,
    doctorName: String,
    onMenuClick: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToRegistration: () -> Unit,
    onNavigateToFilter: () -> Unit
) {
    val patients by viewModel.patients.collectAsState()
    val patientCount = patients.size
    val reportsCount = patients.size

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF)).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("POCKET DEPTH AI", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            Row {
                IconButton(onClick = onNavigateToFilter) { Icon(Icons.Default.FilterList, null, tint = Color(0xFF5D48D1)) }
                IconButton(onClick = onNavigateToNotifications) {
                    BadgedBox(badge = { Badge(containerColor = Color.Red, modifier = Modifier.size(8.dp)) }) {
                        Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF5D48D1))
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Welcome back,", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text("Dr. $doctorName", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StatCard(Icons.Default.Groups, Color(0xFF2196F3), "$patientCount", "Patients", "+12%", Color(0xFF4CAF50))
                }
            }
            StatCard(Icons.Default.Assignment, Color(0xFF9C27B0), "$reportsCount", "Clinical Reports", "+8.2%", Color(0xFF4CAF50))

            Spacer(modifier = Modifier.height(24.dp))
            ChartSection(title = "Clinical Activity Trend", onViewDetails = onNavigateToAnalytics) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFF0F2FF), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val points = listOf(0.2f, 0.4f, 0.35f, 0.7f, 0.55f, 0.85f, 0.8f)
                        val widthStep = size.width / (points.size - 1)
                        val path = Path()
                        points.forEachIndexed { i, p ->
                            val x = i * widthStep
                            val y = size.height * (1 - p)
                            if (i == 0) path.moveTo(x, y) else path.quadraticBezierTo((i-0.5f)*widthStep, size.height * (1 - points[i-1]), x, y)
                        }
                        drawPath(path, Brush.verticalGradient(listOf(Color(0xFF7B61FF), Color(0xFF5D48D1))), style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    LegendItem("New Exams", Color(0xFF5D48D1))
                    Spacer(modifier = Modifier.width(24.dp))
                    LegendItem("Follow-ups", Color(0xFF7B61FF))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Quick Actions", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            QuickActionCard(title = "Add Patient", subtitle = "Register new clinical record", icon = Icons.Default.PersonAdd, backgroundBrush = Brush.linearGradient(colors = listOf(Color(0xFF7B61FF), Color(0xFF5D48D1))), onClick = onNavigateToRegistration)
            Spacer(modifier = Modifier.height(32.dp))

            // System Synchronization Panel
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFFF0F2FF), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Settings, null, tint = Color(0xFF5D48D1), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("System Synchronization", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF1A1A1A))
                    }
                    Spacer(Modifier.height(20.dp))
                    Divider(thickness = 0.5.dp, color = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    DashboardSyncRow("Clinical AI Model", "V2.4 Active")
                    DashboardSyncRow("Cloud Vault", "Encrypted & Synced")
                    DashboardSyncRow("Voice Engine", "High Fidelity")
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}


@Composable
fun DashboardSyncRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onMenuClick: () -> Unit, onNavigateToNotifications: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("ALERTS & UPDATES", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Default.DoneAll, null, tint = Color(0xFF5D48D1)) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Text("Notifications", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text("Stay updated with clinical activities", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))

            NotificationItem(Icons.Default.Psychology, Color(0xFF4CAF50), "AI Analysis Ready", "Classification for patient Sarah Johnson has been finalized.", "Just now")
            NotificationItem(Icons.Default.Mic, Color(0xFF2196F3), "Voice Engine Updated", "Improved accuracy for multi-digit depth values is now active.", "45m ago")
            NotificationItem(Icons.Default.Warning, Color(0xFFD84315), "Critical Pocket Found", "Deep pockets (>6mm) detected in Robert Wilson's recent exam.", "2h ago")
            NotificationItem(Icons.Default.CloudSync, Color(0xFF5D48D1), "Cloud Synchronization", "Successfully exported 4 clinical reports to the medical vault.", "4h ago")
            NotificationItem(Icons.Default.Settings, Color(0xFF9C27B0), "System Maintenance", "Scheduled server update for database backup at 11:00 PM.", "1d ago")

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun NotificationItem(icon: ImageVector, color: Color, title: String, desc: String, time: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(desc, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
            }
            Text(time, fontSize = 11.sp, color = Color.LightGray)
        }
    }
}

// --- REGISTRATION ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRegistrationScreen(viewModel: ToothViewModel, onMenuClick: () -> Unit, onCancel: () -> Unit, onRegisterSuccess: () -> Unit, onNavigateToNotifications: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }
    
    var showError by remember { mutableStateOf<String?>(null) }

    if (showError != null) {
        AlertDialog(
            onDismissRequest = { showError = null },
            confirmButton = { TextButton(onClick = { showError = null }) { Text("OK") } },
            title = { Text("Registration Error") },
            text = { Text(showError!!) }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("Clinical Intake", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null) }
        }
        Column(modifier = Modifier.padding(24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Text("Patient Profile", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Register new clinical record for AI analysis", color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Legal Name") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF5D48D1)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1)))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Secure Email Address") }, leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF5D48D1)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1)))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Clinical Phone Contact") }, leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF5D48D1)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1)))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = history, onValueChange = { history = it }, label = { Text("Medical History Summary (Confidential)") }, modifier = Modifier.fillMaxWidth().height(160.dp), shape = RoundedCornerShape(20.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1)))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = "Patient name is required."
                    } else if (!email.contains("@")) {
                        showError = "Invalid patient email address."
                    } else {
                        viewModel.registerPatient(name, email, phone, history)
                        onRegisterSuccess()
                    }
                }, 
                modifier = Modifier.weight(1.5f).height(64.dp), 
                shape = RoundedCornerShape(32.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
            ) { Text("Sync Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(32.dp)) { Text("Cancel") }
        }
    }
}



@Composable
fun RegistrationSuccessScreen(onMenuClick: () -> Unit, onViewList: () -> Unit, onStartExamination: () -> Unit, onAddAnother: () -> Unit, onNavigateToNotifications: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(100.dp).background(Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.PersonAddAlt, null, modifier = Modifier.size(56.dp), tint = Color(0xFF4CAF50)) }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Success!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Patient record has been synchronized with clinic server", textAlign = TextAlign.Center, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onStartExamination, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))) { Text("Proceed to Examination", fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onViewList, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(32.dp)) { Text("Patient Directory") }
        TextButton(onClick = onAddAnother, modifier = Modifier.padding(top = 12.dp)) { Text("Register Another Patient", fontWeight = FontWeight.Bold, color = Color(0xFF5D48D1)) }
    }
    // Using parameters to satisfy compiler
    LaunchedEffect(Unit) {
        println("Registration success view for $onMenuClick and $onNavigateToNotifications")
    }
}


// --- EXAMINATION ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartExaminationScreen(viewModel: ToothViewModel, onMenuClick: () -> Unit, onAddPatientClick: () -> Unit, onPatientClick: (String) -> Unit, onNavigateToNotifications: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val patients by viewModel.patients.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("EXAMINATION QUEUE", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF5D48D1)) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Text("Select Patient", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text("Initiate voice-activated periodontal charting", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search patient...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF5D48D1)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddPatientClick,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(12.dp))
                Text("New Registration", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text("Active Clinical Queue", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            patients.filter { it.contains(query, ignoreCase = true) }.forEach { p ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onPatientClick(p) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).background(Color(0xFFF0F2FF), CircleShape), contentAlignment = Alignment.Center) {
                            Text(p.take(1), fontWeight = FontWeight.ExtraBold, color = Color(0xFF5D48D1), fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A1A1A))
                            Text("Ready for exam", fontSize = 13.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ArrowForwardIos, null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun ExaminationInstructionsScreen(patientName: String, onMenuClick: () -> Unit, onBack: () -> Unit, onStartExamination: () -> Unit, onNavigateToNotifications: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("VOICE PROTOCOL", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF5D48D1)) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Voice Input Guide", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
            Text("Patient: $patientName", color = Color(0xFF5D48D1), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(24.dp))

            // Highlighted Example Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5D48D1))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("How to Speak", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Say tooth number followed by pocket depth values:", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    // Example entries
                    VoiceExampleRow("\"Tooth 11 three four five\"")
                    VoiceExampleRow("\"Tooth 11 - 3 4 5\"")
                    VoiceExampleRow("\"Tooth twenty-one 2 3 4\"")
                    VoiceExampleRow("\"11 3 4 5\" (short form)")
                    Spacer(Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "The AI will save: Tooth 11 → [3, 4, 5]",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            InstructionCard("1", "Speak Tooth Number", "Say \"Tooth\" followed by the number (1–32). E.g. \"Tooth 11\" or \"Tooth twenty-one\".")
            InstructionCard("2", "Speak Depth Values", "After the tooth number, speak your probe readings separated by spaces or pause. E.g. \"3 4 5\".")
            InstructionCard("3", "Record Multiple Teeth", "Tap the mic button after each tooth entry. Each tooth is saved separately.")
            InstructionCard("4", "Check Saved Entries", "Saved teeth appear in the list below the mic. Tap \"Analyze\" when done.")

            Spacer(modifier = Modifier.height(16.dp))
            // Quick reference table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Valid Depth Range", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A1A))
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VoiceRangeChip("0–3 mm", Color(0xFF4CAF50), "Healthy")
                        VoiceRangeChip("4–5 mm", Color(0xFFFF9800), "Moderate")
                        VoiceRangeChip("6–10 mm", Color(0xFFE53935), "Severe")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onStartExamination,
                modifier = Modifier.weight(1.5f).height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
            ) {
                Icon(Icons.Default.Mic, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Voice Entry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(32.dp)) { Text("Back") }
        }
    }
}

@Composable
fun VoiceExampleRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier.size(6.dp).background(Color.White.copy(alpha = 0.7f), CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RowScope.VoiceRangeChip(range: String, color: Color, label: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(range, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            Text(label, color = color.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

@Composable
fun VoiceInputScreen(patientName: String, viewModel: ToothViewModel, onMenuClick: () -> Unit, onBack: () -> Unit, onContinue: () -> Unit, onSkip: () -> Unit, onNavigateToNotifications: () -> Unit) {
    val isListening by viewModel.isListening.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val error by viewModel.voiceError.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val allToothData by viewModel.allToothData.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.RECORD_AUDIO
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // Pulsing rings animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse2"
    )

    // Track whether the example box is expanded
    var exampleExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF4A3BC0))) {
        // Background decorative circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.04f), radius = 500f, center = Offset(size.width * 0.85f, size.height * 0.1f))
            drawCircle(Color.White.copy(alpha = 0.03f), radius = 380f, center = Offset(size.width * 0.1f, size.height * 0.85f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color.White) }
                Text("VOICE ENTRY", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), letterSpacing = 2.sp)
                IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null, tint = Color.White) }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Voice Charting", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Patient: $patientName", color = Color.White.copy(alpha = 0.65f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))

            // Status text
            Text(
                text = when {
                    !hasPermission -> "⚠️  Tap mic to grant microphone permission"
                    isListening -> "🎙  Listening... speak clearly now"
                    recognizedText.isNotEmpty() -> "✅  Heard! Tap mic to record next tooth"
                    else -> "Tap the mic button and speak a tooth entry"
                },
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Mic button with animated rings
            Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                if (isListening) {
                    Box(modifier = Modifier.size(160.dp).scale(pulse2).background(Color.White.copy(alpha = 0.05f), CircleShape))
                    Box(modifier = Modifier.size(140.dp).scale(pulse).background(Color.White.copy(alpha = 0.08f), CircleShape))
                }
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            brush = when {
                                isListening -> Brush.radialGradient(listOf(Color(0xFFFF5252), Color(0xFFD32F2F)))
                                !hasPermission -> Brush.radialGradient(listOf(Color(0xFFFFB300), Color(0xFFFF8F00)))
                                else -> Brush.radialGradient(listOf(Color.White, Color(0xFFE8E4FF)))
                            },
                            shape = CircleShape
                        )
                        .clickable {
                            when {
                                !hasPermission -> permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                isListening -> viewModel.stopRecording()
                                else -> viewModel.startRecording(patientName)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Recording" else "Start Recording",
                        modifier = Modifier.size(46.dp),
                        tint = if (isListening || !hasPermission) Color.White else Color(0xFF5D48D1)
                    )
                }
            }

            // Waveform bars (purely visual, animated with pulse when listening)
            Row(
                modifier = Modifier.height(36.dp).padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barMultipliers = listOf(0.4f, 0.7f, 1.0f, 0.85f, 0.55f, 0.95f, 0.6f, 0.75f)
                barMultipliers.forEach { mul ->
                    val barH = if (isListening) (8f + (pulse - 1f) * 80f * mul).coerceAtLeast(6f) else 6f
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(barH.dp)
                            .background(
                                Color.White.copy(alpha = if (isListening) 0.85f else 0.2f),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Example Card (collapsible) ───────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { exampleExpanded = !exampleExpanded },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.13f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("How to Speak", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Icon(
                            if (exampleExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (exampleExpanded) {
                        Spacer(Modifier.height(12.dp))
                        Text("Speak tooth number + depth values:", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        VoiceSpeakExample("Tooth 11 - 3 4 5", "Saves tooth 11 with depths 3,4,5")
                        VoiceSpeakExample("Tooth twenty-one 2 3 4", "Saves tooth 21 with depths 2,3,4")
                        VoiceSpeakExample("11 3 4 5", "Short form — no \"tooth\" needed")
                        VoiceSpeakExample("Tooth eleven three four five", "Word numbers also work")
                        Spacer(Modifier.height(8.dp))
                        Divider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "📌  Depth range: 0–10 mm  |  Tooth range: 1–32",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "e.g. \"Tooth 11 - 3 4 5\"  →  Tooth 11: [3, 4, 5]",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ─── Recognized Text + Parse Status ──────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        statusMessage?.startsWith("Saved") == true || statusMessage?.startsWith("Saved & Synced") == true ->
                            Color(0xFF2E7D32).copy(alpha = 0.85f)
                        statusMessage?.startsWith("Could not parse") == true ->
                            Color(0xFFB71C1C).copy(alpha = 0.75f)
                        else -> Color.White.copy(alpha = 0.12f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (recognizedText.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Heard:", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = recognizedText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        // Parse status
                        if (statusMessage != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    if (statusMessage!!.startsWith("Saved")) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(statusMessage!!, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Icon(Icons.Default.Mic, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Speak a tooth entry above...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    // Error display
                    if (error != null && error!!.isNotEmpty() && !isListening) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFFFD54F), modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(error!!, color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Saved Tooth Entries ──────────────────────────────────────
            if (allToothData.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlaylistAddCheck, null, tint = Color(0xFF69F0AE), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Recorded Entries (${allToothData.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        allToothData.takeLast(8).reversed().forEach { tooth ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFF7B61FF), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${tooth.toothNumber}",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "Tooth ${tooth.toothNumber}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            "Depths: ${tooth.values.joinToString(", ")} mm",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                // Color badge for max depth
                                val maxDepth = tooth.values.maxOrNull() ?: 0
                                val badgeColor = when {
                                    maxDepth <= 3 -> Color(0xFF4CAF50)
                                    maxDepth <= 5 -> Color(0xFFFF9800)
                                    else -> Color(0xFFE53935)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "max ${maxDepth}mm",
                                        color = badgeColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        if (allToothData.size > 8) {
                            Text(
                                "+ ${allToothData.size - 8} more entries",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ─── Action Buttons ───────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    enabled = allToothData.isNotEmpty()
                ) {
                    Icon(Icons.Default.Analytics, null, tint = Color(0xFF5D48D1), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (allToothData.isNotEmpty()) "Analyze ${allToothData.size} Recorded Entries"
                        else "Record a tooth entry first",
                        color = Color(0xFF5D48D1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onSkip) {
                    Icon(Icons.Default.Edit, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Switch to Manual Grid", color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Back to Patient List")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun VoiceSpeakExample(command: String, result: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("🎤", fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Column {
            Text(
                "\"$command\"",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "→  $result",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(patientName: String, viewModel: ToothViewModel, onMenuClick: () -> Unit, onBack: () -> Unit, onGenerateCharts: () -> Unit, onNavigateToNotifications: () -> Unit) {
    val allToothData by viewModel.allToothData.collectAsState()
    val measurements = remember { mutableStateMapOf<String, String>() }
    var focusedSite by remember { mutableStateOf<String?>(null) } // e.g. "tooth1_ppd_0"

    // Pre-populate manual grid with voice input data (latest recording per tooth)
    LaunchedEffect(allToothData) {
        val latestToothData = allToothData.groupBy { it.toothNumber }.mapValues { entry ->
            entry.value.maxByOrNull { it.timestamp }!!
        }
        latestToothData.forEach { (toothNum, tooth) ->
            tooth.values.forEachIndexed { index, value ->
                val siteId = "tooth${toothNum}_ppd_$index"
                if (!measurements.containsKey(siteId)) {
                    measurements[siteId] = value.toString()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        // --- Custom Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text(
                "Periodontal AI System",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = Color(0xFF5D48D1)
            )
            IconButton(onClick = onNavigateToNotifications) {
                Box {
                    Icon(Icons.Default.NotificationsNone, null, tint = Color(0xFF5D48D1), modifier = Modifier.size(28.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape).align(Alignment.TopEnd).offset(x = (-2).dp, y = 2.dp))
                }
            }
        }

        // --- Header Section ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Manual Input", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Entering measurements for: ", fontSize = 16.sp, color = Color(0xFF5D48D1).copy(alpha = 0.7f))
                Text(patientName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D48D1))
            }
            Text("Enter PPD measurements for each tooth", fontSize = 14.sp, color = Color.Gray.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(28.dp))
        }

        // --- Tooth Cards Grid ---
        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(scrollState).padding(vertical = 12.dp)) {
                // Upper Jaw
                Text("Upper Jaw (Teeth 1-16)", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A).copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
                for (i in 1..16 step 2) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToothInputCard(
                            modifier = Modifier.weight(1f),
                            toothNumber = i,
                            measurements = measurements,
                            focusedSite = focusedSite,
                            onFocus = { focusedSite = it }
                        )
                        if (i + 1 <= 16) {
                            ToothInputCard(
                                modifier = Modifier.weight(1f),
                                toothNumber = i + 1,
                                measurements = measurements,
                                focusedSite = focusedSite,
                                onFocus = {focusedSite = it }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Lower Jaw
                Text("Lower Jaw (Teeth 17-32)", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A).copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
                for (i in 17..32 step 2) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToothInputCard(
                            modifier = Modifier.weight(1f),
                            toothNumber = i,
                            measurements = measurements,
                            focusedSite = focusedSite,
                            onFocus = { focusedSite = it }
                        )
                        if (i + 1 <= 32) {
                            ToothInputCard(
                                modifier = Modifier.weight(1f),
                                toothNumber = i + 1,
                                measurements = measurements,
                                focusedSite = focusedSite,
                                onFocus = { focusedSite = it }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- Bottom Clinical Keypad (If focused) ---
        if (focusedSite != null) {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(16.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Entering: ${focusedSite?.replace("_", " ")?.uppercase()}", fontWeight = FontWeight.Bold, color = Color.Gray)
                        IconButton(onClick = { focusedSite = null }) { Icon(Icons.Default.Close, null) }
                    }
                    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "C")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        keys.forEach { k ->
                            Button(
                                onClick = {
                                    if (k == "C") {
                                        measurements[focusedSite!!] = ""
                                    } else {
                                        measurements[focusedSite!!] = k
                                    }
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F7FF), contentColor = Color.Black)
                            ) {
                                Text(k, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- Bottom Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val ppd = measurements.filter { it.key.contains("_ppd_") }
                    viewModel.updateManualInput(ppd, emptyMap(), emptyMap())
                    onGenerateCharts()
                },
                modifier = Modifier.weight(1.5f).height(68.dp),
                shape = RoundedCornerShape(34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.BarChart, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Generate Charts", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(68.dp),
                shape = RoundedCornerShape(34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F2FF)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Back", color = Color(0xFF5D48D1), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }
}

@Composable
fun ToothInputCard(
    modifier: Modifier = Modifier,
    toothNumber: Int,
    measurements: MutableMap<String, String>,
    focusedSite: String?,
    onFocus: (String) -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tooth $toothNumber", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1A1A1A))
                Icon(Icons.Default.Edit, null, tint = Color(0xFF7B61FF).copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("PPD (mm)", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { index ->
                    val siteId = "tooth${toothNumber}_ppd_$index"
                    MeasurementBox(
                        value = measurements[siteId] ?: "-",
                        isFilled = measurements[siteId]?.isNotEmpty() == true,
                        isFocused = focusedSite == siteId,
                        onClick = { onFocus(siteId) }
                    )
                }
            }
        }
    }
}

@Composable
fun MeasurementBox(value: String, isFilled: Boolean = false, isFocused: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 38.dp)
            .background(if (isFilled) Color.White else Color(0xFFF8F9FF), RoundedCornerShape(8.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color(0xFF5D48D1) else if (isFilled) Color(0xFF7B61FF).copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = if (isFocused) Color(0xFF5D48D1) else if (isFilled) Color(0xFF5D48D1) else Color.LightGray
        )
    }
}





@Composable
fun ProcessingExaminationScreen(onMenuClick: () -> Unit, onNavigateToNotifications: () -> Unit, onProcessingComplete: () -> Unit) {
    LaunchedEffect(Unit) { delay(2500); onProcessingComplete() }
    Column(modifier = Modifier.fillMaxSize().background(Color.White), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(100.dp), color = Color(0xFF5D48D1), strokeWidth = 8.dp)
            AppLogo(modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Crunching Clinical Data", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("AI is running periodontal classification...", color = Color.Gray)
        Spacer(modifier = Modifier.height(60.dp))
        LinearProgressIndicator(modifier = Modifier.width(200.dp).height(6.dp).clip(CircleShape))
    }
}

// --- RESULTS & REPORTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsSummaryScreen(viewModel: ToothViewModel, onMenuClick: () -> Unit, onBack: () -> Unit, onViewAIClassification: () -> Unit, onNavigateToNotifications: () -> Unit) {
    val meanPpd by viewModel.meanPpd.collectAsState()
    val meanCal by viewModel.meanCal.collectAsState()
    val bopPercentage by viewModel.bopPercentage.collectAsState()
    val deepPocketsCount by viewModel.deepPocketsCount.collectAsState()
    
    val progressRatio = (deepPocketsCount.toFloat() / 96f).coerceIn(0f, 1f)
    val pctExamArea = Math.round(progressRatio * 100f)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("CLINICAL ANALYTICS", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF5D48D1)) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Text("Stat Summary", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text("Automated extraction of periodontal metrics", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))

            StatCard(Icons.Default.LinearScale, Color(0xFF9C27B0), "${meanPpd}mm", "Mean PPD", if (meanPpd > 3.0f) "+0.2" else "+0.0", if (meanPpd > 3.0f) Color.Red else Color(0xFF4CAF50))
            StatCard(Icons.Default.Bloodtype, Color.Red, "${bopPercentage.toInt()}%", "BOP Sites", if (bopPercentage > 30f) "+5%" else "-5%", if (bopPercentage > 30f) Color.Red else Color(0xFF4CAF50))
            StatCard(Icons.Default.Height, Color(0xFF4CAF50), "${meanCal}mm", "Mean CAL", if (meanCal > 2.0f) "+0.1" else "+0.0", if (meanCal > 2.0f) Color.Red else Color(0xFF4CAF50))

            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Deep Pockets Analysis (>4mm)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A1A1A))
                    Text("$deepPocketsCount total sites identified in current exam", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(16.dp).background(Color(0xFFF5F7FF), CircleShape)) {
                        Box(modifier = Modifier.fillMaxWidth(progressRatio).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFFFF7043), Color.Red)), CircleShape))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$pctExamArea% of exam area", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (deepPocketsCount > 5) Color.Red else Color.Gray)
                        Text(if (deepPocketsCount > 5) "Action Required" else "Low Risk", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (deepPocketsCount > 5) Color.Red else Color(0xFF4CAF50))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onViewAIClassification,
                modifier = Modifier.weight(1.5f).height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
            ) {
                Text("AI Diagnostic Verdict", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(32.dp)) { Text("Back", fontSize = 17.sp) }
        }
    }
}


@Composable
fun AIClassificationScreen(viewModel: ToothViewModel, onMenuClick: () -> Unit, onBack: () -> Unit, onGenerateReport: () -> Unit, onNavigateToNotifications: () -> Unit) {
    val verdict by viewModel.clinicalVerdict.collectAsState()
    val stageAndGrade by viewModel.stageAndGrade.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("AI Clinical Verdict", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Text("AI Diagnostic", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("AAPL/EFP 2018 Guidelines Engine", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).background(Color(0xFFF0F2FF), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoGraph, null, tint = Color(0xFF5D48D1), modifier = Modifier.size(40.dp)) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("STAGING & GRADING:", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text(verdict, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = if (verdict.contains("Severe")) Color(0xFFD84315) else if (verdict.contains("Moderate")) Color(0xFFFF9800) else Color(0xFF4CAF50), textAlign = TextAlign.Center)
                    Text(stageAndGrade, fontSize = 20.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(32.dp))
                    Divider(thickness = 0.5.dp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("CLINICAL RECOMMENDATIONS:", modifier = Modifier.align(Alignment.Start), color = Color(0xFF5D48D1), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    when {
                        verdict.contains("Healthy") -> {
                            ClassificationRecommendationItem("Maintain excellent oral hygiene habits")
                            ClassificationRecommendationItem("Routine 6-month clinical evaluation and cleaning")
                            ClassificationRecommendationItem("Continue daily flossing and brushing twice a day")
                        }
                        verdict.contains("Mild") -> {
                            ClassificationRecommendationItem("Oral hygiene instruction and plaque control reinforcement")
                            ClassificationRecommendationItem("Localized scaling and polishing if necessary")
                            ClassificationRecommendationItem("Standard prophylaxis and supportive therapy")
                            ClassificationRecommendationItem("Review of brushing and flossing techniques")
                        }
                        verdict.contains("Moderate") -> {
                            ClassificationRecommendationItem("Scaling and Root Planing (SRP) for affected areas")
                            ClassificationRecommendationItem("Assessment of systemic risk factors (Diabetes/Smoking)")
                            ClassificationRecommendationItem("Periodontal maintenance interval of 3-4 months")
                            ClassificationRecommendationItem("Re-evaluation of pocket depths in 4-6 weeks")
                        }
                        else -> {
                            ClassificationRecommendationItem("Full-mouth mechanical debridement (SRP)")
                            ClassificationRecommendationItem("Assessment of systemic risk factors (Diabetes/Smoking)")
                            ClassificationRecommendationItem("Strict 3-month periodontal maintenance interval")
                            ClassificationRecommendationItem("Surgical therapy evaluation or referral to specialist")
                            ClassificationRecommendationItem("Re-evaluation of pocket depths in 4-6 weeks")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onGenerateReport, modifier = Modifier.weight(1.5f).height(64.dp), shape = RoundedCornerShape(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))) { Text("View Diagnostics", fontWeight = FontWeight.Bold) }
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(32.dp)) { Text("Back") }
        }
    }
}


@Composable
fun ProbabilityBreakdownScreen(viewModel: ToothViewModel, onMenuClick: () -> Unit, onBack: () -> Unit, onGenerateReport: () -> Unit, onNewExamination: () -> Unit, onBackToDashboard: () -> Unit, onNavigateToNotifications: () -> Unit) {
    val probModerate by viewModel.probModerate.collectAsState()
    val probSevere by viewModel.probSevere.collectAsState()
    val probHealthy by viewModel.probHealthy.collectAsState()
    val verdict by viewModel.clinicalVerdict.collectAsState()

    val maxConfidence = maxOf(probModerate, probSevere, probHealthy)
    val confidenceLabel = if (maxConfidence > 80) "High Conf." else if (maxConfidence > 50) "Moderate Conf." else "Low Conf."
    
    val sweepAngle = 360f * (maxConfidence.toFloat() / 100f)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("AI Diagnostics", fontWeight = FontWeight.Bold)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Text("Classification Probabilities", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Confidence analysis from deep learning model", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(Color.White, radius = size.minDimension / 2, center = center)
                    drawCircle(Color(0xFFF0F2FF).copy(alpha = 0.5f), radius = size.minDimension / 2.2f, center = center)

                    drawArc(Color(0xFF5D48D1), -90f, sweepAngle, false, style = Stroke(28.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(Color(0xFF7B61FF).copy(alpha = 0.2f), -90f + sweepAngle, 360f - sweepAngle, false, style = Stroke(28.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$maxConfidence%", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
                    Text(confidenceLabel, color = Color(0xFF5D48D1), fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
            ProbabilityBar("Moderate Periodontitis", probModerate, Color(0xFF5D48D1))
            ProbabilityBar("Severe Periodontitis", probSevere, Color(0xFFD84315))
            ProbabilityBar("Healthy Periodontium", probHealthy, Color(0xFF4CAF50))
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onGenerateReport, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))) {
                Icon(Icons.Default.PictureAsPdf, null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Comprehensive PDF", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onNewExamination, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp), shape = RoundedCornerShape(28.dp)) { Text("Start New Exam") }
            TextButton(onClick = onBackToDashboard, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) { Text("Back to System Dashboard", fontWeight = FontWeight.Bold, color = Color(0xFF5D48D1)) }
        }
    }
}


@Composable
fun ProbabilityBar(label: String, pct: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, fontWeight = FontWeight.Bold); Text("$pct%", fontWeight = FontWeight.Bold, color = color) }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(progress = pct / 100f, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = color, trackColor = Color.LightGray.copy(alpha = 0.2f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateReportScreen(onMenuClick: () -> Unit, onBack: () -> Unit, onGenerateSuccess: () -> Unit, onNavigateToNotifications: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("REPORT CONFIG", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF5D48D1)) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Text("Generate Report", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text("Select specific clinical datasets for inclusion", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(40.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    ReportOptionRow("Periodontal Charts (Buccal/Lingual)", true)
                    Divider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                    ReportOptionRow("AI Diagnostic Breakdown", true)
                    Divider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                    ReportOptionRow("Patient Longitudinal History", true)
                    Divider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                    ReportOptionRow("Treatment Recommendations", true)
                    Divider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                    ReportOptionRow("Clinic Branding & Logo", true)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Output Format: High-Fidelity PDF", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onGenerateSuccess,
                modifier = Modifier.weight(1.5f).height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
            ) {
                Text("Compile Report", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(32.dp)) { Text("Cancel", fontSize = 17.sp) }
        }
    }
}


@Composable
fun ReportOptionRow(label: String, initial: Boolean) {
    var checked by remember { mutableStateOf(initial) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        Switch(checked = checked, onCheckedChange = { checked = it })
    }
}

@Composable
fun ReportPreviewScreen(viewModel: ToothViewModel, onMenuClick: () -> Unit, onBack: () -> Unit, onDownload: () -> Unit, onShare: () -> Unit, onDone: () -> Unit, onNavigateToNotifications: () -> Unit) {
    val patientName by viewModel.activePatientName.collectAsState()
    val doctorName by viewModel.clinicalUserName.collectAsState()
    val verdict by viewModel.clinicalVerdict.collectAsState()
    val stageAndGrade by viewModel.stageAndGrade.collectAsState()
    val meanPpd by viewModel.meanPpd.collectAsState()
    val bopPercentage by viewModel.bopPercentage.collectAsState()
    val deepPocketsCount by viewModel.deepPocketsCount.collectAsState()
    
    val examId = 44000 + Math.abs(patientName.hashCode()) % 1000

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("REPORT PREVIEW", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF5D48D1)) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f)) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AppLogo(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Clinical Periodontal Report", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A1A))
                    Text("$patientName | Exam ID: $examId", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(32.dp))
                    Divider(thickness = 0.5.dp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(24.dp))

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ReportPreviewItem("AI Verdict", "$verdict ($stageAndGrade)")
                        ReportPreviewItem("Mean PPD", "$meanPpd mm")
                        ReportPreviewItem("BOP Positive", "${bopPercentage.toInt()}% of sites")
                        ReportPreviewItem("Deep Pockets", "$deepPocketsCount sites >= 4mm identified")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF5F7FF), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BarChart, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Text("PPD / CAL Cartogram Rendering...", color = Color.LightGray, fontStyle = FontStyle.Italic, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Electronically Signed: Dr. $doctorName", fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onDownload, modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.FileDownload, null); Spacer(Modifier.width(8.dp)); Text("Save PDF") }
                Button(onClick = onShare, modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Share") }
            }
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(64.dp), shape = RoundedCornerShape(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))) { Text("Finalize & Return", fontWeight = FontWeight.Bold, fontSize = 17.sp) }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp), shape = RoundedCornerShape(28.dp)) { Text("Adjust Configuration") }
        }
    }
}

@Composable
fun ReportPreviewItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
    }
}


@Composable
fun ShareReportScreen(
    viewModel: ToothViewModel,
    onMenuClick: () -> Unit,
    onBack: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val patientName by viewModel.activePatientName.collectAsState()
    val doctorName by viewModel.clinicalUserName.collectAsState()
    val verdict by viewModel.clinicalVerdict.collectAsState()
    val stageAndGrade by viewModel.stageAndGrade.collectAsState()
    val meanPpd by viewModel.meanPpd.collectAsState()
    val meanCal by viewModel.meanCal.collectAsState()
    val bopPercentage by viewModel.bopPercentage.collectAsState()
    val deepPocketsCount by viewModel.deepPocketsCount.collectAsState()

    val onShare = { specificPackage: String? ->
        try {
            val file = generateReportPdf(
                context = context,
                patientName = patientName,
                doctorName = doctorName,
                verdict = verdict,
                stageAndGrade = stageAndGrade,
                meanPpd = meanPpd,
                meanCal = meanCal,
                bopPercentage = bopPercentage,
                deepPocketsCount = deepPocketsCount
            )
            sharePdfFile(context, file, specificPackage)
        } catch (e: Exception) {
            // Silence/handled
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Share Report", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onShare(null) }
                    ) {
                        Icon(Icons.Default.Share, null, tint = Color(0xFF5D48D1))
                        Text("Share", fontSize = 12.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onShare("com.whatsapp") }
                    ) {
                        Icon(Icons.Default.Send, null, tint = Color(0xFF25D366))
                        Text("WhatsApp", fontSize = 12.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onShare(null) }
                    ) {
                        Icon(Icons.Default.Email, null, tint = Color(0xFF5D48D1))
                        Text("Email", fontSize = 12.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onShare(null) }
                    ) {
                        Icon(Icons.Default.Print, null, tint = Color(0xFF5D48D1))
                        Text("Print", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DownloadReportScreen(
    viewModel: ToothViewModel,
    onMenuClick: () -> Unit,
    onBack: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val patientName by viewModel.activePatientName.collectAsState()
    val doctorName by viewModel.clinicalUserName.collectAsState()
    val verdict by viewModel.clinicalVerdict.collectAsState()
    val stageAndGrade by viewModel.stageAndGrade.collectAsState()
    val meanPpd by viewModel.meanPpd.collectAsState()
    val meanCal by viewModel.meanCal.collectAsState()
    val bopPercentage by viewModel.bopPercentage.collectAsState()
    val deepPocketsCount by viewModel.deepPocketsCount.collectAsState()

    var statusText by remember { mutableStateOf("Generating PDF Clinical Report...") }
    var isDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val cachedFile = generateReportPdf(
                context = context,
                patientName = patientName,
                doctorName = doctorName,
                verdict = verdict,
                stageAndGrade = stageAndGrade,
                meanPpd = meanPpd,
                meanCal = meanCal,
                bopPercentage = bopPercentage,
                deepPocketsCount = deepPocketsCount
            )
            val savedFile = savePdfToDownloads(context, cachedFile)
            if (savedFile != null) {
                statusText = "Report saved to Downloads!\nFile name: ${savedFile.name}"
            } else {
                statusText = "Failed to save PDF to Downloads."
            }
        } catch (e: Exception) {
            statusText = "Error saving PDF: ${e.localizedMessage}"
        } finally {
            isDone = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isDone) {
            CircularProgressIndicator(color = Color(0xFF5D48D1))
        } else {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = statusText,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1)),
            modifier = Modifier.width(200.dp).height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("Back to Report", color = Color.White)
        }
    }
}

@Composable
fun ReportsScreen(
    viewModel: ToothViewModel,
    onMenuClick: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onStartExamination: () -> Unit
) {
    val patients by viewModel.patients.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("REPORT ARCHIVE", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF5D48D1)) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f)) {
            Text("Clinical Reports", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text("Review and export historical periodontal data", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                patients.forEachIndexed { index, name ->
                    val date = when (index % 4) {
                        0 -> "04/22/2026"
                        1 -> "04/20/2026"
                        2 -> "04/18/2026"
                        else -> "04/15/2026"
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).background(Color(0xFFFFEBEE), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1A1A1A))
                                Text("Examination: $date", fontSize = 13.sp, color = Color.Gray)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onStartExamination,
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(12.dp))
                Text("Initiate New Examination", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


// --- PATIENTS & FILTER ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPatientsScreen(viewModel: ToothViewModel, onMenuClick: () -> Unit, onNewPatientClick: () -> Unit, onPatientClick: (String) -> Unit, onNavigateToNotifications: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val patients by viewModel.patients.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("PATIENT DIRECTORY", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF5D48D1), letterSpacing = 1.sp)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF5D48D1)) }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp).weight(1f).verticalScroll(rememberScrollState())) {
            Text("Directory", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
            Text("Manage clinical records and patient profiles", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name, ID or DOB...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF5D48D1)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF5D48D1), focusedLabelColor = Color(0xFF5D48D1))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNewPatientClick,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
            ) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(12.dp))
                Text("Register New Patient", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text("Active Patients", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            patients.filter { it.contains(query, ignoreCase = true) }.forEach { p ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onPatientClick(p) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(52.dp).background(Color(0xFFF0F2FF), CircleShape), contentAlignment = Alignment.Center) {
                            Text(p.take(1), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF5D48D1))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1A1A1A))
                            Text("ID: P-99${p.hashCode().toString().takeLast(3)}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("04/22/2026", fontSize = 12.sp, color = Color.Gray)
                            Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun FilterDashboardScreen(onMenuClick: () -> Unit, onNavigateToNotifications: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("Dash Filters", fontWeight = FontWeight.Bold)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null) }
        }
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Filter View", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            ReportOptionRow("Show High Risk (Stage III/IV)", true)
            ReportOptionRow("Show Pending Measurements", true)
            ReportOptionRow("Show Maintenance Overdue", false)
            ReportOptionRow("Show Recent AI Successes", true)
        }
    }
}

// --- PROFILE & SETTINGS ---

@Composable
fun UserProfileScreen(onMenuClick: () -> Unit, onNavigateToNotifications: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("Dr. Sarah Johnson") }
    var license by remember { mutableStateOf("DDS-123456789") }
    var clinic by remember { mutableStateOf("Johnson Periodontal Center") }
    var email by remember { mutableStateOf("dr.sarah@clinic.com") }
    var password by remember { mutableStateOf("securePassword123") }
    
    var showError by remember { mutableStateOf<String?>(null) }

    if (showError != null) {
        AlertDialog(
            onDismissRequest = { showError = null },
            confirmButton = { TextButton(onClick = { showError = null }) { Text("OK") } },
            title = { Text("Input Error") },
            text = { Text(showError!!) }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("Clinical Identity", fontWeight = FontWeight.Bold)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null) }
        }
        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(120.dp).background(Color.White, CircleShape).padding(4.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF5D48D1), CircleShape), contentAlignment = Alignment.Center) { Text(name.take(2).uppercase(), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isEditing) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Login Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Login Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("Medical License") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = clinic, onValueChange = { clinic = it }, label = { Text("Clinic Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (!email.contains("@")) {
                            showError = "Invalid email format. Must contain '@'"
                        } else if (password.length < 8) {
                            showError = "Password too short. Minimum 8 characters required."
                        } else if (name.isBlank()) {
                            showError = "Name cannot be empty."
                        } else {
                            // In a real app, we would update credentials in a ViewModel/Repo here
                            isEditing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Chief Periodontist", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(40.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        ProfileInfoRow(Icons.Default.Badge, "License", license)
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        ProfileInfoRow(Icons.Default.Business, "Clinic", clinic)
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        ProfileInfoRow(Icons.Default.Email, "Email", email)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1))
                ) {
                    Text("Edit Clinical Profile", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF5D48D1), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column { Text(label, fontSize = 12.sp, color = Color.Gray); Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onMenuClick: () -> Unit, onBack: () -> Unit, onNavigateToNotifications: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FF))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color(0xFF5D48D1)) }
            Text("System Settings", fontWeight = FontWeight.Bold)
            IconButton(onClick = onNavigateToNotifications) { Icon(Icons.Outlined.Notifications, null) }
        }
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Settings", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            SettingsToggleRow("Push Notifications", true)
            SettingsToggleRow("Cloud Backup", true)
            SettingsToggleRow("Biometric Lock", false)
            SettingsToggleRow("Auto-Generate PDF", true)
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(30.dp)) { Text("Return to Previous Screen", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun SettingsToggleRow(label: String, initial: Boolean) {
    var checked by remember { mutableStateOf(initial) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = { checked = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF5D48D1)))
    }
}

// --- CLINICAL AI XAMPP CONTROL PANEL ---

@Composable
fun ClinicalAIEnginePanel(
    viewModel: ToothViewModel,
    modifier: Modifier = Modifier
) {
    val isTraining by viewModel.isTrainingModel.collectAsState()
    val accuracy by viewModel.aiModelAccuracy.collectAsState()
    val status by viewModel.aiTrainingStatus.collectAsState()
    val isCustomModel by viewModel.isCustomModelTrained.collectAsState()
    
    var showMessage by remember { mutableStateOf<String?>(null) }
    
    if (showMessage != null) {
        AlertDialog(
            onDismissRequest = { showMessage = null },
            confirmButton = { TextButton(onClick = { showMessage = null }) { Text("OK") } },
            title = { Text("Clinical AI Vault") },
            text = { Text(showMessage!!) }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).background(Color(0xFF7B61FF).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, null, tint = Color(0xFF5D48D1), modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Firebase AI Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A1A))
                        Text(
                            text = status ?: "Connected to Firebase",
                            fontSize = 11.sp,
                            color = if (status?.contains("Offline") == true) Color.Red else Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Surface(
                    color = if (isCustomModel) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isCustomModel) "Custom Trained" else "Base Model",
                        color = if (isCustomModel) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Divider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Connection Status", fontSize = 11.sp, color = Color.Gray)
                    Text("ACTIVE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.trainAIModel(
                            onSuccess = { acc ->
                                showMessage = "Model retrained successfully on Firebase Firestore!\nOptimized Accuracy: $acc%"
                            },
                            onError = { err ->
                                showMessage = "AI Training Failed:\n$err"
                            }
                        )
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D48D1)),
                    enabled = !isTraining
                ) {
                    if (isTraining) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Retrain AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                OutlinedButton(
                    onClick = {
                        viewModel.addTrainingSample(
                            onSuccess = {
                                showMessage = "Successfully contributed this case to the Firebase Firestore!\nThis sample will be used to make future predictions more precise."
                            },
                            onError = { err ->
                                showMessage = "Database Error:\n$err"
                            }
                        )
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF5D48D1).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5D48D1))
                ) {
                    Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Case", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

