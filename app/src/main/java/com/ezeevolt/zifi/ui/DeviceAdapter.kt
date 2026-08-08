package com.ezeevolt.zifi.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.ezeevolt.zifi.data.Device
import com.ezeevolt.zifi.data.DeviceStatus

class DeviceAdapter(
    private val onApprove: (Device) -> Unit,
    private val onBlock: (Device) -> Unit,
    private val onSetSpeed: (Device, Int) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.VH>() {

    private var items: List<Device> = emptyList()

    fun submit(list: List<Device>) {
        items = list
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val subtitle: TextView = view.findViewById(android.R.id.text2)
        val approveBtn: Button = Button(view.context)
        val container: LinearLayout = view as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }
        val text1 = TextView(ctx).apply { id = android.R.id.text1; textSize = 16f }
        val text2 = TextView(ctx).apply { id = android.R.id.text2; textSize = 13f; alpha = 0.7f }
        val btnRow = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
        val approve = Button(ctx).apply { text = "Approve" }
        val block = Button(ctx).apply { text = "Block" }
        val speed = Button(ctx).apply { text = "Set speed" }
        btnRow.addView(approve)
        btnRow.addView(block)
        btnRow.addView(speed)
        root.addView(text1)
        root.addView(text2)
        root.addView(btnRow)

        val vh = VH(root)
        return vh.also { holder ->
            approve.setOnClickListener { onApprove(items[holder.bindingAdapterPosition]) }
            block.setOnClickListener { onBlock(items[holder.bindingAdapterPosition]) }
            speed.setOnClickListener {
                val d = items[holder.bindingAdapterPosition]
                // Simple fixed-tier prompt; replace with a proper dialog/slider in the UI later.
                val next = when (d.speedLimitKbps) {
                    0 -> 5000      // 5 Mbps
                    5000 -> 1000   // 1 Mbps
                    1000 -> 256    // 256 kbps
                    else -> 0      // back to unlimited
                }
                onSetSpeed(d, next)
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = items[position]
        holder.title.text = "${d.hostname}  (${d.ip})"
        val speedLabel = if (d.speedLimitKbps > 0) "${d.speedLimitKbps} kbps limit" else "Unlimited"
        holder.subtitle.text = "${d.vendor} • ${d.mac} • ${d.status} • $speedLabel"
    }

    override fun getItemCount() = items.size
}
