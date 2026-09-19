package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
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
import com.example.data.repository.RewardsRepository
import com.example.util.SecurityHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val label: String) {
    DASHBOARD("Dashboard"),
    PLANS("Plans"),
    TASKS("Daily Tasks"),
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    REFERRALS("Referral"),
    NOTIFICATIONS("Alerts"),
    ADMIN("Admin Panel")
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = RewardsRepository(database)

    // Navigation & Screen selection
    private val _currentDestination = MutableStateFlow(AppDestination.DASHBOARD)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Active User ID (null = not logged in; default pre-selected demo user 2 for immediate viewable experience)
    private val _currentUserId = MutableStateFlow<Long?>(2L)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    // Current User state flow
    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Plans
    val activePlans: StateFlow<List<PlanEntity>> = repository.getActivePlansFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlans: StateFlow<List<PlanEntity>> = repository.getAllPlansFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Deposits
    val userDeposits: StateFlow<List<DepositEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserDepositsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeposits: StateFlow<List<DepositEntity>> = repository.getAllDepositsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDepositsCount: StateFlow<Int> = repository.getPendingDepositsCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Withdrawals
    val userWithdrawals: StateFlow<List<WithdrawalEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserWithdrawalsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals: StateFlow<List<WithdrawalEntity>> = repository.getAllWithdrawalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingWithdrawalsCount: StateFlow<Int> = repository.getPendingWithdrawalsCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userMaxWithdrawalLimit: StateFlow<Double> = combine(currentUser, activePlans) { user, plans ->
        if (user == null) return@combine 500.0
        val plan = plans.firstOrNull { it.id == user.activePlanId }
            ?: plans.firstOrNull { it.name == user.activePlanName }
        plan?.maxWithdrawalLimit ?: SecurityHelper.getMaxWithdrawalLimit(user.totalDeposits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 500.0)

    val userDailyTaskReward: StateFlow<Double> = combine(currentUser, activePlans) { user, plans ->
        if (user == null) return@combine 25.0
        val plan = plans.firstOrNull { it.id == user.activePlanId }
            ?: plans.firstOrNull { it.name == user.activePlanName }
        plan?.dailyTaskReward ?: SecurityHelper.getDailyTaskReward(user.totalDeposits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25.0)

    // Daily Tasks
    val activeTasks: StateFlow<List<TaskEntity>> = repository.getActiveTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCompletions: StateFlow<List<TaskCompletionEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserCompletionsTodayFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUserCompletions: StateFlow<List<TaskCompletionEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserAllCompletionsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Referrals
    val userReferrals: StateFlow<List<ReferralEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserReferralsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userReferralRewards: StateFlow<List<ReferralRewardEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserReferralRewardsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referralConfigs: StateFlow<List<ReferralConfigEntity>> = repository.getAllReferralConfigsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions
    val userTransactions: StateFlow<List<WalletTransactionEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserTransactionsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<WalletTransactionEntity>> = repository.getAllTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payment Settings (Configured dynamically in Admin Settings)
    val activePaymentSettings: StateFlow<List<PaymentSettingEntity>> = repository.getActivePaymentSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentSettings: StateFlow<List<PaymentSettingEntity>> = repository.getAllPaymentSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUserId.flatMapLatest { id ->
        if (id != null) repository.getUserNotificationsFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = userNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Admin & Audit
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback Toast / Snackbar message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun navigateTo(dest: AppDestination) {
        _currentDestination.value = dest
    }

    // ---------------- AUTH ACTIONS ---------------- //

    fun login(identifier: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(identifier, password)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _currentUserId.value = user.id
                _currentDestination.value = if (user.role == "ADMIN") AppDestination.ADMIN else AppDestination.DASHBOARD
                onResult(true, "Welcome back, ${user.name}!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(
        name: String,
        mobile: String,
        email: String,
        password: String,
        referralCode: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.register(name, mobile, email, password, referralCode)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                _currentUserId.value = user.id
                _currentDestination.value = AppDestination.DASHBOARD
                onResult(true, "Account created successfully!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun forgotPassword(emailOrMobile: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.forgotPassword(emailOrMobile, newPass)
            if (result.isSuccess) {
                onResult(true, "Password has been updated. You can now log in.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Password reset failed")
            }
        }
    }

    fun logout() {
        _currentUserId.value = null
        _currentDestination.value = AppDestination.DASHBOARD
        showMessage("Logged out successfully.")
    }

    fun switchRole(asAdmin: Boolean) {
        _currentUserId.value = if (asAdmin) 1L else 2L
        _currentDestination.value = if (asAdmin) AppDestination.ADMIN else AppDestination.DASHBOARD
        showMessage("Switched to ${if (asAdmin) "Administrator" else "Regular User"} mode.")
    }

    // ---------------- DEPOSIT ---------------- //

    fun submitDeposit(
        planId: Long,
        amount: Double,
        paymentMethod: String,
        referenceId: String,
        proofNote: String?,
        proofUri: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = _currentUserId.value ?: return onResult(false, "Please log in first.")
        viewModelScope.launch {
            val result = repository.submitDeposit(
                userId = uid,
                planId = planId,
                amount = amount,
                paymentMethod = paymentMethod,
                referenceId = referenceId,
                proofNote = proofNote,
                proofUri = proofUri
            )
            if (result.isSuccess) {
                onResult(true, "Deposit request submitted! Status: PENDING admin approval.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to submit deposit")
            }
        }
    }

    // ---------------- WITHDRAWAL ---------------- //

    fun submitWithdrawal(
        amount: Double,
        method: String,
        accountTitle: String,
        accountNumber: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = _currentUserId.value ?: return onResult(false, "Please log in first.")
        viewModelScope.launch {
            val result = repository.submitWithdrawal(
                userId = uid,
                amount = amount,
                method = method,
                accountTitle = accountTitle,
                accountNumber = accountNumber
            )
            if (result.isSuccess) {
                onResult(true, "Withdrawal request submitted! Status: PENDING admin processing.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to submit withdrawal")
            }
        }
    }

    // ---------------- DAILY TASKS ---------------- //

    fun completeTask(taskId: Long, onResult: (Boolean, String) -> Unit) {
        val uid = _currentUserId.value ?: return onResult(false, "Please log in first.")
        viewModelScope.launch {
            val result = repository.completeTask(uid, taskId)
            if (result.isSuccess) {
                val reward = result.getOrNull() ?: 0.0
                onResult(true, "Task completed! Reward of Rs. $reward added to your balance.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Task completion failed")
            }
        }
    }

    // ---------------- NOTIFICATIONS ---------------- //

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val uid = _currentUserId.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(uid)
            showMessage("All notifications marked as read.")
        }
    }

    // ---------------- ADMIN ACTIONS ---------------- //

    fun adminApproveDeposit(depositId: Long, note: String, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.approveDeposit(admin, depositId, note)
            if (result.isSuccess) {
                onResult(true, "Deposit #$depositId approved and balance credited!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to approve deposit")
            }
        }
    }

    fun adminRejectDeposit(depositId: Long, note: String, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.rejectDeposit(admin, depositId, note)
            if (result.isSuccess) {
                onResult(true, "Deposit #$depositId has been rejected.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to reject deposit")
            }
        }
    }

    fun adminApproveWithdrawal(withdrawalId: Long, note: String, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.approveWithdrawal(admin, withdrawalId, note)
            if (result.isSuccess) {
                onResult(true, "Withdrawal #$withdrawalId approved.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to approve withdrawal")
            }
        }
    }

    fun adminMarkWithdrawalPaid(withdrawalId: Long, note: String, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.markWithdrawalPaid(admin, withdrawalId, note)
            if (result.isSuccess) {
                onResult(true, "Withdrawal #$withdrawalId marked as PAID!")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to mark paid")
            }
        }
    }

    fun adminRejectWithdrawal(withdrawalId: Long, note: String, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.rejectWithdrawal(admin, withdrawalId, note)
            if (result.isSuccess) {
                onResult(true, "Withdrawal #$withdrawalId rejected and balance refunded to user.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to reject withdrawal")
            }
        }
    }

    fun adminSavePlan(plan: PlanEntity, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.savePlan(admin, plan)
            if (result.isSuccess) {
                onResult(true, "Plan '${plan.name}' saved successfully.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to save plan")
            }
        }
    }

    fun adminSaveTask(task: TaskEntity, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.saveTask(admin, task)
            if (result.isSuccess) {
                onResult(true, "Task '${task.title}' saved successfully.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to save task")
            }
        }
    }

    fun adminDeleteTask(taskId: Long, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.deleteTask(admin, taskId)
            if (result.isSuccess) {
                onResult(true, "Task deleted.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to delete task")
            }
        }
    }

    fun adminSavePaymentSetting(setting: PaymentSettingEntity, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.savePaymentSetting(admin, setting)
            if (result.isSuccess) {
                onResult(true, "Payment account '${setting.methodTitle}' saved successfully.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to save payment setting")
            }
        }
    }

    fun adminDeletePaymentSetting(id: Long, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.deletePaymentSetting(admin, id)
            if (result.isSuccess) {
                onResult(true, "Payment account removed.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to delete payment setting")
            }
        }
    }

    fun adminUpdateReferralConfig(config: ReferralConfigEntity, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.updateReferralConfig(admin, config)
            if (result.isSuccess) {
                onResult(true, "Referral tier updated.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to update referral config")
            }
        }
    }

    fun adminBroadcastNotification(title: String, message: String, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.broadcastNotification(admin, title, message)
            if (result.isSuccess) {
                onResult(true, "Announcement broadcasted to all users.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to broadcast notification")
            }
        }
    }

    fun adminToggleUserStatus(targetUserId: Long, onResult: (Boolean, String) -> Unit) {
        val admin = currentUser.value ?: return onResult(false, "Admin session required.")
        viewModelScope.launch {
            val result = repository.toggleUserStatus(admin, targetUserId)
            if (result.isSuccess) {
                onResult(true, "User status updated.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to update user status")
            }
        }
    }
}
