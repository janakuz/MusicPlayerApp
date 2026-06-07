package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.model.GenreInfo
import com.example.musicapp.data.repository.GenreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val genreRepository: GenreRepository
) : ViewModel() {

    val genresWithCounts: StateFlow<List<GenreInfo>> = genreRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}