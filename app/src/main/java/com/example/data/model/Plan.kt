package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plans")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val depositAmount: Double,
    val dailyTaskReward: Double,
    val maxWithdrawalLimit: Double = 1000.0,
    val description: String = "Complete daily engagement tasks to receive rewards.",
    val validityDays: Int = 365,
    val isActive: Boolean = true
)
