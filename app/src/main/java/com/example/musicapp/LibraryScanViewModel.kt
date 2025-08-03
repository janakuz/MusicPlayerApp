package com.example.musicapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val isScanning: Boolean = false,
    val progress: Int = 0, // optional
    val error: String? = null
)

@HiltViewModel
class LibraryScanViewModel @Inject constructor(
    private val scanner: LibraryScanner,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()


    fun startScan(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            try {
                scanner.scanAll(context)
                _uiState.update { it.copy(isScanning = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = e.message) }
            }
        }
    }
}
