package com.example;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewManager {

    public static void configureWebView(WebView webView) {
        WebSettings settings = webView.getSettings();

        // Enable JavaScript
        settings.setJavaScriptEnabled(true);

        // Enable DOM Storage
        settings.setDomStorageEnabled(true);

        // Enable Cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // Enable File Access
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        // Enable Universal Access
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // Enable Media Playback without user gesture
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Enable Mixed Content
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // Enable Cache
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Enable Hardware Acceleration (automatically enabled on Application/Activity level, but can be set on View)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Disable Zoom
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // Disable Long Click & Context Menu (prevents Text Selection)
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true; // Consume long click event
            }
        });
        webView.setLongClickable(false);

        // Disable Haptic Feedback for long clicks if any
        webView.setHapticFeedbackEnabled(false);
    }
}
