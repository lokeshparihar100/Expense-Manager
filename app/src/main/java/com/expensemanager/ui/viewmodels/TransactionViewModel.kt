package com.expensemanager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entity.Tag
import com.expensemanager.data.local.entity.TagType
import com.expensemanager.data.local.entity.Transaction
import com.expensemanager.data.local.entity.TransactionType
import com.expensemanager.data.local.entity.TransactionWithTags
import com.expensemanager.data.repository.TagRepository
import com.expensemanager.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Transaction list and detail screens
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val tagRepository: TagRepository
) : ViewModel() {
    
    private val _transactionsState = MutableStateFlow(TransactionsUiState())
    val transactionsState: StateFlow<TransactionsUiState> = _transactionsState.asStateFlow()
    
    private val _formState = MutableStateFlow(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()
    
    private val _tags = MutableStateFlow(TagsState())
    val tags: StateFlow<TagsState> = _tags.asStateFlow()
    
    init {
        loadTransactions()
        loadTags()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactionsWithTags().collect { transactions ->
                _transactionsState.value = TransactionsUiState(
                    transactions = transactions,
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadTags() {
        viewModelScope.launch {
            combine(
                tagRepository.getPayees(),
                tagRepository.getCategories(),
                tagRepository.getPaymentMethods(),
                tagRepository.getStatuses()
            ) { payees, categories, paymentMethods, statuses ->
                TagsState(
                    payees = payees,
                    categories = categories,
                    paymentMethods = paymentMethods,
                    statuses = statuses
                )
            }.collect { state ->
                _tags.value = state
            }
        }
    }
    
    fun loadTransactionForEdit(transactionId: Long) {
        viewModelScope.launch {
            val transactionWithTags = transactionRepository.getTransactionWithTagsById(transactionId)
            transactionWithTags?.let { twt ->
                _formState.value = TransactionFormState(
                    id = twt.transaction.id,
                    amount = twt.transaction.amount.toString(),
                    description = twt.transaction.description,
                    date = twt.transaction.date,
                    type = twt.transaction.type,
                    payeeId = twt.transaction.payeeId,
                    categoryId = twt.transaction.categoryId,
                    paymentMethodId = twt.transaction.paymentMethodId,
                    statusId = twt.transaction.statusId,
                    isEditing = true
                )
            }
        }
    }
    
    fun updateFormAmount(amount: String) {
        _formState.value = _formState.value.copy(amount = amount)
    }
    
    fun updateFormDescription(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }
    
    fun updateFormDate(date: Long) {
        _formState.value = _formState.value.copy(date = date)
    }
    
    fun updateFormType(type: TransactionType) {
        _formState.value = _formState.value.copy(type = type)
    }
    
    fun updateFormPayee(payeeId: Long?) {
        _formState.value = _formState.value.copy(payeeId = payeeId)
    }
    
    fun updateFormCategory(categoryId: Long?) {
        _formState.value = _formState.value.copy(categoryId = categoryId)
    }
    
    fun updateFormPaymentMethod(paymentMethodId: Long?) {
        _formState.value = _formState.value.copy(paymentMethodId = paymentMethodId)
    }
    
    fun updateFormStatus(statusId: Long?) {
        _formState.value = _formState.value.copy(statusId = statusId)
    }
    
    fun resetForm(type: TransactionType = TransactionType.EXPENSE) {
        _formState.value = TransactionFormState(type = type)
    }
    
    fun saveTransaction(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val form = _formState.value
        
        // Validation
        val amount = form.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            onError("Please enter a valid amount")
            return
        }
        
        if (form.description.isBlank()) {
            onError("Please enter a description")
            return
        }
        
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    id = if (form.isEditing) form.id else 0,
                    amount = amount,
                    description = form.description.trim(),
                    date = form.date,
                    type = form.type,
                    payeeId = form.payeeId,
                    categoryId = form.categoryId,
                    paymentMethodId = form.paymentMethodId,
                    statusId = form.statusId
                )
                
                if (form.isEditing) {
                    transactionRepository.update(transaction)
                } else {
                    transactionRepository.insert(transaction)
                }
                
                resetForm()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save transaction")
            }
        }
    }
    
    fun deleteTransaction(transactionId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            transactionRepository.deleteById(transactionId)
            onSuccess()
        }
    }
    
    fun filterByType(type: TransactionType?) {
        viewModelScope.launch {
            if (type == null) {
                transactionRepository.getAllTransactionsWithTags().collect { transactions ->
                    _transactionsState.value = _transactionsState.value.copy(
                        transactions = transactions,
                        filterType = null
                    )
                }
            } else {
                transactionRepository.getTransactionsWithTagsByType(type).collect { transactions ->
                    _transactionsState.value = _transactionsState.value.copy(
                        transactions = transactions,
                        filterType = type
                    )
                }
            }
        }
    }
    
    fun filterByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            transactionRepository.getTransactionsWithTagsByDateRange(startDate, endDate).collect { transactions ->
                _transactionsState.value = _transactionsState.value.copy(
                    transactions = transactions,
                    startDate = startDate,
                    endDate = endDate
                )
            }
        }
    }
}

data class TransactionsUiState(
    val transactions: List<TransactionWithTags> = emptyList(),
    val isLoading: Boolean = true,
    val filterType: TransactionType? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val error: String? = null
)

data class TransactionFormState(
    val id: Long = 0,
    val amount: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: TransactionType = TransactionType.EXPENSE,
    val payeeId: Long? = null,
    val categoryId: Long? = null,
    val paymentMethodId: Long? = null,
    val statusId: Long? = null,
    val isEditing: Boolean = false
)

data class TagsState(
    val payees: List<Tag> = emptyList(),
    val categories: List<Tag> = emptyList(),
    val paymentMethods: List<Tag> = emptyList(),
    val statuses: List<Tag> = emptyList()
)
