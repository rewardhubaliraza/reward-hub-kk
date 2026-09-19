package com.example.util

import java.security.MessageDigest
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object SecurityHelper {
    fun hashPassword(password: String, salt: String = "earn_rewards_salt_2026"): String {
        val input = password + salt
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateReferralCode(name: String = ""): String {
        val cleanName = name.filter { it.isLetter() }.take(3).uppercase(Locale.ROOT)
        val prefix = if (cleanName.length >= 3) cleanName else "EARN"
        val randomSuffix = UUID.randomUUID().toString().replace("-", "").take(4).uppercase(Locale.ROOT)
        return "$prefix$randomSuffix"
    }

    fun generateTransactionId(prefix: String = "TXN"): String {
        val timePart = System.currentTimeMillis().toString().takeLast(6)
        val randomPart = UUID.randomUUID().toString().replace("-", "").take(4).uppercase(Locale.ROOT)
        return "$prefix-$timePart-$randomPart"
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale.US)
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 2
        return "Rs. ${format.format(amount)}"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getMaxWithdrawalLimit(depositAmount: Double): Double {
        return when {
            depositAmount >= 15000.0 -> 1500.0
            depositAmount >= 10000.0 -> 1500.0
            depositAmount >= 5000.0 -> 1000.0
            depositAmount >= 2000.0 -> 1000.0
            depositAmount >= 1000.0 -> 500.0
            depositAmount >= 500.0 -> 200.0
            else -> 200.0
        }
    }

    fun getDailyTaskReward(depositAmount: Double): Double {
        return when {
            depositAmount >= 15000.0 -> 450.0
            depositAmount >= 10000.0 -> 300.0
            depositAmount >= 5000.0 -> 200.0
            depositAmount >= 2000.0 -> 100.0
            depositAmount >= 1000.0 -> 50.0
            depositAmount >= 500.0 -> 25.0
            else -> 25.0
        }
    }
}
