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
import com.example.musicapp.ui.viewmodels.LabelsDashboardViewModel

@Composable
fun LabelsScreen(
    onLabelClick: (String) -> Unit,
    sortRequest: SortOption?,

    ) {
    val labelsDashboardViewModel: LabelsDashboardViewModel = hiltViewModel()
    val labelsList by labelsDashboardViewModel.labelsWithCounts.collectAsState()

    val visibleLabels = labelsList.filter { it.artistCount > 0 || it.albumCount > 0 }

    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            labelsDashboardViewModel.setSort(it)
        }
    }


    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(visibleLabels) { item ->
            GenreCard(
                genreName = item.label,
                artistCount = item.artistCount,
                albumCount = item.albumCount,
                onClick = { onLabelClick(item.label) }
            ) }
    }
}