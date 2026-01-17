package com.expensemanager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.repository.TransactionRepository
import com.expensemanager.sms.SmsReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for SMS import functionality
 */
@HiltViewModel
class SmsViewModel @Inject constructor(
    private val smsReader: SmsReader,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SmsImportUiState())
    val uiState: StateFlow<SmsImportUiState> = _uiState.asStateFlow()
    
    fun scanSms(daysBack: Int = 30) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            
            try {
                val transactions = withContext(Dispatchers.IO) {
                    smsReader.readBankSms(daysBack)
                }
                
                _uiState.value = _uiState.value.copy(
                    scannedTransactions = transactions,
                    selectedTransactions = transactions.map { true }.toMutableList(),
                    isScanning = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message ?: "Failed to scan SMS"
                )
            }
        }
    }
    
    fun toggleTransactionSelection(index: Int) {
        val current = _uiState.value.selectedTransactions.toMutableList()
        if (index < current.size) {
            current[index] = !current[index]
            _uiState.value = _uiState.value.copy(selectedTransactions = current)
        }
    }
    
    fun selectAll() {
        val size = _uiState.value.scannedTransactions.size
        _uiState.value = _uiState.value.copy(
            selectedTransactions = MutableList(size) { true }
        )
    }
    
    fun deselectAll() {
        val size = _uiState.value.scannedTransactions.size
        _uiState.value = _uiState.value.copy(
            selectedTransactions = MutableList(size) { false }
        )
    }
    
    fun importSelectedTransactions(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val toImport = state.scannedTransactions.filterIndexed { index, _ ->
                state.selectedTransactions.getOrNull(index) == true
            }
            
            if (toImport.isEmpty()) {
                onError("No transactions selected")
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isImporting = true)
            
            try {
                var importedCount = 0
                for (transaction in toImport) {
                    transactionRepository.insert(transaction)
                    importedCount++
                }
                
                _uiState.value = SmsImportUiState() // Reset state
                onSuccess(importedCount)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = e.message ?: "Failed to import transactions"
                )
                onError(e.message ?: "Failed to import transactions")
            }
        }
    }
    
    fun clearScannedTransactions() {
        _uiState.value = SmsImportUiState()
    }
}

data class SmsImportUiState(
    val scannedTransactions: List<Transaction> = emptyList(),
    val selectedTransactions: MutableList<Boolean> = mutableListOf(),
    val isScanning: Boolean = false,
    val isImporting: Boolean = false,
    val error: String? = null
)
