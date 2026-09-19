package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long? = null, // null means platform-wide broadcast announcement
    val title: String,
    val message: String,
    val type: String = "SYSTEM", // "SYSTEM", "DEPOSIT", "WITHDRAWAL", "TASK", "REFERRAL"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
