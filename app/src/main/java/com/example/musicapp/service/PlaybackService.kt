package com.example.musicapp.service

import android.app.PendingIntent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository
    @Inject
    lateinit var trackRepository: TrackRepository
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var currentTrackId: Int? = null
    private var hasMarkedPlayed: Boolean = false
    private var trackerJob: Job? = null

    private var playThresholdRatio: Double = 0.5

    @OptIn(UnstableApi::class)
    private fun observePreferences() {
        serviceScope.launch {
            preferencesRepository.skipSilenceToggle.collect { isEnabled ->
                player.skipSilenceEnabled = isEnabled
            }
        }
        serviceScope.launch {
            preferencesRepository.playThreshold.collect { value ->
                playThresholdRatio = value
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentTrackId = mediaItem?.mediaMetadata?.extras?.getInt("ID")
                hasMarkedPlayed = false
                startPositionTracker()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startPositionTracker()
                } else {
                    trackerJob?.cancel()
                }
            }
        })

        val callback = object : MediaSession.Callback {
            @OptIn(UnstableApi::class)
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val availableSessionCommands =
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .build()
                val availablePlayerCommands =
                    MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                        .add(Player.COMMAND_SET_MEDIA_ITEM)
                        .add(Player.COMMAND_PREPARE)
                        .add(Player.COMMAND_PLAY_PAUSE)
                        .build()

                return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setAvailableSessionCommands(availableSessionCommands)
                    .setAvailablePlayerCommands(availablePlayerCommands)
                    .build()
            }

            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {

                val updatedItems = mediaItems.map { item ->
                    val uri = item.requestMetadata.mediaUri
                        ?: item.localConfiguration?.uri
                        ?: Uri.EMPTY

                    item.buildUpon()
                        .setUri(uri)
                        .build()
                }.toMutableList()
                return Futures.immediateFuture(updatedItems)
            }
        }

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(callback)
            .setSessionActivity(pendingIntent)
            .build()

        observePreferences()
    }


    private fun startPositionTracker() {
        trackerJob?.cancel()
        trackerJob = serviceScope.launch {
            while (player.isPlaying) {
                checkPlayThreshold()
                delay(1000)
            }
        }
    }

    private suspend fun checkPlayThreshold() {
        val trackId = currentTrackId ?: return
        if (hasMarkedPlayed) return

        val duration = player.duration
        val currentPosition = player.currentPosition

        if (duration > 0) {
            val targetMs = (duration * playThresholdRatio).toLong()
            if (currentPosition >= targetMs) {
                hasMarkedPlayed = true

                trackRepository.updateStats(
                    trackId = trackId,
                )
            }
        }
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = mediaSession

    override fun onDestroy() {
        serviceJob.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}