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
class TrackSelectionViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
) : ViewModel() {
    private val _selectionMode = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()


    private val _selectedTrackIds = MutableStateFlow<Set<Int>>(emptySet())

    private val _selectedPlaylistEntryIds = MutableStateFlow<Set<PlaylistSelect>>(emptySet())


    private val _selectedTrackUUIDs = MutableStateFlow<Set<QueueSelect>>(emptySet())

    private val _deletionRequestTrigger = MutableSharedFlow<Unit>(replay = 0)
    val deletionRequestTrigger = _deletionRequestTrigger.asSharedFlow()

    private val _moveTrigger = MutableSharedFlow<Unit>(replay = 0)
    val moveTrigger = _moveTrigger.asSharedFlow()

    val moveEnabled: StateFlow<Boolean> = _selectedTrackIds.map { list ->
        val tracks = trackRepository.getTracksByIds(list.toList())
        val uniqueAlbumIds = tracks.map { it.albumId }.distinct()
        val uniqueArtistIds = tracks.map { it.artistId }.distinct()

        list.size > 1 && uniqueArtistIds.size == 1 && uniqueAlbumIds.size == 1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val selectionState: StateFlow<SelectionState> =
        combine(
            _selectedTrackIds,
            _selectedTrackUUIDs,
            _selectedPlaylistEntryIds
        ) { trackIds, UUIDs, entryIds ->
            SelectionState(
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

    fun toggleSelection(trackId: Int) {
        _selectedTrackIds.update { set ->
            if (trackId in set) set - trackId
            else set + trackId
        }

        _selectionMode.value = _selectedTrackIds.value.isNotEmpty()
    }

    fun toggleSelectionPlaylist(entryId: Int, trackId: Int) {
        val selection = PlaylistSelect(entryId, trackId)
        _selectedPlaylistEntryIds.update { set ->
            if (selection in set) set - selection
            else set + selection
        }

        _selectionMode.value = _selectedPlaylistEntryIds.value.isNotEmpty()
    }

    fun clearSelection() {
        _selectedTrackIds.value = emptySet()
        _selectedTrackUUIDs.value = emptySet()
        _selectedPlaylistEntryIds.value = emptySet()
        _selectionMode.value = false
    }

    fun toggleSelectionQueue(uuid: String, trackId: Int) {
        val selection = QueueSelect(uuid, trackId)
        _selectedTrackUUIDs.update { set ->
            if (selection in set) set - selection
            else set + selection
        }

        _selectionMode.value = _selectedTrackUUIDs.value.isNotEmpty()
    }

    fun requestDeletionOfSelected() {
        viewModelScope.launch {
            _deletionRequestTrigger.emit(Unit)
        }
    }

    fun requestMove() {
        viewModelScope.launch {
            _moveTrigger.emit(Unit)
        }
    }

}

data class SelectionState(
    val selectedTrackIds: Set<Int> = emptySet<Int>(),
    val selectedQueueIds: Set<QueueSelect> = emptySet<QueueSelect>(),
    val selectedPlaylistEntryIds: Set<PlaylistSelect> = emptySet<PlaylistSelect>(),
    val count: Int = 0
)

data class PlaylistSelect(
    val entryId: Int,
    val trackId: Int,
)

data class QueueSelect(
    val queueId: String,
    val trackId: Int
)

enum class SelectSource {
    ALBUM,
    QUEUE,
    PLAYLIST
}