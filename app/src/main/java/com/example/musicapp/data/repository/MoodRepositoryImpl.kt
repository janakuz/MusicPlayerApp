package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.MoodDao
import com.example.musicapp.data.local.entity.Mood
import com.example.musicapp.data.local.model.MoodInfo
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.util.normalizeGenre
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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

    override fun getAll(sortBy: SortOption): Flow<List<MoodInfo>> {
            return when (sortBy.field) {
                SortField.NAME -> moodDao.getAllMoods("name", sortBy.ascending)
                SortField.TOTAL_COUNT -> moodDao.getAllMoods("count", sortBy.ascending)
                else -> moodDao.getAllMoods("count", false)
            }

    }

    override fun getItemsForMood(
        moodId: Int,
        artistLimit: Int,
        albumThreshold: Float
    ): Flow<SearchResult> {
        return combine (
            moodDao.getMoodArtists(moodId, artistLimit),
            moodDao.getMoodAlbums(moodId, albumThreshold),
            moodDao.getMoodTracks(moodId)
        ) { artists, albums, tracks ->
            SearchResult(artists, albums, tracks)
        }
    }

    override fun getMoodName(moodId: Int): Flow<String> {
        return moodDao.getMoodName(moodId)
    }

}