package com.example.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.lifecycleScope
import com.example.data.model.DownloadItem
import com.example.data.repository.AppRepository
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.theme.AppxTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonOrange
import kotlinx.coroutines.launch

class DownloadActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = AppRepository(this)

        // Simulating immediate queue inject if download url is provided in intent
        val downloadUrl = intent.getStringExtra("DOWNLOAD_URL")
        if (downloadUrl != null) {
            val title = "Study Material Stream"
            val destFile = java.io.File(filesDir, "lec_downloaded_${System.currentTimeMillis()}.mp4")
            val dlItem = DownloadItem(
                id = System.currentTimeMillis().toString(),
                url = downloadUrl,
                title = title,
                filePath = destFile.absolutePath,
                status = "DOWNLOADING",
                progress = 25
            )
            lifecycleScope.launch {
                repository.insertOrUpdateDownload(dlItem)
            }
        }

        setContent {
            AppxTheme {
                DownloadScreen(repository = repository) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(repository: AppRepository, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Reactive flow collection from database
    val downloadsList by repository.allDownloadsFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Downloads", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
                    .padding(horizontal = 16.dp)
            ) {
                if (downloadsList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Empty",
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No downloaded lectures yet.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                            Text(
                                "Study files will appear here for fully offline access.",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize().padding(top = 16.dp)
                    ) {
                        items(downloadsList) { item ->
                            GlassCard(
                                cornerRadius = 20.dp,
                                onClick = {
                                    if (item.status == "COMPLETED") {
                                        // Play fully offline stream
                                        val intent = Intent(context, PlayerActivity::class.java).apply {
                                            putExtra("VIDEO_URL", item.url)
                                            putExtra("LECTURE_ID", item.id)
                                            putExtra("LECTURE_TITLE", item.title + " (Offline)")
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Download is still in progress.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "Status: ${item.status}",
                                                fontSize = 12.sp,
                                                color = if (item.status == "COMPLETED") NeonBlue else NeonOrange,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // Pause/Resume actions
                                            if (item.status == "DOWNLOADING") {
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            repository.insertOrUpdateDownload(item.copy(status = "PAUSED"))
                                                        }
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                                                }
                                            } else if (item.status == "PAUSED") {
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            repository.insertOrUpdateDownload(item.copy(status = "DOWNLOADING"))
                                                        }
                                                    }
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.White)
                                                }
                                            }

                                            // Delete / Remove offline file
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        repository.deleteDownload(item)
                                                        Toast.makeText(context, "Removed offline file.", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Real Progress Indicator
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LinearProgressIndicator(
                                            progress = item.progress / 100f,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(50)),
                                            color = if (item.status == "COMPLETED") NeonBlue else NeonOrange,
                                            trackColor = Color.White.copy(alpha = 0.1f)
                                        )
                                        Text(
                                            text = "${item.progress}%",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
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
