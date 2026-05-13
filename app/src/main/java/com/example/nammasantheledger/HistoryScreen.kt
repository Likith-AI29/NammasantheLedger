package com.example.nammasantheledger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

    Scaffold(
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
            items(displayList) { customer ->
                ListItem(
                    headlineContent = {
                        Text(customer.name, fontWeight = FontWeight.Bold)
                    },
                    supportingContent = {
                        Text(
                            if (selectedTabIndex == 0) "Total Pending Due"
                            else "Total Payment Received"
                        )
                    },
                    trailingContent = {
                        val amountToShow =
                            if (selectedTabIndex == 0) customer.pendingUdari
                            else customer.totalPaid
                        Text(
                            "₹$amountToShow",
                            color = if (selectedTabIndex == 0) Color.Red else Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
