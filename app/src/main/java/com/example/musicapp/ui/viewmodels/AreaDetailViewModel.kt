package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AreaRepository
import com.example.musicapp.data.repository.AreaType
import com.example.musicapp.data.repository.SearchResult
import com.example.musicapp.util.getFlagEmoji
import com.example.musicapp.util.getSubtitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AreaDetailViewModel@Inject constructor (
    private val areaRepository: AreaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel()  {

    private val gid: String = savedStateHandle.get<String>("gid")
        ?: throw IllegalStateException("gid not found in SavedStateHandle")

    private val type: String = savedStateHandle.get<String>("type")
        ?: throw IllegalStateException("type not found in SavedStateHandle")

    private val countryCode = savedStateHandle.get<String>("code")
        ?: throw IllegalStateException("country code not found in SavedStateHandle")


    private val areaType = when(type){
        "city" -> AreaType.CITY
        "county" -> AreaType.COUNTY
        "state" -> AreaType.STATE
        else -> AreaType.COUNTRY
    }

    val areaItems = areaRepository.getArtistsFromArea(gid, countryCode, areaType)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult())

    var areaName = ""
    var flag = ""
    var subtitle = ""
    init{
        viewModelScope.launch {
            val name = areaRepository.getAreaName(gid)
            val hierarchy = areaRepository.getAreaHierarchy(gid)
            flag = getFlagEmoji(countryCode, hierarchy.state)
            areaName = name
            subtitle = getSubtitle(hierarchy, areaType)
        }
    }
}