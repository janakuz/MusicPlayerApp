package com.example.musicapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.musicapp.R
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.data.repository.ScanProgress
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException

@HiltWorker
class GenresWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val metadataRepository: MetadataRepository
) : CoroutineWorker(context, params) {

    val isManualScan = inputData.getBoolean("IS_MANUAL_SCAN", true)

    companion object {
        private const val CHANNEL_ID = "metadata_sync_channel"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {

        if (isManualScan) {

            createNotificationChannel()

            setForeground(getForegroundInfo())
        }

        try {
            metadataRepository.backfillGenres().collect { progress ->
                setProgress(progress.toWorkData())
            }
            return Result.success()
        } catch (e: HttpException) {
            if (e.code() == 429 || e.code() == 503) {
                return Result.retry()
            }
        } catch (e: Exception) {
            if (e is IOException) Result.retry() else Result.failure()
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo():
            ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Enriching Library")
            .setSmallIcon(R.drawable.outline_sync_24)
            .setOngoing(true)
            .build()
        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Library Enrichment",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Updating album and artist metadata"
        }
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun ScanProgress.toWorkData() =
        workDataOf(
            "current" to current,
            "total" to total,
            "albumTitle" to currentAlbum
        )
}