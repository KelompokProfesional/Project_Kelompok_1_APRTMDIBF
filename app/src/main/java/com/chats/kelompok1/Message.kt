package com.chats.kelompok1

data class Message(
    val senderId: String = "",
    val displayName: String = "",
    val text: String = "",
    val timestamp: Long = 0
)