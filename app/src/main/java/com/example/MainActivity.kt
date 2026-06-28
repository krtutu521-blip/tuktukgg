package com.example

import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.repository.AppRepository
import com.example.ui.components.GlassBackground
import com.example.ui.theme.AppxTheme
import com.example.ui.theme.NeonOrange

class MainActivity : ComponentActivity() {

    private lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = AppRepository(this)

        setContent {
            AppxTheme {
                var hasError by remember { mutableStateOf(false) }
                var errorType by remember { mutableStateOf("OFFLINE") } // "OFFLINE", "API_ERROR", "404"
                var webViewInstance by remember { mutableStateOf<WebView?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        GlassBackground {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { context ->
                                        WebView(context).apply {
                                            // Configure the webview using our dedicated WebViewManager
                                            WebViewManager.configureWebView(this)

                                            webViewClient = object : WebViewClient() {
                                                override fun onReceivedError(
                                                    view: WebView?,
                                                    request: WebResourceRequest?,
                                                    error: WebResourceError?
                                                ) {
                                                    super.onReceivedError(view, request, error)
                                                    // Detect errors for main frame requests only
                                                    if (request?.isForMainFrame == true) {
                                                        hasError = true
                                                        val errorMsg = error?.description?.toString() ?: ""
                                                        if (errorMsg.contains("404") || errorMsg.contains("not found")) {
                                                            errorType = "404"
                                                        } else if (errorMsg.contains("timeout") || errorMsg.contains("connect")) {
                                                            errorType = "API_ERROR"
                                                        } else {
                                                            errorType = "OFFLINE"
                                                        }
                                                    }
                                                }
                                            }

                                            // Attach high fidelity AppInterface JS bridge
                                            val appInterface = AppInterface(this@MainActivity, this)
                                            addJavascriptInterface(appInterface, "AppInterface")

                                            // Backwards compatible old bridge registration if some elements expect it
                                            addJavascriptInterface(appInterface, "AndroidBridge")

                                            // Store reference
                                            webViewInstance = this

                                            // Load startup url: home.html?domainId=3377 as requested
                                            val initialUrl = "file:///android_asset/home.html"
                                            val startupUrl = DomainManager.appendDomainId(initialUrl)
                                            loadUrl(startupUrl)
                                        }
                                    }
                                )

                                // Gorgeous glassmorphic retry & offline error screen overlays
                                AnimatedVisibility(
                                    visible = hasError,
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xE00B0F19)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(24.dp)
                                                .background(
                                                    color = Color(0x1F2B354F),
                                                    shape = RoundedCornerShape(24.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color.White.copy(alpha = 0.15f),
                                                            Color.White.copy(alpha = 0.02f)
                                                        )
                                                    ),
                                                    shape = RoundedCornerShape(24.dp)
                                                )
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = if (errorType == "404") "🔍 404 - Not Found" 
                                                       else if (errorType == "API_ERROR") "⚠️ Server API Error" 
                                                       else "📶 Connection Offline",
                                                color = Color.White,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = if (errorType == "404") "The requested content or lecture topic is currently unavailable."
                                                       else if (errorType == "API_ERROR") "Our API services are currently unreachable. Please check your network configuration and retry."
                                                       else "You are currently disconnected from the internet. Please connect and try again.",
                                                color = Color.LightGray,
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 20.sp
                                            )
                                            Spacer(modifier = Modifier.height(28.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        hasError = false
                                                        webViewInstance?.reload()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = NeonOrange
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "Retry",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Retry", fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        hasError = false
                                                        val homeUrl = DomainManager.appendDomainId("file:///android_asset/home.html")
                                                        webViewInstance?.loadUrl(homeUrl)
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = Color.White
                                                    ),
                                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Home,
                                                        contentDescription = "Home",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Go Home", fontWeight = FontWeight.Bold)
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
}

