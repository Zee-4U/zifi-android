package com.ezeevolt.zifi.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores router admin panel URL + your WiFi SSID/password locally, encrypted.
 * Nothing here ever leaves the phone - there is no backend server.
 */
object RouterPrefs {

    private const val FILE = "zifi_secure_prefs"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var routerAdminUrl: String?
        get() = prefs.getString("router_admin_url", null)
        set(v) = prefs.edit().putString("router_admin_url", v).apply()

    var wifiSsid: String?
        get() = prefs.getString("wifi_ssid", null)
        set(v) = prefs.edit().putString("wifi_ssid", v).apply()

    var wifiPassword: String?
        get() = prefs.getString("wifi_password", null)
        set(v) = prefs.edit().putString("wifi_password", v).apply()

    var setupComplete: Boolean
        get() = prefs.getBoolean("setup_complete", false)
        set(v) = prefs.edit().putBoolean("setup_complete", v).apply()
}
