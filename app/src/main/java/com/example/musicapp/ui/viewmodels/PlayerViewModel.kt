package com.example.musicapp.ui.viewmodels

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import com.example.musicapp.PlaybackService
import com.example.musicapp.data.dto.PlayQueueItemUUID
import com.example.musicapp.data.dto.QueueItemFull
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.entity.QueueItem
import com.example.musicapp.data.repository.PlayQueueRepository
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.ui.screens.PlayQueuePreview
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.multibindings.ElementsIntoSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class PlayerViewModel @Inject constructor (
    @ApplicationContext private val context: Context,
    private val playQueueRepository: PlayQueueRepository,
    private val trackRepository: TrackRepository,
) : ViewModel() {
//    private val mediaController = MutableStateFlow<MediaController?>(null)
//    val controller: StateFlow<MediaController?> = mediaController.asStateFlow()

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
    val queue: StateFlow<List<PlayQueueItemUUID>> = playQueueRepository.shuffleOn.flatMapLatest { shuffle ->
        playQueueRepository.getCurrentQueue(shuffle)
            .map { q -> q.map {  toQueueItem(it) } }

        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList<PlayQueueItemUUID>()
        )

    private val _currentTrack = MutableStateFlow<TrackInfo?>(null)
    val currentTrack: StateFlow<TrackInfo?> = _currentTrack.asStateFlow()

    private val _draftQueue = MutableStateFlow<List<PlayQueueItemUUID>>(emptyList())
    val draftQueue = _draftQueue.asStateFlow()

    //    val currentTrack: StateFlow<TrackInfo?> = combine(queue, playQueueRepository.currentSession) { currentQueue, session ->
//        if (currentQueue.isEmpty() || session.playQueueIndex !in currentQueue.indices) {
//            null
//        } else {
//            currentQueue[session.playQueueIndex].track
//        }
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5_000),
//        initialValue = null
//    )

//    @OptIn(ExperimentalCoroutinesApi::class)
//    val position: StateFlow<Long> = controller.flatMapLatest { controller ->
//        if (controller == null || controller.currentMediaItem==null) {
//            playQueueRepository.currentSession.map { it.position }
//        } else {
//            tickerFlow(500).map { controller.currentPosition }
//        }
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5_000),
//        initialValue = 0L
//    )

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
    val position: StateFlow<Long> =  hasActivePlayback.flatMapLatest { hasPlayback ->
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
        track?.duration ?: 0L
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0L
    )


    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()


    init {
        viewModelScope.launch {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()


            controllerFuture.addListener({
                controller = controllerFuture.get()
 //               val controller = controllerFuture.get()
 //               mediaController.value = controller

                val c = controller!!

                c.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem.let {
//                            _currentTrack.value = mediaItem?.localConfiguration?.tag as? TrackInfo
                            _currentTrack.value = TrackInfo(
                                title = mediaItem?.mediaMetadata?.title.toString(),
                                trackId = mediaItem?.mediaMetadata?.extras?.getInt("ID") ?: 0,//.mediaId?.toInt() ?: 0,
                                artistName = mediaItem?.mediaMetadata?.artist.toString(),
                                albumTitle = mediaItem?.mediaMetadata?.albumTitle.toString(),
                                albumArt = mediaItem?.mediaMetadata?.artworkUri.toString(),
                                trackNum = mediaItem?.mediaMetadata?.trackNumber,
                                duration = mediaItem?.mediaMetadata?.durationMs ?: 0L,
                                fileUri = mediaItem?.requestMetadata?.mediaUri.toString(),
                            )
                        }
        //                _currentTrack.value = queue.value[c.currentMediaItemIndex].track
                        if (c.repeatMode != Player.REPEAT_MODE_ONE) updatePlaybackSession()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        this@PlayerViewModel._isPlaying.value = isPlaying

                        if (!isPlaying) updatePlaybackSession()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("PlayerError",error.errorCode.toString()+ " " + error.message)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d("player", playbackState.toString())
                    }

                }
                )

                viewModelScope.launch {
                    queue.collect { newQueue ->
          //              val controller = mediaController.value ?: return@collect

                        if (controller!!.mediaItemCount == 0 && newQueue.isNotEmpty()) {
                            val mediaItems = newQueue.map { toMediaItem(it) }
                            controller!!.setMediaItems(mediaItems)

                            val session = playQueueRepository.currentSession.first()
                            controller!!.prepare()
                            controller!!.seekTo(session.playQueueIndex, session.position)
                            _currentTrack.value = queue.value[session.playQueueIndex].track
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


    fun syncDraft() {
        _draftQueue.value = queue.value
    }

    fun togglePlayback(){
//        val controller = mediaController.value ?: return
        if (controller!!.isPlaying) {
            controller!!.pause()
        } else {
            controller!!.play()
        }

    }


    private fun updatePlaybackSession() {
//        val controller = mediaController.value ?: return
        val currentIndex = controller!!.currentMediaItemIndex
        val currentPosition = controller!!.currentPosition

        viewModelScope.launch (Dispatchers.IO) {
            playQueueRepository.saveSession(
                index = currentIndex,
                position = currentPosition
            )
        }
    }

    fun updateQueue(newQueue: List<PlayQueueItemUUID>){
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

    fun moveVisible(fromIndex: Int, toIndex: Int){
        val currentList = _draftQueue.value.toMutableList()
        val trackToMove = currentList.removeAt(fromIndex)
        val newTrack =
            if (isShuffleEnabled.value) trackToMove.copy(shuffledOrder = toIndex)
            else trackToMove.copy(originalOrder = toIndex)
        currentList.add(toIndex, newTrack)


    }

    private var playingIdAtDragStart: String? = null
    private var indexAtDragStart: Int = -1

    fun startDragging() {
//        val controller = mediaController.value ?: return
        indexAtDragStart = controller!!.currentMediaItemIndex
        playingIdAtDragStart = queue.value[controller!!.currentMediaItemIndex].queueId
    }

    fun finalizeMove(currentList: List<PlayQueueItemUUID>){

        val newList =
            if (isShuffleEnabled.value){
                currentList.mapIndexed { id, track ->
                    track.copy(
                        shuffledOrder = id,
                    )}
            }
            else {
                currentList.mapIndexed { id, track -> track.copy(originalOrder = id) }
            }

        updateQueue(newList)
        updatePlaybackSession()

    }

    fun moveTrack(fromIndex: Int, toIndex: Int) {
        controller!!.moveMediaItem(fromIndex, toIndex)
    }

    fun playNext(track: TrackInfo) {
//        val controller = mediaController.value ?: return
        val nextIndex = controller!!.currentMediaItemIndex + 1

        val newTrack = PlayQueueItemUUID(track = track, originalOrder = nextIndex, shuffledOrder = nextIndex)

        controller!!.addMediaItem(nextIndex, toMediaItem(newTrack))
        val dbList = queue.value.map { track ->
            val newOriginal = if (track.originalOrder >= nextIndex) track.originalOrder+1 else track.originalOrder
            val newShuffled = if (track.shuffledOrder >= nextIndex) track.shuffledOrder+1 else track.shuffledOrder
            track.copy(originalOrder = newOriginal, shuffledOrder = newShuffled)
        }

        val newList = dbList.toMutableList().apply {
            add(
                nextIndex,
                newTrack
            ) }
        updateQueue(newList)
    }

    fun addToQueue(track: TrackInfo) {
//        val controller = mediaController.value ?: return

        val queueItem = PlayQueueItemUUID(
            track = track,
            originalOrder = controller!!.mediaItemCount-1,
            shuffledOrder = controller!!.mediaItemCount-1
        )

        val newList = queue.value.toMutableList().apply {
            add(
                queueItem
            )
        }

        controller!!.addMediaItem(toMediaItem(queueItem))

        updateQueue(newList)
    }

    fun playNextList(tracks: List<TrackInfo>) {
//        val controller = mediaController.value ?: return
        val nextIndex = controller!!.currentMediaItemIndex + 1

        val dbList = queue.value.map { track ->
            val newOriginal = if (track.originalOrder >= nextIndex) track.originalOrder+tracks.size else track.originalOrder
            val newShuffled = if (track.shuffledOrder >= nextIndex) track.shuffledOrder+tracks.size else track.shuffledOrder
            track.copy(originalOrder = newOriginal, shuffledOrder = newShuffled)
        }

        val toAdd = tracks.mapIndexed { id, track ->
            PlayQueueItemUUID(
                track = track,
                originalOrder = nextIndex+id,
                shuffledOrder = nextIndex+id
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
    }

    fun addToQueueList(tracks: List<TrackInfo>){
//        val controller = mediaController.value ?: return
        val originalCount = controller!!.mediaItemCount

        val toAdd = tracks.mapIndexed { id, track ->
            PlayQueueItemUUID(
                track = track,
                originalOrder = originalCount+id,
                shuffledOrder = originalCount+id
            )
        }


        val newList = queue.value.toMutableList().apply {
            addAll(
                toAdd
            )
        }


        controller!!.addMediaItems(toAdd.map { track -> toMediaItem(track) })

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


    fun playTrack(queueId: String){
//        val controller = mediaController.value ?: return
        val trackIndex = queue.value.indexOfFirst { it.queueId==queueId }
        _currentTrack.value = queue.value[trackIndex].track
        controller!!.seekTo(trackIndex, 0L)
        controller!!.play()
    }


    fun playTracks(tracks: List<TrackInfo>, selectedTrack: TrackInfo){
        val queueTracks = tracks.mapIndexed { id, track ->
            PlayQueueItemUUID(
                track = track,
                originalOrder = id)
        }

        val mediaItems = queueTracks.map { track ->
            toMediaItem(track)
        }

        val startIndex = tracks.indexOfFirst { it.trackId == selectedTrack.trackId }

        controller!!.setMediaItems(mediaItems)
        controller!!.prepare()
        controller!!.seekTo(startIndex, 0L)
        controller!!.play()

        _isPlaying.value = true
        _currentTrack.value = selectedTrack
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
                            "ID" to queueItem.track.trackId
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
//        mediaController.value?.seekTo(positionMs)
        controller!!.seekTo(positionMs)
    }

    fun skipToNext() {
//        mediaController.value?.seekToNext()
        controller!!.seekToNext()
    }

    fun skipToPrevious() {
//        mediaController.value?.seekToPrevious()
        controller!!.seekToPrevious()
    }

    fun hasNextMediaItem() : Boolean? {
//        return mediaController.value?.hasNextMediaItem()
        return controller!!.hasNextMediaItem()
    }

    fun hasPrevMediaItem() : Boolean? {
//        return mediaController.value?.hasPreviousMediaItem()
        return controller!!.hasPreviousMediaItem()
    }

    fun removeTrackAt(index: Int) {
//        val controller = mediaController.value ?: return
        val currentList = queue.value.toMutableList()
        if (index in currentList.indices) {
            controller!!.removeMediaItem(index)
            currentList.removeAt(index)
            _currentTrack.value = currentList[controller!!.currentMediaItemIndex].track

            val originalOrderLookup = currentList
                .sortedBy { it.originalOrder }
                .mapIndexed { index, track -> track.queueId to index }
                .toMap()

            val newList =
                if (isShuffleEnabled.value){
                    currentList.mapIndexed { id, track ->
                        track.copy(
                            shuffledOrder = id,
                            originalOrder = originalOrderLookup[track.queueId] ?: id
                        )}
                }
                else {
                    currentList.mapIndexed { id, track -> track.copy(originalOrder = id) }
                }
            updateQueue(newList)
        }
    }

    fun toggleShuffle(){

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
                if (oldIndex != newIndex){
                    controller.moveMediaItem(oldIndex, newIndex)
                }
            }
            val startIndex = freshQueue.indexOfFirst { it.uuid==currentPlayingId }
            _currentTrack.value = freshQueue[startIndex].trackInfo
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

    fun toggleRepeat(){
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
