package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.model.AreaInfo
import com.example.musicapp.data.repository.AreaRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class AreaDashboardViewModel @Inject constructor(
    private val areaRepository: AreaRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {

    private var limitFlow = MutableStateFlow(25)

    @OptIn(ExperimentalCoroutinesApi::class)
    val areasWithCounts: StateFlow<List<AreaInfo>> = userPreferencesRepository.areasSortOption
        .combine(limitFlow) { option, currentLimit -> option to currentLimit }
        .flatMapLatest { (option, currentLimit) ->
            areaRepository.getAreaDashboard(option, currentLimit)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateLimit() {
        limitFlow.value += 25
    }

}