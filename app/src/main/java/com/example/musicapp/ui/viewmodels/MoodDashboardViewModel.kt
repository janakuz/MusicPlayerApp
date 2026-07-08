package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.data.local.model.MoodInfo
import com.example.musicapp.data.repository.MoodRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodDashboardViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val moodsWithCounts: StateFlow<List<MoodInfo>> = userPreferencesRepository.moodSortOption
        .flatMapLatest { option ->
            moodRepository.getAll(option)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSort(option: SortOption) {
        viewModelScope.launch {
            userPreferencesRepository.updateMoodSort(option)
        }
    }


}