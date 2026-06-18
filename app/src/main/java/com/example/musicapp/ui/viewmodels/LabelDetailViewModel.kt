package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlin.text.toInt

@HiltViewModel
class LabelDetailViewModel @Inject constructor (
    albumRepository: AlbumRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val label: String = savedStateHandle.get<String>("name")
        ?: throw IllegalStateException("label name not found in SavedStateHandle")

    val decodedName = URLDecoder.decode(label, StandardCharsets.UTF_8.name())

    val labelItems = albumRepository.getLabelItems(decodedName)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult())
}