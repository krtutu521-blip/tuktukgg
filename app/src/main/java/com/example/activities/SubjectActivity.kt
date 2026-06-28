package com.example.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.model.Lecture
import com.example.data.model.DownloadItem
import com.example.data.repository.AppRepository
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingProgressIndicator
import com.example.ui.components.SkeletonLoader
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

class SubjectActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = AppRepository(this)
        val courseId = intent.getStringExtra("COURSE_ID") ?: "course_android_compose"
        val courseName = intent.getStringExtra("COURSE_NAME") ?: "Course Stream"

        setContent {
            AppxTheme {
                SubjectScreenContainer(
                    repository = repository,
                    courseId = courseId,
                    courseName = courseName
                ) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreenContainer(
    repository: AppRepository,
    courseId: String,
    courseName: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }
    var lectures by remember { mutableStateOf<List<Lecture>>(emptyList()) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var isLoadingSubjects by remember { mutableStateOf(true) }
    var isLoadingLectures by remember { mutableStateOf(false) }

    // Fetch subjects on startup
    LaunchedEffect(key1 = courseId) {
        scope.launch {
            try {
                subjects = repository.getSubjects(courseId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingSubjects = false
            }
        }
    }

    // Fetch lectures when subject is selected
    LaunchedEffect(key1 = selectedSubject) {
        val subject = selectedSubject
        if (subject != null) {
            isLoadingLectures = true
            scope.launch {
                try {
                    lectures = repository.getLectures(subject.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoadingLectures = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedSubject?.name ?: courseName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedSubject != null) {
                                selectedSubject = null
                            } else {
                                onBackPressed()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        GlassBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                if (selectedSubject == null) {
                    // 1. SUBJECTS LIST SCREEN
                    if (isLoadingSubjects) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                        ) {
                            repeat(3) { SkeletonLoader(height = 110.dp, shape = RoundedCornerShape(20.dp)) }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                        ) {
                            items(subjects) { subject ->
                                val icon = when (subject.iconName) {
                                    "view_quilt" -> Icons.Default.ViewQuilt
                                    "sync_alt" -> Icons.Default.SyncAlt
                                    "palette" -> Icons.Default.Palette
                                    else -> Icons.Default.Book
                                }

                                GlassCard(
                                    cornerRadius = 20.dp,
                                    glowColor = NeonBlueGlow,
                                    onClick = { selectedSubject = subject }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(NeonBlue.copy(alpha = 0.15f))
                                                .border(1.dp, NeonBlue.copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(icon, contentDescription = "Icon", tint = NeonBlue)
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(subject.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Text("Videos: ${subject.totalVideos}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                                Text("Notes: ${subject.totalNotes}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                LinearProgressIndicator(
                                                    progress = subject.progressPercent / 100f,
                                                    modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(50)),
                                                    color = NeonBlue,
                                                    trackColor = Color.White.copy(alpha = 0.1f)
                                                )
                                                Text("${subject.progressPercent}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonBlue)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 2. LECTURE SCREEN
                    if (isLoadingLectures) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            GlowingProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                        ) {
                            items(lectures) { lecture ->
                                GlassCard(cornerRadius = 20.dp) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = lecture.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Tutor: ${lecture.teacher} • Duration: ${lecture.duration}",
                                                    fontSize = 12.sp,
                                                    color = Color.White.copy(alpha = 0.6f)
                                                )
                                            }
                                            if (lecture.isCompleted) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(NeonBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                        .border(1.dp, NeonBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("COMPLETED", color = NeonBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Play, PDF, Download Buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Play Button
                                            Button(
                                                onClick = {
                                                    val intent = Intent(context, PlayerActivity::class.java).apply {
                                                        putExtra("VIDEO_URL", lecture.videoUrl)
                                                        putExtra("LECTURE_ID", lecture.id)
                                                        putExtra("LECTURE_TITLE", lecture.title)
                                                    }
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("PLAY", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // PDF Button
                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(context, PdfActivity::class.java).apply {
                                                        putExtra("PDF_URL", lecture.pdfUrl)
                                                        putExtra("PDF_TITLE", lecture.title + " - Notes")
                                                    }
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                            ) {
                                                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Notes", tint = NeonBlue)
                                            }

                                            // Download Button
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        val destFile = File(context.filesDir, "lec_${lecture.id}.mp4")
                                                        val dlItem = DownloadItem(
                                                            id = lecture.id,
                                                            url = lecture.videoUrl,
                                                            title = lecture.title,
                                                            filePath = destFile.absolutePath,
                                                            status = "COMPLETED", // Download immediately simulated as completed for offline playback!
                                                            progress = 100
                                                        )
                                                        repository.insertOrUpdateDownload(dlItem)
                                                        Toast.makeText(context, "Saved to offline Downloads queue!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                            ) {
                                                Icon(
                                                    imageVector = if (lecture.isDownloaded) Icons.Default.CloudDone else Icons.Default.Download,
                                                    contentDescription = "Download",
                                                    tint = if (lecture.isDownloaded) NeonOrange else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
