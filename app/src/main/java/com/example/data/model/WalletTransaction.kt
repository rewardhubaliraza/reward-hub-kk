package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TASK_REWARD,
    REFERRAL_BONUS,
    PLAN_ACTIVATION,
    REFUND
}

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey
    val transactionId: String,
    val userId: Long,
    val type: String,
    val amount: Double,
    val balanceAfter: Double,
    val description: String,
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis()
)
