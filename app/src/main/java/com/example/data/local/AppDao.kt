package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserByIdSuspend(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): UserEntity?

    @Query("SELECT * FROM users WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCode(code: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY id DESC")
    suspend fun getAllUsersSuspend(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans WHERE isActive = 1 ORDER BY depositAmount ASC")
    fun getActivePlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans ORDER BY depositAmount ASC")
    fun getAllPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans ORDER BY depositAmount ASC")
    suspend fun getAllPlansSuspend(): List<PlanEntity>

    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlanById(id: Long): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<PlanEntity>)

    @Update
    suspend fun updatePlan(plan: PlanEntity)

    @Delete
    suspend fun deletePlan(plan: PlanEntity)
}

@Dao
interface DepositDao {
    @Query("SELECT * FROM deposits ORDER BY createdAt DESC")
    fun getAllDeposits(): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDepositsByUserId(userId: Long): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits WHERE id = :id")
    suspend fun getDepositById(id: Long): DepositEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity): Long

    @Update
    suspend fun updateDeposit(deposit: DepositEntity)

    @Query("SELECT COUNT(*) FROM deposits WHERE status = 'PENDING'")
    fun getPendingDepositsCount(): Flow<Int>
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals ORDER BY createdAt DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWithdrawalsByUserId(userId: Long): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE id = :id")
    suspend fun getWithdrawalById(id: Long): WithdrawalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Query("SELECT COUNT(*) FROM withdrawals WHERE status = 'PENDING'")
    fun getPendingWithdrawalsCount(): Flow<Int>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY rewardAmount DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY id ASC")
    suspend fun getActiveTasksSuspend(): List<TaskEntity>

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    suspend fun getAllTasksSuspend(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT * FROM task_completions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getCompletionsByUser(userId: Long): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions WHERE userId = :userId AND dateKey = :dateKey ORDER BY completedAt DESC")
    fun getCompletionsByUserAndDate(userId: Long, dateKey: String): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions WHERE userId = :userId AND dateKey = :dateKey")
    suspend fun getCompletionsByUserAndDateSuspend(userId: Long, dateKey: String): List<TaskCompletionEntity>

    @Query("SELECT * FROM task_completions WHERE userId = :userId AND taskId = :taskId AND dateKey = :dateKey LIMIT 1")
    suspend fun getCompletion(userId: Long, taskId: Long, dateKey: String): TaskCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: TaskCompletionEntity): Long
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE referrerUserId = :referrerId ORDER BY createdAt DESC")
    fun getReferralsByReferrer(referrerId: Long): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE referredUserId = :referredId LIMIT 1")
    suspend fun getReferralByReferred(referredId: Long): ReferralEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralEntity): Long

    @Update
    suspend fun updateReferral(referral: ReferralEntity)

    @Query("SELECT * FROM referral_rewards WHERE referrerUserId = :referrerId ORDER BY createdAt DESC")
    fun getReferralRewards(referrerId: Long): Flow<List<ReferralRewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferralReward(reward: ReferralRewardEntity): Long

    @Query("SELECT * FROM referral_configs ORDER BY minDeposit ASC")
    fun getAllReferralConfigs(): Flow<List<ReferralConfigEntity>>

    @Query("SELECT * FROM referral_configs ORDER BY minDeposit ASC")
    suspend fun getAllReferralConfigsSuspend(): List<ReferralConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferralConfigs(configs: List<ReferralConfigEntity>)

    @Update
    suspend fun updateReferralConfig(config: ReferralConfigEntity)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsByUserId(userId: Long): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)
}

@Dao
interface PaymentSettingDao {
    @Query("SELECT * FROM payment_settings ORDER BY id ASC")
    fun getAllPaymentSettings(): Flow<List<PaymentSettingEntity>>

    @Query("SELECT * FROM payment_settings WHERE isActive = 1 ORDER BY id ASC")
    fun getActivePaymentSettings(): Flow<List<PaymentSettingEntity>>

    @Query("SELECT * FROM payment_settings ORDER BY id ASC")
    suspend fun getAllPaymentSettingsSuspend(): List<PaymentSettingEntity>

    @Query("SELECT * FROM payment_settings WHERE id = :id")
    suspend fun getPaymentSettingById(id: Long): PaymentSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentSetting(setting: PaymentSettingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentSettings(settings: List<PaymentSettingEntity>)

    @Update
    suspend fun updatePaymentSetting(setting: PaymentSettingEntity)

    @Query("DELETE FROM payment_settings WHERE id = :id")
    suspend fun deletePaymentSettingById(id: Long)

    @Query("DELETE FROM payment_settings")
    suspend fun deleteAllPaymentSettings()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId OR userId IS NULL ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId OR userId IS NULL")
    suspend fun markAllAsRead(userId: Long)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long
}
