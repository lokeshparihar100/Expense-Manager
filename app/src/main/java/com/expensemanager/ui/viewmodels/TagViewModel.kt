package com.expensemanager.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensemanager.data.local.entity.Tag
import com.expensemanager.data.local.entity.TagType
import com.expensemanager.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Tag management screens
 */
@HiltViewModel
class TagViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()
    
    private val _formState = MutableStateFlow(TagFormState())
    val formState: StateFlow<TagFormState> = _formState.asStateFlow()
    
    private val _selectedType = MutableStateFlow(TagType.CATEGORY)
    val selectedType: StateFlow<TagType> = _selectedType.asStateFlow()
    
    init {
        loadAllTags()
    }
    
    private fun loadAllTags() {
        viewModelScope.launch {
            combine(
                tagRepository.getPayees(),
                tagRepository.getCategories(),
                tagRepository.getPaymentMethods(),
                tagRepository.getStatuses()
            ) { payees, categories, paymentMethods, statuses ->
                TagsUiState(
                    payees = payees,
                    categories = categories,
                    paymentMethods = paymentMethods,
                    statuses = statuses,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun setSelectedType(type: TagType) {
        _selectedType.value = type
    }
    
    fun loadTagForEdit(tagId: Long) {
        viewModelScope.launch {
            val tag = tagRepository.getById(tagId)
            tag?.let {
                _formState.value = TagFormState(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    color = it.color,
                    icon = it.icon,
                    isEditing = true
                )
            }
        }
    }
    
    fun updateFormName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }
    
    fun updateFormType(type: TagType) {
        _formState.value = _formState.value.copy(type = type)
    }
    
    fun updateFormColor(color: String?) {
        _formState.value = _formState.value.copy(color = color)
    }
    
    fun updateFormIcon(icon: String?) {
        _formState.value = _formState.value.copy(icon = icon)
    }
    
    fun resetForm(type: TagType = TagType.CATEGORY) {
        _formState.value = TagFormState(type = type)
    }
    
    fun saveTag(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val form = _formState.value
        
        if (form.name.isBlank()) {
            onError("Please enter a tag name")
            return
        }
        
        viewModelScope.launch {
            try {
                val tag = Tag(
                    id = if (form.isEditing) form.id else 0,
                    name = form.name.trim(),
                    type = form.type,
                    color = form.color,
                    icon = form.icon
                )
                
                if (form.isEditing) {
                    tagRepository.update(tag)
                } else {
                    tagRepository.insert(tag)
                }
                
                resetForm(form.type)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save tag")
            }
        }
    }
    
    fun deleteTag(tagId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                tagRepository.deleteById(tagId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete tag")
            }
        }
    }
    
    fun getTagsByType(type: TagType): List<Tag> {
        return when (type) {
            TagType.PAYEE -> _uiState.value.payees
            TagType.CATEGORY -> _uiState.value.categories
            TagType.PAYMENT_METHOD -> _uiState.value.paymentMethods
            TagType.STATUS -> _uiState.value.statuses
        }
    }
}

data class TagsUiState(
    val payees: List<Tag> = emptyList(),
    val categories: List<Tag> = emptyList(),
    val paymentMethods: List<Tag> = emptyList(),
    val statuses: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class TagFormState(
    val id: Long = 0,
    val name: String = "",
    val type: TagType = TagType.CATEGORY,
    val color: String? = null,
    val icon: String? = null,
    val isEditing: Boolean = false
)
