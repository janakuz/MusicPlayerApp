package com.example.musicapp.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color

interface DynamicThemeRepository {

    suspend fun extractColorsFromUrl(url: String, context: Context): PlayerColors?
}

data class PlayerColors(
    val mainColor: Color,
    val secondaryColor: Color,
    val onColor: Color
)