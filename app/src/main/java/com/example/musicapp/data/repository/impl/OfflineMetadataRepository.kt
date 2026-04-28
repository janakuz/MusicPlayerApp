package com.example.musicapp.data.repository.impl

import android.util.Log
import com.example.musicapp.data.dto.ArtistSearchInfo
import com.example.musicapp.data.dto.ArtistSummary
import com.example.musicapp.data.dto.DiscogsAlbumArtist
import com.example.musicapp.data.dto.Release
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Track
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumMetadataResult
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.isSimilar
import com.example.musicapp.normalizeForMatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.math.min

class OfflineMetadataRepository(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository,
    private val albumArtistRepository: AlbumArtistRepository
) : MetadataRepository {


    private suspend fun getAlbumMB(
        mbAlbum: Release,
        album: Album,
        albumArt: String?,
        isUpdate: Boolean = false,
        releaseDate: String?,
        ) : Album {
        val newAlbumArt =
            if ((albumArt == null || albumArt == "" || isUpdate) && albumRepository.getAlbumArt(mbAlbum.releaseGroup?.id ?: mbAlbum.id) != null)
                albumRepository.getAlbumArt(mbAlbum.releaseGroup?.id ?: mbAlbum.id) else albumArt
        val labelName =
            if (mbAlbum.labelInfo != null && mbAlbum.labelInfo[0].label != null) mbAlbum.labelInfo[0].label!!.name else ""
        val newReleaseDate = releaseDate ?: mbAlbum.date

        val newAlbum = album.copy(
//                mbId = mbAlbum.id,
            mbId = mbAlbum.releaseGroup?.id ?: mbAlbum.id, //TODO:add column to save if group or release. add argument to getAlbumArt and getAllCAAOptions. Add service method.
            image = newAlbumArt,
            label = labelName,
            releaseDate = newReleaseDate,
            isEnriched = true,
            enrichmentAttempted = true
        )
        return newAlbum
    }

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
                    .normalizeForMatching() != albumTitle.lowercase().normalizeForMatching() &&
                artistName.normalizeForMatching() !in mbAlbumSearch.releases[i].artistCredit.map { it.artist.name.normalizeForMatching() }
            ) {
                i++
            }
        }

        if (mbAlbumSearch != null && i == mbAlbumSearch.releases.size) i = 0


        if (mbAlbumSearch != null && mbAlbumSearch.releases.isNotEmpty() &&
            mbAlbumSearch.releases[i].title.lowercase()
                .normalizeForMatching() == albumTitle.lowercase().normalizeForMatching()
        ) {
            val mbAlbum = mbAlbumSearch.releases[i]
            val newAlbum = getAlbumMB(mbAlbum, album, albumArt, isUpdate, releaseDate)
//            val newAlbumArt =
//                if (albumArt == null || albumArt == "" || isUpdate) albumRepository.getAlbumArt(mbAlbum.id) else albumArt
//            val labelName =
//                if (mbAlbum.labelInfo != null && mbAlbum.labelInfo[0].label != null) mbAlbum.labelInfo[0].label!!.name else ""
//            val newReleaseDate = releaseDate ?: mbAlbum.date
//
//            val newAlbum = album.copy(
////                mbId = mbAlbum.id,
//                mbId = mbAlbum.releaseGroup?.id ?: mbAlbum.id,
//                image = newAlbumArt,
//                label = labelName,
//                releaseDate = newReleaseDate,
//                isEnriched = true,
//                enrichmentAttempted = true
//            )
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
        albumToMove: Album?,
        track: Track?
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

            if (track != null){
                artistRepository.moveTracks(oldArtist.id, existingArtist.id, listOf(track.id))
//                albumArtistRepository.insert(AlbumArtist(albumId = track.albumId, artistId = existingArtist.id))
                albumArtistRepository.updateAlbumArtist(albumId = track.albumId, oldArtistId = oldArtist.id, newArtistId = existingArtist.id)
                artistRepository.deleteOrphaned()
                return existingArtist
            }

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

        //else, we need new artist. if artist only -> replace current with new info, if album -> create new
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

        if (albumToMove == null && track == null) {
            artistRepository.update(
                oldArtist.copy(
                    name = newArtistName,
                    searchKey = newArtistName.normalizeForMatching(),
                    mbId = artist.mbId,
                    image = if (artist.image == null || artist.image == "") oldArtist.image else artist.image,
                    discogsId = if (artist.discogsId == null || artist.discogsId != "") oldArtist.discogsId else artist.discogsId,
                    bio = newBio,
                    isEnriched = true,
                    enrichmentAttempted = true
                )
            )

            return oldArtist
        }
        else{
            val toInsert =
                Artist(
                    name = newArtistName,
                    searchKey = newArtistName.normalizeForMatching(),
                    mbId = artist.mbId,
                    image = artist.image,
                    discogsId = if (artist.discogsId == null || artist.discogsId != "") oldArtist.discogsId else artist.discogsId,
                    bio = newBio,
                    isEnriched = true,
                    enrichmentAttempted = true
                )
            val newId = artistRepository.insertWithReturn(
                toInsert
                ).toInt()
            if (albumToMove != null) {
                albumArtistRepository.updateAlbumArtist(
                    albumId = albumToMove.id,
                    oldArtistId = oldArtist.id,
                    newArtistId = newId
                )
            }
            else if (track != null) {
                artistRepository.moveTracks(oldArtist.id, newId, listOf(track.id))
            }

            artistRepository.deleteOrphaned()
            return artist.copy(id = newId)
        }
    }

    override suspend fun refetchAlbum(
        album: Release,
        currentAlbum: Album
    ) {
        val existing = albumRepository.getAlbumByMbid(album.releaseGroup?.id ?: album.id)
        if (existing != null) {
            albumRepository.moveTracks(currentAlbum.id, existing.id)
        }
        else {
            val new = getAlbumMB(
                mbAlbum = album,
                album = currentAlbum,
                albumArt = currentAlbum.image,
                isUpdate = true,
                releaseDate = album.date
            )

            val updated = currentAlbum.copy(
                mbId = new.mbId,
                image = new.image,
                discogsId = new.discogsId,
                releaseDate = new.releaseDate,
                label = new.label,
                enrichmentAttempted = true,
                isEnriched = true
            )

            albumRepository.update(updated)
        }
    }

    override suspend fun refetchArtist(
        mbArtist: ArtistSearchInfo,
        currentArtist: Artist) {
        val existing = artistRepository.getArtistByMbid(mbArtist.id)
        if (existing != null){
            val albums = albumArtistRepository.getAllAlbumsByArtistFull(currentArtist.id).first()
            for (album in albums){
                albumArtistRepository.updateAlbumArtist(
                    album.id,
                    currentArtist.id,
                    existing.id)
            }

            artistRepository.deleteOrphaned()
        }
        else {
            val new = getArtistDataMusicBrainz(
                artistCredit = ArtistSummary(
                    id = mbArtist.id,
                    name = mbArtist.name,
                    sortName = mbArtist.sortName.toString(),
                ),
                artistName = mbArtist.name,
                currentArtist = currentArtist
            )

            val updated = currentArtist.copy(
                mbId = mbArtist.id,
                discogsId = new.discogsId,
                image = new.image,
                bio = new.bio,
                enrichmentAttempted = true,
                isEnriched = true
            )

            artistRepository.update(updated)
        }
    }

    override suspend fun moveToAlbum(
        album: Release,
        tracksToMove: List<Int>,
        oldAlbumId: Int
    ) {
        val existing = albumRepository.getAlbumByMbid(album.releaseGroup?.id ?: album.id)
        if (existing == null) {
            val trackInfos = trackRepository.getTracksByIds(tracksToMove.toSet())
            val totalDuration = trackInfos.sumOf { it.duration }
            val current = Album(
                title = album.title,
                searchKey = album.title.normalizeForMatching(),
                duration = totalDuration,
                numTracks = tracksToMove.size,
                mbId = album.releaseGroup?.id ?: album.id,
                image = null,
                label = null,
                discogsId = null,
                releaseDate = null,
            )
            val new = getAlbumMB(
                mbAlbum = album,
                album = current,
                albumArt = current.image,
                isUpdate = true,
                releaseDate = album.date
            )

            val updated = current.copy(
                mbId = new.mbId,
                image = new.image,
                discogsId = new.discogsId,
                releaseDate = new.releaseDate,
                label = new.label,
                enrichmentAttempted = true,
                isEnriched = true
            )

            val newId = albumRepository.insertWithReturn(updated).toInt()
            val artist = artistRepository.getArtistByMbid(album.artistCredit[0].artist.id)
            var artistId = artist?.id ?: -1
            if (artist == null){
                val new = getArtistDataMusicBrainz(
                    artistCredit = ArtistSummary(
                        id = album.artistCredit[0].artist.id,
                        name = album.artistCredit[0].artist.name,
                        sortName = album.artistCredit[0].artist.sortName.toString(),
                    ),
                    artistName = album.artistCredit[0].artist.name,
                    currentArtist = Artist(name = album.artistCredit[0].artist.name, searchKey = album.artistCredit[0].artist.name.normalizeForMatching())
                )

                artistId = artistRepository.insertWithReturn(new).toInt()
            }

            albumArtistRepository.insert(AlbumArtist(albumId = newId, artistId = artistId))
            albumArtistRepository.removeArtistFromAlbum(oldAlbumId, artistId)
            albumRepository.moveTracks(
                oldAlbumId,
                newId,
                tracksToMove)
        }
        else{
            albumRepository.moveTracks(
                oldAlbumId,
                existing.id,
                tracksToMove
            )
        }
    }

    override suspend fun moveToUnenriched(
        album: String,
        artist: String,
        tracksToMove: List<Int>,
        oldAlbumId: Int,
        markEnriched: Boolean,
    ) {
        val newArtist = artistRepository.getArtistByName(artist.normalizeForMatching())
        if (newArtist.isNotEmpty() && newArtist.size == 1){
            val trackInfos = trackRepository.getTracksByIds(tracksToMove.toSet())
            val totalDuration = trackInfos.sumOf { it.duration }

            val newAlbum = Album(
                title = album,
                searchKey = album.normalizeForMatching(),
                duration = totalDuration,
                numTracks = tracksToMove.size,
                image = null,
                label = null,
                discogsId = null,
                releaseDate = null,
                mbId = null,
                enrichmentAttempted = true,
                isEnriched = markEnriched,
                )

            val newId = albumRepository.insertWithReturn(newAlbum).toInt()
            if (trackInfos[0].artistId != newArtist[0].id)
                albumArtistRepository.removeArtistFromAlbum(oldAlbumId, newArtist[0].id)
            albumArtistRepository.insert(AlbumArtist(albumId = newId, artistId = newArtist[0].id))
            albumRepository.moveTracks(
                oldAlbumId,
                newId,
                tracksToMove)

        }
    }

    override suspend fun updateAlbum(
        newAlbumTitle: String,
        oldAlbum: Album,
        newArtistName: String,
        oldArtist: Artist?,
        newReleaseDate: String?,
        newAlbumArt: String?,
        track: Track?
    ) : AlbumArtistUpdate {

        if (oldAlbum.title.normalizeForMatching() == newAlbumTitle.normalizeForMatching() && oldAlbum.mbId != null) { //assume cleanup/article fix
            albumRepository.update(
                oldAlbum.copy(
                    title = newAlbumTitle,
                    searchKey = newAlbumTitle.normalizeForMatching()
                )
            )
            return AlbumArtistUpdate(oldAlbum, oldArtist)
        }

        val albumData = getAlbumData(
            album = oldAlbum,
            albumTitle = newAlbumTitle,
            artistName = newArtistName,
            releaseDate = newReleaseDate,
            albumArt = newAlbumArt,
            isUpdate = true
        )


        val existingAlbum = albumRepository.getAlbumByMbid(albumData.album.mbId ?: "")
        if (oldAlbum.mbId != null && oldAlbum.mbId==albumData.album.mbId && existingAlbum != null){ //same album
            albumRepository.update(
                oldAlbum.copy(
                    title = newAlbumTitle,
                    searchKey = newAlbumTitle.normalizeForMatching()
                )
            )

            return AlbumArtistUpdate(oldAlbum, oldArtist)
        }

        var albumToMove: Album? = null
        if (oldAlbum.mbId != null && oldAlbum.mbId != albumData.album.mbId && existingAlbum != null){ //different existing album
            albumRepository.update(
                albumData.album.copy(
                    title = newAlbumTitle,
                    searchKey = newAlbumTitle.normalizeForMatching()
                )
            )

            if (track == null) {
                albumRepository.moveTracks(oldAlbum.id, albumData.album.id)
                albumRepository.deleteOrphaned()
                albumToMove = albumData.album
            }
            else{
                albumRepository.moveTracks(oldAlbum.id, albumData.album.id, listOf(track.id))
                albumRepository.deleteOrphaned()

                return AlbumArtistUpdate(albumData.album, oldArtist)
            }
        }

        //else, replace current
        if (track == null) {
            albumRepository.update(
                oldAlbum.copy(
                    title = newAlbumTitle,
                    searchKey = newAlbumTitle.normalizeForMatching(),
                    mbId = albumData.album.mbId,
                    image = albumData.album.image,
                    label = albumData.album.label,
                    releaseDate = if (newReleaseDate == null || newReleaseDate == "") oldAlbum.releaseDate else newReleaseDate,
                    isEnriched = true
                )
            )
            albumToMove = oldAlbum
        }

        else{
            val toInsert = Album(
                title = newAlbumTitle,
                searchKey = newAlbumTitle.normalizeForMatching(),
                image = albumData.album.image,
                duration = track.duration,
                numTracks = 1,
                mbId = albumData.album.mbId,
                label = albumData.album.label,
                discogsId = albumData.album.discogsId,
                releaseDate = albumData.album.releaseDate,
                isEnriched = albumData.album.mbId != null || albumData.album.discogsId != null,
                enrichmentAttempted = true
            )
            val newId = albumRepository.insertWithReturn(toInsert).toInt()
            albumRepository.moveTracks(oldAlbum.id, newId, listOf(track.id))
            albumArtistRepository.insert(AlbumArtist(albumId = newId, artistId = oldArtist!!.id))
            albumRepository.deleteOrphaned()
            return AlbumArtistUpdate(toInsert, oldArtist)
        }

        var updatedArtist: Artist? = null
        val mbAlbumArtists = albumData.mbResponse?.releases?.find { it.id==albumData.album.mbId }?.artistCredit ?: emptyList()
        if (mbAlbumArtists.size == 1 && oldArtist != null) {
            updatedArtist = updateArtist(
                newArtistName,
                oldArtist,
                ArtistSearchInfo(
                    id = mbAlbumArtists[0].artist.id,
                    name = mbAlbumArtists[0].artist.name,
                    sortName = mbAlbumArtists[0].artist.sortName,
                ),
                albumToMove = albumToMove
            )
        }

        return AlbumArtistUpdate(albumToMove, updatedArtist)
    }

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
            Log.d("scan enrich album", albumTitle)
//            if (album.mbId == null && album.discogsId == null) {
                albumRepository.update(albumResponse.album)
//            }
//            else if (album.mbId != null && album.mbId != albumResponse.album.mbId){
//                val newId = albumRepository.insertWithReturn(albumResponse.album).toInt()
//                val allTracks = trackRepository.getAlbumTracks(album.id)
//                val toMove = allTracks.filter { track ->
//                    track.filePath.contains(artistName)
//                }.map { it.trackId }
//                albumRepository.moveTracks(album.id, newId, toMove)
//                albumArtistRepository.insert(AlbumArtist(albumId =  newId, artistId = albumArtist.artistId))
//                Log.d("scan split album", "here")
//            }

            var toInsert = false
            var toUpdate = false


            var currentArtist = artistRepository.getArtist(albumArtist.artistId).first()
            if (albumResponse.mbResponse != null) {
                val mbAlbumArtists = albumResponse.mbResponse.releases.find {release ->
                    val matchesGroup = release.releaseGroup?.id == albumResponse.album.mbId
                    val matchesRelease = release.id == albumResponse.album.mbId
                    matchesGroup || matchesRelease}?.artistCredit ?: emptyList()
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
            Log.d("scan", "after artist $artistName")

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

data class AlbumArtistUpdate(
    val album: Album,
    val artist: Artist?
)