package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AuditLogEntity
import com.example.data.model.DepositEntity
import com.example.data.model.PaymentSettingEntity
import com.example.data.model.PlanEntity
import com.example.data.model.TaskEntity
import com.example.data.model.UserEntity
import com.example.data.model.WithdrawalEntity
import com.example.ui.MainViewModel
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldSecondaryLight
import com.example.ui.theme.IndigoTertiaryLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.util.SecurityHelper

@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allDeposits by viewModel.allDeposits.collectAsState()
    val allWithdrawals by viewModel.allWithdrawals.collectAsState()
    val allPlans by viewModel.allPlans.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allPaymentAccounts by viewModel.allPaymentSettings.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Deposits", "Withdrawals", "Tasks & Plans", "Accounts", "Users & Logs")

    val pendingDeposits = allDeposits.filter { it.status == "PENDING" }
    val pendingWithdrawals = allWithdrawals.filter { it.status == "PENDING" }

    // Dialog States
    var depositToProcess by remember { mutableStateOf<DepositEntity?>(null) }
    var withdrawalToProcess by remember { mutableStateOf<WithdrawalEntity?>(null) }
    var showNewTaskDialog by remember { mutableStateOf(false) }
    var showNewAccountDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = EmeraldPrimaryLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Admin Control Center",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "Signed in as: ${currentUser?.name ?: "Administrator"} (${currentUser?.role ?: "ADMIN"})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = WarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${pendingDeposits.size + pendingWithdrawals.size} Pending",
                        color = WarningAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Navigation Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = currentTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    val badgeCount = when (index) {
                        1 -> pendingDeposits.size
                        2 -> pendingWithdrawals.size
                        else -> 0
                    }
                    Tab(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                if (badgeCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        color = ErrorRed,
                                        shape = CircleShape,
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$badgeCount",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // TAB 0: OVERVIEW
        if (currentTab == 0) {
            val totalDepositVolume = allDeposits.filter { it.status == "APPROVED" }.sumOf { it.amount }
            val totalWithdrawalVolume = allWithdrawals.filter { it.status == "APPROVED" || it.status == "PAID" }.sumOf { it.amount }
            val totalUsersCount = allUsers.size
            val activeUsersCount = allUsers.count { it.isActive && it.activePlanName != null }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Platform Financial Metrics",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = "Total Users",
                            value = "$totalUsersCount",
                            icon = Icons.Default.Group,
                            accentColor = IndigoTertiaryLight,
                            subtext = "$activeUsersCount active plans",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pending Approvals",
                            value = "${pendingDeposits.size + pendingWithdrawals.size}",
                            icon = Icons.Default.Receipt,
                            accentColor = WarningAmber,
                            subtext = "${pendingDeposits.size} dep, ${pendingWithdrawals.size} wdr",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatCard(
                            title = "Approved Deposits",
                            value = SecurityHelper.formatCurrency(totalDepositVolume),
                            icon = Icons.Default.AccountBalance,
                            accentColor = EmeraldPrimaryLight,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Processed Payouts",
                            value = SecurityHelper.formatCurrency(totalWithdrawalVolume),
                            icon = Icons.Default.ArrowUpward,
                            accentColor = GoldSecondaryLight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showBroadcastDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoTertiaryLight),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_broadcast_btn")
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Broadcast Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick pending deposits list
            if (pendingDeposits.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Deposit Verifications (${pendingDeposits.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(pendingDeposits.take(3)) { dep ->
                    AdminDepositCard(
                        deposit = dep,
                        onActionClick = { depositToProcess = dep }
                    )
                }
            }
        }

        // TAB 1: DEPOSITS MANAGEMENT
        else if (currentTab == 1) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Deposit Requests (${allDeposits.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (allDeposits.isEmpty()) {
                item {
                    Text("No deposit records found.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(allDeposits) { dep ->
                    AdminDepositCard(
                        deposit = dep,
                        onActionClick = { depositToProcess = dep }
                    )
                }
            }
        }

        // TAB 2: WITHDRAWALS MANAGEMENT
        else if (currentTab == 2) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Withdrawal Requests (${allWithdrawals.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (allWithdrawals.isEmpty()) {
                item {
                    Text("No withdrawal records found.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(allWithdrawals) { wdr ->
                    AdminWithdrawalCard(
                        withdrawal = wdr,
                        onActionClick = { withdrawalToProcess = wdr }
                    )
                }
            }
        }

        // TAB 3: TASKS & PLANS MANAGEMENT
        else if (currentTab == 3) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Task Management (${allTasks.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Button(
                        onClick = { showNewTaskDialog = true },
                        modifier = Modifier.testTag("admin_add_task_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Task", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(allTasks) { task ->
                AdminTaskCard(
                    task = task,
                    onDelete = {
                        viewModel.adminDeleteTask(task.id) { _, msg -> viewModel.showMessage(msg) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Earning Plans Configuration (${allPlans.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(allPlans) { plan ->
                AdminPlanCard(plan = plan)
            }
        }

        // TAB 4: PAYMENT SETTINGS (DYNAMIC ADMIN ACCOUNTS)
        else if (currentTab == 4) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Admin Deposit Accounts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Dynamic payment accounts shown on user deposit page",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showNewAccountDialog = true },
                        modifier = Modifier.testTag("admin_add_payment_acc_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(allPaymentAccounts) { account ->
                AdminPaymentAccountCard(
                    account = account,
                    onDelete = {
                        viewModel.adminDeletePaymentSetting(account.id) { _, msg -> viewModel.showMessage(msg) }
                    }
                )
            }
        }

        // TAB 5: USERS & AUDIT LOGS
        else if (currentTab == 5) {
            item {
                Text(
                    text = "Registered Users (${allUsers.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(allUsers) { userItem ->
                AdminUserRow(
                    user = userItem,
                    onToggleStatus = {
                        viewModel.adminToggleUserStatus(userItem.id) { _, msg -> viewModel.showMessage(msg) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Audit & Compliance Activity Log (${auditLogs.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(auditLogs.take(15)) { log ->
                AdminAuditLogRow(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Process Deposit Dialog (Approve / Reject)
    if (depositToProcess != null) {
        val dep = depositToProcess!!
        var adminNote by remember { mutableStateOf("") }
        var isActionLoading by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { depositToProcess = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Process Deposit #${dep.id}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("User: ${dep.userName} (ID: ${dep.userId})", fontSize = 12.sp)
                    Text("Plan: ${dep.planName} Plan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Amount: ${SecurityHelper.formatCurrency(dep.amount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimaryLight)
                    Text("Payment Method: ${dep.paymentMethod}", fontSize = 12.sp)
                    Text("Reference ID: ${dep.transactionReferenceId}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (!dep.proofUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Proof: ${dep.proofUri} (${dep.proofNote ?: "No notes"})",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Admin Verification Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isActionLoading = true
                                viewModel.adminRejectDeposit(dep.id, adminNote.ifBlank { "Invalid TID" }) { success, msg ->
                                    isActionLoading = false
                                    viewModel.showMessage(msg)
                                    depositToProcess = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isActionLoading = true
                                viewModel.adminApproveDeposit(dep.id, adminNote.ifBlank { "Payment verified" }) { success, msg ->
                                    isActionLoading = false
                                    viewModel.showMessage(msg)
                                    depositToProcess = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Approve", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Process Withdrawal Dialog (Approve / Mark Paid / Reject)
    if (withdrawalToProcess != null) {
        val wdr = withdrawalToProcess!!
        var adminNote by remember { mutableStateOf("") }
        var isActionLoading by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { withdrawalToProcess = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Process Withdrawal #${wdr.id}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Beneficiary: ${wdr.userName} (ID: ${wdr.userId})", fontSize = 12.sp)
                    Text("Amount: ${SecurityHelper.formatCurrency(wdr.amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldSecondaryLight)
                    Text("Method: ${wdr.method}", fontSize = 12.sp)
                    Text("Account Title: ${wdr.accountTitle}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Account Number: ${wdr.accountNumber}", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adminNote,
                        onValueChange = { adminNote = it },
                        label = { Text("Transfer Reference / Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                isActionLoading = true
                                viewModel.adminRejectWithdrawal(wdr.id, adminNote.ifBlank { "Account details invalid" }) { success, msg ->
                                    isActionLoading = false
                                    viewModel.showMessage(msg)
                                    withdrawalToProcess = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isActionLoading = true
                                viewModel.adminMarkWithdrawalPaid(wdr.id, adminNote.ifBlank { "Transfer executed" }) { success, msg ->
                                    isActionLoading = false
                                    viewModel.showMessage(msg)
                                    withdrawalToProcess = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mark Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add New Task Dialog
    if (showNewTaskDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("VIDEO") }
        var rewardText by remember { mutableStateOf("25") }
        var durationText by remember { mutableStateOf("10") }

        Dialog(onDismissRequest = { showNewTaskDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Create New Daily Task", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = rewardText,
                        onValueChange = { rewardText = it },
                        label = { Text("Reward Amount (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Timer Duration (seconds)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val r = rewardText.toDoubleOrNull() ?: 20.0
                            val d = durationText.toIntOrNull() ?: 10
                            viewModel.adminSaveTask(
                                TaskEntity(
                                    title = title.ifBlank { "Daily Engagement Task" },
                                    description = description.ifBlank { "Complete the interactive partner session" },
                                    category = category,
                                    rewardAmount = r,
                                    durationSeconds = d
                                )
                            ) { success, msg ->
                                viewModel.showMessage(msg)
                                showNewTaskDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Task", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Add New Payment Account Dialog
    if (showNewAccountDialog) {
        var methodTitle by remember { mutableStateOf("") }
        var bankOrProvider by remember { mutableStateOf("") }
        var accountTitle by remember { mutableStateOf("") }
        var accountNumber by remember { mutableStateOf("") }
        var instructionNote by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showNewAccountDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Add Admin Deposit Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = methodTitle,
                        onValueChange = { methodTitle = it },
                        label = { Text("Method Title (e.g. Bank Alfalah)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = bankOrProvider,
                        onValueChange = { bankOrProvider = it },
                        label = { Text("Bank / Provider Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = accountTitle,
                        onValueChange = { accountTitle = it },
                        label = { Text("Account Holder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Account Number / IBAN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = instructionNote,
                        onValueChange = { instructionNote = it },
                        label = { Text("Transfer Instructions") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.adminSavePaymentSetting(
                                PaymentSettingEntity(
                                    methodTitle = methodTitle.ifBlank { "Bank Transfer" },
                                    bankOrProvider = bankOrProvider.ifBlank { "Official Bank" },
                                    accountTitle = accountTitle.ifBlank { "Official Rewards Treasury" },
                                    accountNumber = accountNumber.ifBlank { "00000000000000" },
                                    instructionNote = instructionNote.ifBlank { "Please attach payment screenshot and enter transaction ID." }
                                )
                            ) { _, msg ->
                                viewModel.showMessage(msg)
                                showNewAccountDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Broadcast Notification Dialog
    if (showBroadcastDialog) {
        var title by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showBroadcastDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Broadcast System Alert", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message Body") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.adminBroadcastNotification(
                                title = title.ifBlank { "System Announcement" },
                                message = message.ifBlank { "Platform updates and new partner tasks are now live." }
                            ) { _, msg ->
                                viewModel.showMessage(msg)
                                showBroadcastDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send Broadcast", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDepositCard(
    deposit: DepositEntity,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (deposit.status == "PENDING") WarningAmber.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${deposit.planName} Plan • ${SecurityHelper.formatCurrency(deposit.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(status = deposit.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "User: ${deposit.userName} • TID: ${deposit.transactionReferenceId}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Date: ${SecurityHelper.formatDate(deposit.createdAt)} • Method: ${deposit.paymentMethod}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            if (deposit.status == "PENDING") {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Verify & Process", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalCard(
    withdrawal: WithdrawalEntity,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (withdrawal.status == "PENDING") WarningAmber.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${withdrawal.method} • ${SecurityHelper.formatCurrency(withdrawal.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(status = withdrawal.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Beneficiary: ${withdrawal.userName} • ${withdrawal.accountTitle} (${withdrawal.accountNumber})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (withdrawal.status == "PENDING") {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Process Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminTaskCard(task: TaskEntity, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${task.category} • ${task.durationSeconds}s • Reward: ${SecurityHelper.formatCurrency(task.rewardAmount)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete task", tint = ErrorRed)
            }
        }
    }
}

@Composable
fun AdminPlanCard(plan: PlanEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "${plan.name} Tier",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Deposit: ${SecurityHelper.formatCurrency(plan.depositAmount)} • Daily: ${SecurityHelper.formatCurrency(plan.dailyTaskReward)} • Max Wdr: ${SecurityHelper.formatCurrency(plan.maxWithdrawalLimit)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            StatusBadge(status = if (plan.isActive) "ACTIVE" else "INACTIVE")
        }
    }
}

@Composable
fun AdminPaymentAccountCard(
    account: PaymentSettingEntity,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${account.methodTitle} (${account.bankOrProvider})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Title: ${account.accountTitle}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Number: ${account.accountNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimaryLight
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Account", tint = ErrorRed)
            }
        }
    }
}

@Composable
fun AdminUserRow(user: UserEntity, onToggleStatus: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.name} (${user.role})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${user.email} • ${user.mobile} • Code: ${user.referralCode}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Balance: ${SecurityHelper.formatCurrency(user.balance)} • Plan: ${user.activePlanName ?: "None"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldPrimaryLight
                )
            }

            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (user.isActive) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = "Toggle status",
                    tint = if (user.isActive) SuccessGreen else ErrorRed
                )
            }
        }
    }
}

@Composable
fun AdminAuditLogRow(log: AuditLogEntity) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${log.actorName} • ${log.action}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SecurityHelper.formatDate(log.timestamp),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${log.targetType}: ${log.details}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
