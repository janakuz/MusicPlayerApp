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
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.example.musicapp.LocalLibraryScanner
import com.example.musicapp.MetadataWorker
import com.example.musicapp.data.repository.WorkerManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BackgroundScanViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val scanner: LocalLibraryScanner,
    private val workerManagerRepository: WorkerManagerRepository
//    private val workManager: WorkManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            runSync(context)
        }
    }


    private var isScanning = false


    suspend fun runSync(context: Context){
        if (isScanning) return
        isScanning = true
        withContext(Dispatchers.IO) {
            scanner.findChanges(context)
            workerManagerRepository.startWorker(false)
        }
        isScanning = false
    }

}