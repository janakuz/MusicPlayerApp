package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackMultiEditViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
) : ViewModel() {


    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState(null, null))
    val voiceState = _voiceState.asStateFlow()

    fun onEditMultiple(voiceState: VoiceState, tracks: Set<Int>){
        viewModelScope.launch {
            trackRepository.updateInstrumentalAndVoice(voiceState.instrumental, voiceState.voice, tracks.toList())
        }
    }

    fun onInstrumentalChange(newValue: Boolean){
        _voiceState.update { it.copy(instrumental = newValue) }
    }

    fun onVoiceChange(newValue: String){
        _voiceState.update { it.copy(voice = newValue, instrumental = false) }
    }

}