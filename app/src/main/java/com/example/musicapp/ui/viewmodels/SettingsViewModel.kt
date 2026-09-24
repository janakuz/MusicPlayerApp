package com.example.musicapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.service.DatabaseBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val databaseBackupManager: DatabaseBackupManager,
) : ViewModel() {

    private val _backupUiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val backupUiState = _backupUiState.asStateFlow()

    private val _eventChannel = Channel<String>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()


    val skipSilenceEnabled: StateFlow<Boolean> = userPreferencesRepository.skipSilenceToggle
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val minSimilarityScore: StateFlow<Double> = userPreferencesRepository.minVisibleSimilarityScore
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0.0
        )


    fun updateSkipSilence(enabled: Boolean){
        viewModelScope.launch {
            userPreferencesRepository.updateSkipSilence(enabled)
        }
    }

    fun updateMinSimilarityScore(new: Int){
        viewModelScope.launch {
            userPreferencesRepository.updateMinSimilarityScore(new/100.0)
        }
    }

    fun exportDatabase(destinationUri: Uri) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            databaseBackupManager.exportDatabase(destinationUri)
                .onSuccess {
                    _backupUiState.value = BackupUiState.Success
                    _eventChannel.send("Database exported successfully!")
                }
                .onFailure { error ->
                    _backupUiState.value = BackupUiState.Error
                    _eventChannel.send("Export failed: ${error.localizedMessage}")
                }
        }
    }

    fun importDatabase(sourceUri: Uri, onImportSuccess: () -> Unit) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            databaseBackupManager.importDatabase(sourceUri)
                .onSuccess {
                    _backupUiState.value = BackupUiState.Success
                    _eventChannel.send("Import successful. Restarting app...")
                    delay(1500)
                    onImportSuccess()
                }
                .onFailure { error ->
                    _backupUiState.value = BackupUiState.Error
                    _eventChannel.send("Import failed: ${error.localizedMessage}")
                }
        }
    }


}

sealed interface BackupUiState {
    object Idle : BackupUiState
    object Loading : BackupUiState
    object Success: BackupUiState
    object Error: BackupUiState
}