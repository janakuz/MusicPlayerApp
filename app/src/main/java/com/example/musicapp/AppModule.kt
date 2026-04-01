package com.example.musicapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.work.WorkManager
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.musicapp.data.dao.AlbumArtistDao
import com.example.musicapp.data.dao.AlbumDao
import com.example.musicapp.data.dao.AlbumGenreDao
import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.dao.GenreDao
import com.example.musicapp.data.dao.MoodDao
import com.example.musicapp.data.dao.QueueDao
import com.example.musicapp.data.dao.TrackDao
import com.example.musicapp.data.dao.TrackMoodDao
import com.example.musicapp.data.database.ALL_MIGRATIONS
import com.example.musicapp.data.database.AppDatabase
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumGenreRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.DynamicThemeRepository
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.data.repository.MoodRepository
import com.example.musicapp.data.repository.impl.OfflineAlbumArtistRepository
import com.example.musicapp.data.repository.impl.OfflineAlbumGenreRepository
import com.example.musicapp.data.repository.impl.OfflineAlbumRepository
import com.example.musicapp.data.repository.impl.OfflineArtistRepository
import com.example.musicapp.data.repository.impl.OfflineDynamicThemeRepository
import com.example.musicapp.data.repository.impl.OfflineGenreRepository
import com.example.musicapp.data.repository.impl.OfflineMetadataRepository
import com.example.musicapp.data.repository.impl.OfflineMoodRepository
import com.example.musicapp.data.repository.impl.OfflinePlayQueueRepository
import com.example.musicapp.data.repository.impl.OfflineTrackMoodRepository
import com.example.musicapp.data.repository.impl.OfflineTrackRepository
import com.example.musicapp.data.repository.impl.OfflineUserPreferencesRepository
import com.example.musicapp.data.repository.impl.OfflineWorkerManagerRepository
import com.example.musicapp.data.repository.PlayQueueRepository
import com.example.musicapp.data.repository.TrackMoodRepository
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.data.repository.WorkerManagerRepository
import com.example.musicapp.data.service.CoverArtArchiveApiService
import com.example.musicapp.data.service.DiscogsApiService
import com.example.musicapp.data.service.LastfmApiService
import com.example.musicapp.data.service.MusicbrainzApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MusicBrainzClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DiscogsClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class LaftfmClient

//    @Qualifier
//    @Retention(AnnotationRetention.BINARY)
//    annotation class SpotifyClient


    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MusicBrainzRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DiscogsRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class LastfmRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CoverArtArchiveRetrofit

//    @Qualifier
//    @Retention(AnnotationRetention.BINARY)
//    annotation class SpotifyRetrofit


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {


        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "music_app_db"
        )
            .addMigrations(*ALL_MIGRATIONS)
            .build()
    }


    @Provides
    @Singleton
    fun provideCoilImageLoader(@ApplicationContext context: Context): ImageLoader{
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(521L * 1024 * 1024)
                    .build()
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideArtistDao(db: AppDatabase): ArtistDao = db.artistDao()

    @Provides
    @Singleton
    fun provideArtistRepository(
        artistDao: ArtistDao,
        trackDao: TrackDao,
        musicbrainzApiService: MusicbrainzApiService,
        discogsApiService: DiscogsApiService,
        lastfmApiService: LastfmApiService): ArtistRepository {
        return OfflineArtistRepository(artistDao, trackDao, musicbrainzApiService, discogsApiService, lastfmApiService)
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
    fun provideUserAgentInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .header("User-Agent", BuildConfig.USER_AGENT)
            .build()
        chain.proceed(request)
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @MusicBrainzClient
    fun provideMusicbrainzOkHttpClient(
        userAgentInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @MusicBrainzRetrofit
    @Singleton
    fun provideMusicbrainzRetrofit(@MusicBrainzClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://musicbrainz.org/ws/2/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @CoverArtArchiveRetrofit
    @Singleton
    fun provideCoverArtArchiveRetrofit(@MusicBrainzClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://coverartarchive.org/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideMusicbrainzApiService(@MusicBrainzRetrofit retrofit: Retrofit): MusicbrainzApiService {
        return retrofit.create(MusicbrainzApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCoverArtArchiveApiService(@CoverArtArchiveRetrofit retrofit: Retrofit): CoverArtArchiveApiService {
        return retrofit.create(CoverArtArchiveApiService::class.java)
    }

    @Provides
    @DiscogsClient
    fun provideDiscogsOkHttpClient(
        userAgentInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Discogs key=${BuildConfig.DISCOGS_KEY}, secret=${BuildConfig.DISCOGS_SECRET}")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @DiscogsRetrofit
    @Singleton
    fun provideDiscogsRetrofit(@DiscogsClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.discogs.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideDiscogsApiService(@DiscogsRetrofit retrofit: Retrofit): DiscogsApiService {
        return retrofit.create(DiscogsApiService::class.java)
    }


    @Provides
    @LastfmRetrofit
    @Singleton
    fun provideLastfmRetrofit(@MusicBrainzClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://ws.audioscrobbler.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideLastfmApiService(@LastfmRetrofit retrofit: Retrofit): LastfmApiService {
        return retrofit.create(LastfmApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAlbumRepository(albumDao: AlbumDao,
                               trackDao: TrackDao,
                               musicbrainzApiService: MusicbrainzApiService,
                               coverArtArchiveApiService: CoverArtArchiveApiService,
                               discogsApiService: DiscogsApiService): AlbumRepository {
        return OfflineAlbumRepository(
            albumDao,
            trackDao,
            musicbrainzApiService,
            coverArtArchiveApiService,
            discogsApiService)
    }

    @Provides
    @Singleton
    fun provideAlbumArtistDao(db: AppDatabase): AlbumArtistDao = db.albumArtistDao()

    @Provides
    @Singleton
    fun provideAlbumArtistRepository(
        albumArtistDao: AlbumArtistDao,
        trackDao: TrackDao): AlbumArtistRepository {
        return OfflineAlbumArtistRepository(albumArtistDao, trackDao)
    }

    @Provides
    @Singleton
    fun provideMetadataRepository(
        albumRepository: AlbumRepository,
        artistRepository: ArtistRepository,
        albumArtistRepository: AlbumArtistRepository): MetadataRepository {
        return OfflineMetadataRepository(albumRepository, artistRepository, albumArtistRepository)
    }


    @Provides
    @Singleton
    fun provideQueueDao(db: AppDatabase): QueueDao = db.queueDao()


    @Provides
    @Singleton
    fun provideGenreDao(db: AppDatabase): GenreDao = db.genreDao()

    @Provides
    @Singleton
    fun provideGenreRepository(genreDao: GenreDao): GenreRepository {
        return OfflineGenreRepository(genreDao)
    }

    @Provides
    @Singleton
    fun provideAlbumGenreDao(db: AppDatabase): AlbumGenreDao = db.albumGenreDao()

    @Provides
    @Singleton
    fun provideAlbumGenreRepository(albumGenreDao: AlbumGenreDao, genreDao: GenreDao): AlbumGenreRepository {
        return OfflineAlbumGenreRepository(albumGenreDao, genreDao)
    }


    @Provides
    @Singleton
    fun provideMoodDao(db: AppDatabase): MoodDao = db.moodDao()

    @Provides
    @Singleton
    fun provideMoodRepository(moodDao: MoodDao): MoodRepository {
        return OfflineMoodRepository(moodDao)
    }

    @Provides
    @Singleton
    fun provideTrackMoodDao(db: AppDatabase): TrackMoodDao = db.trackMoodDao()

    @Provides
    @Singleton
    fun provideTrackMoodRepository(trackMoodDao: TrackMoodDao, moodDao: MoodDao): TrackMoodRepository {
        return OfflineTrackMoodRepository(trackMoodDao, moodDao)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create (
            produceFile = {
                context.preferencesDataStoreFile("preference_file")
            }
        )

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(dataStore: DataStore<Preferences>): UserPreferencesRepository {
        return OfflineUserPreferencesRepository(dataStore)
    }

    @Provides
    @Singleton
    fun provideQueueRepository(queueDao: QueueDao, dataStore: DataStore<Preferences>): PlayQueueRepository {
        return OfflinePlayQueueRepository(dataStore, queueDao)
    }



    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWorkManagerRepository(workManager: WorkManager): WorkerManagerRepository {
        return OfflineWorkerManagerRepository(workManager)
    }

    @Provides
    @Singleton
    fun provideDynamicThemeRepository(imageLoader: ImageLoader): DynamicThemeRepository {
        return OfflineDynamicThemeRepository(imageLoader)
    }

}