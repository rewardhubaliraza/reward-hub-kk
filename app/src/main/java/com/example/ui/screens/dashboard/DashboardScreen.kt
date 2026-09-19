package com.example.ui.screens.dashboard

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.AppDestination
import com.example.ui.MainViewModel
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TransactionRow
import com.example.ui.theme.EmeraldPrimaryDark
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.GoldSecondaryLight
import com.example.ui.theme.IndigoTertiaryLight
import com.example.ui.theme.SuccessGreen
import com.example.util.SecurityHelper

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val todayCompletions by viewModel.todayCompletions.collectAsState()
    val maxWithdrawalLimit by viewModel.userMaxWithdrawalLimit.collectAsState()
    val dailyTaskReward by viewModel.userDailyTaskReward.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Welcome Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Hello, ${user?.name ?: "Guest"}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Referral Code: ${user?.referralCode ?: "—"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (user?.activePlanName != null) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(
                        1.dp,
                        if (user?.activePlanName != null) SuccessGreen.copy(alpha = 0.4f) else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Active Plan",
                            tint = if (user?.activePlanName != null) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user?.activePlanName ?: "Free Member",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (user?.activePlanName != null) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Hero Balance Card with Primary Actions
        item {
            HeroBalanceCard(
                user = user,
                maxWithdrawalLimit = maxWithdrawalLimit,
                dailyTaskReward = dailyTaskReward,
                onDepositClick = { viewModel.navigateTo(AppDestination.DEPOSIT) },
                onWithdrawClick = { viewModel.navigateTo(AppDestination.WITHDRAW) },
                onReferClick = { viewModel.navigateTo(AppDestination.REFERRALS) }
            )
        }

        // Compliance & Platform Rules Banner
        item {
            DisclaimerBanner()
        }

        // Financial Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Earnings & Activity Overview",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Total Earnings",
                        value = SecurityHelper.formatCurrency(user?.totalEarnings ?: 0.0),
                        icon = Icons.Default.TrendingUp,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Deposits",
                        value = SecurityHelper.formatCurrency(user?.totalDeposits ?: 0.0),
                        icon = Icons.Default.AccountBalance,
                        accentColor = EmeraldPrimaryLight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.DEPOSIT) }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Total Withdrawals",
                        value = SecurityHelper.formatCurrency(user?.totalWithdrawals ?: 0.0),
                        icon = Icons.Default.ArrowUpward,
                        accentColor = GoldSecondaryLight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.WITHDRAW) }
                    )
                    StatCard(
                        title = "Daily Task Earnings",
                        value = SecurityHelper.formatCurrency(user?.dailyTaskEarnings ?: 0.0),
                        icon = Icons.Default.Assignment,
                        accentColor = IndigoTertiaryLight,
                        subtext = if (todayCompletions.isNotEmpty()) "Claimed today" else "1 available today",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.TASKS) }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Referral Earnings",
                        value = SecurityHelper.formatCurrency(user?.referralEarnings ?: 0.0),
                        icon = Icons.Default.Share,
                        accentColor = GoldSecondaryLight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.REFERRALS) }
                    )
                    StatCard(
                        title = "Active Plan",
                        value = user?.activePlanName ?: "No Plan",
                        icon = Icons.Default.Star,
                        accentColor = EmeraldPrimaryLight,
                        subtext = if (user?.activePlanName == null) "Tap to upgrade" else "Active",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.PLANS) }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(
                        title = "Withdrawal Limit",
                        value = SecurityHelper.formatCurrency(maxWithdrawalLimit),
                        icon = Icons.Default.NorthEast,
                        accentColor = GoldSecondaryLight,
                        subtext = "Plan max limit",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.WITHDRAW) }
                    )
                    StatCard(
                        title = "Daily Task Reward",
                        value = SecurityHelper.formatCurrency(dailyTaskReward),
                        icon = Icons.Default.CheckCircle,
                        accentColor = SuccessGreen,
                        subtext = "Plan daily earnings",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppDestination.TASKS) }
                    )
                }
            }
        }

        // Active Plan Prompt Card (if no plan)
        if (user?.activePlanName == null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GoldSecondaryLight.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GoldSecondaryLight.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Activate an Earning Plan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Select from Starter to Pro tiers to unlock your 1 daily task with up to Rs. 450/day reward and up to Rs. 1,500 withdrawal limit.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { viewModel.navigateTo(AppDestination.PLANS) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondaryLight),
                            modifier = Modifier.testTag("activate_plan_cta")
                        ) {
                            Text("Plans", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (transactions.isNotEmpty()) {
                    Text(
                        text = "${transactions.size} records",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions recorded yet",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Deposit funds or complete daily tasks to see ledger updates.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(transactions.take(8)) { txn ->
                TransactionRow(transaction = txn)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HeroBalanceCard(
    user: UserEntity?,
    maxWithdrawalLimit: Double,
    dailyTaskReward: Double,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onReferClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Available Balance",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Secure Ledger",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = SecurityHelper.formatCurrency(user?.balance ?: 0.0),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Plan Limits & Quota Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Withdrawal Limit: ${SecurityHelper.formatCurrency(maxWithdrawalLimit)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Daily Reward: ${SecurityHelper.formatCurrency(dailyTaskReward)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDepositClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimaryLight,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("dashboard_deposit_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.SouthWest,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deposit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onWithdrawClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("dashboard_withdraw_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.NorthEast,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Withdraw", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onReferClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("dashboard_refer_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
