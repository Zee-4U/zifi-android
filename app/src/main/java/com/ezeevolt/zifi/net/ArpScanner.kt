package com.ezeevolt.zifi.net

import kotlinx.coroutines.*
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface

data class ScannedDevice(val ip: String, val mac: String)

/**
 * Discovers devices on the local WiFi subnet.
 * Step 1: ping-sweep the /24 subnet so the kernel populates its ARP cache.
 * Step 2: read /proc/net/arp for completed (reachable) entries.
 * No root required for this part.
 */
object ArpScanner {

    suspend fun scan(): List<ScannedDevice> = withContext(Dispatchers.IO) {
        val subnet = getSubnetPrefix() ?: return@withContext emptyList()
        pingSweep(subnet)
        readArpCache()
    }

    private fun getSubnetPrefix(): String? {
        val ifacesEnum = NetworkInterface.getNetworkInterfaces() ?: return null
        // Convert Enumeration -> List for idiomatic iteration
        val ifaces = java.util.Collections.list(ifacesEnum)
        for (iface in ifaces) {
            if (!iface.isUp || iface.isLoopback) continue
            if (!iface.name.startsWith("wlan")) continue
            for (addr in iface.interfaceAddresses) {
                val ip = addr.address
                // only IPv4
                if (ip.address.size == 4) {
                    val parts = ip.hostAddress.split(".")
                    if (parts.size == 4) return "${parts[0]}.${parts[1]}.${parts[2]}"
                }
            }
        }
        return null
    }

    private suspend fun pingSweep(subnetPrefix: String) = coroutineScope {
        val jobs = (1..254).map { host ->
            async(Dispatchers.IO) {
                try {
                    InetAddress.getByName("$subnetPrefix.$host").isReachable(150)
                } catch (_: Exception) {
                    // return false for unreachable / error
                    false
                }
            }
        }
        // wait for all pings to complete (results are not needed here)
        jobs.awaitAll()
    }

    private fun readArpCache(): List<ScannedDevice> {
        val results = mutableListOf<ScannedDevice>()
        try {
            File("/proc/net/arp").readLines().drop(1).forEach { line ->
                val cols = line.trim().split(Regex("\\s+"))
                if (cols.size >= 6) {
                    val ip = cols[0]
                    val flags = cols[2]
                    val mac = cols[3].lowercase()
                    // flags "0x2" == ATF_COMPLETE, and skip broadcast/zero macs
                    if (flags == "0x2" && mac != "00:00:00:00:00:00") {
                        results.add(ScannedDevice(ip, mac))
                    }
                }
            }
        } catch (_: Exception) {
            // /proc/net/arp might be unreadable on some ROMs
        }
        return results
    }
}
