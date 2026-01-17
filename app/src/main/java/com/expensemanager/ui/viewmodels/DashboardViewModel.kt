package com.expensemanager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entity.TransactionWithTags
import com.expensemanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Dashboard screen
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            // Get current month date range
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val endOfMonth = calendar.timeInMillis
            
            // Combine all flows
            combine(
                transactionRepository.getTotalExpenses(startOfMonth, endOfMonth),
                transactionRepository.getTotalIncome(startOfMonth, endOfMonth),
                transactionRepository.getAllTransactionsWithTags(),
                transactionRepository.getTransactionCount()
            ) { expenses, income, transactions, count ->
                DashboardUiState(
                    totalExpenses = expenses ?: 0.0,
                    totalIncome = income ?: 0.0,
                    balance = (income ?: 0.0) - (expenses ?: 0.0),
                    recentTransactions = transactions.take(10),
                    transactionCount = count,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            transactionRepository.deleteById(transactionId)
        }
    }
    
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadDashboardData()
    }
}

data class DashboardUiState(
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<TransactionWithTags> = emptyList(),
    val transactionCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)
