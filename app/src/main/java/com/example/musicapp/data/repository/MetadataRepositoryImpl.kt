package com.example.musicapp.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Transformer
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.AlbumArtist
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.entity.SimilarArtists
import com.example.musicapp.data.local.entity.Track
import com.example.musicapp.data.local.entity.TrackLyrics
import com.example.musicapp.data.remote.dto.ArtistSearchInfo
import com.example.musicapp.data.remote.dto.ArtistSummary
import com.example.musicapp.data.remote.dto.DiscogsAlbumArtist
import com.example.musicapp.data.remote.dto.Release
import com.example.musicapp.data.remote.dto.Tag
import com.example.musicapp.util.isSimilar
import com.example.musicapp.util.normalizeForMatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import kotlin.collections.orEmpty
import kotlin.math.min
import kotlin.math.roundToInt

class OfflineMetadataRepository(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository,
    private val trackMoodRepository: TrackMoodRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    private val albumGenreRepository: AlbumGenreRepository,
    private val artistGenreRepository: ArtistGenreRepository,
) : MetadataRepository {


    object MusicBrainzTagFilter {

        private val blacklistedKeywords = setOf(
            "seen live", "live", "vinyl", "bootleg", "cd", "album", "lp", "reissue",
            "usa", "uk", "california", "los angeles", "american", "british", "english",
            "90s", "80s", "70s", "1990s", "1980s", "favorite", "favourite", "soundtrack"
        )

        fun filterTags(incomingTags: List<Tag>): List<Tag> {
            return incomingTags
                .map { it.copy(name = it.name.lowercase().trim()) }
                .filter { tag ->
                    !blacklistedKeywords.contains(tag.name)}
        }
    }


    private val artistGenresMB = HashMap<String, List<String>>()

    private suspend fun getAlbumMB(
        mbAlbum: Release,
        album: Album,
        albumArt: String?,
        isUpdate: Boolean = false,
        releaseDate: String?,
    ): Album {
        val newAlbumArt =
            if ((albumArt == null || albumArt == "" || isUpdate) && albumRepository.getAlbumArt(
                    mbAlbum.releaseGroup?.id ?: mbAlbum.id
                ) != null
            )
                albumRepository.getAlbumArt(mbAlbum.releaseGroup?.id ?: mbAlbum.id) else albumArt
        val labelName =
            if (mbAlbum.labelInfo != null && mbAlbum.labelInfo[0].label != null) mbAlbum.labelInfo[0].label!!.name else ""
        val newReleaseDate = releaseDate ?: mbAlbum.date

        val newAlbum = album.copy(
            mbId = mbAlbum.releaseGroup?.id
                ?: mbAlbum.id, //TODO:add column to save if group or release. add argument to getAlbumArt and getAllCAAOptions. Add service method.
            image = newAlbumArt,
            label = labelName,
            releaseDate = newReleaseDate,
            isEnriched = true,
            enrichmentAttempted = true
        )

        val genres = MusicBrainzTagFilter.filterTags(mbAlbum.tags.orEmpty()).map { it.name }
        if (genres.isNotEmpty())
            albumGenreRepository.insertAlbumGenres(newAlbum.id, genres)

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
                    discogsId = discogsResponse.results[i].resource_url.split("/").last(),
                    image = newAlbumArt,
                    label = labelName,
                    releaseDate = newReleaseDate,
                    isEnriched = true,
                    enrichmentAttempted = true
                )

                val genres = discogsResponse.results[i].style.orEmpty()
                if (genres.isNotEmpty())
                    albumGenreRepository.insertAlbumGenres(newAlbum.id, genres)

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

            val genres = MusicBrainzTagFilter.filterTags(mbArtist.tags.orEmpty()).map { it.name }
            Log.d("genres", genres.joinToString())
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
                        isEnriched = true,
                        countryCode = mbArtist.country,
                        country = Locale.Builder().setRegion(mbArtist.country).build().displayCountry,
                        homeCity = mbArtist.beginArea?.name,
                        homeAreaGid = mbArtist.beginArea?.id,
                        activeStartYear = mbArtist.lifeSpan?.begin,
                        activeEndYear = mbArtist.lifeSpan?.end,
                        isDefunct = mbArtist.lifeSpan?.ended == true
                    )
                    else Artist(
                        image = artistImage,
                        discogsId = discogsId,
                        mbId = mbArtist.id,
                        name = artistName,
                        searchKey = artistName.normalizeForMatching(),
                        enrichmentAttempted = true,
                        isEnriched = true,
                        countryCode = mbArtist.country,
                        country = Locale.Builder().setRegion(mbArtist.country).build().displayCountry,
                        homeCity = mbArtist.beginArea?.name,
                        homeAreaGid = mbArtist.beginArea?.id,
                        activeStartYear = mbArtist.lifeSpan?.begin,
                        activeEndYear = mbArtist.lifeSpan?.end,
                        isDefunct = mbArtist.lifeSpan?.ended == true
                    )

                if (genres.isNotEmpty())
                    artistGenresMB.put(mbArtist.id, genres)
                Log.d("genres", artistGenresMB.entries.joinToString())

                return newArtist
            }

            if (genres.isNotEmpty())
                artistGenreRepository.insertArtistGenres(existingArtist.id, genres)

            return existingArtist
        }
        return currentArtist
    }

    private suspend fun getArtistDataDiscogs(
        artist: DiscogsAlbumArtist,
        artistName: String,
        currentArtist: Artist
    ): Artist {
        if (artist.name.normalizeForMatching() == artistName.normalizeForMatching()) {
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

    override suspend fun updateArtist(
        newArtistName: String,
        oldArtist: Artist,
        mbArtist: ArtistSearchInfo?,
        albumToMove: Album?,
        track: Track?
    ): Artist {

        if (mbArtist == null) {
            artistRepository.update(
                oldArtist.copy(
                    name = newArtistName,
                    searchKey = newArtistName.normalizeForMatching()
                )
            )
            return oldArtist
        }

        if (oldArtist.mbId != null && oldArtist.mbId == mbArtist.id) { //same artist, update only name
            artistRepository.update(
                oldArtist.copy(
                    name = newArtistName,
                    searchKey = newArtistName.normalizeForMatching()
                )
            )
            return oldArtist
        }

        val existingArtist = artistRepository.getArtistByMbid(mbArtist.id)
        if (existingArtist != null) { //different artist
            if (existingArtist.name != newArtistName)
                artistRepository.update(
                    existingArtist.copy(
                        name = newArtistName,
                        searchKey = newArtistName.normalizeForMatching()
                    )
                )

            if (track != null) {
                artistRepository.moveTracks(oldArtist.id, existingArtist.id, listOf(track.id))
//                albumArtistRepository.insert(AlbumArtist(albumId = track.albumId, artistId = existingArtist.id))
                albumArtistRepository.updateAlbumArtist(
                    albumId = track.albumId,
                    oldArtistId = oldArtist.id,
                    newArtistId = existingArtist.id
                )
                artistRepository.deleteOrphaned()
                return existingArtist
            }

            //if editing album, move only that album, else move all albums
            val artistAlbums =
                if (albumToMove == null) albumArtistRepository.getAllAlbumsByArtistFull(oldArtist.id)
                    .first() else listOf(albumToMove)

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
        } else {
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
            } else if (track != null) {
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
        } else {
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
        currentArtist: Artist
    ) {
        val existing = artistRepository.getArtistByMbid(mbArtist.id)
        if (existing != null) {
            val albums = albumArtistRepository.getAllAlbumsByArtistFull(currentArtist.id).first()
            for (album in albums) {
                albumArtistRepository.updateAlbumArtist(
                    album.id,
                    currentArtist.id,
                    existing.id
                )
            }

            artistRepository.deleteOrphaned()
        } else {
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
                countryCode = mbArtist.country,
                country = Locale.Builder().setRegion(mbArtist.country).build().displayCountry,
                homeCity = mbArtist.beginArea?.name,
                homeAreaGid = mbArtist.beginArea?.id,
                activeStartYear = mbArtist.lifeSpan?.begin,
                activeEndYear = mbArtist.lifeSpan?.end,
                isDefunct = mbArtist.lifeSpan?.ended == true,
                enrichmentAttempted = true,
                isEnriched = true
            )

            artistRepository.update(updated)

            if (currentArtist.mbId != null)
                artistGenreRepository.insertArtistGenres(currentArtist.id, artistGenresMB.get(currentArtist.mbId).orEmpty())

        }
    }

    override suspend fun moveToAlbum(
        album: Release,
        tracksToMove: List<Int>,
        oldAlbumId: Int
    ) {
        val existing = albumRepository.getAlbumByMbid(album.releaseGroup?.id ?: album.id)
        if (existing == null) {
            val trackInfos = trackRepository.getTracksByIds(tracksToMove)
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
            if (artist == null) {
                val new = getArtistDataMusicBrainz(
                    artistCredit = ArtistSummary(
                        id = album.artistCredit[0].artist.id,
                        name = album.artistCredit[0].artist.name,
                        sortName = album.artistCredit[0].artist.sortName.toString(),
                    ),
                    artistName = album.artistCredit[0].artist.name,
                    currentArtist = Artist(
                        name = album.artistCredit[0].artist.name,
                        searchKey = album.artistCredit[0].artist.name.normalizeForMatching()
                    )
                )

                artistId = artistRepository.insertWithReturn(new).toInt()
            }

            albumArtistRepository.insert(AlbumArtist(albumId = newId, artistId = artistId))
            val allArtists = albumArtistRepository.getAllAlbumArtists(oldAlbumId)
            if (trackInfos[0].artistId != artistId || allArtists.size > 1)
                albumArtistRepository.removeArtistFromAlbum(oldAlbumId, artistId)
            albumRepository.moveTracks(
                oldAlbumId,
                newId,
                tracksToMove
            )
        } else {
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
        if (newArtist.isNotEmpty() && newArtist.size == 1) {
            val trackInfos = trackRepository.getTracksByIds(tracksToMove)
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
            val allArtists = albumArtistRepository.getAllAlbumArtists(oldAlbumId)
            if (trackInfos[0].artistId != newArtist[0].id || allArtists.size > 1)
                albumArtistRepository.removeArtistFromAlbum(oldAlbumId, newArtist[0].id)
            albumArtistRepository.insert(AlbumArtist(albumId = newId, artistId = newArtist[0].id))
            albumRepository.moveTracks(
                oldAlbumId,
                newId,
                tracksToMove
            )

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
    ): AlbumArtistUpdate {

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
        if (oldAlbum.mbId != null
            && oldAlbum.mbId == albumData.album.mbId &&
            existingAlbum != null &&
            oldAlbum.id == existingAlbum.id) { //same album
            albumRepository.update(
                oldAlbum.copy(
                    title = newAlbumTitle,
                    searchKey = newAlbumTitle.normalizeForMatching()
                )
            )

            return AlbumArtistUpdate(oldAlbum, oldArtist)
        }

        var albumToMove: Album? = null
        if (oldAlbum.mbId != null && existingAlbum != null &&
            (oldAlbum.mbId != albumData.album.mbId || oldAlbum.id != existingAlbum.id)
            ) { //different existing album
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
            } else {
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
        } else {
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
        val mbAlbumArtists =
            albumData.mbResponse?.releases?.find { it.id == albumData.album.mbId }?.artistCredit
                ?: emptyList()
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

    override suspend fun backfillGenres(): Flow<ScanProgress> = flow {
        val allAlbums = albumRepository.getAll()

        for (album in allAlbums){
            val mbId = album.mbId
            if (mbId != null){
                val releaseGroupInfo = albumRepository.findReleaseGroupMB(mbId)
                val genres = MusicBrainzTagFilter.filterTags(releaseGroupInfo?.tags.orEmpty()).map { it.name }
                if (genres.isNotEmpty())
                    albumGenreRepository.insertAlbumGenres(album.id, genres)

                for (artist in releaseGroupInfo?.artistCredit.orEmpty()){
                    val artistMbId = artist.artist.id
                    val dbArtist = artistRepository.getArtistByMbid(artistMbId)
                    if (dbArtist != null){
                        val artistGenres = MusicBrainzTagFilter.filterTags(artist.artist.tags.orEmpty()).map { it.name }
                        if (artistGenres.isNotEmpty())
                            artistGenreRepository.insertArtistGenres(dbArtist.id, artistGenres)
                        val albumGenres = albumGenreRepository.getAlbumGenres(album.id)
                        if (albumGenres.isNotEmpty())
                            artistGenreRepository.insertArtistGenres(dbArtist.id, albumGenres)
                    }
                }
                delay(1000)
            }
        }
    }

    override suspend fun backfillCoutriesAndActivity(): Flow<ScanProgress> = flow {
        val allArtists = artistRepository.getAll()
        var current = 0
        val total = allArtists.size

        for (artist in allArtists){
            if (artist.mbId != null){
                val mbArtist = artistRepository.getArtistMusicbrainzInfo(artist.mbId)
                val updatedArtist = artist.copy(
                    countryCode = mbArtist.country,
                    country = mbArtist.area?.name,
                    homeCity = mbArtist.beginArea?.name,
                    activeStartYear = mbArtist.lifeSpan?.begin,
                    activeEndYear = mbArtist.lifeSpan?.end,
                    isDefunct = mbArtist.lifeSpan?.ended == true
                )
                artistRepository.update(updatedArtist)
                delay(1000)
            }
            val progress = ScanProgress(
                current = current + 1,
                total = total,
                currentAlbum = artist.name
            )

            emit(progress)

            current++
        }
    }

    override suspend fun backfillAreas(): Flow<ScanProgress> = flow {
        val allArtists = artistRepository.getAll()
        var current = 0
        val total = allArtists.size

        for (artist in allArtists){
            if (artist.mbId != null){
                val mbArtist = artistRepository.getArtistMusicbrainzInfo(artist.mbId)
                val updatedArtist = artist.copy(
                    countryCode = mbArtist.country,
                    country = Locale.Builder().setRegion(mbArtist.country).build().displayCountry,
                    homeCity = mbArtist.beginArea?.name,
                    homeAreaGid = mbArtist.beginArea?.id
                )
                artistRepository.update(updatedArtist)
                delay(1000)
            }
            val progress = ScanProgress(
                current = current + 1,
                total = total,
                currentAlbum = artist.name
            )

            emit(progress)

            current++
        }
    }

    override suspend fun backfillSimilar(): Flow<ScanProgress> = flow {
        val allArtists = artistRepository.getAll()
        var current = 0
        val total = allArtists.size

        for (artist in allArtists){
            insertSimilarArtists(artist.id, artist.name)
            delay(500)
            val progress = ScanProgress(
                current = current + 1,
                total = total,
                currentAlbum = artist.name
            )

            emit(progress)

            current++
        }
    }


    override suspend fun getLyrics(): Flow<ScanProgress> = flow {

        val done = trackRepository.getTrackWithLyrics()
        val albums = albumRepository.getAll()

        var current = 0
        val total = albums.size

        for (album in albums) {

            val albumTracks = trackRepository.getAlbumTracks(album.id)
            val trackLyricsInsert = mutableListOf<TrackLyrics>()
            val trackInstrumentalUpdate = mutableListOf<Track>()


            val progress = ScanProgress(
                current = current + 1,
                total = total,
                currentAlbum = album.title
            )

            emit(progress)

            current++


            for (track in albumTracks) {
                if (!done.contains(track.trackId)) {
                    val response = trackRepository.getLyrics(track)
                    if (response != null) {
                        val newLyrics = TrackLyrics(
                            trackId = track.trackId,
                            plainLyrics = if (!response.instrumental) response.plainLyrics else "[Instrumental]",
                            syncedLyrics = if (!response.instrumental) response.syncedLyrics else "[Instrumental]"
                        )
                        trackLyricsInsert.add(newLyrics)

                        val tableTrack = trackRepository.getTrackByUri(track.fileUri)
                        if (tableTrack != null) {
                            val toUpdate = tableTrack.copy(
                                instrumental = response.instrumental
                            )
                            trackInstrumentalUpdate.add(toUpdate)
                        }
                    }
                }
            }



            trackRepository.insertAllLyrics(trackLyricsInsert)
            trackRepository.updateAll(trackInstrumentalUpdate)
        }
    }





    private suspend fun insertSimilarArtists(artistId: Int, artistName: String){
        if (artistRepository.getAllSimilarArtists(artistId).isEmpty()){
            val similar = artistRepository.getSimilarArtistsLastfm(artistName)

            val existingSimilar = mutableListOf<SimilarArtists>()

            for (similarArtist in similar){
                val existingMbId = if (similarArtist.mbid != null) artistRepository.getArtistByMbid(similarArtist.mbid) else null
                val existingName = artistRepository.getArtistByName(similarArtist.name.normalizeForMatching())


                if (existingMbId != null || existingName.size == 1){
                    val existing = existingMbId ?: existingName[0]
                    existingSimilar.add(
                        SimilarArtists(
                            artist1Id = artistId,
                            artist2Id = existing.id,
                            similarityScore = similarArtist.match
                        )
                    )
                }

                else if (existingName.size > 1){
                    for (existing in existingName){
                        existingSimilar.add(
                            SimilarArtists(
                                artist1Id = artistId,
                                artist2Id = existing.id,
                                similarityScore = similarArtist.match
                            )
                        )

                    }
                }
            }
            artistRepository.insertSimilar(existingSimilar)
        }
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
            albumRepository.update(albumResponse.album)

            var toInsert = false
            var toUpdate = false


            var currentArtist = artistRepository.getArtist(albumArtist.artistId).first()
            if (albumResponse.mbResponse != null) {
                val mbAlbumArtists = albumResponse.mbResponse.releases.find { release ->
                    val matchesGroup = release.releaseGroup?.id == albumResponse.album.mbId
                    val matchesRelease = release.id == albumResponse.album.mbId
                    matchesGroup || matchesRelease
                }?.artistCredit ?: emptyList()
                for (artistCredit in mbAlbumArtists) {
                    val artist = artistCredit.artist
                    if (currentArtist.mbId == null) {
                        val updatedArtist =
                            getArtistDataMusicBrainz(artist, artistName, currentArtist)
                        if (currentArtist.discogsId != null && currentArtist.discogsId != updatedArtist.discogsId) toInsert = true
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
                    albumRepository.getAlbumDiscogs(albumResponse.discogsResponse.results[0].resource_url.split("/").last())
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

                val genres = albumGenreRepository.getAlbumGenres(albumArtist.albumId)
                if (genres.isNotEmpty())
                    artistGenreRepository.insertArtistGenres(currentArtist.id, genres)

                if (currentArtist.mbId != null)
                    artistGenreRepository.insertArtistGenres(currentArtist.id, artistGenresMB.get(currentArtist.mbId).orEmpty())

                insertSimilarArtists(currentArtist.id, currentArtist.name)

                artistRepository.update(currentArtist)
            } else if (toInsert) {
                currentArtist = currentArtist.copy(enrichmentAttempted = true)
                val inserted = artistRepository.insertWithReturn(currentArtist).toInt()

                val genres = albumGenreRepository.getAlbumGenres(albumArtist.albumId)
                if (genres.isNotEmpty())
                    artistGenreRepository.insertArtistGenres(inserted, genres)

                if (currentArtist.mbId != null)
                    artistGenreRepository.insertArtistGenres(inserted, artistGenresMB.get(currentArtist.mbId).orEmpty())

                insertSimilarArtists(inserted, currentArtist.name)

                albumArtistRepository.updateAlbumArtist(
                    albumArtist.albumId,
                    albumArtist.artistId,
                    inserted
                )
            } else if (currentArtist.enrichmentAttempted == false) {
                currentArtist = currentArtist.copy(enrichmentAttempted = true)

                val genres = albumGenreRepository.getAlbumGenres(albumArtist.albumId)
                if (genres.isNotEmpty())
                    artistGenreRepository.insertArtistGenres(currentArtist.id, genres)


                insertSimilarArtists(currentArtist.id, currentArtist.name)

                artistRepository.update(currentArtist)
            }

            if (!toUpdate && !toInsert)
                delay(1000)
            Log.d("scan", "after artist $artistName")

            val progress = ScanProgress(
                current = current + 1,
                total = total,
                currentAlbum = album.title
            )

            emit(progress)

            current++
        }

    }

    override suspend fun extractAudioFeatures(context: Context): Flow<ScanProgress> = flow {
        val allTracks = trackRepository.getAllUnEnriched()

        var current = 0
        val total = allTracks.size

        for (track in allTracks){

            val audioFeatures = trackRepository.getAudioFeatures(context, track)

            if (audioFeatures != null) {
                val updatedTrack = track.copy(
                    loudness = audioFeatures.loudness,
                    dynamicComplexity = audioFeatures.dynamicComplexity,
                    approachability = audioFeatures.approachability,
                    engagement = audioFeatures.engagement,
                    danceability = audioFeatures.danceability,
                    moodAggressive = audioFeatures.moodAggressive,
                    moodHappy = audioFeatures.moodHappy,
                    moodParty = audioFeatures.moodParty,
                    moodRelaxed = audioFeatures.moodRelaxed,
                    moodSad = audioFeatures.moodSad,
                    instrumental = audioFeatures.instrumental, //only if currently null/not set by lrclib
                    voice = audioFeatures.voice,
                    bpm = audioFeatures.bpm.roundToInt(),
                    key = "${audioFeatures.key.key} ${audioFeatures.key.scale}"
                )

                trackRepository.update(updatedTrack)

                trackMoodRepository.addTrackMoods(track.id, audioFeatures.moods)

            }

            val progress = ScanProgress(
                current = current + 1,
                total = total,
                currentAlbum = track.title
            )

            emit(progress)

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