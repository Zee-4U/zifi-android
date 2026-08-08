package com.ezeevolt.zifi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceStatus { PENDING, APPROVED, BLOCKED }

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey val mac: String,          // normalized lowercase, colon-separated
    var ip: String,
    var hostname: String = "Unknown device",
    var vendor: String = "Unknown",
    var status: DeviceStatus = DeviceStatus.PENDING,
    var speedLimitKbps: Int = 0,          // 0 = unlimited
    var firstSeen: Long = System.currentTimeMillis(),
    var lastSeen: Long = System.currentTimeMillis(),
    var isThisPhone: Boolean = false
)
