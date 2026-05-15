package com.example.musicapp.ui.viewmodels

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicapp.data.local.entity.QueueItem
import com.example.musicapp.data.local.model.PlayQueueItemUUID
import com.example.musicapp.data.local.model.PlaylistTrack
import com.example.musicapp.data.local.model.QueueItemFull
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.repository.DynamicThemeRepository
import com.example.musicapp.data.repository.PlayQueueRepository
import com.example.musicapp.data.repository.PlayerColors
import com.example.musicapp.data.repository.PlaylistTracksRepository
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.service.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playQueueRepository: PlayQueueRepository,
    private val trackRepository: TrackRepository,
    private val playlistTracksRepository: PlaylistTracksRepository,
    private val dynamicThemeRepository: DynamicThemeRepository,
) : ViewModel() {

    private var controller: MediaController? = null

    val isShuffleEnabled: StateFlow<Boolean> = playQueueRepository.shuffleOn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val repeatMode: StateFlow<Int> = playQueueRepository.repeatMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val queue: StateFlow<List<PlayQueueItemUUID>> =
        playQueueRepository.shuffleOn.flatMapLatest { shuffle ->
            playQueueRepository.getCurrentQueue(shuffle)
                .map { q -> q.map { toQueueItem(it) } }

        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList<PlayQueueItemUUID>()
            )

    private val _currentTrack = MutableStateFlow<PlayQueueItemUUID?>(null)
    val currentTrack: StateFlow<PlayQueueItemUUID?> = _currentTrack.asStateFlow()

    private val _eventChannel = Channel<String>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()


    private val _currentSpeed = MutableStateFlow<Float>(1.0f)
    val currentSpeed = _currentSpeed.asStateFlow()

    var albumColors by mutableStateOf(
        PlayerColors(
            mainColor = Color(0xFF121212),
            secondaryColor = Color.Cyan,
            onColor = Color.White
        )
    )

    val hasActivePlayback: StateFlow<Boolean> =
        currentTrack.map {
            it != null
        }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                false
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val position: StateFlow<Long> = hasActivePlayback.flatMapLatest { hasPlayback ->
        if (!hasPlayback) {
            playQueueRepository.currentSession.map { it.position }
        } else {
            tickerFlow(500).map { controller?.currentPosition ?: 0L }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0L
    )


    val duration: StateFlow<Long> = currentTrack.map { track ->
        track?.track?.duration ?: 0L
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0L
    )


    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()


    init {
        viewModelScope.launch {
            val sessionToken =
                SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()


            controllerFuture.addListener({
                controller = controllerFuture.get()

                val c = controller!!

                c.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem.let {
                            val track = TrackInfo(
                                title = mediaItem?.mediaMetadata?.title.toString(),
                                trackId = mediaItem?.mediaMetadata?.extras?.getInt("ID")
                                    ?: 0,//.mediaId?.toInt() ?: 0,
                                artistName = mediaItem?.mediaMetadata?.artist.toString(),
                                albumTitle = mediaItem?.mediaMetadata?.albumTitle.toString(),
                                albumArt = mediaItem?.mediaMetadata?.artworkUri.toString(),
                                trackNum = mediaItem?.mediaMetadata?.trackNumber,
                                duration = mediaItem?.mediaMetadata?.durationMs ?: 0L,
                                fileUri = mediaItem?.requestMetadata?.mediaUri.toString(),
                                albumId = mediaItem?.mediaMetadata?.extras?.getInt("albumId") ?: 0,
                                artistId = mediaItem?.mediaMetadata?.extras?.getInt("artistId")
                                    ?: 0,
                                filePath = ""
                            )
                            _currentTrack.value = PlayQueueItemUUID(
                                queueId = mediaItem?.mediaId ?: "",
                                playlistEntryId = mediaItem?.mediaMetadata?.extras?.getInt("entryId"),
                                track = track,
                                originalOrder = 0,
                                shuffledOrder = 0
                            )
                        }
                        if (c.repeatMode != Player.REPEAT_MODE_ONE) updatePlaybackSession()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        this@PlayerViewModel._isPlaying.value = isPlaying

                        if (!isPlaying) updatePlaybackSession()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("PlayerError", error.errorCode.toString() + " " + error.message)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d("player", playbackState.toString())
                    }

                }
                )

                viewModelScope.launch {
                    queue.collect { newQueue ->

                        if (controller!!.mediaItemCount == 0 && newQueue.isNotEmpty()) {
                            val mediaItems = newQueue.map { toMediaItem(it) }
                            controller!!.setMediaItems(mediaItems)

                            val session = playQueueRepository.currentSession.first()
                            controller!!.prepare()
                            controller!!.seekTo(session.playQueueIndex, session.position)
                            _currentTrack.value = queue.value[session.playQueueIndex]
                        }
                    }
                }


            }, ContextCompat.getMainExecutor(context))

        }
    }

    private fun tickerFlow(period: Long) = flow {
        while (true) {
            emit(Unit)
            delay(period)
        }
    }

    private fun toQueueItem(item: QueueItemFull): PlayQueueItemUUID {
        return PlayQueueItemUUID(
            originalOrder = item.orderIndex,
            shuffledOrder = item.shuffledIndex ?: -1,
            queueId = item.uuid,
            track = item.trackInfo
        )
    }

    fun updateSpeed(speed: Float){
        _currentSpeed.value = speed
        controller!!.setPlaybackSpeed(speed)
    }

    fun getAlbumColors(imagePath: String) {
        viewModelScope.launch {
            dynamicThemeRepository.extractColorsFromUrl(imagePath, context = context)?.let {
                albumColors = it
            }
        }
    }


    fun togglePlayback() {
        if (controller!!.isPlaying) {
            controller!!.pause()
        } else {
            controller!!.play()
        }

    }


    private fun updatePlaybackSession() {
        val currentIndex = controller!!.currentMediaItemIndex
        val currentPosition = controller!!.currentPosition

        viewModelScope.launch(Dispatchers.IO) {
            playQueueRepository.saveSession(
                index = currentIndex,
                position = currentPosition
            )
        }
    }

    fun updateQueue(newQueue: List<PlayQueueItemUUID>) {
        viewModelScope.launch {
            playQueueRepository.replaceQueue(newQueue.mapIndexed { index, track ->
                QueueItem(
                    uuid = track.queueId,
                    trackId = track.track.trackId,
                    orderIndex = track.originalOrder,
                    shuffledIndex = track.shuffledOrder
                )
            })
        }
    }

    private var playingIdAtDragStart: String? = null
    private var indexAtDragStart: Int = -1

    fun startDragging() {
        indexAtDragStart = controller!!.currentMediaItemIndex
        playingIdAtDragStart = queue.value[controller!!.currentMediaItemIndex].queueId
    }

    fun finalizeMove(currentList: List<PlayQueueItemUUID>) {

        val newList =
            if (isShuffleEnabled.value) {
                currentList.mapIndexed { id, track ->
                    track.copy(
                        shuffledOrder = id,
                    )
                }
            } else {
                currentList.mapIndexed { id, track -> track.copy(originalOrder = id) }
            }

        updateQueue(newList)
        updatePlaybackSession()

    }

    fun moveTrack(fromIndex: Int, toIndex: Int) {
        controller!!.moveMediaItem(fromIndex, toIndex)
    }

    fun playNext(track: TrackInfo) {
        val nextIndex = controller!!.currentMediaItemIndex + 1

        val newTrack =
            PlayQueueItemUUID(track = track, originalOrder = nextIndex, shuffledOrder = nextIndex)

        controller!!.addMediaItem(nextIndex, toMediaItem(newTrack))
        val dbList = queue.value.map { track ->
            val newOriginal =
                if (track.originalOrder >= nextIndex) track.originalOrder + 1 else track.originalOrder
            val newShuffled =
                if (track.shuffledOrder >= nextIndex) track.shuffledOrder + 1 else track.shuffledOrder
            track.copy(originalOrder = newOriginal, shuffledOrder = newShuffled)
        }

        val newList = dbList.toMutableList().apply {
            add(
                nextIndex,
                newTrack
            )
        }
        updateQueue(newList)

        viewModelScope.launch {
            _eventChannel.send("Added ${track.title} to queue")
        }
    }

    fun addToQueue(track: TrackInfo) {
        val queueItem = PlayQueueItemUUID(
            track = track,
            originalOrder = controller!!.mediaItemCount - 1,
            shuffledOrder = controller!!.mediaItemCount - 1
        )

        val newList = queue.value.toMutableList().apply {
            add(
                queueItem
            )
        }

        controller!!.addMediaItem(toMediaItem(queueItem))

        updateQueue(newList)

        viewModelScope.launch {
            _eventChannel.send("Added ${track.title} to queue")
        }
    }

    fun playNextList(tracks: List<TrackInfo>) {
        val nextIndex = controller!!.currentMediaItemIndex + 1

        val dbList = queue.value.map { track ->
            val newOriginal =
                if (track.originalOrder >= nextIndex) track.originalOrder + tracks.size else track.originalOrder
            val newShuffled =
                if (track.shuffledOrder >= nextIndex) track.shuffledOrder + tracks.size else track.shuffledOrder
            track.copy(originalOrder = newOriginal, shuffledOrder = newShuffled)
        }

        val toAdd = tracks.mapIndexed { id, track ->
            PlayQueueItemUUID(
                track = track,
                originalOrder = nextIndex + id,
                shuffledOrder = nextIndex + id
            )
        }

        val newList = dbList.toMutableList().apply {
            addAll(
                nextIndex,
                toAdd
            )
        }


        controller!!.addMediaItems(nextIndex, toAdd.map { track -> toMediaItem(track) })

        updateQueue(newList)

        viewModelScope.launch {
            _eventChannel.send("Added ${tracks.size} tracks to queue")
        }
    }

    fun addToQueueList(tracks: List<TrackInfo>) {
        val originalCount = controller!!.mediaItemCount

        val toAdd = tracks.mapIndexed { id, track ->
            PlayQueueItemUUID(
                track = track,
                originalOrder = originalCount + id,
                shuffledOrder = originalCount + id
            )
        }


        val newList = queue.value.toMutableList().apply {
            addAll(
                toAdd
            )
        }


        controller!!.addMediaItems(toAdd.map { track -> toMediaItem(track) })

        updateQueue(newList)

        viewModelScope.launch {
            _eventChannel.send("Added ${tracks.size} tracks to queue")
        }

    }

    fun playNextListIds(trackIds: Set<Int>) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksByIds(trackIds)
            playNextList(tracks)
        }
    }

    fun addToQueueListIds(trackIds: Set<Int>) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksByIds(trackIds)
            addToQueueList(tracks)
        }
    }


    fun playNextAlbum(albumId: Int) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksInAlbum(albumId).first()

            playNextList(tracks)
        }
    }

    fun addToQueueAlbum(albumId: Int) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksInAlbum(albumId).first()

            addToQueueList(tracks)
        }
    }


    fun playNextArtist(artistId: Int) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksByArtist(artistId).first()

            playNextList(tracks)
        }
    }

    fun addToQueueArtist(artistId: Int) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksByArtist(artistId).first()

            addToQueueList(tracks)
        }
    }


    fun playTrack(queueId: String) {
        val trackIndex = queue.value.indexOfFirst { it.queueId == queueId }
        _currentTrack.value = queue.value[trackIndex]
        controller!!.seekTo(trackIndex, 0L)
        controller!!.play()
    }

    fun addToQueuePlaylist(playlistId: Int) {
        viewModelScope.launch {
            val tracks =
                playlistTracksRepository.getTracksInPlaylist(playlistId).map { it.trackInfo }

            addToQueueList(tracks)
        }
    }

    fun playNextPlaylist(playlistId: Int) {
        viewModelScope.launch {
            val tracks =
                playlistTracksRepository.getTracksInPlaylist(playlistId).map { it.trackInfo }

            playNextList(tracks)
        }
    }


    fun playPlaylist(playlistId: Int) {
        viewModelScope.launch {
            val tracks = playlistTracksRepository.getTracksInPlaylist(playlistId)
            val trackInfos = tracks.map { it.trackInfo }
            val selectedTrack = trackInfos[0]
            val startEntryId = tracks[0].entryId
            val entryIds = tracks.map { it.entryId }

            playTracks(trackInfos, selectedTrack, startEntryId, entryIds)
        }
    }


    fun playTracks(
        tracks: List<TrackInfo>,
        selectedTrack: TrackInfo,
        currentEntryId: Int? = null,
        entryIds: List<Int>? = null
    ) {
        val queueTracks = tracks.mapIndexed { id, track ->
            PlayQueueItemUUID(
                track = track,
                originalOrder = id,
                playlistEntryId = entryIds?.get(id)
            )
        }

        val mediaItems = queueTracks.map { track ->
            toMediaItem(track)
        }

        val startIndex =
            if (currentEntryId != null) queueTracks.indexOfFirst { it.playlistEntryId == currentEntryId }
            else tracks.indexOfFirst { it.trackId == selectedTrack.trackId }

        controller!!.setMediaItems(mediaItems)
        controller!!.prepare()
        controller!!.seekTo(startIndex, 0L)
        controller!!.play()

        _isPlaying.value = true
        _currentTrack.value = queueTracks[startIndex]
        updateQueue(
            queueTracks
        )

        viewModelScope.launch {
            playQueueRepository.updateShuffle(false)
        }
    }

    fun toMediaItem(queueItem: PlayQueueItemUUID): MediaItem {
        val mediaItem = MediaItem.Builder()
            .setUri(queueItem.track.fileUri.toUri())
            .setMediaId(queueItem.queueId)
            .setTag(queueItem.track.trackId)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setExtras(
                        bundleOf(
                            "ID" to queueItem.track.trackId,
                            "artistId" to queueItem.track.artistId,
                            "albumId" to queueItem.track.albumId,
                            "entryId" to queueItem.playlistEntryId
                        )
                    )
                    .setTitle(queueItem.track.title.toString())
                    .setArtist(queueItem.track.artistName.toString())
                    .setArtworkUri(queueItem.track.albumArt?.toUri())
                    .setDurationMs(queueItem.track.duration)
                    .setAlbumTitle(queueItem.track.albumTitle)
                    .build()
            )
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(queueItem.track.fileUri.toUri())
                    .build()
            )
            .build()

        return mediaItem
    }

    fun seekTo(positionMs: Long) {
        controller!!.seekTo(positionMs)
    }

    fun skipToNext() {
        controller!!.seekToNext()
    }

    fun skipToPrevious() {
        controller!!.seekToPrevious()
    }

    fun hasNextMediaItem(): Boolean? {
        return controller!!.hasNextMediaItem()
    }

    fun hasPrevMediaItem(): Boolean? {
        return controller!!.hasPreviousMediaItem()
    }

    fun removeFromQueue(uuids: Set<String>) {
        val currentList = queue.value.filterNot { it.queueId in uuids }

        viewModelScope.launch {
            val controller = controller ?: return@launch
            for (uuid in uuids) {
                val index = findCurrentIndexInController(controller, uuid)
                controller.removeMediaItem(index)
            }
        }

        val originalOrderLookup = currentList
            .sortedBy { it.originalOrder }
            .mapIndexed { index, track -> track.queueId to index }
            .toMap()

        val newList =
            if (isShuffleEnabled.value) {
                currentList.mapIndexed { id, track ->
                    track.copy(
                        shuffledOrder = id,
                        originalOrder = originalOrderLookup[track.queueId] ?: id
                    )
                }
            } else {
                currentList.mapIndexed { id, track -> track.copy(originalOrder = id) }
            }
        updateQueue(newList)
    }

    fun removeTrackAt(index: Int) {
        val currentList = queue.value.toMutableList()
        if (index in currentList.indices) {
            controller!!.removeMediaItem(index)
            currentList.removeAt(index)
            _currentTrack.value = currentList[controller!!.currentMediaItemIndex]

            val originalOrderLookup = currentList
                .sortedBy { it.originalOrder }
                .mapIndexed { index, track -> track.queueId to index }
                .toMap()

            val newList =
                if (isShuffleEnabled.value) {
                    currentList.mapIndexed { id, track ->
                        track.copy(
                            shuffledOrder = id,
                            originalOrder = originalOrderLookup[track.queueId] ?: id
                        )
                    }
                } else {
                    currentList.mapIndexed { id, track -> track.copy(originalOrder = id) }
                }
            updateQueue(newList)
        }
    }

    fun playShuffledPlaylist(tracks: List<PlaylistTrack>) {
        viewModelScope.launch {
            val tracksShuffled = tracks.shuffled()
            val newQueue = tracksShuffled.mapIndexed { index, it ->
                PlayQueueItemUUID(
                    originalOrder = it.position,
                    shuffledOrder = index,
                    playlistEntryId = it.entryId,
                    track = it.trackInfo
                )
            }

            val mediaItems = newQueue.map { track ->
                toMediaItem(track)
            }

            val startIndex = 0
            controller!!.setMediaItems(mediaItems)
            controller!!.prepare()
            controller!!.seekTo(startIndex, 0L)
            controller!!.play()

            _isPlaying.value = true
            _currentTrack.value = newQueue[startIndex]
            updateQueue(
                newQueue
            )

            playQueueRepository.updateShuffle(true)
        }
    }

    fun toggleShuffle() {

        viewModelScope.launch {
            val currentPlayingId = queue.value[controller!!.currentMediaItemIndex].queueId
            val newShuffleState = !isShuffleEnabled.value
            val freshQueue = withContext(Dispatchers.IO) {
                playQueueRepository.updateShuffle(newShuffleState)
                if (newShuffleState) {
                    playQueueRepository.shuffleQueue(currentPlayingId)
                }
                playQueueRepository.getCurrentQueue(newShuffleState).first()
            }

            val controller = controller ?: return@launch
            freshQueue.forEachIndexed { newIndex, item ->
                val oldIndex = findCurrentIndexInController(controller, item.uuid)
                if (oldIndex != newIndex) {
                    controller.moveMediaItem(oldIndex, newIndex)
                }
            }
            val startIndex = freshQueue.indexOfFirst { it.uuid == currentPlayingId }
            _currentTrack.value = PlayQueueItemUUID(
                queueId = freshQueue[startIndex].uuid,
                originalOrder = freshQueue[startIndex].orderIndex,
                shuffledOrder = freshQueue[startIndex].shuffledIndex ?: 0,
                track = freshQueue[startIndex].trackInfo
            )

            updatePlaybackSession()

        }
    }

    private fun findCurrentIndexInController(controller: MediaController, uuid: String): Int {
        for (i in 0 until controller.mediaItemCount) {
            if (controller.getMediaItemAt(i).mediaId == uuid) {
                return i
            }
        }
        return -1
    }

    fun toggleRepeat() {
        viewModelScope.launch {
            val newMode = when (controller!!.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
            controller!!.repeatMode = newMode
            playQueueRepository.updateRepeat(newMode)
        }

    }

}
