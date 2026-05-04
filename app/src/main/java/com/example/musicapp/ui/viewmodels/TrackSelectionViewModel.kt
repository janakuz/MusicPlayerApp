package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackSelectionViewModel @Inject constructor (
    private val trackRepository: TrackRepository
): ViewModel() {
    private val _selectionMode = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()


    private val _selectedTrackIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedTrackIds = _selectedTrackIds.asStateFlow()

    private val _selectedPlaylistEntryIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPlaylistEntryIds = _selectedPlaylistEntryIds.asStateFlow()


    private val _selectedTrackUUIDs = MutableStateFlow<Set<String>>(emptySet())
    val selectedTrackUUIDs = _selectedTrackUUIDs.asStateFlow()

    private val _deletionRequestTrigger = MutableSharedFlow<Unit>(replay = 0)
    val deletionRequestTrigger = _deletionRequestTrigger.asSharedFlow()

    private val _moveTrigger = MutableSharedFlow<Unit>(replay = 0)
    val moveTrigger = _moveTrigger.asSharedFlow()

    val moveEnabled: StateFlow<Boolean> = _selectedTrackIds.map { list ->
        val tracks = trackRepository.getTracksByIds(list.toSet())
        val uniqueAlbumIds = tracks.map { it.albumId }.distinct()
        val uniqueArtistIds = tracks.map { it.artistId }.distinct()

        list.size > 1 && uniqueArtistIds.size == 1 && uniqueAlbumIds.size == 1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val selectionState: StateFlow<SelectionState> =
        combine (
            _selectedTrackIds,
            _selectedTrackUUIDs,
            _selectedPlaylistEntryIds
        ) { trackIds, UUIDs, entryIds ->
            SelectionState (
                selectedTrackIds = trackIds,
                selectedQueueIds = UUIDs,
                selectedPlaylistEntryIds = entryIds,
                count = if (UUIDs.isNotEmpty()) UUIDs.size else if (entryIds.isNotEmpty()) entryIds.size else trackIds.size
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            SelectionState(
                emptySet(),
                emptySet(),
                emptySet(),
                0
            )
        )

    fun toggleSelection(trackId: Int){
        _selectedTrackIds.update { set ->
            if (trackId in set) set - trackId
            else set + trackId
        }

        _selectionMode.value = _selectedTrackIds.value.isNotEmpty()
    }

    fun toggleSelectionPlaylist(entryId: Int){
        _selectedPlaylistEntryIds.update { set ->
            if (entryId in set) set - entryId
            else set + entryId
        }

        _selectionMode.value = _selectedPlaylistEntryIds.value.isNotEmpty()
    }


    fun clearSelection() {
        _selectedTrackIds.value = emptySet()
        _selectedTrackUUIDs.value = emptySet()
        _selectedPlaylistEntryIds.value = emptySet()
        _selectionMode.value = false
    }

    fun toggleSelection(uuid: String){
        _selectedTrackUUIDs.update { set ->
            if (uuid in set) set - uuid
            else set + uuid
        }

        _selectionMode.value = _selectedTrackUUIDs.value.isNotEmpty()
    }

    fun requestDeletionOfSelected() {
        viewModelScope.launch {
            _deletionRequestTrigger.emit(Unit)
        }
    }

    fun requestMove(){
        viewModelScope.launch {
            _moveTrigger.emit(Unit)
        }
    }

}

data class SelectionState(
    val selectedTrackIds: Set<Int> = emptySet<Int>(),
    val selectedQueueIds: Set<String> = emptySet<String>(),
    val selectedPlaylistEntryIds: Set<Int> = emptySet<Int>(),
    val count: Int = 0
)