package com.example.musicapp.ui.viewmodels

import android.content.Context
import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.data.local.model.BlockWithTracks
import com.example.musicapp.data.local.model.CompatibleTrack
import com.example.musicapp.data.repository.SequencerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SequencerViewModel @Inject constructor(
    private val sequencerRepository: SequencerRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val playlistId: Int = savedStateHandle.get<Int>("playlistId")?.toInt() ?: -1

    val uiBlocks: StateFlow<List<BlockWithTracks>> = sequencerRepository.getBlocks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _findPrev = MutableStateFlow(false)
    val findPrev = _findPrev.asStateFlow()

    private val _bpmTolerance = MutableStateFlow(10)
    val bpmTolerance = _bpmTolerance.asStateFlow()

    private val _loudnessTolerance = MutableStateFlow(2.5F)
    val loudnessTolerance = _loudnessTolerance.asStateFlow()

    private val _selectedBlockNumber = MutableStateFlow<Int?>(null)

    val selectedBlock: StateFlow<BlockWithTracks?> =
        combine(_selectedBlockNumber, uiBlocks) { number, blocks ->
            if (number == null) null else blocks.find { it.blockNumber == number }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val compatibleTracks: StateFlow<List<CompatibleTrack>> = combine(
        selectedBlock,
        _findPrev,
        _bpmTolerance,
        _loudnessTolerance
    ) { block, prev, bpm, loudness ->
        CompatibilitySettings(block, prev, bpm, loudness)
    }.flatMapLatest { compatibilitySettings ->
        if (compatibilitySettings.block == null) flowOf(emptyList())
        else sequencerRepository.getCompatible(
            compatibilitySettings.block,
            playlistId,
            compatibilitySettings.findPrev,
            compatibilitySettings.bpmTolerance,
            compatibilitySettings.loudnessTolerance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val incompatibleTracks: StateFlow<List<CompatibleTrack>> = combine(
        selectedBlock,
        _findPrev,
        _bpmTolerance,
        _loudnessTolerance
    ) { block, prev, bpm, loudness ->
        CompatibilitySettings(block, prev, bpm, loudness)
    }.flatMapLatest { compatibilitySettings ->
        if (compatibilitySettings.block == null) flowOf(emptyList())
        else sequencerRepository.getIncompatible(
            compatibilitySettings.block,
            playlistId,
            compatibilitySettings.findPrev,
            compatibilitySettings.bpmTolerance,
            compatibilitySettings.loudnessTolerance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    private var previewPlayer: ExoPlayer? = null

    var previewState by mutableStateOf<Int?>(null)
        private set

    private var previewTimer: CountDownTimer? = null

    init {
        viewModelScope.launch {
            sequencerRepository.setUpSequencer(playlistId)
        }
        previewPlayer = ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    fun selectBlock(block: Int) {
        _selectedBlockNumber.value = block
    }

    fun onSave() {
        viewModelScope.launch {
            sequencerRepository.saveNewOrder(playlistId)
        }
    }

    fun onDiscard() {
        viewModelScope.launch {
            sequencerRepository.clearSequencer()
        }
    }


    fun onMerge(startBlock: Int, goalBlock: Int) {
        viewModelScope.launch {
            sequencerRepository.mergeBlocks(startBlock, goalBlock, _findPrev.value)
            _selectedBlockNumber.value = if (startBlock < goalBlock) goalBlock - 1 else goalBlock
        }
    }

    fun onSplit(block: Int, splitIndex: Int) {
        viewModelScope.launch {
            sequencerRepository.splitBlock(block, splitIndex)
        }
    }

    fun setDirection(lookBack: Boolean) {
        _findPrev.value = lookBack
    }

    fun updateBPMTolerance(newValue: Int) {
        _bpmTolerance.value = newValue
    }


    fun updateLoudnessTolerance(newValue: Float) {
        _loudnessTolerance.value = newValue
    }

    fun reorder(reordered: List<BlockWithTracks>) {
        viewModelScope.launch {
            sequencerRepository.reorder(reordered)
        }
    }




    fun togglePreview(trackId: Int, audioUrl: String, isSuggestedTrack: Boolean) {
        if (previewState == trackId) {
            stopPreview()
            return
        }

        stopPreview()

        previewState = trackId

        val mediaItem = MediaItem.fromUri(audioUrl)
        previewPlayer?.setMediaItem(mediaItem)
        previewPlayer?.prepare()

        if (isSuggestedTrack) {
            previewPlayer?.seekTo(0)
            startPreviewCutoffTimer(15000)
        } else {
            previewPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val duration = previewPlayer?.duration ?: 0L
                        if (duration > 15000L) {
                            previewPlayer?.seekTo(duration - 15000L)
                        } else {
                            previewPlayer?.seekTo(0)
                        }
                        previewPlayer?.removeListener(this)
                        startPreviewCutoffTimer(15000)
                    }
                }
            })
        }
    }

    private fun startPreviewCutoffTimer(durationMs: Long) {
        previewTimer?.cancel()
        previewTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                stopPreview()
            }
        }.start()
    }

    fun stopPreview() {
        previewTimer?.cancel()
        previewPlayer?.stop()
        previewState = null
    }

    override fun onCleared() {
        super.onCleared()
        previewTimer?.cancel()
        previewPlayer?.release()
        previewPlayer = null
    }

}

data class CompatibilitySettings(
    val block: BlockWithTracks?,
    val findPrev: Boolean,
    val bpmTolerance: Int,
    val loudnessTolerance: Float
)