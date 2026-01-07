package com.example.musicapp.ui.viewmodels

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.media3.common.Player
import com.example.musicapp.PlaybackService
import com.example.musicapp.data.dto.PlayQueueItemUUID
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.entity.QueueItem
import com.example.musicapp.data.repository.PlayQueueRepository
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor (
    @ApplicationContext private val context: Context,
    private val playQueueRepository: PlayQueueRepository,
    private val trackRepository: TrackRepository,
) : ViewModel() {
    private val mediaController = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = mediaController.asStateFlow()

    private val _queue = MutableStateFlow<List<PlayQueueItemUUID>>(emptyList())
    val queue: StateFlow<List<PlayQueueItemUUID>> = _queue.asStateFlow()

    private val _currentTrack = MutableStateFlow<TrackInfo?>(null)
    val currentTrack: StateFlow<TrackInfo?> = _currentTrack.asStateFlow()


    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

//    val nowPlayingUiState: StateFlow<NowPlayingUiState> = combine (
//        currentTrack,
//        isPlaying
//    ) {
//        track, isPlaying ->
//        NowPlayingUiState(track, isPlaying)
//    }.stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5_000L),
//        NowPlayingUiState(null, false)
//    )

    init {
        viewModelScope.launch {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()


            controllerFuture.addListener({
                val controller = controllerFuture.get()
                mediaController.value = controller

                viewModelScope.launch {
                    val savedQueue = playQueueRepository.getCurrentQueue().first()
                    val currentSession = playQueueRepository.currentSession.first()

                    if (savedQueue.isNotEmpty()) {
                        _queue.value = savedQueue.map { item -> PlayQueueItemUUID(queueId = item.uuid, track = item.trackInfo) }

                        val mediaItems = savedQueue.map { toMediaItem(it.trackInfo) }
                        val startIndex =
                            currentSession.playQueueIndex.coerceIn(0, savedQueue.size - 1)

                        controller.setMediaItems(mediaItems, startIndex, currentSession.position)
                        controller.prepare()

                        _currentTrack.value = savedQueue.getOrNull(startIndex)?.trackInfo
                    }
                }

                controller.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val currentIndex = controller.currentMediaItemIndex
                        val tracks = _queue.value
                        if (currentIndex in tracks.indices) {
                            _currentTrack.value = tracks[currentIndex].track
                        }

                        updatePlaybackSession()
                    }

                })

                controller.addListener(object  : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            val controller = mediaController.value ?: return
                            _position.value = controller.currentPosition
                            _duration.value = controller.duration
                        }
                    }
                })

                controller.addListener(object: Player.Listener{
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        this@PlayerViewModel._isPlaying.value = isPlaying

                        if (!isPlaying) updatePlaybackSession()
                    }

                }
                )


            }, ContextCompat.getMainExecutor(context))

            while (true) {
                mediaController.value?.let { controller ->
                    if (controller.isPlaying) {
                        _duration.value = controller.duration
                        _position.value = controller.currentPosition
                    }
                }
                delay(500)
            }

        }
    }

    fun togglePlayback(){
        val controller = mediaController.value ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }

    }


    private fun updatePlaybackSession() {
        val controller = mediaController.value ?: return
        val currentIndex = controller.currentMediaItemIndex
        val currentPosition = controller.currentPosition

        viewModelScope.launch (Dispatchers.IO) {
            playQueueRepository.saveSession(
                index = currentIndex,
                position = currentPosition
            )
        }
    }

    fun updateQueue(newQueue: List<PlayQueueItemUUID>){
//        val controller = mediaController.value ?: return
//        _queue.value = newQueue
//
//        val mediaItems = newQueue.map { toMediaItem(it) }
//        val currentIndex = controller.currentMediaItemIndex
//        val currentPos = controller.currentPosition
//
//        controller.setMediaItems(mediaItems, currentIndex, currentPos)

        viewModelScope.launch {
            playQueueRepository.replaceQueue(newQueue.mapIndexed { index, track ->
                QueueItem(
                    uuid = track.queueId,
                    trackId = track.track.trackId,
                    orderIndex = index
                )
            })
        }
    }

    fun moveTrack(fromIndex: Int, toIndex: Int) {
        val currentList = _queue.value.toMutableList()

        val trackToMove = currentList.removeAt(fromIndex)
        currentList.add(toIndex, trackToMove)

        _queue.value = currentList

        val controller = mediaController.value ?: return

        controller.moveMediaItem(fromIndex, toIndex)

        updateQueue(currentList)

//        viewModelScope.launch {
//            val entities = currentList.mapIndexed { index, track ->
//                QueueItem(
//                    trackId = track.trackId,
//                    orderIndex = index
//                )
//            }
//            playQueueRepository.replaceQueue(entities)
//        }
    }

    fun playNext(track: TrackInfo) {
        val controller = mediaController.value ?: return
        val nextIndex = controller.currentMediaItemIndex + 1

        controller.addMediaItem(nextIndex, toMediaItem(track))

        val newList = _queue.value.toMutableList().apply { add(nextIndex, PlayQueueItemUUID(track = track)) }
        _queue.value = newList
        updateQueue(newList)
    }

    fun addToQueue(track: TrackInfo) {
        val controller = mediaController.value ?: return

        controller.addMediaItem(toMediaItem(track))

        val newList = _queue.value.toMutableList().apply { add(PlayQueueItemUUID(track = track)) }
        _queue.value = newList
        updateQueue(newList)
    }

    fun playNextList(tracks: List<TrackInfo>) {
        val controller = mediaController.value ?: return
        val nextIndex = controller.currentMediaItemIndex + 1

        controller.addMediaItems(nextIndex, tracks.map { track -> toMediaItem(track) })
        val newList = _queue.value.toMutableList().apply { addAll(nextIndex, tracks.map { track -> PlayQueueItemUUID(track = track) }) }
        _queue.value = newList
        updateQueue(newList)
    }

    fun addToQueueList(tracks: List<TrackInfo>){
        val controller = mediaController.value ?: return

        controller.addMediaItems(tracks.map { track -> toMediaItem(track) })

        val newList = _queue.value.toMutableList().apply { addAll(tracks.map { track -> PlayQueueItemUUID(track = track) }) }
        _queue.value = newList
        updateQueue(newList)
    }

    fun playNextAlbum(albumId: Int){
        viewModelScope.launch {
            val tracks = trackRepository.getTracksInAlbum(albumId).first()

            playNextList(tracks)
        }
    }

    fun addToQueueAlbum(albumId: Int){
        viewModelScope.launch {
            val tracks = trackRepository.getTracksInAlbum(albumId).first()

            addToQueueList(tracks)
        }
    }


    fun playNextArtist(artistId: Int){
        viewModelScope.launch {
            val tracks = trackRepository.getTracksByArtist(artistId).first()

            playNextList(tracks)
        }
    }

    fun addToQueueArtist(artistId: Int){
        viewModelScope.launch {
            val tracks = trackRepository.getTracksByArtist(artistId).first()

            addToQueueList(tracks)
        }
    }


    fun playTrack(track: PlayQueueItemUUID){
        val controller = mediaController.value ?: return
        val trackIndex = _queue.value.indexOf(track)
        controller.seekTo(trackIndex, 0L)
        _currentTrack.value = _queue.value[trackIndex].track
        controller.prepare()
        controller.play()
    }


    fun playTracks(tracks: List<TrackInfo>, selectedTrack: TrackInfo){
        _queue.value = tracks.map { track -> PlayQueueItemUUID(track = track) }
        _currentTrack.value = selectedTrack
        _isPlaying.value = true

        val mediaItems = tracks.map { track ->
            toMediaItem(track)
        }

        val startIndex = tracks.indexOfFirst { it.trackId == selectedTrack.trackId }

        mediaController.value?.apply {
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            play()
        }

        updateQueue(tracks.map { track -> PlayQueueItemUUID(track = track) })
    }

    fun toMediaItem(track: TrackInfo): MediaItem {
        val mediaItem = MediaItem.Builder()
            .setUri(track.fileUri.toUri())
            .setMediaId(track.trackId.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title.toString())
                    .setArtist(track.artistName.toString())
                    .setArtworkUri(track.albumArt?.toUri())
                    .build()
            )
            .build()

        return mediaItem
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

    fun hasNextMediaItem() : Boolean? {
        return mediaController.value?.hasNextMediaItem()
    }

    fun hasPrevMediaItem() : Boolean? {
        return mediaController.value?.hasPreviousMediaItem()
    }

    fun removeTrackAt(index: Int) {
        val controller = mediaController.value ?: return
        val currentList = _queue.value.toMutableList()
        if (index in currentList.indices) {
            controller.removeMediaItem(index)
            currentList.removeAt(index)
            _queue.value = currentList
            updateQueue(currentList)
        }
    }

}

//data class NowPlayingUiState(
//    val currentTrack: TrackInfo?,
//    val isPlaying: Boolean
//)
