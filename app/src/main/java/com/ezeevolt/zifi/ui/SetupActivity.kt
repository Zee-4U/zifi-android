package com.ezeevolt.zifi.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ezeevolt.zifi.data.RouterPrefs

/**
 * One-time setup, matches the flow the user described:
 * 1) They already changed their WiFi password manually (kicks everyone off).
 * 2) Here they tell ZiFi the new SSID/password (used later for QR sharing)
 *    and their router admin URL (used to jump into the MAC-filter page).
 * 3) They tap "Open Router Admin" once to turn on MAC allow-list mode -
 *    this is manual because every router brand's admin panel is different,
 *    and ZiFi opens it inside a WebView so they never leave the app.
 */
class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RouterPrefs.init(this)

        val pad = 40
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        val intro = TextView(this).apply {
            text = "Set up ZiFi\n\n" +
                "Step 1: You've already changed your WiFi password so old " +
                "devices are disconnected.\n\n" +
                "Step 2: Enter your new WiFi name/password below (used to " +
                "generate a QR code when you approve someone).\n\n" +
                "Step 3: Enter your router's admin address (usually " +
                "192.168.1.1 or 192.168.100.1 - check the label on your " +
                "router) and turn on \"MAC Filter / Access Control\" mode " +
                "in allow-list mode. ZiFi will open that page for you."
            textSize = 14f
        }

        val ssidInput = EditText(this).apply { hint = "WiFi name (SSID)" }
        val passInput = EditText(this).apply { hint = "WiFi password" }
        val adminInput = EditText(this).apply {
            hint = "Router admin address, e.g. 192.168.1.1"
        }

        val openAdminBtn = Button(this).apply { text = "Open Router Admin" }
        val saveBtn = Button(this).apply { text = "Save & Continue" }

        root.addView(intro)
        root.addView(ssidInput)
        root.addView(passInput)
        root.addView(adminInput)
        root.addView(openAdminBtn)
        root.addView(saveBtn)
        setContentView(root)

        // Prefill if already set
        RouterPrefs.wifiSsid?.let { ssidInput.setText(it) }
        RouterPrefs.wifiPassword?.let { passInput.setText(it) }
        RouterPrefs.routerAdminUrl?.let { adminInput.setText(it) }

        openAdminBtn.setOnClickListener {
            val raw = adminInput.text.toString().trim()
            if (raw.isEmpty()) return@setOnClickListener
            val url = if (raw.startsWith("http")) raw else "http://$raw"
            RouterPrefs.routerAdminUrl = url
            startActivity(Intent(this, RouterWebViewActivity::class.java).apply {
                putExtra(RouterWebViewActivity.EXTRA_URL, url)
            })
        }

        saveBtn.setOnClickListener {
            RouterPrefs.wifiSsid = ssidInput.text.toString().trim()
            RouterPrefs.wifiPassword = passInput.text.toString()
            RouterPrefs.setupComplete = true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
