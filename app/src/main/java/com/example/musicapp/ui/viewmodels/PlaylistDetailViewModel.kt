package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.PlaylistRepository
import com.example.musicapp.data.repository.PlaylistTracksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.text.toInt

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playlistTracksRepository: PlaylistTracksRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlistId: Int = savedStateHandle.get<String>("playlistId")?.toInt()
        ?: throw IllegalStateException("playlistId not found in SavedStateHandle")


    val playlistTracks = playlistTracksRepository.getAllTracksInPlaylist(playlistId, "position", true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlistInfo = playlistRepository.getPlaylist(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val playlistStats = playlistRepository.getPlaylistStats(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

}

