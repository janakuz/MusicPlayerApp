package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.SearchRepository
import com.example.musicapp.data.repository.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scopeType: String? = savedStateHandle["scopeType"]
    private val scopeId: Int = savedStateHandle.get<Int>("scopeId")?.toInt() ?: -1

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery = _searchQuery.asStateFlow()

    val scope = if (scopeType != null) {
        SearchScope(scopeType, scopeId)
    } else null

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.length < 2) {
                flowOf(SearchResult())
            } else {
                when (scopeType) {
                    "ARTIST" -> searchRepository.searchWithinArtist(query, scopeId.toInt())
                    "ALBUM" -> searchRepository.searchWithinAlbum(query, scopeId.toInt())
                    else -> searchRepository.globalSearch(query)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, SearchResult())


    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }
}


data class SearchScope(
    val scopeType: String? = "global",
    val scopeId: Int? = -1
)
