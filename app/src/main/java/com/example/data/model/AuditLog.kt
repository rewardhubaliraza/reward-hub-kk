package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actorId: Long,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val details: String,
    val targetType: String,
    val targetId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
