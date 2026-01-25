package com.chats.kelompok1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val onUserClick: (Map<String, String>) -> Unit // Hanya 1 parameter di sini
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var allUsers = ArrayList<Map<String, String>>()
    private var activeChats = ArrayList<Map<String, String>>()
    private var displayList = ArrayList<Map<String, String>>()

    fun setAllUsers(list: List<Map<String, String>>) {
        allUsers.clear()
        allUsers.addAll(list)
    }

    fun setActiveChats(list: List<Map<String, String>>, isSearching: Boolean) {
        activeChats.clear()
        activeChats.addAll(list)
        if (!isSearching) {
            displayList.clear()
            displayList.addAll(activeChats)
            notifyDataSetChanged()
        }
    }

    fun filter(query: String) {
        val lowercaseQuery = query.lowercase().trim()
        displayList.clear()
        if (lowercaseQuery.isEmpty()) {
            displayList.addAll(activeChats)
        } else {
            for (user in allUsers) {
                val name = user["displayName"]?.lowercase() ?: ""
                val id = user["userId"]?.lowercase() ?: ""
                if (name.contains(lowercaseQuery) || id.contains(lowercaseQuery)) {
                    displayList.add(user)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = displayList[position]
        holder.tvName.text = user["displayName"]
        holder.tvId.text = "@${user["userId"]}"
        holder.tvInitials.text = user["displayName"]?.take(1)?.uppercase()
        holder.itemView.setOnClickListener { onUserClick(user) }
    }

    override fun getItemCount() = displayList.size

    class UserViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvUserName)
        val tvId: TextView = v.findViewById(R.id.tvUserId)
        val tvInitials: TextView = v.findViewById(R.id.tvUserInitials)
    }
}