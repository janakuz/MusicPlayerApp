package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.MoodDao
import com.example.musicapp.data.dao.TrackMoodDao
import com.example.musicapp.data.entity.Genre
import com.example.musicapp.data.entity.Mood
import com.example.musicapp.normalizeGenre

class OfflineTrackMoodRepository(
    private val trackMoodDao: TrackMoodDao,
    private val moodDao: MoodDao
) : TrackMoodRepository {

    override suspend fun updateTrackMoods(
        trackId: Int,
        moods: List<String>
    ) {
        val moodIds = moods.map { name ->
            val normalized = name.normalizeGenre()
            moodDao.getMoodByName(normalized)?.id ?:
            moodDao.insert(Mood(name = normalized)).toInt()
        }

        trackMoodDao.syncMoods(trackId, moodIds)
    }

    override suspend fun getTrackMoods(trackId: Int): List<String> {
        return trackMoodDao.getTrackMoods(trackId)
    }
}