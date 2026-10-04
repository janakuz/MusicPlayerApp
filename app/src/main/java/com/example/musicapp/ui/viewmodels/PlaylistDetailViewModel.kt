package com.example.musicapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.model.PlaylistTrack
import com.example.musicapp.data.repository.PlaylistRepository
import com.example.musicapp.data.repository.PlaylistStats
import com.example.musicapp.data.repository.PlaylistTracksRepository
import com.example.musicapp.data.repository.SmartPlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playlistTracksRepository: PlaylistTracksRepository,
    private val smartPlaylistRepository: SmartPlaylistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlistId: Int = savedStateHandle.get<String>("playlistId")?.toInt()
        ?: throw IllegalStateException("playlistId not found in SavedStateHandle")

    val isSmartPlaylist: Boolean = savedStateHandle["isSmart"] ?: false

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlistTracks = if (isSmartPlaylist) {
        smartPlaylistRepository.getSmartPlaylist(playlistId).flatMapLatest { smartPlaylist ->
            smartPlaylistRepository.getSmartPlaylistTracks(smartPlaylist.filterJson).map { tracks ->
                tracks.mapIndexed { index, track ->
                    PlaylistTrack(
                        position = index,
                        trackInfo = track,
                        playlistId = playlistId,
                        entryId = track.trackId,
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    } else {
        playlistTracksRepository.getAllTracksInPlaylist(playlistId, "position", true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

        val playlistInfo = if (isSmartPlaylist) {
            smartPlaylistRepository.getSmartPlaylist(playlistId).map { playlist ->
                Playlist(
                    name = playlist.name,
                    description = playlist.description,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } else {
            playlistRepository.getPlaylist(playlistId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlistStats = if (isSmartPlaylist) {
        smartPlaylistRepository.getSmartPlaylist(playlistId)
            .flatMapLatest { playlist ->
                smartPlaylistRepository.getSmartPlaylistStats(playlist.filterJson)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        playlistRepository.getPlaylistStats(playlistId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }


    fun reorder(reordered: List<PlaylistTrack>) {
        viewModelScope.launch {
            playlistTracksRepository.reorder(reordered)
        }
    }

}

