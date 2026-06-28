package com.example;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import com.example.activities.DownloadActivity;
import com.example.activities.PdfActivity;
import com.example.activities.PlayerActivity;

public class AppInterface {

    private final Activity activity;
    private final WebView webView;
    private final SharedPreferences sharedPreferences;

    public AppInterface(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
        this.sharedPreferences = activity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
    }

    private void loadUrlInWebView(final String url) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                String formattedUrl = DomainManager.appendDomainId(url);
                webView.loadUrl(formattedUrl);
            }
        });
    }

    @JavascriptInterface
    public void goToHome() {
        loadUrlInWebView("file:///android_asset/home.html");
    }

    @JavascriptInterface
    public void forceGoToHome() {
        goToHome();
    }

    @JavascriptInterface
    public void openSubject(String id) {
        loadUrlInWebView("file:///android_asset/get-sub.html?id=" + id);
    }

    @JavascriptInterface
    public void openTopic(String courseId, String subjectId) {
        loadUrlInWebView("file:///android_asset/get-topic.html?subjectid=" + subjectId + "&courseid=" + courseId);
    }

    @JavascriptInterface
    public void openContent(String courseId, String subjectId, String topicId) {
        loadUrlInWebView("file:///android_asset/content.html?courseid=" + courseId + "&topicid=" + topicId + "&subjectid=" + subjectId);
    }

    @JavascriptInterface
    public void openPlay(String videoId) {
        loadUrlInWebView("file:///android_asset/play.html?id=" + videoId);
    }

    @JavascriptInterface
    public void openPlayer(String url) {
        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra("VIDEO_URL", url);
        intent.putExtra("LECTURE_TITLE", "Video Lesson");
        intent.putExtra("LECTURE_ID", "lec_" + System.currentTimeMillis());
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void openPdf(String url) {
        Intent intent = new Intent(activity, PdfActivity.class);
        intent.putExtra("PDF_URL", url);
        intent.putExtra("PDF_TITLE", "Study Material Guide");
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void showToast(String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void copyText(String text) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            showToast("Copied to clipboard");
        }
    }

    @JavascriptInterface
    public void shareText(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(intent, "Share via"));
    }

    @JavascriptInterface
    public void savePreference(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    @JavascriptInterface
    public String getPreference(String key) {
        return sharedPreferences.getString(key, "");
    }

    @JavascriptInterface
    public void downloadFile(String url) {
        Intent intent = new Intent(activity, DownloadActivity.class);
        intent.putExtra("DOWNLOAD_URL", url);
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public void finishActivity() {
        activity.finish();
    }
}
