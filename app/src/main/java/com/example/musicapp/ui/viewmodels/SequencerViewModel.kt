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
    val compatibleTracks: StateFlow<List<CompatibleTrack>> = selectedBlock.flatMapLatest { currentBlock ->
        if (currentBlock == null) flowOf(emptyList())
        else sequencerRepository.getCompatible(currentBlock, playlistId, false)
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
            sequencerRepository.mergeBlocks(startBlock, goalBlock)
            _selectedBlockNumber.value = if (startBlock < goalBlock) goalBlock-1 else goalBlock
        }
    }

    fun onSplit(block: Int, splitIndex: Int){
        viewModelScope.launch {
            sequencerRepository.splitBlock(block, splitIndex)
        }
    }
}