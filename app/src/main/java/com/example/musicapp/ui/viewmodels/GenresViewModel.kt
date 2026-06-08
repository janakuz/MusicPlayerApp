package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.data.repository.GenreRepository
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
class GenresViewModel @Inject constructor(
    private val genreRepository: GenreRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val genresWithCounts: StateFlow<List<GenreInfo>> = userPreferencesRepository.genresSortOption
        .flatMapLatest { option ->
        genreRepository.getAll(option)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSort(option: SortOption) {
        viewModelScope.launch {
            userPreferencesRepository.updateGenresSort(option)
        }
    }
}