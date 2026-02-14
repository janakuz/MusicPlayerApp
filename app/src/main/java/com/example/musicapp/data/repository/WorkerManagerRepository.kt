package com.example.musicapp.data.repository

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface WorkerManagerRepository {

    fun startWorker(manual: Boolean = false)

    fun getEnrichmentProgress(): Flow<WorkInfo?>
}