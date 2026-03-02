package com.example.musicapp.data.repository

import android.util.Log
import androidx.work.workDataOf
import com.example.musicapp.data.dto.ArtistCredit
import com.example.musicapp.data.dto.ArtistSearchInfo
import com.example.musicapp.data.dto.ArtistSummary
import com.example.musicapp.data.dto.DiscogsAlbumArtist
import com.example.musicapp.data.dto.DiscogsSearchResponse
import com.example.musicapp.data.dto.ReleaseSearchResponse
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.isSimilar
import com.example.musicapp.normalizeForMatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import kotlin.math.min

class OfflineMetadataRepository(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val albumArtistRepository: AlbumArtistRepository
) : MetadataRepository {


    private suspend fun getAlbumData(
        album: Album,
        albumTitle: String,
        artistName: String,
        releaseDate: String?,
        albumArt: String?,
        isUpdate: Boolean = false
    ): AlbumMetadataResult {
        val mbQuery = "release:${albumTitle} artist:${artistName} date:${releaseDate}"
        val mbAlbumSearch = albumRepository.findAlbumMB(mbQuery)

        var i = 0

        if (mbAlbumSearch != null && mbAlbumSearch.releases.isNotEmpty()) {
            while (i < mbAlbumSearch.releases.size && mbAlbumSearch.releases[i].title.lowercase()
                    .normalizeForMatching() != albumTitle.lowercase().normalizeForMatching()
            ) {
                i++
            }
        }

        if (mbAlbumSearch != null && i == mbAlbumSearch.releases.size) i = 0

        Log.d("find album", i.toString())

        if (mbAlbumSearch != null && mbAlbumSearch.releases.isNotEmpty() &&
            mbAlbumSearch.releases[i].title.lowercase()
                .normalizeForMatching() == albumTitle.lowercase().normalizeForMatching()
        ) {
            val mbAlbum = mbAlbumSearch.releases[i]
            val newAlbumArt =
                if (albumArt == null || albumArt == "" || isUpdate) albumRepository.getAlbumArt(mbAlbum.id) else albumArt
            val labelName =
                if (mbAlbum.labelInfo != null && mbAlbum.labelInfo[0].label != null) mbAlbum.labelInfo[0].label!!.name else ""
            val newReleaseDate = releaseDate ?: mbAlbum.date

            val newAlbum = album.copy(
                mbId = mbAlbum.id,
                image = newAlbumArt,
                label = labelName,
                releaseDate = newReleaseDate,
                isEnriched = true,
                enrichmentAttempted = true
            )
            return AlbumMetadataResult(mbAlbumSearch, null, newAlbum)
        } else {
            val discogsResponse =
                albumRepository.findAlbumDiscogs(artistName, albumTitle, releaseDate?.take(4))

            var i = 0

            if (discogsResponse != null && discogsResponse.results.isNotEmpty()) {
                while (i < discogsResponse.results.size &&
                    discogsResponse.results[i].title.split(" - ")[1].normalizeForMatching() != albumTitle.normalizeForMatching()
                ) {
                    i++
                }
            }

            if (discogsResponse != null && i == discogsResponse.results.size) i = 0

            if (discogsResponse != null && discogsResponse.results.isNotEmpty() &&
                discogsResponse.results[i].title.split(" - ")[1].normalizeForMatching() == albumTitle.normalizeForMatching()
            ) {
                val newAlbumArt =
                    if (albumArt == null || albumArt == "" || isUpdate) discogsResponse.results[i].cover_image else albumArt
                val labelName =
                    if (discogsResponse.results[i].label != null && discogsResponse.results[i].label?.isNotEmpty() == true)
                        discogsResponse.results[i].label?.get(0) else ""
                val newReleaseDate = releaseDate ?: discogsResponse.results[i].year
                val newAlbum = album.copy(
                    image = newAlbumArt,
                    label = labelName,
                    releaseDate = newReleaseDate,
                    isEnriched = true,
                    enrichmentAttempted = true
                )
                return AlbumMetadataResult(mbAlbumSearch, discogsResponse, newAlbum)
            }

        }
        return AlbumMetadataResult(null, null, album.copy(enrichmentAttempted = true))
    }

    private suspend fun getArtistDataMusicBrainz(
        artistCredit: ArtistSummary,
        artistName: String,
        currentArtist: Artist,
    ): Artist {
        if (artistCredit.name.lowercase().normalizeForMatching() == artistName.lowercase()
                .normalizeForMatching()
        ) {
            delay(1000)
            val mbArtist = artistRepository.getArtistMusicbrainzInfo(artistCredit.id)
            val existingArtist = artistRepository.getArtistByMbid(mbArtist.id)

            if (existingArtist == null) {
                var artistImage = ""
                var discogsId = ""
                if (mbArtist.urlRelations != null) {
                    val discogs = mbArtist.urlRelations.find { it.type.equals("discogs") }
                    if (discogs != null && discogs.url != null) {
                        val discogsLink = discogs.url.resource.toString().split("/")
                        discogsId = discogsLink[discogsLink.size - 1]
                        if (currentArtist.discogsId != null && currentArtist.discogsId == discogsId) {
                            val newArtist = currentArtist.copy(mbId = mbArtist.id)
                            return newArtist
                        }
                        val artistInfo = artistRepository.getArtistDiscogsInfo(discogsId)
                        if (artistInfo != null && artistInfo.images != null && artistInfo.images.isNotEmpty()) {
                            artistImage = artistInfo.images[0].resourceUrl
                        }
                    }
                }
                val newArtist =
                    if (currentArtist.discogsId == null) currentArtist.copy(
                        image = artistImage,
                        discogsId = discogsId,
                        mbId = mbArtist.id,
                        enrichmentAttempted = true,
                        isEnriched = true
                    )
                    else Artist(
                        image = artistImage,
                        discogsId = discogsId,
                        mbId = mbArtist.id,
                        name = artistName,
                        searchKey = artistName.normalizeForMatching(),
                        enrichmentAttempted = true,
                        isEnriched = true
                    )
                return newArtist
            }
            return existingArtist
        }
        return currentArtist
    }

    private suspend fun getArtistDataDiscogs(
        artist: DiscogsAlbumArtist,
        artistName: String,
        currentArtist: Artist
    ): Artist {
        if (artist.name.normalizeForMatching() == artistName) {
            delay(1000)
            val discogsArtist = artistRepository.getArtistDiscogsInfo(artist.id)
            val discogsId = discogsArtist?.id.toString()

            val artistImage =
                if (discogsArtist?.images != null && discogsArtist.images.isNotEmpty()) discogsArtist.images.get(
                    0
                ).resourceUrl else ""

            val newArtist =
                currentArtist.copy(image = artistImage, discogsId = discogsId, isEnriched = true)
            return newArtist
        }
        return currentArtist
    }

    suspend fun updateArtistOld(
        newArtistName: String,
        oldArtist: Artist,
        newAlbumMeta: AlbumMetadataResult?
    ) {
        Log.d("update artist", "starting update")

        var artistInfoMB: ArtistSummary? = null
        if (newAlbumMeta != null){
            val mbAlbumArtists = newAlbumMeta.mbResponse?.releases?.find { it.id==newAlbumMeta.album.mbId }?.artistCredit ?: emptyList()
            artistInfoMB =
                mbAlbumArtists.find { it.artist.name.normalizeForMatching() == newArtistName.normalizeForMatching() }?.artist
        }

        if ( (artistInfoMB != null && artistInfoMB.id== oldArtist.mbId) || (artistInfoMB == null  &&
            oldArtist.name.normalizeForMatching() == newArtistName.normalizeForMatching() && oldArtist.mbId != null)) { //assume cleanup/article fix
            artistRepository.update(
                oldArtist.copy(
                    name = newArtistName,
                    searchKey = newArtistName.normalizeForMatching()
                )
            )
            Log.d("update artist", "only name")

            return
        }


        val artistAlbums = albumArtistRepository.getAllAlbumsByArtistFull(oldArtist.id).first()
        val albumData = newAlbumMeta
            ?: getAlbumData(
                album = artistAlbums[0],
                albumTitle = artistAlbums[0].title,
                artistName = newArtistName,
                releaseDate = artistAlbums[0].releaseDate,
                albumArt = artistAlbums[0].image,
            )
        val mbAlbumArtists = albumData.mbResponse?.releases?.find { it.id==albumData.album.mbId }?.artistCredit ?: emptyList()
        val artistInfo =
            mbAlbumArtists.find { it.artist.name.normalizeForMatching() == newArtistName.normalizeForMatching() }?.artist
        if (artistInfo != null) {
            val artist = getArtistDataMusicBrainz(
                artistCredit = artistInfo,
                artistName = newArtistName,
                currentArtist = oldArtist
            )

            Log.d("update artist - album", albumData.album.toString())
            Log.d("update artist - artist", artist.toString())

            if (newAlbumMeta != null && oldArtist.mbId != artist.mbId && artist.id == 0){ //if changed on specific album, can be new artist. insert and move only that album

                val newBio = artistRepository.getArtistBio(artist.mbId, newArtistName)

                val artistId = artistRepository.insertWithReturn(
                    artist.copy(
                        bio = newBio,
                        )).toInt()

                albumArtistRepository.updateAlbumArtist(
                    albumId = newAlbumMeta.album.id,
                    oldArtistId = oldArtist.id,
                    newArtistId = artistId
                )
                artistRepository.deleteOrphaned()
                Log.d("update artist", "move to new")

            }

            else if (oldArtist.mbId != artist.mbId && (artist.id == 0 || artist.id == oldArtist.id)) { // currently no mbId or artist with new mbId doesn't exist in db
                val newBio = artistRepository.getArtistBio(artist.mbId, newArtistName)

                artistRepository.update(
                    oldArtist.copy(
                        name = newArtistName,
                        searchKey = newArtistName.normalizeForMatching(),
                        mbId = artist.mbId,
                        image = if (oldArtist.image != null && oldArtist.image != "") oldArtist.image else artist.image,
                        discogsId = if (oldArtist.discogsId != null && oldArtist.discogsId != "") oldArtist.discogsId else artist.discogsId,
                        bio =  newBio,
                        isEnriched = true
                    )
                )
                Log.d("update artist", "replace")

            } else if (oldArtist.mbId == artist.mbId && oldArtist.id == artist.id) { // same mbId, same artist, presumably has info, only update name
                artistRepository.update(
                    oldArtist.copy(
                        name = newArtistName,
                        searchKey = newArtistName.normalizeForMatching()
                    )
                )
                Log.d("update artist", "only name 2")

            } else if (oldArtist.mbId != artist.mbId && newArtistName.normalizeForMatching() == artist.name.normalizeForMatching()) {
                artistRepository.update(artist.copy(name =  newArtistName, searchKey = newArtistName.normalizeForMatching()))

                for (album in artistAlbums) {
                    albumArtistRepository.updateAlbumArtist(
                        albumId = album.id,
                        oldArtistId = oldArtist.id,
                        newArtistId = artist.id
                    )
                }
                Log.d("update artist", "merge")

                artistRepository.deleteOrphaned()
            }

        }
    }

    override suspend fun updateArtist(
        newArtistName: String,
        oldArtist: Artist,
        mbArtist: ArtistSearchInfo?,
        albumToMove: Album?
        ) : Artist {

        if (mbArtist == null){
            artistRepository.update(oldArtist.copy(name = newArtistName, searchKey = newArtistName.normalizeForMatching()))
            return oldArtist
        }

        if (oldArtist.mbId != null && oldArtist.mbId == mbArtist.id){ //same artist, update only name
            artistRepository.update(oldArtist.copy(name = newArtistName, searchKey = newArtistName.normalizeForMatching()))
            return oldArtist
        }

        val existingArtist = artistRepository.getArtistByMbid(mbArtist.id)
        if (existingArtist != null){ //different artist
            if (existingArtist.name != newArtistName)
                artistRepository.update(existingArtist.copy(name =  newArtistName, searchKey = newArtistName.normalizeForMatching()))

            //if editing album, move only that album, else move all albums
            val artistAlbums = if (albumToMove == null) albumArtistRepository.getAllAlbumsByArtistFull(oldArtist.id).first() else listOf(albumToMove)

            for (album in artistAlbums) {
                albumArtistRepository.updateAlbumArtist(
                    albumId = album.id,
                    oldArtistId = oldArtist.id,
                    newArtistId = existingArtist.id
                )
            }

            artistRepository.deleteOrphaned()
            return existingArtist
        }

        //else, replace current with new info
        val artist = getArtistDataMusicBrainz(
            artistCredit = ArtistSummary(
                id = mbArtist.id,
                name = mbArtist.name,
                sortName = mbArtist.sortName.toString(),
            ),
            artistName = newArtistName,
            currentArtist = oldArtist
        )
        val newBio = artistRepository.getArtistBio(mbArtist.id, newArtistName)
        artistRepository.update(
            oldArtist.copy(
                name = newArtistName,
                searchKey = newArtistName.normalizeForMatching(),
                mbId = artist.mbId,
                image = if (artist.image == null || artist.image == "") oldArtist.image else artist.image,
                discogsId = if (artist.discogsId == null || artist.discogsId != "") oldArtist.discogsId else artist.discogsId,
                bio =  newBio,
                isEnriched = true,
                enrichmentAttempted = true
            )
        )

        return oldArtist
    }

//    override suspend fun updateAlbum(
//        newAlbumTitle: String,
//        oldAlbum: Album,
//        newArtistName: String,
//        oldArtist: Artist,
//        newReleaseDate: String?,
//        newAlbumArt: String?
//    ) {
//         Log.d("update album", "starting update")
//
//        if (oldAlbum.title.normalizeForMatching() == newAlbumTitle.normalizeForMatching() && oldAlbum.mbId != null) { //assume cleanup/article fix
//            albumRepository.update(
//                oldAlbum.copy(
//                    title = newAlbumTitle,
//                    searchKey = newAlbumTitle.normalizeForMatching()
//                )
//            )
//            Log.d("update album", "only title")
//            return
//        }
//
//        val albumData = getAlbumData(
//            album = oldAlbum,
//            albumTitle = newAlbumTitle,
//            artistName = newArtistName,
//            releaseDate = newReleaseDate,
//            albumArt = newAlbumArt,
//            isUpdate = true
//        )
//
//        if (oldAlbum.mbId == null || oldAlbum.mbId != albumData.album.mbId && (albumData.album.id == 0 || albumData.album.id == oldAlbum.id)) { // currently no mbId or album with new mbId doesn't exist in db
//            albumRepository.update(
//                oldAlbum.copy(
//                    title = newAlbumTitle,
//                    searchKey = newAlbumTitle.normalizeForMatching(),
//                    mbId = albumData.album.mbId,
//                    image =  albumData.album.image,
//                    label = albumData.album.label,
//                    releaseDate = if (newReleaseDate == "") oldAlbum.releaseDate else newReleaseDate,
//                    isEnriched = true
//                )
//            )
//            Log.d("update album", "replace")
//        } else if (oldAlbum.mbId == albumData.album.mbId && oldAlbum.id == albumData.album.id) { // same mbId, same album, presumably has info, only update name
//            albumRepository.update(
//                oldAlbum.copy(
//                    title = newAlbumTitle,
//                    searchKey = newAlbumTitle.normalizeForMatching()
//                )
//            )
//            Log.d("update album", "only title 2")
//
//        } else if (oldAlbum.mbId != albumData.album.mbId && newAlbumTitle.normalizeForMatching() == albumData.album.title.normalizeForMatching()) {
//            albumRepository.update(albumData.album.copy(title =  newAlbumTitle, searchKey = newAlbumTitle.normalizeForMatching()))
//            albumRepository.moveTracks(oldAlbum.id, albumData.album.id)
//            albumRepository.deleteOrphaned()
//            Log.d("update album", "move to existing")
//        }
//
//        updateArtist(newArtistName, oldArtist, albumData)
//
//    }

    override suspend fun enrichMetadata(isManual: Boolean): Flow<ScanProgress> = flow {
        val currentAlbumArtists =
            if (isManual) albumArtistRepository.getAllUnenriched() else albumArtistRepository.getAllUnattempted()

        var current = 0
        val total = currentAlbumArtists.size

        for (albumArtist in currentAlbumArtists) {
            val albumTitle = albumArtist.title
            val artistName = albumArtist.artistName
            val releaseDate = albumArtist.releaseDate
            val albumArt = albumArtist.image

            val album = albumRepository.getAlbum(albumArtist.albumId).first()
            val albumResponse = getAlbumData(album, albumTitle, artistName, releaseDate, albumArt)
            albumRepository.update(albumResponse.album)

            var toInsert = false
            var toUpdate = false


            var currentArtist = artistRepository.getArtist(albumArtist.artistId).first()
            if (albumResponse.mbResponse != null) {
                val mbAlbumArtists = albumResponse.mbResponse.releases.find { it.id==albumResponse.album.mbId }?.artistCredit ?: emptyList()
                for (artistCredit in mbAlbumArtists) {
                    val artist = artistCredit.artist
                    if (currentArtist.mbId == null) {
                        val updatedArtist =
                            getArtistDataMusicBrainz(artist, artistName, currentArtist)
                        if (currentArtist.discogsId != null && currentArtist.discogsId != updatedArtist.discogsId) toInsert =
                            true
                        if (updatedArtist != currentArtist) {
                            currentArtist = updatedArtist
                            toUpdate = true
                        }
                    } else if (currentArtist.mbId != artist.id && artistName.normalizeForMatching() == artist.name.normalizeForMatching()) {
                        val newCurrent =
                            Artist(name = currentArtist.name, searchKey = currentArtist.searchKey)
                        val newArtist = getArtistDataMusicBrainz(artist, artistName, newCurrent)
                        currentArtist = newArtist
                        toInsert = true
                    }
                }
            } else if (albumResponse.discogsResponse != null) {
                delay(1000)
                val discogsAlbum =
                    albumRepository.getAlbumDiscogs(albumResponse.discogsResponse.results[0].resource_url)
                if (discogsAlbum != null && discogsAlbum.artists.isNotEmpty()) {
                    for (artist in discogsAlbum.artists) {
                        if (currentArtist.discogsId == null) {
                            val updatedArtist =
                                getArtistDataDiscogs(artist, artistName, currentArtist)
                            if (updatedArtist != currentArtist) {
                                currentArtist = updatedArtist
                                toUpdate = true
                            }
                        } else if (currentArtist.discogsId != artist.id && artistName.normalizeForMatching() == artist.name.normalizeForMatching()) {
                            val newCurrent = Artist(
                                name = currentArtist.name,
                                searchKey = currentArtist.searchKey
                            )
                            val newArtist = getArtistDataDiscogs(artist, artistName, newCurrent)
                            currentArtist = newArtist
                            toInsert = true
                        }
                    }
                }
            }
            if (currentArtist.bio == null || currentArtist.bio == "") {
                val bio = artistRepository.getArtistBio(currentArtist.mbId, artistName)
                currentArtist = currentArtist.copy(bio = bio)
                toUpdate = true
            }
            if (toUpdate && !toInsert) {
                currentArtist = currentArtist.copy(enrichmentAttempted = true)
                artistRepository.update(currentArtist)
            } else if (toInsert) {
                currentArtist = currentArtist.copy(enrichmentAttempted = true)
                val inserted = artistRepository.insertWithReturn(currentArtist).toInt()
                albumArtistRepository.updateAlbumArtist(
                    albumArtist.albumId,
                    albumArtist.artistId,
                    inserted
                )
            } else if (currentArtist.enrichmentAttempted == false) {
                currentArtist = currentArtist.copy(enrichmentAttempted = true)
                artistRepository.update(currentArtist)
            }

            val progress = ScanProgress(
                current = current+1,
                total = total,
                currentAlbum = album.title
            )

            emit(progress)

//            val progressData = workDataOf(
//                "current" to current + 1,
//                "total" to total,
//                "albumTitle" to album.title
//            )
//            setProgress(progressData)

//            yield()

            current++
        }

    }

    private suspend fun fuzzyMatch(
        albumTitleLocal: String,
        albumTitleAPI: String,
        artistLocal: String,
        artistAPI: String,
        releaseDateLocal: String?,
        releaseDateAPI: String?
    ): Boolean {
        if (albumTitleLocal == albumTitleAPI) return true
        if (albumTitleLocal.isSimilar(albumTitleAPI, threshold = 0.85)) return true
        if (albumTitleLocal.isSimilar(albumTitleAPI, threshold = 0.65) &&
            artistLocal == artistAPI && (releaseDateLocal == null || releaseDateAPI == null || releaseDateLocal == releaseDateAPI)
        ) return true
        if ((albumTitleLocal.startsWith(albumTitleAPI) || albumTitleAPI.startsWith(albumTitleLocal)) &&
            artistLocal == artistAPI && min(albumTitleAPI.length, albumTitleLocal.length) > 10 &&
            (releaseDateLocal == null || releaseDateAPI == null || releaseDateLocal == releaseDateAPI)
        ) return true
        return false
    }

}



data class ScanProgress(
    val current: Int = 0,
    val total: Int = 0,
    val currentAlbum: String = ""
)

