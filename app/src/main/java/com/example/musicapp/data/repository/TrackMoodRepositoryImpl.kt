package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.MoodDao
import com.example.musicapp.data.local.dao.TrackMoodDao
import com.example.musicapp.data.local.entity.Mood
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
}