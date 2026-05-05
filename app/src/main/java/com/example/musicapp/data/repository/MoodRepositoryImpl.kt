package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.MoodDao
import com.example.musicapp.data.local.entity.Mood
import com.example.musicapp.util.normalizeGenre
import kotlinx.coroutines.flow.Flow

class MoodRepositoryImpl(
    private val moodDao: MoodDao
) : MoodRepository {
    override suspend fun getOrCreateMood(name: String): Int {
        val normalized = name.normalizeGenre()
        val existingMood = moodDao.getMoodByName(normalized)
        return existingMood?.id ?: moodDao.insert(Mood(name = normalized)).toInt()

    }

    override fun findMood(query: String): Flow<List<String>> {
        return moodDao.findMood(query.normalizeGenre())
    }

}