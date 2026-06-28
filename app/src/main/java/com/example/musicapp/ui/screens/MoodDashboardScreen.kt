package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.viewmodels.MoodDashboardViewModel
import com.example.musicapp.util.toTitleCase

@Composable
fun MoodDashboardScreen(
    onMoodClick: (Int) -> Unit,
    sortRequest: SortOption?,
    ){
    val moodDashboardViewModel: MoodDashboardViewModel = hiltViewModel()
    val moodsList by moodDashboardViewModel.moodsWithCounts.collectAsState()

    val visibleMoods = moodsList.filter { it.trackCount > 0 }

    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            moodDashboardViewModel.setSort(it)
        }
    }


    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(visibleMoods) { item ->
            GenreCard(
                genreName = item.mood.name.toTitleCase(),
                artistCount = 0,
                albumCount = item.trackCount,
                isTracks = true,
                onClick = { onMoodClick(item.mood.id) },
//                onDelete = { mood.deleteGenre(item.mood) },
//                onRename = { newName -> moodDashboardViewModel.renameMood(item.mood, newName)}
            ) }
    }

}