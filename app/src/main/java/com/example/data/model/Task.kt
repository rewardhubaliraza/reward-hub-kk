package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "SURVEY", "VIDEO", "CHECK_IN", "REVIEW", "SPONSOR"
    val rewardAmount: Double,
    val durationSeconds: Int = 10,
    val actionUrl: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_completions")
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val taskId: Long,
    val taskTitle: String,
    val rewardEarned: Double,
    val dateKey: String, // e.g. "2026-09-18" to prevent duplicate completions on the same day
    val completedAt: Long = System.currentTimeMillis()
)
