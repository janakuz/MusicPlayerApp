package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.SequencerBlock
import com.example.musicapp.data.local.model.BlockWithTracks
import com.example.musicapp.data.local.model.CompatibleTrack
import kotlinx.coroutines.flow.Flow

interface SequencerRepository {

    fun getCompatible(block: BlockWithTracks, playlistId: Int, findPrev: Boolean = false, valid: List<Int>): Flow<List<CompatibleTrack>>

    fun getLastTracksInBlock(): Flow<List<Int>>

    fun getFirstTracksInBlock(): Flow<List<Int>>

    suspend fun setUpSequencer(playlistId: Int)

    suspend fun clearSequencer()

    suspend fun saveNewOrder(playlistId: Int)

    fun getBlocks(): Flow<List<BlockWithTracks>>

    suspend fun mergeBlocks(startBlock: Int, goalBlock: Int)

    suspend fun splitBlock(startBlock: Int, splitIndex: Int)

    suspend fun reorder(reordered: List<BlockWithTracks>)

}