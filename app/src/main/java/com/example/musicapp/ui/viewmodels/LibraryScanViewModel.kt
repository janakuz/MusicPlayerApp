package com.example.musicapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.musicapp.LocalLibraryScanner
import com.example.musicapp.MetadataWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ScanUiState(
    val isScanning: Boolean = false,
    val isEnriching: Boolean = false,
    val scanProgress: Float = 0f,
    val enrichmentProgress: Float = 0f,
    val statusMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class LibraryScanViewModel @Inject constructor(
    private val scanner: LocalLibraryScanner,
    private val workManager: WorkManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        observeEnrichment()
    }

    fun startScan(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            try {
                scanner.scanAll(context) { progress ->
                    _uiState.update { it.copy(scanProgress = progress) }
                }

                _uiState.update { it.copy(isScanning = false, isEnriching = true) }

                val request = OneTimeWorkRequestBuilder<MetadataWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                    )
                    .build()
                workManager.enqueueUniqueWork(
                    "MetadataSync",
                    ExistingWorkPolicy.KEEP,
                    request
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = e.message) }
            }
        }
    }

    fun observeEnrichment() {
        workManager.getWorkInfosForUniqueWorkFlow("MetadataSync")
            .onEach { workInfos ->
                val info = workInfos.firstOrNull() ?: return@onEach

                val current = info.progress.getInt("current", 0)
                val total = info.progress.getInt("total", 0)
                val albumTitle = info.progress.getString("albumTitle") ?: ""

                if (info.state == WorkInfo.State.RUNNING) {
                    _uiState.update { it.copy(
                        isEnriching = true,
                        enrichmentProgress = if (total > 0) current.toFloat() / total else 0f,
                        statusMessage = "Enriching $albumTitle..."
                    ) }
                } else if (info.state.isFinished) {
                    _uiState.update { it.copy(isEnriching = false, isScanning = false) }
                }

            }
    }

}
