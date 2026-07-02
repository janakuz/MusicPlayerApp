package com.example.musicapp.data.repository

import android.util.Log
import com.example.musicapp.data.local.dao.PlaylistTracksDao
import com.example.musicapp.data.local.dao.SequencerDao
import com.example.musicapp.data.local.entity.PlaylistTracks
import com.example.musicapp.data.local.entity.SequencerBlock
import com.example.musicapp.data.local.model.BlockWithTracks
import com.example.musicapp.data.local.model.CompatibleTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SequencerRepositoryImpl(
    private val sequencerDao: SequencerDao,
    private val playlistTracksDao: PlaylistTracksDao
) : SequencerRepository {
    override fun getCompatible(
        block: BlockWithTracks,
        playlistId: Int,
        findPrev: Boolean,
    ): Flow<List<CompatibleTrack>> {
        val trackId = if (findPrev) block.tracks.first().trackId else block.tracks.last().trackId

        return sequencerDao.getCompatibleTracks(trackId, playlistId, block.blockNumber)
    }

    override fun getLastTracksInBlock(): Flow<List<Int>> {
        return sequencerDao.getLastTracksInBlock()
    }

    override fun getFirstTracksInBlock(): Flow<List<Int>> {
        return sequencerDao.getFirstTracksInBlock()
    }

    override suspend fun setUpSequencer(playlistId: Int) {
        sequencerDao.createScratchpad(playlistId)
    }

    override suspend fun clearSequencer() {
        sequencerDao.clearScratchpad()
    }

    override suspend fun saveNewOrder(playlsitId: Int) {
        val currentBlocks = sequencerDao.getAll()
        val newOrder = currentBlocks.mapIndexed { index, track ->
            PlaylistTracks(
                playlistId = playlsitId,
                trackId = track.trackId,
                position = index
            )
        }
        playlistTracksDao.replacePlaylistOrder(playlsitId, newOrder)
        sequencerDao.clearScratchpad()
    }

    override fun getBlocks(): Flow<List<BlockWithTracks>> {
        return sequencerDao.getAllBlocks().map { flatRows ->
            flatRows
                .groupBy { it.blockNumber }
                .map { (blockNum, rowsForThisBlock) ->
                    BlockWithTracks(
                        blockNumber = blockNum,
                        tracks = rowsForThisBlock.map { it.trackInfo }
                    )
                }
                .sortedBy { it.blockNumber }
        }
    }

    override suspend fun mergeBlocks(startBlock: Int, goalBlock: Int) {
        sequencerDao.mergeBlocks(startBlock, goalBlock)
    }

    override suspend fun splitBlock(startBlock: Int, splitIndex: Int) {
        sequencerDao.splitBlocks(startBlock, splitIndex)
    }

    override suspend fun reorder(reordered: List<BlockWithTracks>) {
        val newBlocks = reordered.flatMapIndexed { index, block ->
            block.tracks.mapIndexed { trackIndex, trackInfo ->
                SequencerBlock(
                    blockNumber = index,
                    blockOrder = trackIndex,
                    trackId = trackInfo.trackId
                )
            }
        }
        sequencerDao.reorder(newBlocks)
    }

}