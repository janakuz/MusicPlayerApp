package com.example.musicapp.data.repository

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.example.musicapp.MetadataWorker
import java.util.concurrent.TimeUnit

class OfflineWorkerManagerRepository(private val workManager: WorkManager) : WorkerManagerRepository {

    override fun startWorker(manual: Boolean) {

        val policy = if (manual) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP

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
            .setInputData(workDataOf("IS_MANUAL_SCAN" to manual))
            .build()
        workManager.enqueueUniqueWork(
            "MetadataSync",
            policy,
            request
        )
    }
}