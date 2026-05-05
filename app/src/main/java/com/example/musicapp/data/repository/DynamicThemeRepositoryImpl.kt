package com.example.musicapp.data.repository

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DynamicThemeRepositoryImpl(
    private val imageLoader: ImageLoader,
) : DynamicThemeRepository {
    override suspend fun extractColorsFromUrl(
        url: String,
        context: Context
    ): PlayerColors? {
        return withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as BitmapDrawable).bitmap

                withContext(Dispatchers.Default) {
                    val palette = Palette.from(bitmap).generate()
                    val swatch =
                        palette.vibrantSwatch ?: palette.mutedSwatch ?: palette.dominantSwatch

                    PlayerColors(
                        mainColor = swatch?.rgb?.let { Color(it) } ?: Color(0xFF121212),
                        secondaryColor = palette.lightVibrantSwatch?.rgb?.let { Color(it) }
                            ?: Color.Companion.Cyan,
                        onColor = swatch?.titleTextColor?.let { Color(it) } ?: Color.Companion.White
                    )
                }
            } else null
        }
    }
}