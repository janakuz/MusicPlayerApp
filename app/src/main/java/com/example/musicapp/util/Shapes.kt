package com.example.musicapp.util

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.vector.Path

const val SlantOffset = 12f

val SlantedLeftShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width - SlantOffset, size.height)
    lineTo(0f, size.height)
    close()
}

val SlantedRightShape = GenericShape { size, _ ->
    moveTo(SlantOffset, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}