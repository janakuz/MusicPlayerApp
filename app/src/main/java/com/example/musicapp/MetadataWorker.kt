package com.example.musicapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.musicapp.data.dto.ArtistSummary
import com.example.musicapp.data.dto.DiscogsAlbumArtist
import com.example.musicapp.data.dto.DiscogsSearchResponse
import com.example.musicapp.data.dto.ReleaseSearchResponse
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

@HiltWorker
class MetadataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val albumArtistRepository: AlbumArtistRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "metadata_sync_channel"
        private const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        setForeground(getForegroundInfo())

        try {
            enrichMetadata()
            return Result.success()
        }
        catch (e: HttpException){
            if (e.code() == 429 || e.code() == 503){
                return Result.retry()
            }
        }
        catch (e: Exception){
            if (e is java.io.IOException) Result.retry() else Result.failure()
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo():
            ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext,CHANNEL_ID)
            .setContentTitle("Enriching Library")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Library Enrichment",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Updating album and artist metadata"
        }
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun enrichMetadata(){
        val currentAlbumArtists = albumArtistRepository.getAllUnenriched()

        var current = 0
        val total = currentAlbumArtists.size

        for (albumArtist in currentAlbumArtists){
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
            if (albumResponse.mbResponse != null){
                for (artistCredit in albumResponse.mbResponse.releases[0].artistCredit){
                    val artist = artistCredit.artist
                    if (currentArtist.mbId == null) {
                        val updatedArtist = getArtistDataMusicBrainz(artist, artistName, currentArtist)
                        if (currentArtist.discogsId != null && currentArtist.discogsId != updatedArtist.discogsId) toInsert = true
                        if (updatedArtist != currentArtist) {
                            currentArtist = updatedArtist
                            toUpdate = true
                        }
                    }
                    else if (currentArtist.mbId != artist.id && artistName.normalizeForMatching()==artist.name.normalizeForMatching()){
                        val newCurrent = Artist(name = currentArtist.name, searchKey = currentArtist.searchKey)
                        val newArtist = getArtistDataMusicBrainz(artist, artistName, newCurrent)
                        currentArtist = newArtist
                        toInsert = true
                    }
                }
            }
            else if (albumResponse.discogsResponse != null){
                delay(1000)
                val discogsAlbum = albumRepository.getAlbumDiscogs(albumResponse.discogsResponse.results[0].resource_url)
                if (discogsAlbum != null && discogsAlbum.artists.isNotEmpty()) {
                    for (artist in discogsAlbum.artists) {
                        if (currentArtist.discogsId == null) {
                            val updatedArtist = getArtistDataDiscogs(artist, artistName, currentArtist)
                            if (updatedArtist != currentArtist) {
                                currentArtist = updatedArtist
                                toUpdate = true
                            }
                        }
                        else if (currentArtist.discogsId != artist.id && artistName.normalizeForMatching()==artist.name.normalizeForMatching()){
                            val newCurrent = Artist(name = currentArtist.name, searchKey = currentArtist.searchKey)
                            val newArtist = getArtistDataDiscogs(artist, artistName, newCurrent)
                            currentArtist = newArtist
                            toInsert = true
                        }
                    }
                }
            }
            if (currentArtist.bio== null || currentArtist.bio=="") {
                val bio = artistRepository.getArtistBio(currentArtist.mbId, artistName)
                currentArtist = currentArtist.copy(bio = bio)
                toUpdate = true
            }
            if (toUpdate && !toInsert) {
                artistRepository.update(currentArtist)
            }
            else if (toInsert) {
                val inserted = artistRepository.insertWithReturn(currentArtist).toInt()
                albumArtistRepository.updateAlbumArtist(albumArtist.albumId, albumArtist.artistId, inserted)
            }

            val progressData = workDataOf(
                "current" to current + 1,
                "total" to total,
                "albumTitle" to album.title
            )
            setProgress(progressData)

            current++
        }

    }

    private suspend fun getAlbumData(album: Album, albumTitle: String, artistName: String, releaseDate: String?, albumArt: String?): AlbumMetadataResult{
        val mbQuery = "release:${albumTitle} artist:${artistName} date:${releaseDate}"
        val mbAlbumSearch = albumRepository.findAlbumMB(mbQuery)

        var i = 0

        if (mbAlbumSearch != null && mbAlbumSearch.releases.isNotEmpty()){
            while (i < mbAlbumSearch.releases.size && mbAlbumSearch.releases[i].title.lowercase().normalizeForMatching()!=albumTitle.lowercase().normalizeForMatching()){
                i++
            }
        }

        if (mbAlbumSearch!= null && i == mbAlbumSearch.releases.size) i = 0

        if (mbAlbumSearch != null && mbAlbumSearch.releases.isNotEmpty() &&
            mbAlbumSearch.releases[i].title.lowercase().normalizeForMatching()==albumTitle.lowercase().normalizeForMatching()) {
            val mbAlbum = mbAlbumSearch.releases[0]
            val newAlbumArt = if (albumArt == null || albumArt=="") albumRepository.getAlbumArt(mbAlbum.id) else albumArt
            val labelName = if (mbAlbum.labelInfo != null && mbAlbum.labelInfo[0].label != null) mbAlbum.labelInfo[0].label!!.name else ""
            val newReleaseDate = releaseDate ?: mbAlbum.date

            val newAlbum = album.copy(mbId = mbAlbum.id, image = newAlbumArt, label = labelName, releaseDate = newReleaseDate, isEnriched = true)
            return AlbumMetadataResult(mbAlbumSearch, null, newAlbum)
        }
        else {
            val discogsResponse = albumRepository.findAlbumDiscogs(artistName, albumTitle, releaseDate?.take(4))

            var i = 0

            if (discogsResponse != null && discogsResponse.results.isNotEmpty()){
                while (i < discogsResponse.results.size &&
                    discogsResponse.results[i].title.split(" - ")[1].normalizeForMatching() == albumTitle.normalizeForMatching()){
                    i++
                }
            }

            if (discogsResponse!= null && i == discogsResponse.results.size) i = 0

            if (discogsResponse != null && discogsResponse.results.isNotEmpty() &&
                discogsResponse.results[i].title.split(" - ")[1].normalizeForMatching() == albumTitle.normalizeForMatching()) {
                val newAlbumArt = if (albumArt == null || albumArt=="") discogsResponse.results[i].cover_image else albumArt
                val labelName = if (discogsResponse.results[i].label != null && discogsResponse.results[i].label?.isNotEmpty() == true)
                    discogsResponse.results[i].label?.get(0) else ""
                val newReleaseDate = releaseDate ?: discogsResponse.results[i].year
                val newAlbum = album.copy(image = newAlbumArt, label = labelName, releaseDate = newReleaseDate, isEnriched = true)
                return AlbumMetadataResult(mbAlbumSearch, discogsResponse, newAlbum)
            }

        }
        return AlbumMetadataResult(null, null, album)
    }


    private suspend fun getArtistDataMusicBrainz(artistCredit: ArtistSummary, artistName: String, currentArtist: Artist): Artist {
        if (artistCredit.name.lowercase().normalizeForMatching() == artistName.lowercase().normalizeForMatching()) {
            delay(1000)
            val mbArtist = artistRepository.getArtistMusicbrainzInfo(artistCredit.id)
            val existingArtist = artistRepository.getArtistByMbid(mbArtist.id)

            if (existingArtist == null){
                var artistImage = ""
                var discogsId = ""
                if (mbArtist.urlRelations != null) {
                    val discogs = mbArtist.urlRelations.find { it.type.equals("discogs") }
                    if (discogs != null && discogs.url != null) {
                        val discogsLink = discogs.url.resource.toString().split("/")
                        discogsId = discogsLink[discogsLink.size - 1]
                        if (currentArtist.discogsId != null && currentArtist.discogsId==discogsId){
                            val newArtist = currentArtist.copy(mbId = mbArtist.id)
                            return newArtist
                        }
                        val artistInfo = artistRepository.getArtistDiscogsInfo(discogsId)
                        if (artistInfo != null && artistInfo.images != null && artistInfo.images.isNotEmpty()){
                            artistImage = artistInfo.images[0].resourceUrl
                        }
                    }
                }
                val newArtist =
                    if (currentArtist.discogsId==null) currentArtist.copy(image = artistImage, discogsId = discogsId, mbId = mbArtist.id, isEnriched = true)
                    else Artist(image = artistImage, discogsId = discogsId, mbId = mbArtist.id, name = artistName, searchKey = artistName.normalizeForMatching(), isEnriched = true)
                return newArtist
            }
        }
        return currentArtist
    }

    private suspend fun getArtistDataDiscogs(artist: DiscogsAlbumArtist, artistName: String, currentArtist: Artist): Artist{
        if (artist.name.normalizeForMatching() == artistName) {
            delay(1000)
            val discogsArtist = artistRepository.getArtistDiscogsInfo(artist.id)
            val discogsId = discogsArtist?.id.toString()

            val artistImage = if (discogsArtist?.images != null && discogsArtist.images.isNotEmpty()) discogsArtist.images.get(0).resourceUrl else ""

            val newArtist = currentArtist.copy(image = artistImage, discogsId = discogsId, isEnriched = true)
            return newArtist
        }
        return currentArtist
    }


}


data class AlbumMetadataResult(
    val mbResponse: ReleaseSearchResponse?,
    val discogsResponse: DiscogsSearchResponse?,
    val album: Album
)