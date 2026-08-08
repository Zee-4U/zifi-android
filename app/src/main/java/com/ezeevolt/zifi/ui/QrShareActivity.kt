package com.ezeevolt.zifi.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ezeevolt.zifi.data.RouterPrefs
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.zxing.BarcodeFormat

/**
 * Shown right after you tap "Approve" on a device. Scanning this QR joins
 * the WiFi automatically (standard Android/iOS WIFI: QR format) - you only
 * show it to people/devices you've explicitly approved, and if MAC
 * filtering is on at the router, unapproved MACs still can't join even if
 * the password leaks separately.
 */
class QrShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RouterPrefs.init(this)

        val ssid = RouterPrefs.wifiSsid.orEmpty()
        val password = RouterPrefs.wifiPassword.orEmpty()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val label = TextView(this).apply {
            text = if (ssid.isEmpty())
                "Set your WiFi name/password in Setup first."
            else
                "Scan to join \"$ssid\"\nOnly share this with approved devices."
            textSize = 16f
        }
        root.addView(label)

        if (ssid.isNotEmpty()) {
            val qrContent = "WIFI:T:WPA;S:${escape(ssid)};P:${escape(password)};;"
            val bitmap = BarcodeEncoder().encodeBitmap(qrContent, BarcodeFormat.QR_CODE, 700, 700)
            val imageView = ImageView(this).apply { setImageBitmap(bitmap) }
            root.addView(imageView)
        }

        setContentView(root)
    }

    private fun escape(s: String) = s.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace(":", "\\:")
}
