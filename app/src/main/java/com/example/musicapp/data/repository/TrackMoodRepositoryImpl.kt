package com.example.musicapp.data.repository

import android.util.Log
import androidx.room.util.splitToIntList
import com.example.musicapp.data.local.dao.MoodDao
import com.example.musicapp.data.local.dao.TrackMoodDao
import com.example.musicapp.data.local.entity.Mood
import com.example.musicapp.data.local.entity.TrackMood
import com.example.musicapp.util.normalizeGenre

class TrackMoodRepositoryImpl(
    private val trackMoodDao: TrackMoodDao,
    private val moodDao: MoodDao
) : TrackMoodRepository {

    override suspend fun updateTrackMoods(
        trackId: Int,
        moods: List<String>
    ) {
        val moodIds = moods.map { name ->
            val normalized = name.normalizeGenre()
            moodDao.getMoodByName(normalized)?.id ?: moodDao.insert(Mood(name = normalized)).toInt()
        }

        trackMoodDao.syncMoods(trackId, moodIds)
    }

    override suspend fun getTrackMoods(trackId: Int): List<String> {
        return trackMoodDao.getTrackMoods(trackId)
    }

    override suspend fun addTrackMoods(
        trackId: Int,
        moods: List<String>
    ) {
        moods.forEach { mood ->
            moodDao.insert(Mood(name = mood.normalizeGenre()))
        }

        val moodIds = moods.map { mood ->
            moodDao.getMoodByName(mood.normalizeGenre())?.id
        }.filter { it != null }

        val toInsert = moodIds.map { moodId ->
            TrackMood(trackId = trackId, moodId = moodId!!)
        }

        if (toInsert.isNotEmpty())
            trackMoodDao.insertAll(toInsert)
    }
}