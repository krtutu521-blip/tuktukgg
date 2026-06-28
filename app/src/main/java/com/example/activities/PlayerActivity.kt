package com.example.activities

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.*
import androidx.compose.ui.graphics.graphicsLayer
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var repository: AppRepository
    private var lectureId: String = "lec_1"
    private var videoUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        repository = AppRepository(this)
        lectureId = intent.getStringExtra("LECTURE_ID") ?: "lec_1"
        videoUrl = intent.getStringExtra("VIDEO_URL") ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val lectureTitle = intent.getStringExtra("LECTURE_TITLE") ?: "Lecture Stream"

        // Keep screen on during video playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            AppxTheme {
                CustomPlayerScreen(
                    videoUrl = videoUrl,
                    lectureId = lectureId,
                    lectureTitle = lectureTitle,
                    repository = repository,
                    onEnterPip = { enterPipMode() },
                    onBackPressed = { finish() }
                )
            }
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        } else {
            Toast.makeText(this, "Picture-in-Picture mode not supported on this Android version.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Automatically enter PIP on home swipe
        enterPipMode()
    }

    override fun onPause() {
        super.onPause()
        // Save current resume position when paused
        exoPlayer?.let { player ->
            val position = player.currentPosition
            val isCompleted = position > (player.duration * 0.9f) && player.duration > 0
            android.util.Log.d("PlayerActivity", "Saving playback progress: $position ms")
            lifecycleScope.launch {
                repository.saveLectureProgress(lectureId, isCompleted, position)
            }
            player.playWhenReady = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}

@OptIn(UnstableApi::class)
@Composable
fun CustomPlayerScreen(
    videoUrl: String,
    lectureId: String,
    lectureTitle: String,
    repository: AppRepository,
    onEnterPip: () -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var playerInstance by remember { mutableStateOf<ExoPlayer?>(null) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var videoQuality by remember { mutableStateOf("Auto") }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showQualityMenu by remember { mutableStateOf(false) }

    // Gesture control variables
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var currentVolume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    var brightnessLevel by remember { mutableStateOf(0.5f) } // Simulating brightness state

    var showControls by remember { mutableStateOf(true) }

    // Hide controls after 4 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4000)
            showControls = false
        }
    }

    // Initialize ExoPlayer and load saved resume progress
    DisposableEffect(Unit) {
        val player = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
            
            // Resume progress from local database
            scope.launch {
                val progress = repository.getLectureProgress(lectureId)
                if (progress != null && progress.resumePositionMs > 1000) {
                    seekTo(progress.resumePositionMs)
                    Toast.makeText(context, "Resuming playback from previous position...", Toast.LENGTH_SHORT).show()
                }
                playWhenReady = true
            }
        }
        playerInstance = player
        
        onDispose {
            player.release()
        }
    }

    val finalPlayer = playerInstance

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                // Double tap and vertical swipe gesture handling
                detectTapGestures(
                    onDoubleTap = { offset ->
                        // Detect double tap left side (rewind) or right side (fast forward)
                        val width = size.width
                        if (offset.x < width / 2) {
                            finalPlayer?.let {
                                val newPos = (it.currentPosition - 10000).coerceAtLeast(0)
                                it.seekTo(newPos)
                                Toast.makeText(context, "⏪ Seek Back 10s", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            finalPlayer?.let {
                                val newPos = (it.currentPosition + 10000).coerceAtMost(it.duration)
                                it.seekTo(newPos)
                                Toast.makeText(context, "⏩ Seek Forward 10s", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val width = size.width
                    // Left half controls brightness, right half controls volume
                    if (change.position.x < width / 2) {
                        brightnessLevel = (brightnessLevel - dragAmount.y / 500f).coerceIn(0.1f, 1.0f)
                    } else {
                        val volumeStep = if (dragAmount.y < 0) 1 else -1
                        val nextVol = (currentVolume + volumeStep).coerceIn(0, maxVolume)
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nextVol, 0)
                        currentVolume = nextVol
                    }
                }
            }
    ) {
        // Player Surface View
        if (finalPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = finalPlayer
                        useController = false // Custom controls in Compose UI overlay!
                        keepScreenOn = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gesture levels displays
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Volume overlay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (currentVolume == 0) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = NeonBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Vol: ${(currentVolume * 100 / maxVolume)}%", color = Color.White, fontSize = 11.sp)
                }
                // Brightness overlay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Brightness6,
                        contentDescription = "Brightness",
                        tint = NeonOrange,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Bright: ${(brightnessLevel * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Custom HUD Controls layer (Gleaming glass theme)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = lectureTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Quality button
                        IconButton(onClick = { showQualityMenu = !showQualityMenu }) {
                            Icon(Icons.Default.Settings, contentDescription = "Quality", tint = Color.White)
                        }
                        // Speed Selection
                        IconButton(onClick = { showSpeedMenu = !showSpeedMenu }) {
                            Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White)
                        }
                        // Picture in Picture
                        IconButton(onClick = onEnterPip) {
                            Icon(Icons.Default.PictureInPicture, contentDescription = "PiP", tint = Color.White)
                        }
                    }
                }

                // Central play/pause actions
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { finalPlayer?.let { it.seekTo((it.currentPosition - 10000).coerceAtLeast(0)) } },
                        modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "Rewind", tint = Color.White)
                    }

                    val isPlaying = finalPlayer?.isPlaying == true
                    IconButton(
                        onClick = {
                            finalPlayer?.let {
                                if (it.isPlaying) it.pause() else it.play()
                            }
                        },
                        modifier = Modifier.size(64.dp).background(NeonOrange, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = { finalPlayer?.let { it.seekTo((it.currentPosition + 10000).coerceAtMost(it.duration)) } },
                        modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = "Fast Forward", tint = Color.White)
                    }
                }

                // Speed Select Menu Modal
                if (showSpeedMenu) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 80.dp, end = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                Text(
                                    text = "${speed}x",
                                    color = if (playbackSpeed == speed) NeonOrange else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            playbackSpeed = speed
                                            finalPlayer?.setPlaybackParameters(PlaybackParameters(speed))
                                            showSpeedMenu = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // Quality Select Menu Modal
                if (showQualityMenu) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 80.dp, end = 60.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            listOf("Auto", "1080p (FHD)", "720p (HD)", "480p", "360p").forEach { quality ->
                                Text(
                                    text = quality,
                                    color = if (videoQuality == quality) NeonBlue else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            videoQuality = quality
                                            showQualityMenu = false
                                            Toast.makeText(context, "Switched stream quality to: $quality", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
