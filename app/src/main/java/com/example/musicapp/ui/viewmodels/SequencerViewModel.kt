package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.model.BlockWithTracks
import com.example.musicapp.data.local.model.CompatibleTrack
import com.example.musicapp.data.repository.SequencerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

    val selectedBlock: StateFlow<BlockWithTracks?> = combine(_selectedBlockNumber, uiBlocks) { number, blocks ->
        if (number == null) null else blocks.find { it.blockNumber == number }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

//    @OptIn(ExperimentalCoroutinesApi::class)
//    private val validCandidates: Flow<List<Int>> = _findPrev.flatMapLatest { isLookingBack ->
//        if (isLookingBack) {
//            sequencerRepository.getLastTracksInBlock()
//        } else {
//            sequencerRepository.getFirstTracksInBlock()
//        }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val compatibleTracks: StateFlow<List<CompatibleTrack>> = combine(
        selectedBlock,
        _findPrev,
        _bpmTolerance,
        _loudnessTolerance
    ) {
        block, prev, bpm, loudness ->
        CompatibilitySettings(block, prev, bpm, loudness)
    }.flatMapLatest { compatibilitySettings ->
        if (compatibilitySettings.block == null) flowOf(emptyList())
        else sequencerRepository.getCompatible(compatibilitySettings.block,
                                            playlistId,
                                            compatibilitySettings.findPrev,
                                            compatibilitySettings.bpmTolerance,
                                            compatibilitySettings.loudnessTolerance)
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
    ) {
            block, prev, bpm, loudness ->
        CompatibilitySettings(block, prev, bpm, loudness)
    }.flatMapLatest { compatibilitySettings ->
        if (compatibilitySettings.block == null) flowOf(emptyList())
        else sequencerRepository.getIncompatible(compatibilitySettings.block,
            playlistId,
            compatibilitySettings.findPrev,
            compatibilitySettings.bpmTolerance,
            compatibilitySettings.loudnessTolerance)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    init{
        viewModelScope.launch {
            sequencerRepository.setUpSequencer(playlistId)
        }
    }

    fun selectBlock(block: BlockWithTracks) {
        _selectedBlockNumber.value = block.blockNumber
    }


    fun onReorderBlocks(reorderedList: List<BlockWithTracks>) {
        viewModelScope.launch {
            sequencerRepository.reorder(reorderedList)
        }
    }

    fun onSave(){
        viewModelScope.launch {
            sequencerRepository.saveNewOrder(playlistId)
        }
    }

    fun onDiscard(){
        viewModelScope.launch {
            sequencerRepository.clearSequencer()
        }
    }


    fun onMerge(startBlock: Int, goalBlock: Int){
        viewModelScope.launch {
            sequencerRepository.mergeBlocks(startBlock, goalBlock, _findPrev.value)
            _selectedBlockNumber.value = if (startBlock < goalBlock) goalBlock-1 else goalBlock
        }
    }

    fun onSplit(block: Int, splitIndex: Int){
        viewModelScope.launch {
            sequencerRepository.splitBlock(block, splitIndex)
        }
    }

    fun setDirection(lookBack: Boolean){
        _findPrev.value = lookBack
    }

    fun updateBPMTolerance(newValue: Int){
        _bpmTolerance.value = newValue
    }


    fun updateLoudnessTolerance(newValue: Float){
        _loudnessTolerance.value = newValue
    }

}

data class CompatibilitySettings(
    val block: BlockWithTracks?,
    val findPrev: Boolean,
    val bpmTolerance: Int,
    val loudnessTolerance: Float
)