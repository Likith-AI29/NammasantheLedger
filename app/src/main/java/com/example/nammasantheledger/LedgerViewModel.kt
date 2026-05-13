package com.example.nammasantheledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LedgerViewModel(private val dao: TransactionDao) : ViewModel() {

    val customerSummaries = dao.getAllCustomerSummaries()

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            val userCount = dao.getUserCount()
            _isUserLoggedIn.value = userCount > 0
        }
    }

    val totalUdariBalance: StateFlow<Double> = customerSummaries.map { list ->
        list.sumOf { it.pendingUdari }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPaidBalance: StateFlow<Double> = customerSummaries.map { list ->
        list.sumOf { it.totalPaid }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addEntry(name: String, amount: Double, isCredit: Boolean) {
        viewModelScope.launch {
            dao.insert(Transaction(customerName = name, amount = amount, isCredit = isCredit))
            val existing = dao.getCustomerByName(name)

            if (isCredit) {
                val updated = existing?.copy(
                    pendingUdari = existing.pendingUdari + amount,
                    balance = existing.balance + amount
                ) ?: Customer(name = name, pendingUdari = amount, totalPaid = 0.0, balance = amount)
                dao.upsertCustomer(updated)
            } else {
                val updated = existing?.copy(
                    pendingUdari = (existing.pendingUdari - amount).coerceAtLeast(0.0),
                    totalPaid = existing.totalPaid + amount,
                    balance = existing.balance - amount
                ) ?: Customer(name = name, pendingUdari = 0.0, totalPaid = amount, balance = -amount)
                dao.upsertCustomer(updated)
            }
        }
    }

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            dao.registerUser(User(email = email, name = name, password = password))
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = dao.getUserByEmail(email)
            if (user != null && user.password == password) {
                onResult(true, user.name)
            } else {
                onResult(false, "")
            }
        }
    }
}
