package com.example.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.LiveClass
import com.example.data.model.Course
import com.example.data.repository.AppRepository
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingProgressIndicator
import com.example.ui.components.SkeletonLoader
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = AppRepository(this)

        setContent {
            AppxTheme {
                HomeScreen(repository = repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: AppRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var liveClasses by remember { mutableStateOf<List<LiveClass>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Navigation tab index: 0 = Home, 1 = Downloads, 2 = Profile, 3 = Settings
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = true) {
        scope.launch {
            try {
                courses = repository.getCourses()
                liveClasses = repository.getLiveClasses()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonOrange,
                        selectedTextColor = NeonOrange,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = NeonOrangeGlow
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        context.startActivity(Intent(context, DownloadActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Downloads") },
                    label = { Text("Downloads", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonOrange,
                        selectedTextColor = NeonOrange,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = NeonOrangeGlow
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonOrange,
                        selectedTextColor = NeonOrange,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = NeonOrangeGlow
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonOrange,
                        selectedTextColor = NeonOrange,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = NeonOrangeGlow
                    )
                )
            }
        }
    ) { innerPadding ->
        GlassBackground {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GlowingProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading Dashboard...", color = Color.White, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header / Search Bar
                    item {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Hello, Karan!",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Let's learn something new today",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(CircleShape)
                                        .background(NeonOrange)
                                        .clickable {
                                            context.startActivity(Intent(context, ProfileActivity::class.java))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Search bar
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search your interest...", color = Color.White.copy(alpha = 0.5f)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f)),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = NeonOrange,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }

                    // Banner Slider promotion (Glass Card)
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            glowColor = NeonOrangeGlow
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .background(NeonOrange, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("NEW RELEASE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Jetpack Compose 1.8", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Learn state performance optimization & graphics pipelines", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color.White.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.School, contentDescription = "Icon", tint = NeonOrange, modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }

                    // Continue Learning section
                    val activeCourse = courses.firstOrNull { it.progress > 0.1f }
                    if (activeCourse != null) {
                        item {
                            Column {
                                Text("Continue Learning", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(10.dp))
                                GlassCard(
                                    onClick = {
                                        val intent = Intent(context, CourseActivity::class.java).apply {
                                            putExtra("COURSE", activeCourse)
                                        }
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = activeCourse.thumbnail,
                                            contentDescription = "Thumbnail",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(activeCourse.title, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("By ${activeCourse.teacher}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = activeCourse.progress,
                                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                                color = NeonBlue,
                                                trackColor = Color.White.copy(alpha = 0.1f)
                                            )
                                        }
                                        Text("${(activeCourse.progress * 100).toInt()}%", modifier = Modifier.padding(start = 8.dp), color = NeonBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Live Classes Section (Pulsing live badge!)
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔴 Live Classes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("View All", fontSize = 12.sp, color = NeonOrange, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(liveClasses) { live ->
                                    GlassCard(
                                        modifier = Modifier.width(240.dp),
                                        cornerRadius = 16.dp,
                                        onClick = {
                                            val intent = Intent(context, PlayerActivity::class.java).apply {
                                                putExtra("VIDEO_URL", live.streamUrl)
                                                putExtra("LECTURE_TITLE", live.title)
                                            }
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (live.isLive) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.Red, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.DarkGray, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("UPCOMING", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(live.startTime, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(live.title, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Speaker: ${live.teacher}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }

                    // Featured Courses Section
                    item {
                        Column {
                            Text("Featured Courses", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    val filteredCourses = courses.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
                    }

                    items(filteredCourses) { course ->
                        GlassCard(
                            cornerRadius = 20.dp,
                            onClick = {
                                val intent = Intent(context, CourseActivity::class.java).apply {
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
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(course.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    Text("By ${course.teacher}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text("Lectures: ${course.totalLectures}", fontSize = 11.sp, color = NeonBlue)
                                        Text("PDFs: ${course.totalPdfs}", fontSize = 11.sp, color = NeonOrange)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = course.progress,
                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                        color = NeonOrange,
                                        trackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
