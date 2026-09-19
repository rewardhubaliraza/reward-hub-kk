package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WithdrawalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PAID
}

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val amount: Double,
    val method: String,
    val accountTitle: String,
    val accountNumber: String,
    val status: String = WithdrawalStatus.PENDING.name,
    val adminNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
