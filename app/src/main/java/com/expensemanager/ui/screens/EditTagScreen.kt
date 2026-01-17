package com.expensemanager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.expensemanager.data.local.entity.TagType
import com.expensemanager.ui.viewmodels.TagViewModel

/**
 * Screen for editing or creating a tag
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagScreen(
    tagType: String,
    tagId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: TagViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val scrollState = rememberScrollState()
    
    val isEditing = tagId != null
    val type = try {
        TagType.valueOf(tagType)
    } catch (e: Exception) {
        TagType.CATEGORY
    }
    
    LaunchedEffect(tagId, tagType) {
        if (isEditing && tagId != null) {
            viewModel.loadTagForEdit(tagId)
        } else {
            viewModel.resetForm(type)
        }
    }
    
    val title = if (isEditing) "Edit Tag" else "Add ${getTagTypeName(type)}"
    
    val availableColors = listOf(
        "#FF5722", "#4CAF50", "#2196F3", "#9C27B0",
        "#FF9800", "#E91E63", "#00BCD4", "#607D8B",
        "#F44336", "#8BC34A", "#3F51B5", "#795548"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            // Tag Name
            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.updateFormName(it) },
                label = { Text("Tag Name") },
                placeholder = { Text("Enter tag name") },
                leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Color Selection
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    availableColors.take(6).forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = formState.color == color,
                            onClick = { viewModel.updateFormColor(color) }
                        )
                    }
                }
                
                // Second row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    availableColors.drop(6).forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = formState.color == color,
                            onClick = { viewModel.updateFormColor(color) }
                        )
                    }
                }
                
                // No color option
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { viewModel.updateFormColor(null) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = formState.color == null,
                        onClick = { viewModel.updateFormColor(null) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No color")
                }
            }
            
            // Preview
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val previewColor = formState.color?.let {
                            try { Color(android.graphics.Color.parseColor(it)) }
                            catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        } ?: MaterialTheme.colorScheme.primary
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(previewColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = null,
                                tint = previewColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = formState.name.ifBlank { "Tag Name" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    viewModel.saveTag(
                        onSuccess = {
                            Toast.makeText(
                                context,
                                if (isEditing) "Tag updated" else "Tag added",
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
                    .height(50.dp),
                enabled = formState.name.isNotBlank()
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Update Tag" else "Add Tag")
            }
        }
    }
}

@Composable
private fun ColorOption(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(parsedColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun getTagTypeName(type: TagType): String {
    return when (type) {
        TagType.PAYEE -> "Payee"
        TagType.CATEGORY -> "Category"
        TagType.PAYMENT_METHOD -> "Payment Method"
        TagType.STATUS -> "Status"
    }
}
