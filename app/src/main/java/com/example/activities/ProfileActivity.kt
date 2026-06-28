package com.example.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.data.repository.AppRepository
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingProgressIndicator
import com.example.ui.theme.AppxTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonOrangeGlow
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = AppRepository(this)

        setContent {
            AppxTheme {
                ProfileScreen(repository = repository) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(repository: AppRepository, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCertificate by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = true) {
        scope.launch {
            try {
                profile = repository.getUserProfile()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
            if (isLoading || profile == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    GlowingProgressIndicator()
                }
            } else {
                val user = profile!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Profile Header Card (Glass Card)
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            cornerRadius = 20.dp,
                            glowColor = NeonOrangeGlow
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = user.avatar,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, NeonOrange, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = user.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "krtutu521@gmail.com",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .background(NeonBlue.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                        .border(1.dp, NeonBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Premium Member", color = NeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Progress Overview Arc
                    item {
                        GlassCard(cornerRadius = 20.dp) {
                            Text("Learning Milestones", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Progress", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text("${(user.overallProgress * 100).toInt()}% Done", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonOrange)
                                }
                                Box(
                                    modifier = Modifier.size(64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = user.overallProgress,
                                        color = NeonOrange,
                                        strokeWidth = 6.dp,
                                        modifier = Modifier.fillMaxSize(),
                                        trackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = NeonOrange)
                                }
                            }
                        }
                    }

                    // Certificates List (Horizontal or vertical)
                    item {
                        Column {
                            Text("My Certificates", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            if (user.certificates.isEmpty()) {
                                Text("No certificates earned yet. Finish 100% of a course to unlock certificates!", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                            } else {
                                user.certificates.forEach { cert ->
                                    GlassCard(
                                        cornerRadius = 16.dp,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        onClick = { selectedCertificate = cert }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CardMembership, contentDescription = "Cert", tint = NeonBlue)
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(cert, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                                    Text("Verified Certificate", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                                                }
                                            }
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = NeonBlue)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Purchased Courses List
                    item {
                        Column {
                            Text("Registered Courses", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    items(user.purchasedCourses) { course ->
                        GlassCard(
                            cornerRadius = 16.dp,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            onClick = {
                                val intent = android.content.Intent(context, CourseActivity::class.java).apply {
                                    putExtra("COURSE", course)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = course.thumbnail,
                                    contentDescription = "Thumbnail",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text("By ${course.teacher}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Beautiful Certificate Modal Overlay dialog!
            selectedCertificate?.let { cert ->
                AlertDialog(
                    onDismissRequest = { selectedCertificate = null },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CardMembership, contentDescription = "Cert", tint = NeonOrange, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Official Achievement Certificate", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .padding(20.dp)
                        ) {
                            Text("PROUDLY PRESENTED TO", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(profile?.name ?: "Student", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("for successfully completing and mastering all lectures, notes, and criteria for the course:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(cert, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeonBlue, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("ID: CERT-${cert.hashCode().coerceAtLeast(10000)} • Date: June 2026", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedCertificate = null }) {
                            Text("CLOSE", color = NeonOrange, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = Color(0xFF0F172A)
                )
            }
        }
    }
}
