package com.example.musicapp.ui.viewmodels

import android.app.PendingIntent
import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackDeletionViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository,
) : ViewModel() {

    private val _pendingDeleteUris = MutableStateFlow<List<String>>(emptyList())
    val pendingDeleteUris = _pendingDeleteUris.asStateFlow()

    fun prepareDeletion(tracks: List<Int>) {
        viewModelScope.launch {
            val fullTracks = trackRepository.getTracksByIds(tracks.toSet())
            val uris = fullTracks.map { it.fileUri }
            _pendingDeleteUris.value = uris
        }
    }


    fun finalizeDeletion(tracks: List<String>) {

        viewModelScope.launch(Dispatchers.IO) {
            trackRepository.deleteByUri(tracks)
            artistRepository.deleteOrphaned()
            albumRepository.deleteOrphaned()

            clearPendingDeletion()
        }

    }

    fun clearPendingDeletion() {
        _pendingDeleteUris.value = emptyList()
    }

    fun getDeleteIntent(context: Context, uriStrings: List<String>): PendingIntent {
        val uris = uriStrings.map { it.toUri() }
        return MediaStore.createDeleteRequest(context.contentResolver, uris)
    }

}