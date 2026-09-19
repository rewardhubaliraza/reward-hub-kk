package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.DepositEntity
import com.example.data.model.DepositStatus
import com.example.data.model.NotificationEntity
import com.example.data.model.PaymentSettingEntity
import com.example.data.model.PlanEntity
import com.example.data.model.ReferralConfigEntity
import com.example.data.model.ReferralEntity
import com.example.data.model.ReferralRewardEntity
import com.example.data.model.TaskCompletionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WalletTransactionEntity
import com.example.data.model.WithdrawalEntity
import com.example.data.model.WithdrawalStatus
import com.example.util.SecurityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RewardsRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val planDao = database.planDao()
    private val depositDao = database.depositDao()
    private val withdrawalDao = database.withdrawalDao()
    private val taskDao = database.taskDao()
    private val referralDao = database.referralDao()
    private val walletTransactionDao = database.walletTransactionDao()
    private val paymentSettingDao = database.paymentSettingDao()
    private val notificationDao = database.notificationDao()
    private val auditLogDao = database.auditLogDao()

    // ---------------- AUTHENTICATION ---------------- //

    suspend fun login(identifier: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanIdentifier = identifier.trim()
        val user = if (cleanIdentifier.contains("@")) {
            userDao.getUserByEmail(cleanIdentifier.lowercase())
        } else {
            userDao.getUserByMobile(cleanIdentifier)
        } ?: return@withContext Result.failure(Exception("Account not found with this identifier."))

        if (!user.isActive) {
            return@withContext Result.failure(Exception("This account has been deactivated. Please contact support."))
        }

        val inputHash = SecurityHelper.hashPassword(password)
        if (inputHash != user.passwordHash) {
            return@withContext Result.failure(Exception("Incorrect password. Please verify and try again."))
        }

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = user.id,
                actorName = user.name,
                actorRole = user.role,
                action = "USER_LOGIN",
                details = "User logged in successfully via ${if (cleanIdentifier.contains("@")) "Email" else "Mobile"}",
                targetType = "USER",
                targetId = user.id.toString()
            )
        )

        Result.success(user)
    }

    suspend fun register(
        name: String,
        mobile: String,
        email: String,
        password: String,
        referralCodeInput: String?
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        if (name.isBlank() || mobile.isBlank() || email.isBlank() || password.isBlank()) {
            return@withContext Result.failure(Exception("All fields are required."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(Exception("Password must be at least 6 characters."))
        }

        val existingEmail = userDao.getUserByEmail(email.trim().lowercase())
        if (existingEmail != null) {
            return@withContext Result.failure(Exception("An account with this email already exists."))
        }

        val existingMobile = userDao.getUserByMobile(mobile.trim())
        if (existingMobile != null) {
            return@withContext Result.failure(Exception("An account with this mobile number already exists."))
        }

        var referrerUser: UserEntity? = null
        val cleanRefCode = referralCodeInput?.trim()?.uppercase()
        if (!cleanRefCode.isNullOrEmpty()) {
            referrerUser = userDao.getUserByReferralCode(cleanRefCode)
            if (referrerUser == null) {
                return@withContext Result.failure(Exception("Referral code '$cleanRefCode' is invalid."))
            }
        }

        val newUser = UserEntity(
            name = name.trim(),
            mobile = mobile.trim(),
            email = email.trim().lowercase(),
            passwordHash = SecurityHelper.hashPassword(password),
            role = UserRole.USER.name,
            referralCode = SecurityHelper.generateReferralCode(name),
            referredByCode = referrerUser?.referralCode,
            balance = 0.0,
            totalEarnings = 0.0,
            totalDeposits = 0.0,
            totalWithdrawals = 0.0,
            dailyTaskEarnings = 0.0,
            referralEarnings = 0.0
        )

        val newUserId = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = newUserId)

        // Record referral connection if registered via code
        if (referrerUser != null) {
            referralDao.insertReferral(
                ReferralEntity(
                    referrerUserId = referrerUser.id,
                    referredUserId = newUserId,
                    referredUserName = createdUser.name,
                    referredUserEmail = createdUser.email,
                    isQualified = false
                )
            )

            notificationDao.insertNotification(
                NotificationEntity(
                    userId = referrerUser.id,
                    title = "New Referral Joined!",
                    message = "${createdUser.name} registered using your referral code. Complete qualifying deposit to claim reward.",
                    type = "REFERRAL"
                )
            )
        }

        // Welcome notification
        notificationDao.insertNotification(
            NotificationEntity(
                userId = newUserId,
                title = "Welcome to EarnRewards!",
                message = "Your account has been registered successfully. Explore available plans or start daily tasks.",
                type = "SYSTEM"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = newUserId,
                actorName = createdUser.name,
                actorRole = createdUser.role,
                action = "USER_REGISTER",
                details = "User registered successfully with referral code ${createdUser.referralCode}",
                targetType = "USER",
                targetId = newUserId.toString()
            )
        )

        Result.success(createdUser)
    }

    suspend fun forgotPassword(emailOrMobile: String, newPassword: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val clean = emailOrMobile.trim()
        if (clean.isBlank() || newPassword.length < 6) {
            return@withContext Result.failure(Exception("Please enter a valid identifier and password of at least 6 characters."))
        }

        val user = if (clean.contains("@")) {
            userDao.getUserByEmail(clean.lowercase())
        } else {
            userDao.getUserByMobile(clean)
        } ?: return@withContext Result.failure(Exception("No account found matching this email or mobile."))

        val updated = user.copy(passwordHash = SecurityHelper.hashPassword(newPassword))
        userDao.updateUser(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Password Changed",
                message = "Your account password was updated successfully. If this wasn't you, contact admin immediately.",
                type = "SYSTEM"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = user.id,
                actorName = user.name,
                actorRole = user.role,
                action = "PASSWORD_RESET",
                details = "Password reset completed successfully",
                targetType = "USER",
                targetId = user.id.toString()
            )
        )

        Result.success(true)
    }

    fun getUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)
    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun toggleUserStatus(adminUser: UserEntity, targetUserId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized: Admin privileges required."))
        }
        val target = userDao.getUserByIdSuspend(targetUserId) ?: return@withContext Result.failure(Exception("User not found."))
        val newStatus = !target.isActive
        userDao.updateUser(target.copy(isActive = newStatus))

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = if (newStatus) "ACTIVATE_USER" else "DEACTIVATE_USER",
                details = "Admin set user '${target.name}' status to ${if (newStatus) "Active" else "Deactivated"}",
                targetType = "USER",
                targetId = targetUserId.toString()
            )
        )

        Result.success(true)
    }

    // ---------------- PLANS ---------------- //

    fun getActivePlansFlow(): Flow<List<PlanEntity>> = planDao.getActivePlans()
    fun getAllPlansFlow(): Flow<List<PlanEntity>> = planDao.getAllPlans()

    suspend fun savePlan(adminUser: UserEntity, plan: PlanEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        if (plan.depositAmount <= 0 || plan.dailyTaskReward <= 0) {
            return@withContext Result.failure(Exception("Deposit and reward amounts must be positive numbers."))
        }

        if (plan.id == 0L) {
            planDao.insertPlan(plan)
        } else {
            planDao.updatePlan(plan)
        }

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "PLAN_CONFIG_SAVE",
                details = "Admin saved plan '${plan.name}' with deposit Rs. ${plan.depositAmount} and daily reward Rs. ${plan.dailyTaskReward}",
                targetType = "PLAN",
                targetId = plan.id.toString()
            )
        )

        Result.success(true)
    }

    // ---------------- DEPOSIT SYSTEM ---------------- //

    fun getUserDepositsFlow(userId: Long): Flow<List<DepositEntity>> = depositDao.getDepositsByUserId(userId)
    fun getAllDepositsFlow(): Flow<List<DepositEntity>> = depositDao.getAllDeposits()
    fun getPendingDepositsCountFlow(): Flow<Int> = depositDao.getPendingDepositsCount()

    suspend fun submitDeposit(
        userId: Long,
        planId: Long,
        amount: Double,
        paymentMethod: String,
        referenceId: String,
        proofNote: String?,
        proofUri: String?
    ): Result<Long> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSuspend(userId) ?: return@withContext Result.failure(Exception("User not found."))
        val plan = planDao.getPlanById(planId) ?: return@withContext Result.failure(Exception("Selected plan does not exist."))

        if (amount < plan.depositAmount) {
            return@withContext Result.failure(Exception("Deposit amount must be at least Rs. ${plan.depositAmount} for plan ${plan.name}."))
        }

        if (referenceId.isBlank()) {
            return@withContext Result.failure(Exception("Please enter transaction or reference ID from payment receipt."))
        }

        val deposit = DepositEntity(
            userId = userId,
            userName = user.name,
            planId = planId,
            planName = plan.name,
            amount = amount,
            paymentMethod = paymentMethod,
            transactionReferenceId = referenceId.trim(),
            proofUri = proofUri,
            proofNote = proofNote?.trim(),
            status = DepositStatus.PENDING.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val depositId = depositDao.insertDeposit(deposit)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Deposit Request Submitted",
                message = "Your deposit of Rs. $amount for ${plan.name} plan is pending verification by admin.",
                type = "DEPOSIT"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = userId,
                actorName = user.name,
                actorRole = user.role,
                action = "DEPOSIT_SUBMIT",
                details = "Submitted deposit of Rs. $amount for ${plan.name} with TID: $referenceId",
                targetType = "DEPOSIT",
                targetId = depositId.toString()
            )
        )

        Result.success(depositId)
    }

    suspend fun approveDeposit(adminUser: UserEntity, depositId: Long, adminNote: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }

        val deposit = depositDao.getDepositById(depositId) ?: return@withContext Result.failure(Exception("Deposit request not found."))
        if (deposit.status != DepositStatus.PENDING.name) {
            return@withContext Result.failure(Exception("Deposit is already ${deposit.status}."))
        }

        val user = userDao.getUserByIdSuspend(deposit.userId) ?: return@withContext Result.failure(Exception("User not found."))

        // 1. Update deposit status
        val updatedDeposit = deposit.copy(
            status = DepositStatus.APPROVED.name,
            adminNotes = adminNote.ifBlank { "Approved by Admin" },
            updatedAt = System.currentTimeMillis()
        )
        depositDao.updateDeposit(updatedDeposit)

        // 2. Update user wallet balance & activate plan
        val newBalance = user.balance + deposit.amount
        val newTotalDeposits = user.totalDeposits + deposit.amount
        val updatedUser = user.copy(
            balance = newBalance,
            totalDeposits = newTotalDeposits,
            activePlanId = deposit.planId,
            activePlanName = deposit.planName
        )
        userDao.updateUser(updatedUser)

        // 3. Record wallet transaction
        val txnId = SecurityHelper.generateTransactionId("DEP")
        walletTransactionDao.insertTransaction(
            WalletTransactionEntity(
                transactionId = txnId,
                userId = user.id,
                type = TransactionType.DEPOSIT.name,
                amount = deposit.amount,
                balanceAfter = newBalance,
                description = "Deposit approved for plan: ${deposit.planName} (Ref: ${deposit.transactionReferenceId})"
            )
        )

        // 4. Check qualifying referral reward
        if (!user.referredByCode.isNullOrBlank()) {
            val referrer = userDao.getUserByReferralCode(user.referredByCode)
            if (referrer != null) {
                val existingRef = referralDao.getReferralByReferred(user.id)
                if (existingRef != null && !existingRef.isQualified) {
                    // Match reward from referral configs
                    val configs = referralDao.getAllReferralConfigsSuspend()
                    val matchedConfig = configs.firstOrNull { deposit.amount >= it.minDeposit && deposit.amount <= it.maxDeposit }
                    val rewardAmount = matchedConfig?.rewardAmount ?: (if (deposit.amount >= 500) 250.0 else 0.0)

                    if (rewardAmount > 0) {
                        // Mark referral qualified
                        referralDao.updateReferral(
                            existingRef.copy(
                                isQualified = true,
                                qualifyingDepositAmount = deposit.amount,
                                rewardEarned = rewardAmount
                            )
                        )

                        // Insert referral reward record
                        referralDao.insertReferralReward(
                            ReferralRewardEntity(
                                referrerUserId = referrer.id,
                                referredUserId = user.id,
                                depositAmount = deposit.amount,
                                rewardAmount = rewardAmount
                            )
                        )

                        // Credit referrer balance
                        val referrerNewBalance = referrer.balance + rewardAmount
                        val referrerNewTotal = referrer.totalEarnings + rewardAmount
                        val referrerNewRefEarnings = referrer.referralEarnings + rewardAmount
                        userDao.updateUser(
                            referrer.copy(
                                balance = referrerNewBalance,
                                totalEarnings = referrerNewTotal,
                                referralEarnings = referrerNewRefEarnings
                            )
                        )

                        // Transaction for referrer
                        val refTxnId = SecurityHelper.generateTransactionId("REF")
                        walletTransactionDao.insertTransaction(
                            WalletTransactionEntity(
                                transactionId = refTxnId,
                                userId = referrer.id,
                                type = TransactionType.REFERRAL_BONUS.name,
                                amount = rewardAmount,
                                balanceAfter = referrerNewBalance,
                                description = "Referral bonus for qualifying deposit of ${user.name} (${deposit.planName})"
                            )
                        )

                        // Notification for referrer
                        notificationDao.insertNotification(
                            NotificationEntity(
                                userId = referrer.id,
                                title = "Referral Reward Credited!",
                                message = "You earned Rs. $rewardAmount because ${user.name} activated ${deposit.planName} plan.",
                                type = "REFERRAL"
                            )
                        )
                    }
                }
            }
        }

        // Notify user
        notificationDao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Deposit Approved!",
                message = "Your deposit of Rs. ${deposit.amount} is approved and ${deposit.planName} plan is now active.",
                type = "DEPOSIT"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "DEPOSIT_APPROVE",
                details = "Approved deposit #$depositId of Rs. ${deposit.amount} for ${user.name}",
                targetType = "DEPOSIT",
                targetId = depositId.toString()
            )
        )

        Result.success(true)
    }

    suspend fun rejectDeposit(adminUser: UserEntity, depositId: Long, adminNote: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }

        val deposit = depositDao.getDepositById(depositId) ?: return@withContext Result.failure(Exception("Deposit not found."))
        if (deposit.status != DepositStatus.PENDING.name) {
            return@withContext Result.failure(Exception("Deposit is already ${deposit.status}."))
        }

        val updatedDeposit = deposit.copy(
            status = DepositStatus.REJECTED.name,
            adminNotes = adminNote.ifBlank { "Rejected by Admin (Invalid reference/proof)" },
            updatedAt = System.currentTimeMillis()
        )
        depositDao.updateDeposit(updatedDeposit)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = deposit.userId,
                title = "Deposit Request Rejected",
                message = "Your deposit of Rs. ${deposit.amount} was rejected. Note: ${updatedDeposit.adminNotes}",
                type = "DEPOSIT"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "DEPOSIT_REJECT",
                details = "Rejected deposit #$depositId for user ${deposit.userName}. Reason: $adminNote",
                targetType = "DEPOSIT",
                targetId = depositId.toString()
            )
        )

        Result.success(true)
    }

    // ---------------- WITHDRAWAL SYSTEM ---------------- //

    fun getUserWithdrawalsFlow(userId: Long): Flow<List<WithdrawalEntity>> = withdrawalDao.getWithdrawalsByUserId(userId)
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>> = withdrawalDao.getAllWithdrawals()
    fun getPendingWithdrawalsCountFlow(): Flow<Int> = withdrawalDao.getPendingWithdrawalsCount()

    suspend fun submitWithdrawal(
        userId: Long,
        amount: Double,
        method: String,
        accountTitle: String,
        accountNumber: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (amount <= 0.0) {
            return@withContext Result.failure(Exception("Please enter a valid withdrawal amount."))
        }
        if (accountTitle.isBlank() || accountNumber.isBlank()) {
            return@withContext Result.failure(Exception("Account title and account number are required."))
        }

        val user = userDao.getUserByIdSuspend(userId) ?: return@withContext Result.failure(Exception("User not found."))

        // Check user's maximum withdrawal limit based on deposited plan
        val plan = user.activePlanId?.let { planDao.getPlanById(it) }
            ?: (if (user.activePlanName != null) planDao.getAllPlansSuspend().firstOrNull { it.name == user.activePlanName } else null)
        val depositBasis = plan?.depositAmount ?: user.totalDeposits
        val maxLimit = plan?.maxWithdrawalLimit ?: SecurityHelper.getMaxWithdrawalLimit(depositBasis)

        if (amount > maxLimit) {
            return@withContext Result.failure(Exception("Withdrawal amount exceeds your plan's maximum withdrawal limit of ${SecurityHelper.formatCurrency(maxLimit)}."))
        }

        if (user.balance < amount) {
            return@withContext Result.failure(Exception("Insufficient available balance. Available: ${SecurityHelper.formatCurrency(user.balance)}"))
        }

        // Deduct balance immediately to prevent double spending
        val newBalance = user.balance - amount
        userDao.updateUser(user.copy(balance = newBalance))

        val withdrawal = WithdrawalEntity(
            userId = userId,
            userName = user.name,
            amount = amount,
            method = method,
            accountTitle = accountTitle.trim(),
            accountNumber = accountNumber.trim(),
            status = WithdrawalStatus.PENDING.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val withdrawalId = withdrawalDao.insertWithdrawal(withdrawal)

        // Record pending transaction
        val txnId = SecurityHelper.generateTransactionId("WDR")
        walletTransactionDao.insertTransaction(
            WalletTransactionEntity(
                transactionId = txnId,
                userId = userId,
                type = TransactionType.WITHDRAWAL.name,
                amount = -amount,
                balanceAfter = newBalance,
                status = "PENDING",
                description = "Withdrawal request via $method to $accountNumber ($accountTitle)"
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Withdrawal Request Received",
                message = "Your withdrawal request for Rs. $amount via $method is being processed by administration.",
                type = "WITHDRAWAL"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = userId,
                actorName = user.name,
                actorRole = user.role,
                action = "WITHDRAWAL_SUBMIT",
                details = "Requested withdrawal of Rs. $amount via $method",
                targetType = "WITHDRAWAL",
                targetId = withdrawalId.toString()
            )
        )

        Result.success(withdrawalId)
    }

    suspend fun approveWithdrawal(adminUser: UserEntity, withdrawalId: Long, adminNote: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Withdrawal not found."))
        if (withdrawal.status != WithdrawalStatus.PENDING.name) {
            return@withContext Result.failure(Exception("Withdrawal is already ${withdrawal.status}."))
        }

        val updated = withdrawal.copy(
            status = WithdrawalStatus.APPROVED.name,
            adminNotes = adminNote.ifBlank { "Approved by Admin" },
            updatedAt = System.currentTimeMillis()
        )
        withdrawalDao.updateWithdrawal(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = withdrawal.userId,
                title = "Withdrawal Approved",
                message = "Your withdrawal of Rs. ${withdrawal.amount} has been approved and is queued for payout.",
                type = "WITHDRAWAL"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "WITHDRAWAL_APPROVE",
                details = "Approved withdrawal #$withdrawalId of Rs. ${withdrawal.amount}",
                targetType = "WITHDRAWAL",
                targetId = withdrawalId.toString()
            )
        )

        Result.success(true)
    }

    suspend fun markWithdrawalPaid(adminUser: UserEntity, withdrawalId: Long, adminNote: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Withdrawal not found."))
        if (withdrawal.status == WithdrawalStatus.PAID.name || withdrawal.status == WithdrawalStatus.REJECTED.name) {
            return@withContext Result.failure(Exception("Withdrawal is already ${withdrawal.status}."))
        }

        val user = userDao.getUserByIdSuspend(withdrawal.userId)
        if (user != null) {
            userDao.updateUser(user.copy(totalWithdrawals = user.totalWithdrawals + withdrawal.amount))
        }

        val updated = withdrawal.copy(
            status = WithdrawalStatus.PAID.name,
            adminNotes = adminNote.ifBlank { "Disbursed / Paid to user account" },
            updatedAt = System.currentTimeMillis()
        )
        withdrawalDao.updateWithdrawal(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = withdrawal.userId,
                title = "Withdrawal Disbursed / Paid!",
                message = "Rs. ${withdrawal.amount} has been sent to your ${withdrawal.method} account (${withdrawal.accountNumber}).",
                type = "WITHDRAWAL"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "WITHDRAWAL_PAID",
                details = "Marked withdrawal #$withdrawalId paid (Rs. ${withdrawal.amount} via ${withdrawal.method})",
                targetType = "WITHDRAWAL",
                targetId = withdrawalId.toString()
            )
        )

        Result.success(true)
    }

    suspend fun rejectWithdrawal(adminUser: UserEntity, withdrawalId: Long, adminNote: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId) ?: return@withContext Result.failure(Exception("Withdrawal not found."))
        if (withdrawal.status == WithdrawalStatus.PAID.name || withdrawal.status == WithdrawalStatus.REJECTED.name) {
            return@withContext Result.failure(Exception("Withdrawal is already ${withdrawal.status}."))
        }

        // Refund user balance
        val user = userDao.getUserByIdSuspend(withdrawal.userId)
        if (user != null) {
            val refundedBalance = user.balance + withdrawal.amount
            userDao.updateUser(user.copy(balance = refundedBalance))

            val refundTxnId = SecurityHelper.generateTransactionId("REFUND")
            walletTransactionDao.insertTransaction(
                WalletTransactionEntity(
                    transactionId = refundTxnId,
                    userId = user.id,
                    type = TransactionType.REFUND.name,
                    amount = withdrawal.amount,
                    balanceAfter = refundedBalance,
                    description = "Refund for rejected withdrawal #$withdrawalId. Reason: $adminNote"
                )
            )
        }

        val updated = withdrawal.copy(
            status = WithdrawalStatus.REJECTED.name,
            adminNotes = adminNote.ifBlank { "Rejected by Admin. Amount refunded to balance." },
            updatedAt = System.currentTimeMillis()
        )
        withdrawalDao.updateWithdrawal(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                userId = withdrawal.userId,
                title = "Withdrawal Rejected & Refunded",
                message = "Your withdrawal request for Rs. ${withdrawal.amount} was rejected. Amount has been restored to your balance. Note: $adminNote",
                type = "WITHDRAWAL"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "WITHDRAWAL_REJECT",
                details = "Rejected withdrawal #$withdrawalId and refunded Rs. ${withdrawal.amount} to user",
                targetType = "WITHDRAWAL",
                targetId = withdrawalId.toString()
            )
        )

        Result.success(true)
    }

    // ---------------- DAILY TASKS ---------------- //

    fun getActiveTasksFlow(): Flow<List<TaskEntity>> = taskDao.getActiveTasks()
    fun getAllTasksFlow(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    fun getUserCompletionsTodayFlow(userId: Long): Flow<List<TaskCompletionEntity>> =
        taskDao.getCompletionsByUserAndDate(userId, SecurityHelper.getTodayDateKey())
    fun getUserAllCompletionsFlow(userId: Long): Flow<List<TaskCompletionEntity>> =
        taskDao.getCompletionsByUser(userId)

    suspend fun completeTask(userId: Long, taskId: Long): Result<Double> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdSuspend(userId) ?: return@withContext Result.failure(Exception("User not found."))

        val todayKey = SecurityHelper.getTodayDateKey()
        // Strict Rule: A user can claim/complete the Daily Task only ONCE per calendar day
        val todayCompletions = taskDao.getCompletionsByUserAndDateSuspend(userId, todayKey)
        if (todayCompletions.isNotEmpty()) {
            return@withContext Result.failure(Exception("You have already claimed today's daily reward. Please come back tomorrow after reset!"))
        }

        val task = taskDao.getTaskById(taskId) ?: taskDao.getActiveTasksSuspend().firstOrNull()
            ?: return@withContext Result.failure(Exception("Task not available."))

        // Calculate daily task reward strictly and automatically based on user's active deposit plan
        val plan = user.activePlanId?.let { planDao.getPlanById(it) }
            ?: (if (user.activePlanName != null) planDao.getAllPlansSuspend().firstOrNull { it.name == user.activePlanName } else null)
            ?: (if (user.totalDeposits > 0) planDao.getAllPlansSuspend().filter { it.depositAmount <= user.totalDeposits }.maxByOrNull { it.depositAmount } else null)

        val dailyReward = plan?.dailyTaskReward ?: SecurityHelper.getDailyTaskReward(user.totalDeposits)

        // Record completion
        val completion = TaskCompletionEntity(
            userId = userId,
            taskId = task.id,
            taskTitle = task.title,
            rewardEarned = dailyReward,
            dateKey = todayKey,
            completedAt = System.currentTimeMillis()
        )
        taskDao.insertCompletion(completion)

        // Update user balances atomically
        val newBalance = user.balance + dailyReward
        val newTotal = user.totalEarnings + dailyReward
        val newDailyTaskEarnings = user.dailyTaskEarnings + dailyReward

        userDao.updateUser(
            user.copy(
                balance = newBalance,
                totalEarnings = newTotal,
                dailyTaskEarnings = newDailyTaskEarnings
            )
        )

        // Wallet transaction
        val txnId = SecurityHelper.generateTransactionId("TSK")
        walletTransactionDao.insertTransaction(
            WalletTransactionEntity(
                transactionId = txnId,
                userId = userId,
                type = TransactionType.TASK_REWARD.name,
                amount = dailyReward,
                balanceAfter = newBalance,
                description = "Daily Task Reward: ${task.title}"
            )
        )

        // Notification
        notificationDao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Daily Task Completed!",
                message = "You earned ${SecurityHelper.formatCurrency(dailyReward)} from today's daily task.",
                type = "TASK"
            )
        )

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = userId,
                actorName = user.name,
                actorRole = user.role,
                action = "DAILY_TASK_CLAIMED",
                details = "Claimed Daily Task #${task.id} reward ${SecurityHelper.formatCurrency(dailyReward)} (Date: $todayKey)",
                targetType = "TASK",
                targetId = task.id.toString()
            )
        )

        Result.success(dailyReward)
    }

    suspend fun saveTask(adminUser: UserEntity, task: TaskEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        if (task.title.isBlank() || task.rewardAmount <= 0) {
            return@withContext Result.failure(Exception("Title and positive reward amount are required."))
        }

        if (task.id == 0L) {
            taskDao.insertTask(task)
        } else {
            taskDao.updateTask(task)
        }

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "TASK_CONFIG_SAVE",
                details = "Admin saved task '${task.title}' with reward Rs. ${task.rewardAmount}",
                targetType = "TASK",
                targetId = task.id.toString()
            )
        )

        Result.success(true)
    }

    suspend fun deleteTask(adminUser: UserEntity, taskId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        taskDao.deleteTaskById(taskId)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "TASK_DELETE",
                details = "Admin deleted task #$taskId",
                targetType = "TASK",
                targetId = taskId.toString()
            )
        )
        Result.success(true)
    }

    // ---------------- REFERRAL PROGRAM ---------------- //

    fun getUserReferralsFlow(userId: Long): Flow<List<ReferralEntity>> = referralDao.getReferralsByReferrer(userId)
    fun getUserReferralRewardsFlow(userId: Long): Flow<List<ReferralRewardEntity>> = referralDao.getReferralRewards(userId)
    fun getAllReferralConfigsFlow(): Flow<List<ReferralConfigEntity>> = referralDao.getAllReferralConfigs()

    suspend fun updateReferralConfig(adminUser: UserEntity, config: ReferralConfigEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        referralDao.updateReferralConfig(config)
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "REFERRAL_CONFIG_UPDATE",
                details = "Admin updated tier (${config.minDeposit}-${config.maxDeposit}) to Rs. ${config.rewardAmount}",
                targetType = "REFERRAL_CONFIG",
                targetId = config.id.toString()
            )
        )
        Result.success(true)
    }

    // ---------------- PAYMENT SETTINGS (ADMIN DYNAMIC CONFIG) ---------------- //

    fun getActivePaymentSettingsFlow(): Flow<List<PaymentSettingEntity>> = paymentSettingDao.getActivePaymentSettings()
    fun getAllPaymentSettingsFlow(): Flow<List<PaymentSettingEntity>> = paymentSettingDao.getAllPaymentSettings()

    suspend fun savePaymentSetting(adminUser: UserEntity, setting: PaymentSettingEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        if (setting.methodTitle.isBlank() || setting.accountNumber.isBlank()) {
            return@withContext Result.failure(Exception("Method title and account number are required."))
        }

        if (setting.id == 0L) {
            paymentSettingDao.insertPaymentSetting(setting)
        } else {
            paymentSettingDao.updatePaymentSetting(setting)
        }

        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "PAYMENT_SETTING_SAVE",
                details = "Admin updated payment account: ${setting.methodTitle} (${setting.accountNumber})",
                targetType = "PAYMENT_SETTING",
                targetId = setting.id.toString()
            )
        )

        Result.success(true)
    }

    suspend fun deletePaymentSetting(adminUser: UserEntity, id: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        paymentSettingDao.deletePaymentSettingById(id)
        Result.success(true)
    }

    // ---------------- TRANSACTIONS & NOTIFICATIONS & AUDIT ---------------- //

    fun getUserTransactionsFlow(userId: Long): Flow<List<WalletTransactionEntity>> = walletTransactionDao.getTransactionsByUserId(userId)
    fun getAllTransactionsFlow(): Flow<List<WalletTransactionEntity>> = walletTransactionDao.getAllTransactions()

    fun getUserNotificationsFlow(userId: Long): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForUser(userId)

    suspend fun markNotificationRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsRead(userId: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun broadcastNotification(adminUser: UserEntity, title: String, message: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN.name) {
            return@withContext Result.failure(Exception("Unauthorized."))
        }
        notificationDao.insertNotification(
            NotificationEntity(
                userId = null,
                title = title.trim(),
                message = message.trim(),
                type = "SYSTEM"
            )
        )
        auditLogDao.insertAuditLog(
            AuditLogEntity(
                actorId = adminUser.id,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "ANNOUNCEMENT_BROADCAST",
                details = "Broadcasted announcement: '$title'",
                targetType = "NOTIFICATION"
            )
        )
        Result.success(true)
    }

    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()
}
