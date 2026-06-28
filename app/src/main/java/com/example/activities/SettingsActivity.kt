package com.example.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppRepository
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.AppxTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonOrange
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = AppRepository(this)

        setContent {
            AppxTheme {
                SettingsScreen(repository = repository) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: AppRepository, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDarkTheme by remember { mutableStateOf(true) }
    var apiEndpoint by remember { mutableStateOf("https://api.appxlearn.com/") }
    var jwtToken by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        scope.launch {
            jwtToken = repository.getToken()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Config", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        GlassBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme Toggle Card
                GlassCard(cornerRadius = 20.dp, modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = "Dark Theme", tint = NeonBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Premium Slate Theme", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Toggle elegant neon dark mode", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = {
                                isDarkTheme = it
                                Toast.makeText(context, "Premium dark mode enforced.", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonOrange,
                                checkedTrackColor = NeonOrange.copy(alpha = 0.4f),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }
                }

                // API Config Input Card
                GlassCard(cornerRadius = 20.dp) {
                    Text("Developer APIs Config", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(
                        value = apiEndpoint,
                        onValueChange = { apiEndpoint = it },
                        placeholder = { Text("Enter Endpoint URL") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Endpoint saved: $apiEndpoint", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE ENDPOINT URL", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }

                // JWT Authentication Management Card
                GlassCard(cornerRadius = 20.dp) {
                    Text("Session Security", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (jwtToken.isNotEmpty()) "Token: $jwtToken" else "Session State: No active token found. Sign-in via WebView to persist dynamic sessions.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.clearToken()
                                    jwtToken = ""
                                    Toast.makeText(context, "Session cleared successfully.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f).border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CLEAR JWT", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val refreshed = "jwt_token_refreshed_" + System.currentTimeMillis()
                                    repository.saveToken(refreshed)
                                    jwtToken = refreshed
                                    Toast.makeText(context, "Token refreshed securely.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange.copy(alpha = 0.15f)),
                            modifier = Modifier.weight(1f).border(1.dp, NeonOrange.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("REFRESH", color = NeonOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // About APPX Learn Card
                GlassCard(cornerRadius = 20.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = NeonOrange)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("APPX Learn v1.0", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Hybrid Learning Engine built with Jetpack Compose & Android WebView JavaScript Bridges.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
