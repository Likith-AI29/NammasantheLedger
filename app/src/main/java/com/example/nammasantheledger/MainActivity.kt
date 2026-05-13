package com.example.nammasantheledger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nammasantheledger.ui.theme.NammaSantheLedgerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.lifecycle.lifecycleScope


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val viewModel = LedgerViewModel(db.transactionDao())
        val sessionManager = SessionManager(applicationContext)

        enableEdgeToEdge()

        setContent {
            NammaSantheLedgerTheme {

                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = false)
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {

                    composable("splash") {
                        SplashScreenUI()
                        
                        LaunchedEffect(Unit) {
                            // Wait for checkSession to complete
                            delay(500)
                            if (isUserLoggedIn == true) {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else if (isUserLoggedIn == false) {
                                navController.navigate("login") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    }

                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { email, userName ->
                                lifecycleScope.launch {
                                    sessionManager.setLoggedIn(email, userName)
                                }
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToSignup = {
                                navController.navigate("signup")
                            }
                        )
                    }

                    composable("signup") {
                        SignupScreen(
                            viewModel = viewModel,
                            onSignupSuccess = { name, email ->
                                lifecycleScope.launch {
                                    sessionManager.setLoggedIn(email, name)
                                }
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        MainScreen(
                            viewModel,
                            navController,
                            onNavigateToHistory = {
                                navController.navigate("history")
                            }
                        )
                    }

                    composable("history") {
                        HistoryScreen(
                            viewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("profile") {
                        val userName by sessionManager.loggedInName.collectAsState(initial = "")
                        val userEmail by sessionManager.loggedInEmail.collectAsState(initial = "")

                        ProfileScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            userName = userName,
                            userEmail = userEmail
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: LedgerViewModel,
    navController: androidx.navigation.NavController,
    onNavigateToHistory: () -> Unit
) {
    var inputAmount by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }

    val totalUdari by viewModel.totalUdariBalance.collectAsState()
    val totalPaid by viewModel.totalPaidBalance.collectAsState()
    val customerSummaries by viewModel.customerSummaries.collectAsState(initial = emptyList())
    val activeCustomer = customerSummaries.find { it.name.equals(customerName, ignoreCase = true) }
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Namma Santhe Ledger",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A237E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color(0xFF1A237E),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "History",
                            tint = Color(0xFF1A237E)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(10.dp).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (inputAmount.isNotEmpty() && customerName.isNotBlank()) {
                                viewModel.addEntry(customerName.trim(), inputAmount.toDouble(), true)
                                inputAmount = ""
                            }
                        },
                        modifier = Modifier.weight(1f).height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("GIVE UDARI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Button(
                        onClick = {
                            if (inputAmount.isNotEmpty() && customerName.isNotBlank()) {
                                viewModel.addEntry(customerName.trim(), inputAmount.toDouble(), false)
                                inputAmount = ""
                            }
                        },
                        modifier = Modifier.weight(1f).height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("RECEIVED", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            PremiumDashboard(totalUdari, totalPaid)

            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF1A237E)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF1A237E))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Amount", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Text(
                            "₹ $inputAmount",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (inputAmount.isEmpty()) Color.LightGray else Color.Black
                        )
                    }
                }
            }

            Button(
                onClick = { sendWhatsAppReminder(context, customerName, activeCustomer?.pendingUdari ?: 0.0) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("QUICK REMINDER", fontWeight = FontWeight.Bold)
            }

            Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                PremiumKeypad(
                    onNumberClick = { if (inputAmount.length < 8) inputAmount += it },
                    onDelete = { if (inputAmount.isNotEmpty()) inputAmount = inputAmount.dropLast(1) }
                )
            }
        }
    }
}

@Composable
fun PremiumDashboard(totalUdari: Double, totalPaid: Double) {
    val netBalance = totalUdari - totalPaid
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1A237E), Color(0xFF3949AB))))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("NET OUTSTANDING", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
            Text(
                "₹ ${if (netBalance < 0) -netBalance else netBalance}",
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DashboardStat("Given", "₹$totalUdari", Color.White)
                VerticalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.height(30.dp))
                DashboardStat("Collected", "₹$totalPaid", Color.White)
            }
        }
    }
}

@Composable
fun DashboardStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = color.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        Text(value, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun PremiumKeypad(
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit
) {

    val haptic = LocalHapticFeedback.current

    val buttons = listOf(
        "1","2","3",
        "4","5","6",
        "7","8","9",
        ".","0","DEL"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(horizontal = 5.dp)
    ) {

        items(buttons) { button ->

            ElevatedButton(

                onClick = {

                    haptic.performHapticFeedback(
                        HapticFeedbackType.TextHandleMove
                    )

                    if (button == "DEL") {
                        onDelete()
                    } else {
                        onNumberClick(button)
                    }
                },

                modifier = Modifier
                    .padding(8.dp)
                    .height(50.dp),

                shape = RoundedCornerShape(20.dp),

                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.White
                )

            ) {

                Text(
                    text = button,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
@Composable
fun CustomerBalanceSection(
    customers: List<Customer>,
    context: android.content.Context
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        Text(
            text = "Customer Balances",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        customers.take(5).forEach { customer ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),

                shape = RoundedCornerShape(22.dp),

                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),

                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(18.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                text = customer.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Pending Balance",
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = "₹${customer.pendingUdari}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column {

                            Text(
                                text = "Udari Given",
                                color = Color.Gray
                            )

                            Text(
                                text = "₹${customer.pendingUdari + customer.totalPaid}",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {

                            Text(
                                text = "Amount Paid",
                                color = Color.Gray
                            )

                            Text(
                                text = "₹${customer.totalPaid}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {

                            sendWhatsAppReminder(
                                context,
                                customer.name,
                                customer.pendingUdari
                            )
                        },

                        modifier = Modifier.fillMaxWidth(),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        ),

                        shape = RoundedCornerShape(16.dp)
                    ) {

                        Text(
                            text = "SEND REMINDER",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
fun sendWhatsAppReminder(
    context: android.content.Context,
    name: String,
    amount: Double
) {

    val message =
        "Namaste $name, your pending due is ₹$amount. Please settle it when possible."

    val intent = Intent(Intent.ACTION_VIEW).apply {

        data = Uri.parse(
            "https://api.whatsapp.com/send?text=${Uri.encode(message)}"
        )
    }

    context.startActivity(intent)
}
