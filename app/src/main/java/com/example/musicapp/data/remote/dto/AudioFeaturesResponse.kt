package com.example.musicapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AudioFeaturesResponse(
    val bpm: Double,
    val key: Key,
    val loudness: Double,
    @SerializedName("dynamic_complexity") val dynamicComplexity: Double,
    val approachability: Double,
    val engagement: Double,
    val danceability: Double,
    @SerializedName("mood_aggressive") val moodAggressive: Double,
    @SerializedName("mood_happy") val moodHappy: Double,
    @SerializedName("mood_party") val moodParty: Double,
    @SerializedName("mood_relaxed") val moodRelaxed: Double,
    @SerializedName("mood_sad") val moodSad: Double,
    val instrumental: Boolean,
    val voice: String? = null,
    val moods: List<String>
)

data class Key(
    val key: String?,
    val scale: String?
)
