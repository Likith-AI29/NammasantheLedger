package com.example.nammasantheledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: LedgerViewModel, onBack: () -> Unit) {

    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Udari (Credit)", "Paid (Received)")
    val customerSummaries by viewModel.customerSummaries.collectAsState(initial = emptyList())

    val searchedCustomers = customerSummaries.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val displayList = if (selectedTabIndex == 0)
        searchedCustomers.filter { it.pendingUdari > 0 }
    else
        searchedCustomers.filter { it.totalPaid > 0 }

    val totalCredit = displayList.sumOf { it.pendingUdari }
    val totalCustomers = displayList.size

    Scaffold(
        modifier = Modifier.background(Color.White),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Transaction History", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    placeholder = { Text("Search customer...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (selectedTabIndex == 0) "Total Credit ₹$totalCredit" else "Total Received ₹$totalCredit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Customers: $totalCustomers",
                            color = Color.Gray
                        )
                    }
                }
            }
            items(displayList) { customer ->
                val amountToShow =
                    if (selectedTabIndex == 0) customer.pendingUdari
                    else customer.totalPaid

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A237E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.first().uppercaseChar().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(customer.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (selectedTabIndex == 0) "Total Pending Due" else "Total Payment Received",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "₹$amountToShow",
                        color = if (selectedTabIndex == 0) Color.Red else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
