package com.ezeevolt.zifi.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezeevolt.zifi.data.AppDatabase
import com.ezeevolt.zifi.data.Device
import com.ezeevolt.zifi.data.DeviceStatus
import com.ezeevolt.zifi.data.RouterPrefs
import com.ezeevolt.zifi.net.ArpScanner
import com.ezeevolt.zifi.net.MacVendorLookup
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RouterPrefs.init(this)

        if (!RouterPrefs.setupComplete) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        db = AppDatabase.get(this)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val scanBtn = Button(this).apply { text = "Rescan network" }
        val adminBtn = Button(this).apply { text = "Router admin" }
        topRow.addView(scanBtn)
        topRow.addView(adminBtn)

        val recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        root.addView(topRow)
        root.addView(recycler)
        setContentView(root)

        adapter = DeviceAdapter(
            onApprove = { d ->
                lifecycleScope.launch {
                    db.deviceDao().setStatus(d.mac, DeviceStatus.APPROVED)
                    startActivity(Intent(this@MainActivity, QrShareActivity::class.java))
                }
            },
            onBlock = { d -> lifecycleScope.launch { db.deviceDao().setStatus(d.mac, DeviceStatus.BLOCKED) } },
            onSetSpeed = { d, kbps -> lifecycleScope.launch { db.deviceDao().setSpeedLimit(d.mac, kbps) } }
        )
        recycler.adapter = adapter
        db.deviceDao().observeAll().observe(this) { list -> adapter.submit(list) }

        scanBtn.setOnClickListener { runScan() }
        adminBtn.setOnClickListener {
            val url = RouterPrefs.routerAdminUrl
            if (url != null) {
                startActivity(Intent(this, RouterWebViewActivity::class.java).apply {
                    putExtra(RouterWebViewActivity.EXTRA_URL, url)
                })
            }
        }

        runScan()
    }

    private fun runScan() {
        lifecycleScope.launch {
            val found = ArpScanner.scan()
            val dao = db.deviceDao()
            for (dev in found) {
                val existing = dao.getByMac(dev.mac)
                if (existing == null) {
                    dao.insert(
                        Device(
                            mac = dev.mac,
                            ip = dev.ip,
                            vendor = MacVendorLookup.lookup(dev.mac),
                            status = DeviceStatus.PENDING
                        )
                    )
                } else {
                    dao.touch(dev.mac, dev.ip, System.currentTimeMillis())
                }
            }
        }
    }
}
