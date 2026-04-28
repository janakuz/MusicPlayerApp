package com.example.musicapp.data.repository.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.musicapp.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.components.SortField
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


class OfflineUserPreferencesRepository(private val dataStore: DataStore<Preferences>) :
    UserPreferencesRepository {
    private companion object {
        val ARTIST_SORT_FIELD = stringPreferencesKey("artist_sort_field")
        val ARTIST_SORT_ASC = booleanPreferencesKey("artist_sort_ascending")

        val ALBUM_SORT_FIELD = stringPreferencesKey("album_sort_field")
        val ALBUM_SORT_ASC = booleanPreferencesKey("album_sort_ascending")

        val TRACK_SORT_FIELD = stringPreferencesKey("track_sort_field")
        val TRACK_SORT_ASC = booleanPreferencesKey("track_sort_ascending")

        val ARTIST_ALBUMS_SORT_FIELD = stringPreferencesKey("artist_albums_sort_field")
        val ARTIST_ALBUMS_SORT_ASC = booleanPreferencesKey("artist_album_sort_ascending")
    }

    override val artistSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[ARTIST_SORT_FIELD] ?: SortField.NAME.name),
            ascending = prefs[ARTIST_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()

    override val albumSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[ALBUM_SORT_FIELD] ?: SortField.NAME.name),
            ascending = prefs[ALBUM_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()

    override val trackSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[TRACK_SORT_FIELD] ?: SortField.NAME.name),
            ascending = prefs[TRACK_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()

    override val artistAlbumsSortOption: Flow<SortOption> = dataStore.data.map { prefs ->
        SortOption(
            field = SortField.valueOf(prefs[ARTIST_ALBUMS_SORT_FIELD] ?: SortField.RELEASE_DATE.name),
            ascending = prefs[ARTIST_ALBUMS_SORT_ASC] ?: true
        )
    }.distinctUntilChanged()


    override suspend fun updateArtistSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[ARTIST_SORT_FIELD] = option.field.name
            prefs[ARTIST_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updateAlbumSort(option: SortOption) {
        dataStore.edit { prefs ->
            prefs[ALBUM_SORT_FIELD] = option.field.name
            prefs[ALBUM_SORT_ASC] = option.ascending
        }
    }

    override suspend fun updateTrackSort(option: SortOption) {
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
}