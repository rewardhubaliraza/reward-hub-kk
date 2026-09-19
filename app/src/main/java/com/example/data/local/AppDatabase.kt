package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.DepositEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PaymentSettingEntity
import com.example.data.model.PlanEntity
import com.example.data.model.ReferralConfigEntity
import com.example.data.model.ReferralEntity
import com.example.data.model.ReferralRewardEntity
import com.example.data.model.TaskCompletionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserEntity
import com.example.data.model.WalletTransactionEntity
import com.example.data.model.WithdrawalEntity
import com.example.util.SecurityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PlanEntity::class,
        DepositEntity::class,
        WithdrawalEntity::class,
        TaskEntity::class,
        TaskCompletionEntity::class,
        ReferralEntity::class,
        ReferralRewardEntity::class,
        ReferralConfigEntity::class,
        WalletTransactionEntity::class,
        PaymentSettingEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun planDao(): PlanDao
    abstract fun depositDao(): DepositDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun taskDao(): TaskDao
    abstract fun referralDao(): ReferralDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun paymentSettingDao(): PaymentSettingDao
    abstract fun notificationDao(): NotificationDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "earn_rewards_database.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed database
                        scope.launch {
                            val database = getDatabase(context, scope)
                            seedDatabase(database)
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        scope.launch {
                            val database = getDatabase(context, scope)
                            syncPaymentSettings(database)
                            syncPlans(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            val adminPasswordHash = SecurityHelper.hashPassword("admin123")
            val userPasswordHash = SecurityHelper.hashPassword("password123")

            // Default Admin
            val adminUser = UserEntity(
                id = 1,
                name = "Platform Administrator",
                mobile = "03001234567",
                email = "admin@platform.com",
                passwordHash = adminPasswordHash,
                role = "ADMIN",
                referralCode = "ADMIN01",
                balance = 100000.0,
                createdAt = System.currentTimeMillis()
            )
            // Default Demo User
            val demoUser = UserEntity(
                id = 2,
                name = "Zubair Ahmed",
                mobile = "03123456789",
                email = "user@example.com",
                passwordHash = userPasswordHash,
                role = "USER",
                referralCode = "ZUB789",
                referredByCode = "ADMIN01",
                balance = 250.0,
                totalEarnings = 250.0,
                dailyTaskEarnings = 250.0,
                createdAt = System.currentTimeMillis()
            )
            db.userDao().insertUser(adminUser)
            db.userDao().insertUser(demoUser)

            // Seed Plans with updated limits and rewards
            val plans = listOf(
                PlanEntity(
                    id = 1,
                    name = "Starter",
                    depositAmount = 500.0,
                    dailyTaskReward = 25.0,
                    maxWithdrawalLimit = 200.0,
                    description = "Entry plan with daily engagement task. Reward subject to 1 task per day."
                ),
                PlanEntity(
                    id = 2,
                    name = "Basic",
                    depositAmount = 1000.0,
                    dailyTaskReward = 50.0,
                    maxWithdrawalLimit = 500.0,
                    description = "Standard tier for consistent daily task completion."
                ),
                PlanEntity(
                    id = 3,
                    name = "Standard",
                    depositAmount = 2000.0,
                    dailyTaskReward = 100.0,
                    maxWithdrawalLimit = 1000.0,
                    description = "Recommended tier with elevated daily engagement tasks."
                ),
                PlanEntity(
                    id = 4,
                    name = "Premium",
                    depositAmount = 5000.0,
                    dailyTaskReward = 200.0,
                    maxWithdrawalLimit = 1000.0,
                    description = "High tier with priority partner video tasks and surveys."
                ),
                PlanEntity(
                    id = 5,
                    name = "Pro",
                    depositAmount = 10000.0,
                    dailyTaskReward = 300.0,
                    maxWithdrawalLimit = 1500.0,
                    description = "Top tier for maximum daily reward task participation."
                ),
                PlanEntity(
                    id = 6,
                    name = "Elite",
                    depositAmount = 15000.0,
                    dailyTaskReward = 450.0,
                    maxWithdrawalLimit = 1500.0,
                    description = "Executive tier with maximum daily rewards and dedicated withdrawal limit."
                )
            )
            db.planDao().insertPlans(plans)

            // Seed Referral Configs (Requirement 6)
            val referralConfigs = listOf(
                ReferralConfigEntity(1, 500.0, 999.0, 250.0, "Deposit Rs. 500 -> Earn Rs. 250"),
                ReferralConfigEntity(2, 1000.0, 1999.0, 400.0, "Deposit Rs. 1,000 -> Earn Rs. 400"),
                ReferralConfigEntity(3, 2000.0, 4999.0, 500.0, "Deposit Rs. 2,000 -> Earn Rs. 500"),
                ReferralConfigEntity(4, 5000.0, 9999.0, 700.0, "Deposit Rs. 5,000 -> Earn Rs. 700"),
                ReferralConfigEntity(5, 10000.0, 999999.0, 1000.0, "Deposit Rs. 10,000+ -> Earn Rs. 1,000")
            )
            db.referralDao().insertReferralConfigs(referralConfigs)

            // Seed Dynamic Payment Accounts - JazzCash only (03227422095)
            val paymentSettings = listOf(
                PaymentSettingEntity(
                    id = 1,
                    methodTitle = "JazzCash",
                    accountTitle = "Official JazzCash Escrow",
                    accountNumber = "03227422095",
                    bankOrProvider = "JazzCash",
                    instructionNote = "Send money to JazzCash: 03227422095. Copy the Transaction ID (TID) from the confirmation SMS and upload payment proof."
                )
            )
            db.paymentSettingDao().insertPaymentSettings(paymentSettings)

            // Seed Daily Tasks (Requirement: Exactly ONE Daily Task per user per day)
            val tasks = listOf(
                TaskEntity(
                    id = 1,
                    title = "Daily Partner Engagement Task",
                    description = "Watch the verified financial awareness briefing clip and confirm participation to claim your active plan's daily reward.",
                    category = "VIDEO",
                    rewardAmount = 25.0,
                    durationSeconds = 8,
                    actionUrl = "https://partner.rewards.example/briefing"
                )
            )
            db.taskDao().insertTasks(tasks)

            // Seed initial notification
            db.notificationDao().insertNotification(
                NotificationEntity(
                    userId = null,
                    title = "Welcome to EarnRewards Platform!",
                    message = "Explore earning plans, complete daily tasks, and share your referral link to earn rewards securely.",
                    type = "SYSTEM"
                )
            )

            // Seed initial wallet transaction for demo user
            db.walletTransactionDao().insertTransaction(
                WalletTransactionEntity(
                    transactionId = SecurityHelper.generateTransactionId("INIT"),
                    userId = 2,
                    type = "TASK_REWARD",
                    amount = 250.0,
                    balanceAfter = 250.0,
                    description = "Welcome bonus & introductory tasks completion reward."
                )
            )
        }

        private suspend fun syncPaymentSettings(db: AppDatabase) {
            val existing = db.paymentSettingDao().getAllPaymentSettingsSuspend()
            val hasOnlyJazzCash = existing.size == 1 &&
                    existing.first().methodTitle.contains("JazzCash", ignoreCase = true) &&
                    existing.first().accountNumber == "03227422095"

            if (!hasOnlyJazzCash) {
                db.paymentSettingDao().deleteAllPaymentSettings()
                db.paymentSettingDao().insertPaymentSetting(
                    PaymentSettingEntity(
                        id = 1,
                        methodTitle = "JazzCash",
                        accountTitle = "Official JazzCash Escrow",
                        accountNumber = "03227422095",
                        bankOrProvider = "JazzCash",
                        instructionNote = "Send money to JazzCash: 03227422095. Copy the Transaction ID (TID) from the confirmation SMS and upload payment proof.",
                        isActive = true
                    )
                )
            }
        }

        private suspend fun syncPlans(db: AppDatabase) {
            val defaultPlans = listOf(
                PlanEntity(
                    id = 1,
                    name = "Starter",
                    depositAmount = 500.0,
                    dailyTaskReward = 25.0,
                    maxWithdrawalLimit = 200.0,
                    description = "Entry plan with daily engagement task. Reward subject to 1 task per day."
                ),
                PlanEntity(
                    id = 2,
                    name = "Basic",
                    depositAmount = 1000.0,
                    dailyTaskReward = 50.0,
                    maxWithdrawalLimit = 500.0,
                    description = "Standard tier for consistent daily task completion."
                ),
                PlanEntity(
                    id = 3,
                    name = "Standard",
                    depositAmount = 2000.0,
                    dailyTaskReward = 100.0,
                    maxWithdrawalLimit = 1000.0,
                    description = "Recommended tier with elevated daily engagement tasks."
                ),
                PlanEntity(
                    id = 4,
                    name = "Premium",
                    depositAmount = 5000.0,
                    dailyTaskReward = 200.0,
                    maxWithdrawalLimit = 1000.0,
                    description = "High tier with priority partner video tasks and surveys."
                ),
                PlanEntity(
                    id = 5,
                    name = "Pro",
                    depositAmount = 10000.0,
                    dailyTaskReward = 300.0,
                    maxWithdrawalLimit = 1500.0,
                    description = "Top tier for maximum daily reward task participation."
                ),
                PlanEntity(
                    id = 6,
                    name = "Elite",
                    depositAmount = 15000.0,
                    dailyTaskReward = 450.0,
                    maxWithdrawalLimit = 1500.0,
                    description = "Executive tier with maximum daily rewards and dedicated withdrawal limit."
                )
            )
            val currentPlans = db.planDao().getAllPlansSuspend()
            val needsUpdate = currentPlans.any { plan ->
                when (plan.depositAmount.toInt()) {
                    500 -> plan.dailyTaskReward != 25.0 || plan.maxWithdrawalLimit != 200.0
                    1000 -> plan.dailyTaskReward != 50.0 || plan.maxWithdrawalLimit != 500.0
                    2000 -> plan.dailyTaskReward != 100.0 || plan.maxWithdrawalLimit != 1000.0
                    5000 -> plan.dailyTaskReward != 200.0 || plan.maxWithdrawalLimit != 1000.0
                    10000 -> plan.dailyTaskReward != 300.0 || plan.maxWithdrawalLimit != 1500.0
                    15000 -> plan.dailyTaskReward != 450.0 || plan.maxWithdrawalLimit != 1500.0
                    else -> false
                }
            } || currentPlans.size < 6

            if (needsUpdate || currentPlans.isEmpty()) {
                db.planDao().insertPlans(defaultPlans)
            }
        }
    }
}
