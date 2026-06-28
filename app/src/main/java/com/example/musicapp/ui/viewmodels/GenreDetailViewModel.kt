package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AlbumGenreRepository
import com.example.musicapp.data.repository.ArtistGenreRepository
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GenreDetailViewModel @Inject constructor (
    private val genreRepository: GenreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val genreId: Int = savedStateHandle.get<String>("genreId")?.toInt()
        ?: throw IllegalStateException("genreId not found in SavedStateHandle")

    val genreItems = genreRepository.getGenreArtistsAndAlbums(genreId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult())

    val genreName = genreRepository.getGenreName(genreId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),"")
}