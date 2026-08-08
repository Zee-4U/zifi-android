package com.ezeevolt.zifi.net

/**
 * Minimal offline OUI (first 3 MAC octets) -> vendor lookup.
 * This starter table covers common phone/laptop/IoT vendors.
 * For full coverage, drop the IEEE OUI CSV into assets/oui.csv and
 * load it lazily instead (left as a TODO to keep APK size small by default).
 */
object MacVendorLookup {

    private val table = mapOf(
        "3c:5a:b4" to "Google",
        "f4:f5:d8" to "Google",
        "a4:83:e7" to "Apple",
        "dc:a6:32" to "Apple",
        "b8:27:eb" to "Raspberry Pi Foundation",
        "dc:44:6d" to "Xiaomi",
        "64:09:80" to "Xiaomi",
        "1c:bf:ce" to "Samsung",
        "8c:79:f5" to "Samsung",
        "00:1a:11" to "Google",
        "18:fe:34" to "Espressif (ESP32/ESP8266)",
        "24:0a:c4" to "Espressif (ESP32/ESP8266)",
        "b0:be:76" to "Huawei",
        "70:3a:cb" to "Huawei"
    )

    fun lookup(mac: String): String {
        val oui = mac.lowercase().take(8)
        return table[oui] ?: "Unknown vendor"
    }
}
