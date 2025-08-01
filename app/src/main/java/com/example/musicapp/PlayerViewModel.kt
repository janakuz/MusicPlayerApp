package com.example.musicapp

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicapp.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.media3.common.Player
import kotlinx.coroutines.delay


class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaController = MutableStateFlow<MediaController?>(null)
//        private set
    val controller: StateFlow<MediaController?> = mediaController.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()


    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    init {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            controllerFuture.addListener({
                val controller = controllerFuture.get()
                mediaController.value = controller

                controller.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val mediaId = mediaItem?.mediaId?.toIntOrNull()
                        val matchingTrack = _queue.value.find { it.id == mediaId }
                        _currentTrack.value = matchingTrack
                    }
                })





            }, ContextCompat.getMainExecutor(context))

            while (true) {
                mediaController.value?.let { controller ->
                    if (controller.isPlaying) {
                        _position.value = controller.currentPosition
                        _duration.value = controller.duration
                    }
                }
                delay(500)
            }

        }
    }

    fun playTrack(uri: Uri) {
        val mediaItem = MediaItem.Builder()
            .setUri("asset:///03_guest_list.mp3".toUri()) // OR raw resource URI
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Test Track")
                    .setArtist("Demo Artist")
                    .build()
            )
            .build()
        mediaController.value?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun playTracks(tracks: List<Track>, selectedTrack: Track){
        _queue.value = tracks
        _currentTrack.value = selectedTrack

        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.file.toUri()) // could be a file path, content URI, or raw resource
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title.toString())
                        .setArtist(track.artist.toString())
                        .build()
                )
                .build()
        }

        val startIndex = tracks.indexOfFirst { it.id == selectedTrack.id }

        mediaController.value?.apply {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController.value?.seekTo(positionMs)
    }

    fun skipToNext() {
        mediaController.value?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController.value?.seekToPrevious()
    }

    fun hasNextMediaItem(){
        mediaController.value?.hasNextMediaItem()
    }


    fun removeTrackAt(index: Int) {
        _queue.value = _queue.value.toMutableList().apply { removeAt(index) }
    }

    fun moveTrack(from: Int, to: Int) {
        Log.d("tag", "$from $to")
        _queue.value = _queue.value.toMutableList().apply {
            add(to, removeAt(from))
        }
        Log.d("tag", _queue.value.toString())
    }

    fun updateQueue(newQueue: List<Track>) {
        Log.d("tag", newQueue.toString())
        val currentMediaId = mediaController.value?.currentMediaItem?.mediaId?.toIntOrNull()
        val currentTrack = newQueue.find { it.id == currentMediaId }
        val newIndex = newQueue.indexOfFirst { it.id == currentMediaId }

        // If somehow not found, just fallback to 0
        val safeIndex = if (newIndex >= 0) newIndex else 0

        _queue.value = newQueue
        _currentTrack.value = currentTrack

        val mediaItems = newQueue.map { track ->
            MediaItem.Builder()
                .setUri(track.file.toUri())
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title.toString())
                        .setArtist(track.artist.toString())
                        .build()
                )
                .build()
        }

        mediaController.value?.setMediaItems(
            mediaItems,
            /* startIndex = */ safeIndex,
            /* startPositionMs = */ mediaController.value?.currentPosition ?: 0L
        )
    }
}
