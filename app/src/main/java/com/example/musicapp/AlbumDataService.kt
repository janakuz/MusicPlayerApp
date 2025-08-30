package com.example.musicapp

import com.example.musicapp.data.dto.Release
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.repository.AlbumRepository
import javax.inject.Inject

class AlbumDataService @Inject constructor(
    private val albumRepository: AlbumRepository,
) {
    suspend fun getMbAlbumData(album: AlbumKey): Release {
        val testQuery = "release:${album.title} artist:${album.artist} date:${album.year}"
        val mbAlbumSearch = albumRepository.findAlbumMB(testQuery)
        val mbAlbum = mbAlbumSearch.releases[0]
        return mbAlbum
    }

    suspend fun createNewAlbum(
        mbAlbum: Release,
        album: AlbumKey
    ): Album {
        val albumArt = albumRepository.getAlbumArt(mbAlbum.id)

        var labelName = ""
        if (mbAlbum.labelInfo != null && mbAlbum.labelInfo[0].label != null) {
            labelName = mbAlbum.labelInfo[0].label!!.name
        }

        var newAlbum = Album(
            title = album.title,
            image = albumArt,
            duration = 0L,
            mbId = mbAlbum.id,
            discogsId = null,
            releaseDate = album.year,
            label = labelName,
            numTracks = 0
        )
        return newAlbum
    }

    suspend fun insertNewAlbum(album: Album): Int{
        return albumRepository.insertWithReturn(album).toInt()
    }

}