package com.example.musicapp.data.repository

interface TrackMoodRepository {
    suspend fun updateTrackMoods(trackId: Int, moods: List<String>)

    suspend fun getTrackMoods(trackId: Int): List<String>

    suspend fun addTrackMoods(trackId: Int, moods: List<String>)

}