package com.example.musicapp.data.repository

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface WorkerManagerRepository {

    fun startWorker(manual: Boolean = false)

    fun startWorkerGenres()

    fun startWorkerArtistMetadata()

    fun startWorkerArtistArea()

    fun startWorkerSimilarArtists()

    fun getEnrichmentProgress(): Flow<WorkInfo?>
}