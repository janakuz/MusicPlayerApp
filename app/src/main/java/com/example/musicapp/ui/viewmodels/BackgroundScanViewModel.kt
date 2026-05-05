package com.example.musicapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.service.LocalLibraryScanner
import com.example.musicapp.data.repository.WorkerManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BackgroundScanViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val scanner: LocalLibraryScanner,
    private val workerManagerRepository: WorkerManagerRepository
//    private val workManager: WorkManager
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(true)
    val isInitialized = _isInitialized.asStateFlow()

    init {
        viewModelScope.launch {
            runSync(context)
        }
    }


    private var isScanning = false


    suspend fun runSync(context: Context) {
        if (isScanning) return
        isScanning = true
        withContext(Dispatchers.IO) {
            val isLibraryInitialized = scanner.findChanges(context)
            if (isLibraryInitialized) {
                _isInitialized.value = true
                workerManagerRepository.startWorker(false)
            }
            else {
                _isInitialized.value = false
            }
        }
        isScanning = false
    }

}