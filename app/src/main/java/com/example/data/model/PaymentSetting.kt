package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_settings")
data class PaymentSettingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val methodTitle: String, // e.g. "JazzCash", "Easypaisa", "Bank Alfalah", "USDT (TRC20)"
    val accountTitle: String, // e.g. "Admin Official Financial Services"
    val accountNumber: String, // e.g. "03001234567"
    val bankOrProvider: String, // e.g. "Bank Alfalah Limited"
    val instructionNote: String = "Send payment and enter the transaction reference ID along with screenshot proof below.",
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
