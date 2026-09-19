package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppDestination
import com.example.ui.MainViewModel
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.screens.auth.AuthModalDialog
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.deposit.DepositScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.plans.PlansScreen
import com.example.ui.screens.referral.ReferralScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.screens.withdraw.WithdrawScreen
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.GoldSecondaryLight
import com.example.ui.theme.IndigoTertiaryLight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SuccessGreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EarnRewardsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarnRewardsApp(mainViewModel: MainViewModel = viewModel()) {
    val currentDestination by mainViewModel.currentDestination.collectAsState()
    val currentUser by mainViewModel.currentUser.collectAsState()
    val unreadNotifsCount by mainViewModel.unreadNotificationsCount.collectAsState()
    val userMessage by mainViewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { mainViewModel.navigateTo(AppDestination.DASHBOARD) }
                    ) {
                        Surface(
                            color = EmeraldPrimaryLight,
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "EarnRewards Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "EarnRewards",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentUser?.role == "ADMIN") "Administrator Mode" else "Member Portal",
                                fontSize = 10.sp,
                                color = if (currentUser?.role == "ADMIN") IndigoTertiaryLight else SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Quick Switcher Chip / Menu
                    Box {
                        Surface(
                            color = if (currentUser?.role == "ADMIN") IndigoTertiaryLight.copy(alpha = 0.15f) else EmeraldPrimaryLight.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showRoleMenu = true }
                                .testTag("role_switcher_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentUser?.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (currentUser?.role == "ADMIN") IndigoTertiaryLight else EmeraldPrimaryLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentUser?.role == "ADMIN") "Admin" else (currentUser?.name?.split(" ")?.firstOrNull() ?: "Login"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser?.role == "ADMIN") IndigoTertiaryLight else EmeraldPrimaryLight
                                )
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Switch to User (Zubair)") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                onClick = {
                                    mainViewModel.switchRole(asAdmin = false)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Switch to Admin (Tahir)") },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                                onClick = {
                                    mainViewModel.switchRole(asAdmin = true)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Login / Register Dialog") },
                                leadingIcon = { Icon(Icons.Default.Login, contentDescription = null) },
                                onClick = {
                                    showAuthDialog = true
                                    showRoleMenu = false
                                }
                            )
                            if (currentUser != null) {
                                DropdownMenuItem(
                                    text = { Text("Log Out") },
                                    leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) },
                                    onClick = {
                                        mainViewModel.logout()
                                        showRoleMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Notifications Icon with Unread Badge
                    IconButton(
                        onClick = { mainViewModel.navigateTo(AppDestination.NOTIFICATIONS) },
                        modifier = Modifier.testTag("nav_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge(containerColor = GoldSecondaryLight) {
                                        Text("$unreadNotifsCount", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val isAdmin = currentUser?.role == "ADMIN"

                val navItems = if (isAdmin) {
                    listOf(
                        NavigationItemData(AppDestination.ADMIN, "Admin", Icons.Default.AdminPanelSettings),
                        NavigationItemData(AppDestination.DASHBOARD, "Dashboard", Icons.Default.Home),
                        NavigationItemData(AppDestination.DEPOSIT, "Deposit", Icons.Default.ArrowDownward),
                        NavigationItemData(AppDestination.WITHDRAW, "Withdraw", Icons.Default.ArrowUpward),
                        NavigationItemData(AppDestination.TASKS, "Tasks", Icons.Default.Assignment)
                    )
                } else {
                    listOf(
                        NavigationItemData(AppDestination.DASHBOARD, "Home", Icons.Default.Home),
                        NavigationItemData(AppDestination.PLANS, "Plans", Icons.Default.Star),
                        NavigationItemData(AppDestination.TASKS, "Tasks", Icons.Default.Assignment),
                        NavigationItemData(AppDestination.DEPOSIT, "Deposit", Icons.Default.ArrowDownward),
                        NavigationItemData(AppDestination.WITHDRAW, "Withdraw", Icons.Default.ArrowUpward),
                        NavigationItemData(AppDestination.REFERRALS, "Refer", Icons.Default.Share)
                    )
                }

                navItems.forEach { item ->
                    val isSelected = currentDestination == item.destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { mainViewModel.navigateTo(item.destination) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimaryLight,
                            selectedTextColor = EmeraldPrimaryLight,
                            indicatorColor = EmeraldPrimaryLight.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${item.destination.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentDestination) {
                AppDestination.DASHBOARD -> DashboardScreen(viewModel = mainViewModel)
                AppDestination.PLANS -> PlansScreen(viewModel = mainViewModel)
                AppDestination.TASKS -> TasksScreen(viewModel = mainViewModel)
                AppDestination.DEPOSIT -> DepositScreen(viewModel = mainViewModel)
                AppDestination.WITHDRAW -> WithdrawScreen(viewModel = mainViewModel)
                AppDestination.REFERRALS -> ReferralScreen(viewModel = mainViewModel)
                AppDestination.NOTIFICATIONS -> NotificationsScreen(viewModel = mainViewModel)
                AppDestination.ADMIN -> AdminScreen(viewModel = mainViewModel)
            }
        }
    }

    if (showAuthDialog) {
        AuthModalDialog(
            viewModel = mainViewModel,
            onDismiss = { showAuthDialog = false }
        )
    }
}

data class NavigationItemData(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector
)
