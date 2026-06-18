package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.data.local.model.LabelInfo
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.AreaRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabelsDashboardViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
)  : ViewModel(){

    private var limitFlow = MutableStateFlow(25)


    @OptIn(ExperimentalCoroutinesApi::class)
    val labelsWithCounts: StateFlow<List<LabelInfo>> = userPreferencesRepository.labelSortOption
        .combine(limitFlow) { option, currentLimit -> option to currentLimit }
        .flatMapLatest { (option, currentLimit) ->
            albumRepository.getTopLabels(option, currentLimit)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSort(option: SortOption) {
        viewModelScope.launch {
            userPreferencesRepository.updateLabelSort(option)
        }
    }

    fun updateLimit() {
        limitFlow.value += 25
    }

}