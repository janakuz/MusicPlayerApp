package com.example.musicapp.data.repository

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.musicapp.data.local.dao.AlbumDao
import com.example.musicapp.data.local.dao.ArtistDao
import com.example.musicapp.data.local.dao.TrackDao
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.remote.dto.Key
import com.example.musicapp.util.normalizeGenre
import kotlinx.coroutines.flow.Flow


class FilterRepositoryImpl(
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val trackDao: TrackDao,
) : FilterRepository {

    private fun resolveActiveIntRanges(
        savedRanges: List<IntRange>,
        activeRange: IntRange?,
    ): List<IntRange> {
        return if (activeRange == null) {
            savedRanges
        } else {
            savedRanges + listOf(activeRange)
        }
    }

    private fun resolveActiveLongRanges(
        savedRanges: List<LongRange>,
        activeRange: LongRange,
        defaultSpectrum: LongRange
    ): List<LongRange> {
        return if (activeRange == defaultSpectrum) {
            savedRanges
        } else {
            savedRanges + listOf(activeRange)
        }
    }

    private fun resolveActiveFloatRanges(
        savedRanges: List<ClosedFloatingPointRange<Float>>,
        activeRange: ClosedFloatingPointRange<Float>,
        defaultSpectrum: ClosedFloatingPointRange<Float> = 0.0f..1.0f
    ): List<ClosedFloatingPointRange<Float>> {
        return if (activeRange == defaultSpectrum) {
            savedRanges
        } else {
            savedRanges + listOf(activeRange)
        }
    }


    override fun buildLibraryQueryParts(
        filter: LibraryFilter,
        type: FilterSection,
        initialTimestamp: Long
    ): BoundQuery {
        val conditions = mutableListOf<String>()
        val bindArgs = mutableListOf<Any>()

        val effectiveReleaseDateRanges =
            if (type == FilterSection.ALBUMS || type == FilterSection.GLOBAL)
                resolveActiveIntRanges(
                    filter.dateRanges,
                    filter.activeRange,
                )
            else emptyList()

        val effectiveArtistStartRanges =
            if (type == FilterSection.ARTISTS || type == FilterSection.GLOBAL)
                resolveActiveIntRanges(
                    filter.artistFormedRanges,
                    filter.activeArtistStartRange,
                )
            else emptyList()


        val effectiveArtistEndRanges =
            if ((type == FilterSection.ARTISTS || type == FilterSection.GLOBAL)
                && filter.defunctStatus== DefunctFilterStatus.DEFUNCT)
                resolveActiveIntRanges(
                    filter.artistEndedRanges,
                    filter.activeArtistEndRange,
                )
            else emptyList()

        val effectiveBPMRanges = resolveActiveIntRanges(
            filter.bpmRanges,
            filter.activeBPMRange,
        )

        val effectiveKeys =
            if (filter.activeKeySelection == Key(null, null)) filter.selectedKeys
            else filter.selectedKeys + listOf(filter.activeKeySelection)

        val effectiveApproachabilityRanges = resolveActiveFloatRanges(
            filter.approachabilityRanges,
            filter.activeApproachabilityRange
        )

        val effectiveEngagementRanges = resolveActiveFloatRanges(
            filter.engagementRanges,
            filter.activeEngagementRange
        )

        val effectiveDanceabilityRanges = resolveActiveFloatRanges(
            filter.danceabilityRanges,
            filter.activeDanceabilityRange
        )

        val effectiveMoodAggressiveRanges = resolveActiveFloatRanges(
            filter.moodAggressiveRanges,
            filter.activeAggressiveRange
        )

        val effectiveMoodSadRanges = resolveActiveFloatRanges(
            filter.moodSadRanges,
            filter.activeSadRange
        )

        val effectiveMoodHappyRanges = resolveActiveFloatRanges(
            filter.moodHappyRanges,
            filter.activeHappyRange
        )

        val effectiveMoodPartyRanges = resolveActiveFloatRanges(
            filter.moodPartyRanges,
            filter.activePartyRange
        )

        val effectiveMoodRelaxedRanges = resolveActiveFloatRanges(
            filter.moodRelaxedRanges,
            filter.activeRelaxedRange
        )

        if (effectiveReleaseDateRanges.isNotEmpty()) {
            val rangeClauses = effectiveReleaseDateRanges.map { range ->
                bindArgs.add(range.first)
                bindArgs.add(range.last)
                "(al.releaseDate BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (filter.selectedLabels.isNotEmpty()) {
            val labels = filter.selectedLabels.joinToString(",") { "?" }
            bindArgs.addAll(filter.selectedLabels)
            conditions.add("al.label in ($labels)")
        }

        if (filter.selectedAlbumGenres.isNotEmpty()) {
            val genres = filter.selectedAlbumGenres.map { it.normalizeGenre() }.joinToString(",") { "?" }
            bindArgs.addAll(filter.selectedAlbumGenres)
            conditions.add("g2.name in ($genres)")
        }

        if (filter.selectedArtistGenres.isNotEmpty()) {
            val genres = filter.selectedArtistGenres.map { it.normalizeGenre() }.joinToString(",") { "?" }
            bindArgs.addAll(filter.selectedArtistGenres)
            conditions.add("g1.name in ($genres)")
        }


        if (filter.selectedMoods.isNotEmpty()) {
            val moods = filter.selectedMoods.map { it.normalizeGenre() }.joinToString(",") { "?" }
            bindArgs.addAll(filter.selectedMoods)
            conditions.add("m.name in ($moods)")
        }


        if (filter.selectedCountries.isNotEmpty()) {
            val countries = filter.selectedCountries.joinToString(",") { "?" }
            bindArgs.addAll(filter.selectedCountries)
            conditions.add("ar.countryCode in ($countries)")
        }

        if (filter.selectedAreas.isNotEmpty()){
            val cityGids = mutableListOf<String>()
            val countyGids = mutableListOf<String>()
            val stateGids = mutableListOf<String>()
            val countryGids = mutableListOf<String>()

            filter.selectedAreas.forEach { area ->
                when {
                    area.city != null && area.city == area.county -> countyGids.add(area.county)
                    area.city != null -> cityGids.add(area.city)
                    area.county != null -> countyGids.add(area.county)
                    area.state != null -> stateGids.add(area.state)
                    area.country != null -> countryGids.add(area.country)
                }
            }

            val areaConditions = mutableListOf<String>()

            if (cityGids.isNotEmpty()) {
                val placeholders = cityGids.joinToString(",") { "?" }
                bindArgs.addAll(cityGids)
                areaConditions.add("ah.city IN ($placeholders)")
            }

            if (countyGids.isNotEmpty()) {
                val placeholders = countyGids.joinToString(",") { "?" }
                bindArgs.addAll(countyGids)
                areaConditions.add("ah.county IN ($placeholders)")
            }

            if (stateGids.isNotEmpty()) {
                val placeholders = stateGids.joinToString(",") { "?" }
                bindArgs.addAll(stateGids)
                areaConditions.add("ah.state IN ($placeholders)")
            }

            if (countryGids.isNotEmpty()) {
                val placeholders = countryGids.joinToString(",") { "?" }
                bindArgs.addAll(countryGids)
                areaConditions.add("ah.country IN ($placeholders)")
            }

            Log.d("area filter", "(${areaConditions.joinToString(" OR ")})")

            conditions.add("(${areaConditions.joinToString(" OR ")})")
        }

        when (filter.defunctStatus) {
            DefunctFilterStatus.ALL -> {}
            DefunctFilterStatus.ACTIVE -> {conditions.add("ar.isDefunct = false")}
            DefunctFilterStatus.DEFUNCT -> {conditions.add("ar.isDefunct = true")}
        }


        if (effectiveArtistStartRanges.isNotEmpty()) {
            val rangeClauses = effectiveArtistStartRanges.map { range ->
                bindArgs.add(range.first)
                bindArgs.add(range.last)
                "(ar.activeStartYear BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }


        if (effectiveArtistEndRanges.isNotEmpty()) {
            val rangeClauses = effectiveArtistEndRanges.map { range ->
                bindArgs.add(range.first)
                bindArgs.add(range.last)
                "(ar.activeEndYear BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }


        when (filter.instrumental) {
            Instrumental.ANY -> {}
            Instrumental.VOCAL ->  {conditions.add("t.instrumental = false")}
            Instrumental.INSTRUMENTAL -> {conditions.add("t.instrumental = true")}
        }

        when (filter.voice) {
            VoiceGender.ALL -> {}
            VoiceGender.MALE -> {
                conditions.add("t.voice = ?")
                bindArgs.add("male")
            }
            VoiceGender.FEMALE -> {
                conditions.add("t.voice = ?")
                bindArgs.add("female")
            }
            VoiceGender.MIXED -> {
                conditions.add("t.voice = ?")
                bindArgs.add("mixed")
            }
        }

        if (effectiveKeys.isNotEmpty()) {
            val keyClauses = effectiveKeys.map { fullKey ->
                val pattern = when {
                    fullKey.key != null && fullKey.scale != null -> "${fullKey.key} ${fullKey.scale}"
                    fullKey.key != null -> "${fullKey.key} %"
                    fullKey.scale != null -> "% ${fullKey.scale}"
                    else -> "%"
                }
                bindArgs.add(pattern)
                "t.key LIKE ?"
            }
            conditions.add("(${keyClauses.joinToString(" OR ")})")
        }

        if (effectiveBPMRanges.isNotEmpty()) {
            val rangeClauses = effectiveBPMRanges.map { range ->
                bindArgs.add(range.first)
                bindArgs.add(range.last)
                "(t.bpm BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (effectiveApproachabilityRanges.isNotEmpty()) {
            val rangeClauses = effectiveApproachabilityRanges.map { range ->
                bindArgs.add(range.start)
                bindArgs.add(range.endInclusive)
                "(t.approachability BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (effectiveEngagementRanges.isNotEmpty()) {
            val rangeClauses = effectiveEngagementRanges.map { range ->
                bindArgs.add(range.start)
                bindArgs.add(range.endInclusive)
                "(t.engagement BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (effectiveDanceabilityRanges.isNotEmpty()) {
            val rangeClauses = effectiveDanceabilityRanges.map { range ->
                bindArgs.add(range.start)
                bindArgs.add(range.endInclusive)
                "(t.danceability BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (effectiveMoodAggressiveRanges.isNotEmpty()) {
            val rangeClauses = effectiveMoodAggressiveRanges.map { range ->
                bindArgs.add(range.start)
                bindArgs.add(range.endInclusive)
                "(t.moodAggressive BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (effectiveMoodHappyRanges.isNotEmpty()) {
            val rangeClauses = effectiveMoodHappyRanges.map { range ->
                bindArgs.add(range.start)
                bindArgs.add(range.endInclusive)
                "(t.moodHappy BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (effectiveMoodPartyRanges.isNotEmpty()) {
            val rangeClauses = effectiveMoodPartyRanges.map { range ->
                bindArgs.add(range.start)
                bindArgs.add(range.endInclusive)
                "(t.moodParty BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (effectiveMoodRelaxedRanges.isNotEmpty()) {
            val rangeClauses = effectiveMoodRelaxedRanges.map { range ->
                bindArgs.add(range.start)
                bindArgs.add(range.endInclusive)
                "(t.moodRelaxed BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (effectiveMoodSadRanges.isNotEmpty()) {
            val rangeClauses = effectiveMoodSadRanges.map { range ->
                bindArgs.add(range.start)
                bindArgs.add(range.endInclusive)
                "(t.moodSad BETWEEN ? AND ?)"
            }
            conditions.add("(${rangeClauses.joinToString(" OR ")})")
        }

        if (filter.addedInPastDays != null) {
            val cutoffTimestamp = System.currentTimeMillis() - (filter.addedInPastDays.toLong() * 24 * 60 * 60 * 1000L)
            val finalThreshold = maxOf(cutoffTimestamp, initialTimestamp)
            conditions.add("t.dateAdded > ?")
            bindArgs.add(finalThreshold)
        }

        if (filter.playedInPastDays != null) {
            val cutoffTimestamp = System.currentTimeMillis() - (filter.playedInPastDays.toLong() * 24 * 60 * 60 * 1000L)
            conditions.add("t.lastPlayed >= ?")
            bindArgs.add(cutoffTimestamp)
        }

        if (filter.minPlays != null) {
            bindArgs.add(filter.minPlays)
            conditions.add("t.plays >= ?")
        }

        if (filter.maxPlays != null) {
            bindArgs.add(filter.maxPlays)
            conditions.add("t.plays <= ?")
        }

        if (filter.topPlaysPercentage != null){
            bindArgs.add(filter.topPlaysPercentage)
            conditions.add("t.plays > 0")
            conditions.add(
                """
                t.plays >= (
                        SELECT plays FROM tracks 
                        WHERE plays > 0 
                        ORDER BY plays DESC 
                        LIMIT 1 OFFSET MAX(0, CAST(ROUND((SELECT COUNT(*) FROM tracks WHERE plays > 0) * ? / 100.0) AS INT) - 1)
                    )
                """)
        }


        val baseQuery =
            when (type) {
                FilterSection.ALBUMS ->
                    """
                     SELECT al.id as albumId, al.title, al.releaseDate, ar.name as artistName, ar.id as artistId, al.image, al.label, al.mbId, al.duration, al.numTracks
                     FROM albums al
                     JOIN album_artists aa ON al.id=aa.albumId
                     JOIN artists ar ON ar.id=aa.artistId
                     LEFT JOIN album_genres ag ON ag.albumId=al.id
                     LEFT JOIN genres g2 ON ag.genreId=g2.id
                    """.trimIndent()

                FilterSection.ARTISTS ->
                    """
                    SELECT ar.*
                    FROM artists ar
                    LEFT JOIN artist_genres ag on ag.artistId=ar.id
                    LEFT JOIN area_hierarchy ah on ah.gid=ar.homeAreaGid
                    LEFT JOIN genres g1 on ag.genreId=g1.id
                """.trimIndent()

                FilterSection.TRACKS ->
                    """
                    SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, al.image as albumArt, t.trackNumber as trackNum, 
                            t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
                    FROM tracks t
                    JOIN artists ar ON t.artistId=ar.id
                    JOIN albums al ON t.albumId=al.id
                    LEFT JOIN track_moods tm ON tm.trackId=t.id
                    LEFT JOIN moods m on tm.moodId=m.id
                """.trimIndent()

                FilterSection.GLOBAL ->
                    """
                    SELECT t.id as trackId, t.title as title, ar.name as artistName, al.title as albumTitle, al.image as albumArt, t.trackNumber as trackNum, 
                            t.duration as duration, t.fileUri as fileUri, t.filePath as filePath, t.albumId as albumId, t.artistId as artistId 
                    FROM tracks t
                    JOIN artists ar ON t.artistId=ar.id
                    JOIN albums al ON t.albumId=al.id
                    LEFT JOIN track_moods tm ON tm.trackId=t.id
                    LEFT JOIN moods m on tm.moodId=m.id
                    LEFT JOIN artist_genres ag on ag.artistId=ar.id
                    LEFT JOIN genres g1 on ag.genreId=g1.id
                    LEFT JOIN album_genres alg ON alg.albumId=al.id
                    LEFT JOIN genres g2 ON alg.genreId=g2.id
                """.trimIndent()
            }

        val joiner = if (filter.logic == FilterLogic.AND) " AND " else " OR "
        val sql = baseQuery + if (conditions.isNotEmpty()) {
            " WHERE ${conditions.joinToString(joiner)}"
        } else ""
        val sqlGrouped = if (type == FilterSection.ALBUMS) "$sql GROUP BY al.id" else if (type == FilterSection.ARTISTS) "$sql GROUP BY ar.id" else "$sql GROUP BY t.id"

        return BoundQuery(sqlGrouped, bindArgs.toTypedArray())
    }

    override fun buildLibraryQuery(
        filter: LibraryFilter,
        section: FilterSection,
        initialTimestamp: Long
    ): SimpleSQLiteQuery {
        return buildLibraryQueryParts(filter, section, initialTimestamp).toSimpleSQLiteQuery()
    }

    override fun getFilteredAlbums(filter: LibraryFilter, initialTimestamp: Long): Flow<List<AlbumInfo>> {
        val rawQuery = buildLibraryQuery(filter, FilterSection.ALBUMS, initialTimestamp)

        return albumDao.getFilteredAlbums(rawQuery)
    }

    override fun getFilteredArtists(filter: LibraryFilter, initialTimestamp: Long): Flow<List<Artist>> {
        val rawQuery = buildLibraryQuery(filter, FilterSection.ARTISTS, initialTimestamp)

        return artistDao.getFilteredArtists(rawQuery)
    }

    override fun getFilteredTracks(filter: LibraryFilter, initialTimestamp: Long): Flow<List<TrackInfo>> {
        val rawQuery = buildLibraryQuery(filter, FilterSection.TRACKS, initialTimestamp)

        return trackDao.getFilteredTracks(rawQuery)
    }

    override fun getGlobalTracks(filter: LibraryFilter, initialTimestamp: Long): Flow<List<TrackInfo>> {
        val rawQuery = buildLibraryQuery(filter, FilterSection.GLOBAL, initialTimestamp)

        return trackDao.getFilteredTracksGlobal(rawQuery)
    }

    override fun getMinYear(): Flow<Int> {
        return albumDao.getMinYear()
    }

    override fun getMaxYear(): Flow<Int> {
        return albumDao.getMaxYear()
    }

    override fun getMinYearArtists(): Flow<Int> {
        return artistDao.getMinYear()
    }

    override fun getMaxYearArtists(): Flow<Int> {
        return artistDao.getMaxYear()
    }

    override fun getAllLabels(): Flow<List<String>> {
        return albumDao.getAllLabels()
    }

    override fun findLabel(query: String): Flow<List<String>> {
        return albumDao.findLabel(query)
    }
}