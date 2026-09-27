package com.example.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.CalculationHistoryEntity
import com.example.database.CalculationHistoryRepository
import com.example.models.CalculationResult
import com.example.models.MetalType
import com.example.sharing.CalculationShareHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter {
    ALL,
    GOLD,
    SILVER
}

class HistoryViewModel(
    private val repository: CalculationHistoryRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilter.ALL)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

    private val _selectedItemForDetails = MutableStateFlow<CalculationResult?>(null)
    val selectedItemForDetails: StateFlow<CalculationResult?> = _selectedItemForDetails.asStateFlow()

    val historyItems: StateFlow<List<CalculationHistoryEntity>> = combine(
        repository.allHistory,
        _selectedFilter
    ) { all, filter ->
        when (filter) {
            HistoryFilter.ALL -> all
            HistoryFilter.GOLD -> all.filter { it.metalType.equals(MetalType.GOLD.name, ignoreCase = true) }
            HistoryFilter.SILVER -> all.filter { it.metalType.equals(MetalType.SILVER.name, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    fun showDetails(result: CalculationResult) {
        _selectedItemForDetails.value = result
    }

    fun hideDetails() {
        _selectedItemForDetails.value = null
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun shareItem(context: Context, result: CalculationResult) {
        CalculationShareHelper.shareCalculation(context, result)
    }
}
