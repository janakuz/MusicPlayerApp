package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TrackSelectionViewModel @Inject constructor (): ViewModel() {
    private val _selectionMode = MutableStateFlow(false)
    val selectionMode = _selectionMode.asStateFlow()


    private val _selectedTrackIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedTrackIds = _selectedTrackIds.asStateFlow()

    private val _selectedTrackUUIDs = MutableStateFlow<Set<String>>(emptySet())
    val selectedTrackUUIDs = _selectedTrackUUIDs.asStateFlow()

    val selectionState: StateFlow<SelectionState> =
        combine (
            _selectedTrackIds,
            _selectedTrackUUIDs
        ) { trackIds, UUIDs ->
            SelectionState (
                selectedTrackIds = trackIds,
                selectedQueueIds = UUIDs,
                count = if (UUIDs.isNotEmpty()) UUIDs.size else trackIds.size
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            SelectionState(
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

    fun clearSelection() {
        _selectedTrackIds.value = emptySet()
        _selectedTrackUUIDs.value = emptySet()
        _selectionMode.value = false
    }

    fun toggleSelection(uuid: String){
        _selectedTrackUUIDs.update { set ->
            if (uuid in set) set - uuid
            else set + uuid
        }

        _selectionMode.value = _selectedTrackUUIDs.value.isNotEmpty()
    }


}

data class SelectionState(
    val selectedTrackIds: Set<Int> = emptySet<Int>(),
    val selectedQueueIds: Set<String> = emptySet<String>(),
    val count: Int = 0
)