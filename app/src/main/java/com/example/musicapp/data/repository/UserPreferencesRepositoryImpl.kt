package com.example.musicapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl(private val dataStore: DataStore<Preferences>) :
    UserPreferencesRepository {
    private companion object {
        val INIT_COMPLETED = longPreferencesKey("initial_scan_completed")

        val ARTIST_SORT_FIELD = stringPreferencesKey("artist_sort_field")
        val ARTIST_SORT_ASC = booleanPreferencesKey("artist_sort_ascending")

        val ALBUM_SORT_FIELD = stringPreferencesKey("album_sort_field")
        val ALBUM_SORT_ASC = booleanPreferencesKey("album_sort_ascending")

        val TRACK_SORT_FIELD = stringPreferencesKey("track_sort_field")
        val TRACK_SORT_ASC = booleanPreferencesKey("track_sort_ascending")

        val ARTIST_ALBUMS_SORT_FIELD = stringPreferencesKey("artist_albums_sort_field")
        val ARTIST_ALBUMS_SORT_ASC = booleanPreferencesKey("artist_album_sort_ascending")

        val PLAYLISTS_SORT_FIELD = stringPreferencesKey("playlists_sort_field")
        val PLAYLISTS_SORT_ASC = booleanPreferencesKey("playlists_sort_ascending")

        val GENRES_SORT_FIELD = stringPreferencesKey("genres_sort_field")
        val GENRES_SORT_ASC = booleanPreferencesKey("genres_sort_ascending")

        val COUNTRIES_SORT_FIELD = stringPreferencesKey("countries_sort_field")
        val COUNTRIES_SORT_ASC = booleanPreferencesKey("countries_sort_ascending")

        val AREA_SORT_FIELD = stringPreferencesKey("area_sort_field")

        val LABEL_SORT_FIELD = stringPreferencesKey("label_sort_field")

        val MOODS_SORT_FIELD = stringPreferencesKey("moods_sort_field")
        val MOODS_SORT_ASC = booleanPreferencesKey("moods_sort_ascending")

        val SKIP_SILENCE = booleanPreferencesKey("skip_silence")
        val PLAY_THRESHOLD = doublePreferencesKey("play_threshold")
        val MIN_SIMILARITY_SCORE = doublePreferencesKey("min_similarity_score")
    }

    override val initialScanCompleted: Flow<Long> = dataStore.data.map { prefs ->
        prefs[INIT_COMPLETED] ?: 0
    }.distinctUntilChanged()

    override suspend fun setInitialScanCompleted(timestamp: Long){
        dataStore.edit { prefs ->
            prefs[INIT_COMPLETED] = timestamp
        }
    }

    override val artistsSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[ARTIST_SORT_FIELD] ?: SortField.NAME.name),
            ascending = prefs[ARTIST_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()

    override val albumsSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[ALBUM_SORT_FIELD] ?: SortField.NAME.name),
            ascending = prefs[ALBUM_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()

    override val tracksSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[TRACK_SORT_FIELD] ?: SortField.NAME.name),
            ascending = prefs[TRACK_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()

    override val artistAlbumsSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(
                prefs[ARTIST_ALBUMS_SORT_FIELD] ?: SortField.RELEASE_DATE.name
            ),
            ascending = prefs[ARTIST_ALBUMS_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()


    override val playlistsSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[PLAYLISTS_SORT_FIELD] ?: SortField.NAME.name),
            ascending = prefs[PLAYLISTS_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()

    override val genresSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[GENRES_SORT_FIELD] ?: SortField.TOTAL_COUNT.name),
            ascending = prefs[GENRES_SORT_ASC] ?: false
        )
    }.distinctUntilChanged()

    override val countriesSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[COUNTRIES_SORT_FIELD] ?: SortField.TOTAL_COUNT.name),
            ascending = prefs[COUNTRIES_SORT_ASC] ?: false
        )
    }.distinctUntilChanged()

    override val areasSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[AREA_SORT_FIELD] ?: SortField.TOTAL_COUNT.name),
            ascending = false
        )
    }.distinctUntilChanged()

    override val labelsSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[LABEL_SORT_FIELD] ?: SortField.TOTAL_COUNT.name),
            ascending = false
        )
    }.distinctUntilChanged()

    override val moodsSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[MOODS_SORT_FIELD] ?: SortField.TOTAL_COUNT.name),
            ascending = prefs[MOODS_SORT_ASC] ?: false
        )
    }.distinctUntilChanged()


    override val skipSilenceToggle: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SKIP_SILENCE] ?: false
    }.distinctUntilChanged()

    override val playThreshold: Flow<Double> = dataStore.data.map { prefs ->
        prefs[PLAY_THRESHOLD] ?: 0.5
    }.distinctUntilChanged()

    override val minVisibleSimilarityScore: Flow<Double> = dataStore.data.map { prefs ->
        prefs[MIN_SIMILARITY_SCORE] ?: 0.0
    }.distinctUntilChanged()


    override suspend fun updateArtistsSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[ARTIST_SORT_FIELD] = option.field.name
            prefs[ARTIST_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updateAlbumsSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[ALBUM_SORT_FIELD] = option.field.name
            prefs[ALBUM_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updateTracksSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[TRACK_SORT_FIELD] = option.field.name
            prefs[TRACK_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updateArtistAlbumsSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[ARTIST_ALBUMS_SORT_FIELD] = option.field.name
            prefs[ARTIST_ALBUMS_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updatePlaylistsSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[PLAYLISTS_SORT_FIELD] = option.field.name
            prefs[PLAYLISTS_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updateGenresSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[GENRES_SORT_FIELD] = option.field.name
            prefs[GENRES_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updateCountriesSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[COUNTRIES_SORT_FIELD] = option.field.name
            prefs[COUNTRIES_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updateAreasSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[AREA_SORT_FIELD] = option.field.name
        }
    }


    override suspend fun updateLabelsSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[LABEL_SORT_FIELD] = option.field.name
        }
    }

    override suspend fun updateMoodsSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[MOODS_SORT_FIELD] = option.field.name
            prefs[MOODS_SORT_ASC] = option.ascending
        }
    }



    override suspend fun updateSkipSilence(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[SKIP_SILENCE] = enabled
        }
    }

    override suspend fun updatePlayThreshold(newValue: Double) {
        dataStore.edit { prefs ->
            prefs[PLAY_THRESHOLD] = newValue
        }
    }

    override suspend fun updateMinSimilarityScore(newValue: Double) {
        dataStore.edit { prefs ->
            prefs[MIN_SIMILARITY_SCORE] = newValue
        }
    }


}