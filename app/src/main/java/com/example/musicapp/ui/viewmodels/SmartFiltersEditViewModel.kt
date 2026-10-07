package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.FilterRepository
import com.example.musicapp.data.repository.LibraryFilter
import com.example.musicapp.data.repository.SmartPlaylistRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartFiltersEditViewModel @Inject constructor(
    private val smartPlaylistRepository: SmartPlaylistRepository,
    private val filterRepository: FilterRepository,
    private val smartPlaylistGson: Gson,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlistId: Int = savedStateHandle.get<String>("playlistId")?.toInt()
        ?: throw IllegalStateException("playlistId not found in SavedStateHandle")


    private var initialFilter: LibraryFilter = LibraryFilter()

    private val _draftFilter = MutableStateFlow(LibraryFilter())
    val draftFilter = _draftFilter.asStateFlow()

    val hasUnsavedChanges: StateFlow<Boolean> = _draftFilter.map { draft ->
        draft != initialFilter
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredGlobalTracks = _draftFilter.flatMapLatest { filter ->
        filterRepository.getGlobalTracks(filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSave(){
        viewModelScope.launch {
            val current = smartPlaylistRepository.getSmartPlaylistById(playlistId)
            val new = current.copy(filterJson = smartPlaylistGson.toJson(_draftFilter.value))
            smartPlaylistRepository.updatePlaylist(new)
        }
    }

    fun updateDraft(newFilter: LibraryFilter) {
        _draftFilter.value = newFilter
    }

    init {
        loadSavedFilter()
    }

    fun loadSavedFilter(){
        viewModelScope.launch {
            val current = smartPlaylistRepository.getSmartPlaylistById(playlistId)
            val currentFilter = smartPlaylistGson.fromJson(current.filterJson, LibraryFilter::class.java)
            initialFilter = currentFilter
            _draftFilter.value = currentFilter
        }
    }

    fun resetDraft(){
        _draftFilter.value = initialFilter
    }

}