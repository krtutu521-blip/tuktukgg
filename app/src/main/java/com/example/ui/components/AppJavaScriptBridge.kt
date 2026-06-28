package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.example.activities.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.runBlocking

class AppJavaScriptBridge(
    private val context: Context,
    private val repository: AppRepository
) {

    @JavascriptInterface
    fun openPlayer(videoUrl: String) {
        val intent = Intent(context, PlayerActivity::class.java).apply {
            putExtra("VIDEO_URL", videoUrl)
            putExtra("LECTURE_TITLE", "Lecture Video Stream")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun openPdf(pdfUrl: String) {
        val intent = Intent(context, PdfActivity::class.java).apply {
            putExtra("PDF_URL", pdfUrl)
            putExtra("PDF_TITLE", "Course Notes & Material")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun openDownload(url: String) {
        val intent = Intent(context, DownloadActivity::class.java).apply {
            putExtra("DOWNLOAD_URL", url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun saveToken(token: String) {
        runBlocking {
            repository.saveToken(token)
        }
        showToast("Session Token saved securely in Native Encrypted DB")
    }

    @JavascriptInterface
    fun getToken(): String {
        return runBlocking {
            repository.getToken()
        }
    }

    @JavascriptInterface
    fun openHome() {
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun shareCourse() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Learn on APPX Learn!")
            putExtra(Intent.EXTRA_TEXT, "Hey! Check out this premium Android Development course on APPX Learn: https://appxlearn.com/course/compose")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Course via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    @JavascriptInterface
    fun copyText(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("APPX Learn Copy", text)
        clipboard.setPrimaryClip(clip)
        showToast("Copied to clipboard: $text")
    }

    @JavascriptInterface
    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
