package com.ezeevolt.zifi.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun observeAll(): LiveData<List<Device>>

    @Query("SELECT * FROM devices WHERE mac = :mac LIMIT 1")
    suspend fun getByMac(mac: String): Device?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(device: Device)

    @Update
    suspend fun update(device: Device)

    @Query("UPDATE devices SET status = :status WHERE mac = :mac")
    suspend fun setStatus(mac: String, status: DeviceStatus)

    @Query("UPDATE devices SET speedLimitKbps = :kbps WHERE mac = :mac")
    suspend fun setSpeedLimit(mac: String, kbps: Int)

    @Query("UPDATE devices SET ip = :ip, lastSeen = :ts WHERE mac = :mac")
    suspend fun touch(mac: String, ip: String, ts: Long)

    @Query("SELECT * FROM devices WHERE status = :status")
    suspend fun getByStatus(status: DeviceStatus): List<Device>
}
