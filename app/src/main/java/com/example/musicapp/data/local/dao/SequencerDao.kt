package com.example.musicapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.musicapp.data.local.entity.SequencerBlock
import com.example.musicapp.data.local.model.BlockWithTrackInfo
import com.example.musicapp.data.local.model.BlockWithTracks
import com.example.musicapp.data.local.model.CompatibleTrack
import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

@Dao
interface SequencerDao {

    @Query("""
        INSERT INTO sequencer_blocks(blockNumber, blockOrder, trackId)
        SELECT pt.position, 0, pt.trackId
        FROM playlist_tracks pt
        WHERE pt.playlistId = :playlistId
    """)
    suspend fun createScratchpad(playlistId: Int)

    @Query("DELETE FROM sequencer_blocks")
    suspend fun clearScratchpad()

    @Query("SELECT * FROM sequencer_blocks ORDER BY blockNumber, blockOrder")
    suspend fun getAll(): List<SequencerBlock>


    @Query("""
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, al.image as albumArt, t.trackNumber as trackNum, 
                t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId,
                t.instrumental, t.voice, t.bpm, t.`key`, kc.matchDescription, t.bpm - st.bpm as tempoDifference, t.loudness - st.loudness as loudnessDifference,
                sb.blockNumber as currentBlock, false as wrongBPM, false as wrongKey, false as wrongLoudness,
                (SELECT COUNT(*) 
                FROM sequencer_blocks sb2 
                WHERE sb2.blockNumber = sb.blockNumber) > 1 as inMultiTrackBlock
        FROM tracks st
        JOIN key_compatibility kc ON st.`key` = kc.sourceKey
        JOIN tracks t ON t.`key` = kc.compatibleKey
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        JOIN playlist_tracks pt ON pt.trackId=t.id
        JOIN sequencer_blocks sb on sb.trackId=t.id
        JOIN sequencer_blocks ssb ON ssb.trackId=st.id
        WHERE st.id = :sourceTrackId AND pt.playlistId=:playlistId 
            AND t.id != :sourceTrackId
            AND CASE WHEN :lookBack = false THEN sb.blockOrder = 0 ELSE sb.blockOrder = (SELECT MAX(blockOrder) FROM sequencer_blocks sb3 WHERE sb3.blockNumber=sb.blockNumber) END
            AND ssb.blockNumber != sb.blockNumber AND :sourceBlock != sb.blockNumber
            AND t.bpm BETWEEN (st.bpm - :bpmTolerance) AND (st.bpm + :bpmTolerance) 
            AND t.loudness BETWEEN (st.loudness - :loudnessTolerance) AND (st.loudness + :loudnessTolerance)
        GROUP BY currentBlock, t.id
        ORDER BY ABS(kc.harmonicDistance) ASC
    """)
    fun getCompatibleTracks(
        sourceTrackId: Int,
        playlistId: Int,
        sourceBlock: Int,
        lookBack: Boolean = false,
        bpmTolerance: Int = 10,
        loudnessTolerance: Float = 2.5F
    ): Flow<List<CompatibleTrack>>


    @Query(
        """
        SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, al.image as albumArt, t.trackNumber as trackNum, 
                t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId,
                t.instrumental, t.voice, t.bpm, t.`key`, 
                COALESCE(
                       (SELECT kc.matchDescription 
                        FROM key_compatibility kc 
                        JOIN tracks st ON st.`key`=kc.sourceKey 
                        WHERE kc.compatibleKey=t.`key` and st.id=:sourceTrackId), "Incompatible Key") as matchDescription, 
                t.bpm - (SELECT bpm FROM tracks st WHERE st.id=:sourceTrackId) as tempoDifference, 
                t.loudness - (SELECT loudness FROM tracks st WHERE st.id=:sourceTrackId) as loudnessDifference,
                sb.blockNumber as currentBlock, 
                ABS(t.bpm - (SELECT bpm FROM tracks st WHERE st.id=:sourceTrackId)) > :bpmTolerance as wrongBPM, 
                t.`key` NOT IN (SELECT kc.compatibleKey FROM key_compatibility kc JOIN tracks st ON st.`key`=kc.sourceKey WHERE st.id=:sourceTrackId) as wrongKey, 
                ABS(t.loudness - (SELECT loudness FROM tracks st WHERE st.id=:sourceTrackId)) > :loudnessTolerance as wrongLoudness,
                (SELECT COUNT(*) 
                FROM sequencer_blocks sb2 
                WHERE sb2.blockNumber = sb.blockNumber) > 1 as inMultiTrackBlock
        FROM tracks t
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id
        JOIN playlist_tracks pt ON pt.trackId=t.id
        JOIN sequencer_blocks sb on sb.trackId=t.id
        WHERE pt.playlistId=:playlistId 
            AND t.id != :sourceTrackId
            AND CASE WHEN :lookBack = false THEN sb.blockOrder = 0 ELSE sb.blockOrder = (SELECT MAX(blockOrder) FROM sequencer_blocks sb3 WHERE sb3.blockNumber=sb.blockNumber) END
            AND :sourceBlock != sb.blockNumber
        GROUP BY currentBlock, t.id
        """
    )
    fun getIncompatibleAvailableTracks(
        sourceTrackId: Int,
        playlistId: Int,
        sourceBlock: Int,
        lookBack: Boolean = false,
        bpmTolerance: Int = 10,
        loudnessTolerance: Float = 2.5F
    ): Flow<List<CompatibleTrack>>

    @Query("SELECT trackid FROM sequencer_blocks GROUP BY blockNumber HAVING MAX(blockOrder)")
    fun getLastTracksInBlock(): Flow<List<Int>>

    @Query("SELECT trackid FROM sequencer_blocks WHERE blockOrder=0")
    fun getFirstTracksInBlock(): Flow<List<Int>>


    @Query("""
        SELECT sb.blockNumber, t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, al.image as albumArt, t.trackNumber as trackNum, 
                t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId,
                t.instrumental, t.voice, t.bpm, t.`key`
        FROM sequencer_blocks sb
        JOIN tracks t ON sb.trackId=t.id
        JOIN artists ar on t.artistId=ar.id
        JOIN albums al on t.albumId=al.id 
        ORDER BY sb.blockNumber, sb.blockOrder
    """)
    fun getAllBlocks(): Flow<List<BlockWithTrackInfo>>

    @Update
    suspend fun reorder(reordered: List<SequencerBlock>)

    @Query("SELECT MAX(blockOrder) FROM sequencer_blocks WHERE blockNumber=:blockNUmber")
    suspend fun getMaxOrder(blockNUmber: Int): Int

    @Query("UPDATE sequencer_blocks SET blockNumber = blockNumber - 1 WHERE blockNumber >= :startBlock")
    suspend fun shiftPositionsUp(startBlock: Int)

    @Query("UPDATE sequencer_blocks SET blockNumber = :goalBlock, blockOrder = blockOrder + :startOrder WHERE blockNumber = :startBlock")
    suspend fun moveToBlock(startBlock: Int, goalBlock: Int, startOrder: Int)

    @Transaction
    suspend fun mergeBlocks(startBlock: Int, goalBlock: Int){
        val maxOrderGoal = getMaxOrder(goalBlock)
        moveToBlock(startBlock, goalBlock, maxOrderGoal+1)
        shiftPositionsUp(startBlock+1)
    }

    @Query("UPDATE sequencer_blocks SET blockNumber = blockNumber + 1 WHERE blockNumber >= :startBlock")
    suspend fun shiftPositionsDown(startBlock: Int)


    @Query("UPDATE sequencer_blocks SET blockNumber = blockNumber + 1, blockOrder = blockOrder - :splitIndex WHERE blockNumber = :blockNumber")
    suspend fun splitToBlock(blockNumber: Int, splitIndex: Int)

    @Transaction
    suspend fun splitBlocks(blockNumber: Int, splitIndex: Int){
        shiftPositionsDown(blockNumber+1)
        splitToBlock(blockNumber, splitIndex)
    }


}