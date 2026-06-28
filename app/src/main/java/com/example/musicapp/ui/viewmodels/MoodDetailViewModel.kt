package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.MoodRepository
import com.example.musicapp.data.repository.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.text.toInt

@HiltViewModel
class MoodDetailViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

        private val moodId: Int = savedStateHandle.get<String>("moodId")?.toInt()
            ?: throw IllegalStateException("moodId not found in SavedStateHandle")

        val moodItems = moodRepository.getItemsForMood(moodId, artistLimit = 20, albumThreshold = 0.8F)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult())

        val moodName = moodRepository.getMoodName(moodId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),"")
}