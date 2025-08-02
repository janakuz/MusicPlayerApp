package com.example.musicapp

import android.content.Context
import androidx.room.Room
import com.example.musicapp.data.dao.AlbumArtistDao
import com.example.musicapp.data.dao.AlbumDao
import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.dao.TrackDao
import com.example.musicapp.data.database.AppDatabase
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.OfflineAlbumArtistRepository
import com.example.musicapp.data.repository.OfflineAlbumRepository
import com.example.musicapp.data.repository.OfflineArtistRepository
import com.example.musicapp.data.repository.OfflineTrackRepository
import com.example.musicapp.data.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "music_app_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideArtistDao(db: AppDatabase): ArtistDao = db.artistDao()

    @Provides
    @Singleton
    fun provideArtistRepository(artistDao: ArtistDao): ArtistRepository {
        return OfflineArtistRepository(artistDao)
    }

    @Provides
    @Singleton
    fun provideTrackDao(db: AppDatabase): TrackDao = db.trackDao()

    @Provides
    @Singleton
    fun provideTrackRepository(trackDao: TrackDao): TrackRepository {
        return OfflineTrackRepository(trackDao)
    }

    @Provides
    @Singleton
    fun provideAlbumDao(db: AppDatabase): AlbumDao = db.albumDao()

    @Provides
    @Singleton
    fun provideAlbumRepository(albumDao: AlbumDao): AlbumRepository {
        return OfflineAlbumRepository(albumDao)
    }

    @Provides
    @Singleton
    fun provideAlbumArtistDao(db: AppDatabase): AlbumArtistDao = db.albumArtistDao()

    @Provides
    @Singleton
    fun provideAlbumArtistRepository(albumArtistDao: AlbumArtistDao): AlbumArtistRepository {
        return OfflineAlbumArtistRepository(albumArtistDao)
    }

}