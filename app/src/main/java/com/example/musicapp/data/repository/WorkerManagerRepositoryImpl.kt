package com.example.musicapp.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.example.musicapp.service.ArtistAreaWorker
import com.example.musicapp.service.ArtistMetadataWorker
import com.example.musicapp.service.GenresWorker
import com.example.musicapp.service.MetadataWorker
import com.example.musicapp.service.SimilarArtistsWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class WorkerManagerRepositoryImpl(private val workManager: WorkManager) :
    WorkerManagerRepository {

    override fun startWorker(manual: Boolean) {

        val policy = if (manual) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP

        val request = OneTimeWorkRequestBuilder<MetadataWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.Companion.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setInputData(workDataOf("IS_MANUAL_SCAN" to manual))
            .build()
        workManager.enqueueUniqueWork(
            "MetadataSync",
            policy,
            request
        )
    }


    override fun startWorkerGenres() {

        val policy = ExistingWorkPolicy.KEEP

        val request = OneTimeWorkRequestBuilder<GenresWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.Companion.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setInputData(workDataOf("IS_MANUAL_SCAN" to true))
            .build()
        workManager.enqueueUniqueWork(
            "MetadataSync",
            policy,
            request
        )
    }

    override fun startWorkerArtistMetadata() {
        val policy = ExistingWorkPolicy.KEEP

        val request = OneTimeWorkRequestBuilder<ArtistMetadataWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.Companion.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setInputData(workDataOf("IS_MANUAL_SCAN" to true))
            .build()
        workManager.enqueueUniqueWork(
            "MetadataSync",
            policy,
            request
        )
    }


    override fun startWorkerArtistArea() {
        val policy = ExistingWorkPolicy.KEEP

        val request = OneTimeWorkRequestBuilder<ArtistAreaWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.Companion.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setInputData(workDataOf("IS_MANUAL_SCAN" to true))
            .build()
        workManager.enqueueUniqueWork(
            "MetadataSync",
            policy,
            request
        )
    }

    override fun startWorkerSimilarArtists() {
        val policy = ExistingWorkPolicy.KEEP

        val request = OneTimeWorkRequestBuilder<SimilarArtistsWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.Companion.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setInputData(workDataOf("IS_MANUAL_SCAN" to true))
            .build()
        workManager.enqueueUniqueWork(
            "MetadataSync",
            policy,
            request
        )
    }

    override fun getEnrichmentProgress(): Flow<WorkInfo?> {
        return workManager
            .getWorkInfosForUniqueWorkFlow("MetadataSync")
            .map { it.firstOrNull() }
    }
}