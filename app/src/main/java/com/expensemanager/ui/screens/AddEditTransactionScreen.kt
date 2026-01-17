package com.expensemanager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entity.TransactionType
import com.expensemanager.ui.components.ClickableDateField
import com.expensemanager.ui.components.TagSelector
import com.expensemanager.ui.viewmodels.TransactionViewModel

/**
 * Screen for adding or editing a transaction
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionType: String,
    transactionId: Long? = null,
    onNavigateBack: () -> Unit,
    onNavigateToAddTag: (String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val scrollState = rememberScrollState()
    
    val isEditing = transactionId != null && transactionId > 0
    val type = if (transactionType == "income") TransactionType.INCOME else TransactionType.EXPENSE
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(transactionId) {
        if (isEditing && transactionId != null) {
            viewModel.loadTransactionForEdit(transactionId)
        } else {
            viewModel.resetForm(type)
        }
    }
    
    val title = when {
        isEditing -> "Edit Transaction"
        type == TransactionType.INCOME -> "Add Income"
        else -> "Add Expense"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Toggle (only for editing or when not specified)
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = formState.type == TransactionType.EXPENSE,
                        onClick = { viewModel.updateFormType(TransactionType.EXPENSE) },
                        label = { Text("Expense") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    
                    FilterChip(
                        selected = formState.type == TransactionType.INCOME,
                        onClick = { viewModel.updateFormType(TransactionType.INCOME) },
                        label = { Text("Income") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
            
            // Amount Field
            OutlinedTextField(
                value = formState.amount,
                onValueChange = { viewModel.updateFormAmount(it) },
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description Field
            OutlinedTextField(
                value = formState.description,
                onValueChange = { viewModel.updateFormDescription(it) },
                label = { Text("Description") },
                placeholder = { Text("What was this for?") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            
            // Date Picker
            ClickableDateField(
                label = "Date",
                selectedDate = formState.date,
                onDateSelected = { viewModel.updateFormDate(it) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Payee Selector
            TagSelector(
                title = "Payee",
                tags = tags.payees,
                selectedTagId = formState.payeeId,
                onTagSelected = { viewModel.updateFormPayee(it) },
                onAddNew = { onNavigateToAddTag("PAYEE") }
            )
            
            // Category Selector
            TagSelector(
                title = "Category",
                tags = tags.categories,
                selectedTagId = formState.categoryId,
                onTagSelected = { viewModel.updateFormCategory(it) },
                onAddNew = { onNavigateToAddTag("CATEGORY") }
            )
            
            // Payment Method Selector
            TagSelector(
                title = "Payment Method",
                tags = tags.paymentMethods,
                selectedTagId = formState.paymentMethodId,
                onTagSelected = { viewModel.updateFormPaymentMethod(it) },
                onAddNew = { onNavigateToAddTag("PAYMENT_METHOD") }
            )
            
            // Status Selector
            TagSelector(
                title = "Status",
                tags = tags.statuses,
                selectedTagId = formState.statusId,
                onTagSelected = { viewModel.updateFormStatus(it) },
                onAddNew = { onNavigateToAddTag("STATUS") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    viewModel.saveTransaction(
                        onSuccess = {
                            Toast.makeText(
                                context,
                                if (isEditing) "Transaction updated" else "Transaction added",
                                Toast.LENGTH_SHORT
                            ).show()
                            onNavigateBack()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Update Transaction" else "Add Transaction")
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionId?.let {
                            viewModel.deleteTransaction(it) {
                                Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
