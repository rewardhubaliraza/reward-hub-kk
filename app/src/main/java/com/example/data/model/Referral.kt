package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val referrerUserId: Long,
    val referredUserId: Long,
    val referredUserName: String,
    val referredUserEmail: String,
    val qualifyingDepositAmount: Double = 0.0,
    val isQualified: Boolean = false,
    val rewardEarned: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "referral_rewards")
data class ReferralRewardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val referrerUserId: Long,
    val referredUserId: Long,
    val depositAmount: Double,
    val rewardAmount: Double,
    val status: String = "CREDITED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "referral_configs")
data class ReferralConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val minDeposit: Double,
    val maxDeposit: Double,
    val rewardAmount: Double,
    val description: String
)
