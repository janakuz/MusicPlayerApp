package com.example.musicapp

import com.example.musicapp.data.dto.ArtistMBResponse
import com.example.musicapp.data.dto.Release
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.ArtistRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class ArtistDataService @Inject constructor(
    private val artistRepository: ArtistRepository
){

    suspend fun getArtists(mbAlbum: Release): List<Pair<String, Artist>>{
        val resultArtists = mutableListOf<Pair<String, Artist>>()

        val artists = mbAlbum.artistCredit
        for (artist in artists){
            val artistMbid = artist.artist.id
            val existingArtist = artistRepository.getArtistByMbid(artistMbid)
            if (existingArtist != null){
                resultArtists.add(Pair(artistMbid, existingArtist))
            }
            else {
                val artistData = getArtistData(artistMbid)
                val newArtist = createNewArtist(artistData, artistMbid)
                val insertedArtist = artistRepository.insertWithReturn(newArtist).toInt()

                resultArtists.add(Pair(artistMbid, newArtist.copy(id = insertedArtist)))
            }
        }
        return resultArtists
    }

    private suspend fun getArtistData(artistMbid: String): ArtistMBResponse {
        delay(1000)
        val mbArtist = artistRepository.getArtistMusicbrainzInfo(artistMbid)
        return mbArtist
    }

    suspend fun createNewArtist(mbArtist: ArtistMBResponse, artistMbid: String): Artist{
        var artistImage = ""
        var discogsId = ""
        if (mbArtist.urlRelations != null) {
            val discogs = mbArtist.urlRelations.find { it.type.equals("discogs") }
            if (discogs != null && discogs.url != null) {
                val discogsLink = discogs.url.resource.toString().split("/")
                discogsId = discogsLink[discogsLink.size - 1]
                artistImage = artistRepository.getArtistImage(discogsId)
            }

        }

        val bio = artistRepository.getArtistBio(artistMbid)

        val newArtist = Artist(
            name =  mbArtist.name,
            bio = bio,
            mbId = artistMbid,
            image = artistImage,
            discogsId = discogsId
        )

        return newArtist

    }
}