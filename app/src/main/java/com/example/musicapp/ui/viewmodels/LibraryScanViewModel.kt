package com.example.musicapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.musicapp.data.repository.WorkerManagerRepository
import com.example.musicapp.service.LocalLibraryScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val isScanning: Boolean = false,
    val isEnriching: Boolean = false,
    val scanProgress: Float = 0f,
    val enrichmentProgress: Float = 0f,
    val statusMessage: String? = null,
    val error: String? = null
)

sealed class Phase {
    object Idle : Phase()
    object Scanning : Phase()
    object Enriching : Phase()
    data class Error(val error: String) : Phase()
}

@HiltViewModel
class LibraryScanViewModel @Inject constructor(
    private val scanner: LocalLibraryScanner,
    private val workerManagerRepository: WorkerManagerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _workflowState = MutableStateFlow<Phase>(Phase.Idle)
    val workflowState = _workflowState.asStateFlow()

    init {
        observeEnrichment()
    }

    fun startScan(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isScanning = true, error = null) }
            _workflowState.value = Phase.Scanning
            try {
                scanner.scanAll(context) { progress ->
                    _uiState.update { it.copy(scanProgress = progress) }
                }

                _uiState.update { it.copy(isScanning = false, isEnriching = true) }
                _workflowState.value = Phase.Enriching

                workerManagerRepository.startWorker(true)

            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = e.message) }
                _workflowState.value = Phase.Error(e.message.toString())
            }
        }
    }

    fun backfillLocalGenres(context: Context){
        viewModelScope.launch {
            scanner.backfillLocalGenres(
                context = context,
                onProgressUpdate = { progress ->
                    _uiState.update { it.copy(scanProgress = progress) }})

            _uiState.update { it.copy(isScanning = false, isEnriching = true) }

            workerManagerRepository.startWorkerGenres()

        }
    }

    fun backfillArtistMetadata(){
        viewModelScope.launch {
            workerManagerRepository.startWorkerArtistMetadata()

        }
    }

    fun backfillArtistArea(){
        viewModelScope.launch {
            workerManagerRepository.startWorkerArtistArea()

        }
    }


    fun backfillSimilar(){
        viewModelScope.launch {
            workerManagerRepository.startWorkerSimilarArtists()

        }
    }

    fun extractAudioFeatures(){
        viewModelScope.launch {
            workerManagerRepository.startWorkerTrackAudioFeatures()

        }
    }


    fun observeEnrichment() {
        workerManagerRepository.getEnrichmentProgress()
            .onEach { info ->
                if (info == null) return@onEach

                val current = info.progress.getInt("current", 0)
                val total = info.progress.getInt("total", 0)
                val albumTitle = info.progress.getString("albumTitle") ?: ""

                if (info.state == WorkInfo.State.RUNNING) {
                    _workflowState.value = Phase.Enriching
                    _uiState.update {
                        it.copy(
                            isEnriching = true,
                            enrichmentProgress = if (total > 0) current.toFloat() / total else 0f,
                            statusMessage = "Enriching $albumTitle..."
                        )
                    }
                } else if (info.state.isFinished) {
                    _uiState.update { it.copy(isEnriching = false, isScanning = false) }
                    _workflowState.value = Phase.Idle
                }
            }
            .launchIn(viewModelScope)
    }

}
