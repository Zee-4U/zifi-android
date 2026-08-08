package com.ezeevolt.zifi.ui

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * Embeds your router's own admin panel inside ZiFi so you never have to
 * leave the app to flip on MAC filtering or check the connected-devices
 * list. ZiFi does not read or intercept anything here - it's your router's
 * real interface, same as opening it in a browser.
 */
class RouterWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(EXTRA_URL) ?: return finish()

        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
        }
        setContentView(webView)
        webView.loadUrl(url)
    }
}
