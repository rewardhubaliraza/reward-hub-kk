package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DepositStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val planId: Long,
    val planName: String,
    val amount: Double,
    val paymentMethod: String,
    val transactionReferenceId: String,
    val proofUri: String? = null,
    val proofNote: String? = null,
    val status: String = DepositStatus.PENDING.name,
    val adminNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
