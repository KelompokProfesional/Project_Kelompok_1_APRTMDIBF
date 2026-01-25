package com.chats.kelompok1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Model data sederhana untuk item menu
data class MenuItem(
    val title: String,
    val subtitle: String,
    val iconResId: Int, // ID resource gambar (misal: R.drawable.ic_menu_community)
    val onClickAction: () -> Unit // Fungsi yang dijalankan saat diklik
)

class MenuAdapter(private val menuItems: List<MenuItem>) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    // Membuat tampilan per baris (ViewHolder)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_chat, parent, false)
        return MenuViewHolder(view)
    }

    // Mengisi data ke tampilan
    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuItems[position]
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle

        // Set ikon (Gunakan gambar placeholder jika ikon asli belum ada)
        try {
            holder.icon.setImageResource(item.iconResId)
        } catch (e: Exception) {
            holder.icon.setImageResource(android.R.drawable.ic_menu_myplaces) // Fallback
        }

        // Set aksi klik pada seluruh item
        holder.itemView.setOnClickListener {
            item.onClickAction()
        }
    }

    override fun getItemCount() = menuItems.size

    // Kelas ViewHolder untuk menyimpan referensi view di item_menu_chat.xml
    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.textViewTitle)
        val subtitle: TextView = view.findViewById(R.id.textViewSubtitle)
        val icon: ImageView = view.findViewById(R.id.imageViewIcon)
        val date: TextView = view.findViewById(R.id.textViewDate) // Bisa diset statis atau dihilangkan
    }
}