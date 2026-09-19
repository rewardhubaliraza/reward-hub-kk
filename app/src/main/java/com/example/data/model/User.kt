package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    USER,
    ADMIN
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String,
    val email: String,
    val passwordHash: String,
    val role: String = UserRole.USER.name,
    val referralCode: String,
    val referredByCode: String? = null,
    val balance: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val totalDeposits: Double = 0.0,
    val totalWithdrawals: Double = 0.0,
    val dailyTaskEarnings: Double = 0.0,
    val referralEarnings: Double = 0.0,
    val activePlanId: Long? = null,
    val activePlanName: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
