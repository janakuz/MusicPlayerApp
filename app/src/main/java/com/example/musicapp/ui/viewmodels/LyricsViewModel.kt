package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.TrackLyrics
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _lyricsUiState = MutableStateFlow<LyricsUiState>(LyricsUiState.Hidden)
    val lyricsUiState: StateFlow<LyricsUiState> = _lyricsUiState.asStateFlow()

    fun resetLyrics(){
        _lyricsUiState.value = LyricsUiState.Hidden
    }

    fun toggleLyrics(trackInfo: TrackInfo){
        if (_lyricsUiState.value !is LyricsUiState.Hidden) {
            _lyricsUiState.value = LyricsUiState.Hidden
            return
        }

        viewModelScope.launch {
            _lyricsUiState.value = LyricsUiState.Loading

            val localLyrics = trackRepository.getCachedLyrics(trackInfo.trackId)

            if (localLyrics != null) {
                emitLyricsState(localLyrics.plainLyrics, localLyrics.syncedLyrics)
                return@launch
            }

            try {
                val response = trackRepository.getLyrics(trackInfo)
                if (response != null) {
                    val finalPlain = if (response.instrumental) "[Instrumental]" else response.plainLyrics
                    val finalSynced = if (response.instrumental) "[Instrumental]" else response.syncedLyrics

                    val newLyrics = TrackLyrics(
                        trackId = trackInfo.trackId,
                        plainLyrics = finalPlain,
                        syncedLyrics = finalSynced
                    )
                    trackRepository.insertLyrics(newLyrics)

                    trackRepository.updateInstrumental(response.instrumental, trackInfo.trackId)

                    emitLyricsState(finalPlain, finalSynced)
                } else {
                    _lyricsUiState.value = LyricsUiState.NotFound
                }
            } catch (e: Exception) {
                _lyricsUiState.value = LyricsUiState.NotFound
            }
        }
    }


    private fun emitLyricsState(plain: String?, synced: String?) {
        when {
            plain == "[Instrumental]" || synced == "[Instrumental]" -> {
                _lyricsUiState.value = LyricsUiState.Instrumental
            }
            !synced.isNullOrBlank() -> {
                _lyricsUiState.value = LyricsUiState.Synced(parseLrc(synced))
            }
            !plain.isNullOrBlank() -> {
                _lyricsUiState.value = LyricsUiState.Plain(plain)
            }
            else -> {
                _lyricsUiState.value = LyricsUiState.NotFound
            }
        }
    }

    fun parseLrc(rawSyncedLyrics: String?): List<LyricLine> {
        if (rawSyncedLyrics.isNullOrBlank()) return emptyList()

        val lines = mutableListOf<LyricLine>()
        val timeRegex = Regex("""\[(\d{2}):(\d{2})[.:](\d{2,3})]""")

        rawSyncedLyrics.lines().forEach { line ->
            val match = timeRegex.find(line)
            if (match != null) {
                val minutes = match.groupValues[1].toLong()
                val seconds = match.groupValues[2].toLong()
                val fraction = match.groupValues[3].toLong()

                val msFraction = if (match.groupValues[3].length == 2) fraction * 10 else fraction
                val totalTimestampMs = (minutes * 60 * 1000) + (seconds * 1000) + msFraction

                val lyricText = line.replace(timeRegex, "").trim()

                lines.add(LyricLine(timestampMs = totalTimestampMs, text = lyricText))
            }
        }
        return lines.sortedBy { it.timestampMs }
    }
}

data class LyricLine(
    val timestampMs: Long,
    val text: String
)


sealed interface LyricsUiState {
    object Hidden : LyricsUiState
    object Loading : LyricsUiState
    object NotFound : LyricsUiState
    object Instrumental : LyricsUiState
    data class Plain(val text: String) : LyricsUiState
    data class Synced(val lines: List<LyricLine>) : LyricsUiState
}