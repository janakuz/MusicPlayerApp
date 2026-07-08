package com.example.musicapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.musicapp.BuildConfig
import com.example.musicapp.data.local.dao.AlbumArtistDao
import com.example.musicapp.data.local.dao.AlbumDao
import com.example.musicapp.data.local.dao.AlbumGenreDao
import com.example.musicapp.data.local.dao.ArtistDao
import com.example.musicapp.data.local.dao.ArtistGenreDao
import com.example.musicapp.data.local.dao.AreaDao
import com.example.musicapp.data.local.dao.GenreDao
import com.example.musicapp.data.local.dao.MoodDao
import com.example.musicapp.data.local.dao.PlaylistDao
import com.example.musicapp.data.local.dao.PlaylistTracksDao
import com.example.musicapp.data.local.dao.QueueDao
import com.example.musicapp.data.local.dao.SequencerDao
import com.example.musicapp.data.local.dao.TrackDao
import com.example.musicapp.data.local.dao.TrackMoodDao
import com.example.musicapp.data.local.database.ALL_MIGRATIONS
import com.example.musicapp.data.local.database.AppDatabase
import com.example.musicapp.data.local.database.populateMetadataFromAsset
import com.example.musicapp.data.remote.service.CoverArtArchiveApiService
import com.example.musicapp.data.remote.service.DiscogsApiService
import com.example.musicapp.data.remote.service.EssentiaApiService
import com.example.musicapp.data.remote.service.LRCLibApiService
import com.example.musicapp.data.remote.service.LastfmApiService
import com.example.musicapp.data.remote.service.MusicbrainzApiService
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumArtistRepositoryImpl
import com.example.musicapp.data.repository.AlbumGenreRepository
import com.example.musicapp.data.repository.AlbumGenreRepositoryImpl
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.AlbumRepositoryImpl
import com.example.musicapp.data.repository.ArtistGenreRepository
import com.example.musicapp.data.repository.ArtistGenreRepositoryImpl
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.ArtistRepositoryImpl
import com.example.musicapp.data.repository.AreaRepository
import com.example.musicapp.data.repository.AreaRepositoryImpl
import com.example.musicapp.data.repository.DynamicThemeRepository
import com.example.musicapp.data.repository.DynamicThemeRepositoryImpl
import com.example.musicapp.data.repository.FilterRepository
import com.example.musicapp.data.repository.FilterRepositoryImpl
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.GenreRepositoryImpl
import com.example.musicapp.data.repository.MetadataRepository
import com.example.musicapp.data.repository.MoodRepository
import com.example.musicapp.data.repository.MoodRepositoryImpl
import com.example.musicapp.data.repository.OfflineMetadataRepository
import com.example.musicapp.data.repository.OfflinePlayQueueRepository
import com.example.musicapp.data.repository.PlayQueueRepository
import com.example.musicapp.data.repository.PlaylistRepository
import com.example.musicapp.data.repository.PlaylistRepositoryImpl
import com.example.musicapp.data.repository.PlaylistTracksRepository
import com.example.musicapp.data.repository.PlaylistTracksRepositoryImpl
import com.example.musicapp.data.repository.SearchRepository
import com.example.musicapp.data.repository.SearchRepositoryImpl
import com.example.musicapp.data.repository.SequencerRepository
import com.example.musicapp.data.repository.SequencerRepositoryImpl
import com.example.musicapp.data.repository.TrackMoodRepository
import com.example.musicapp.data.repository.TrackMoodRepositoryImpl
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.data.repository.TrackRepositoryImpl
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.data.repository.UserPreferencesRepositoryImpl
import com.example.musicapp.data.repository.WorkerManagerRepository
import com.example.musicapp.data.repository.WorkerManagerRepositoryImpl
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
import java.util.concurrent.TimeUnit
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
    annotation class LastfmClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class EssentiaClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class LRCLibClient

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
    annotation class LRCLibRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CoverArtArchiveRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class EssentiaRetrofit

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {


        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "music_app_db"
        )
            .addMigrations(*ALL_MIGRATIONS)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)

                    val cursor = db.query("SELECT COUNT(*) FROM area_hierarchy")
                    cursor.moveToFirst()
                    val count = cursor.getInt(0)
                    cursor.close()

                    if (count == 0) {
                        populateMetadataFromAsset(context, db)
                    }
                }
            })
            .build()
    }


    @Provides
    @Singleton
    fun provideCoilImageLoader(@ApplicationContext context: Context): ImageLoader {
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
        lastfmApiService: LastfmApiService
    ): ArtistRepository {
        return ArtistRepositoryImpl(
            artistDao,
            trackDao,
            musicbrainzApiService,
            discogsApiService,
            lastfmApiService
        )
    }

    @Provides
    @Singleton
    fun provideTrackDao(db: AppDatabase): TrackDao = db.trackDao()

    @Provides
    @Singleton
    fun provideTrackRepository(
        trackDao: TrackDao,
        audioFeaturesApi: EssentiaApiService,
        lyricsApi: LRCLibApiService): TrackRepository {
        return TrackRepositoryImpl(trackDao, audioFeaturesApi, lyricsApi)
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
                .addHeader(
                    "Authorization",
                    "Discogs key=${BuildConfig.DISCOGS_KEY}, secret=${BuildConfig.DISCOGS_SECRET}"
                )
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
    @EssentiaClient
    fun provideEssentiaOkHttpClient(
        userAgentInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("X-API-KEY", BuildConfig.ESSENTIA_KEY)
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @EssentiaRetrofit
    fun provideEssentiaRetrofit(
        @EssentiaClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.72:8000/")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideEssentiaApiService(@EssentiaRetrofit retrofit: Retrofit): EssentiaApiService {
        return retrofit.create(EssentiaApiService::class.java)
    }


    @Provides
    @LRCLibClient
    fun provideLRCLibOkHttpClient(
        userAgentInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()


    @Provides
    @LRCLibRetrofit
    @Singleton
    fun provideLRCLIbRetrofit(@LRCLibClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideLRCLibService(@LRCLibRetrofit retrofit: Retrofit): LRCLibApiService {
        return retrofit.create(LRCLibApiService::class.java)
    }


    @Provides
    @Singleton
    fun provideAlbumRepository(
        albumDao: AlbumDao,
        trackDao: TrackDao,
        musicbrainzApiService: MusicbrainzApiService,
        coverArtArchiveApiService: CoverArtArchiveApiService,
        discogsApiService: DiscogsApiService
    ): AlbumRepository {
        return AlbumRepositoryImpl(
            albumDao,
            trackDao,
            musicbrainzApiService,
            coverArtArchiveApiService,
            discogsApiService
        )
    }

    @Provides
    @Singleton
    fun provideAlbumArtistDao(db: AppDatabase): AlbumArtistDao = db.albumArtistDao()

    @Provides
    @Singleton
    fun provideAlbumArtistRepository(
        albumArtistDao: AlbumArtistDao,
        trackDao: TrackDao
    ): AlbumArtistRepository {
        return AlbumArtistRepositoryImpl(albumArtistDao, trackDao)
    }



    @Provides
    @Singleton
    fun provideQueueDao(db: AppDatabase): QueueDao = db.queueDao()


    @Provides
    @Singleton
    fun provideGenreDao(db: AppDatabase): GenreDao = db.genreDao()


    @Provides
    @Singleton
    fun provideAlbumGenreDao(db: AppDatabase): AlbumGenreDao = db.albumGenreDao()

    @Provides
    @Singleton
    fun provideArtistGenreDao(db: AppDatabase): ArtistGenreDao = db.artistGenreDao()


    @Provides
    @Singleton
    fun provideAlbumGenreRepository(
        albumGenreDao: AlbumGenreDao,
        genreDao: GenreDao
    ): AlbumGenreRepository {
        return AlbumGenreRepositoryImpl(albumGenreDao, genreDao)
    }


    @Provides
    @Singleton
    fun provideMoodDao(db: AppDatabase): MoodDao = db.moodDao()

    @Provides
    @Singleton
    fun provideMoodRepository(moodDao: MoodDao): MoodRepository {
        return MoodRepositoryImpl(moodDao)
    }

    @Provides
    @Singleton
    fun provideTrackMoodDao(db: AppDatabase): TrackMoodDao = db.trackMoodDao()

    @Provides
    @Singleton
    fun provideTrackMoodRepository(
        trackMoodDao: TrackMoodDao,
        moodDao: MoodDao
    ): TrackMoodRepository {
        return TrackMoodRepositoryImpl(trackMoodDao, moodDao)
    }



    @Provides
    @Singleton
    fun provideArtistGenreRepository(
        artistDao: ArtistGenreDao,
        genreDao: GenreDao
    ): ArtistGenreRepository {
        return ArtistGenreRepositoryImpl(artistDao, genreDao)
    }

    @Provides
    @Singleton
    fun provideGenreRepository(genreDao: GenreDao, albumGenreDao: AlbumGenreDao, artistGenreDao: ArtistGenreDao): GenreRepository {
        return GenreRepositoryImpl(genreDao, albumGenreDao, artistGenreDao)
    }


    @Provides
    @Singleton
    fun provideSearchRepository(
        artistDao: ArtistDao,
        albumDao: AlbumDao,
        trackDao: TrackDao
    ): SearchRepository {
        return SearchRepositoryImpl(artistDao, albumDao, trackDao)
    }

    @Provides
    @Singleton
    fun provideFilterRepository(albumDao: AlbumDao, artistDao: ArtistDao, trackDao: TrackDao): FilterRepository {
        return FilterRepositoryImpl(albumDao, artistDao, trackDao)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile("preference_file")
            }
        )

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(dataStore: DataStore<Preferences>): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideQueueRepository(
        queueDao: QueueDao,
        dataStore: DataStore<Preferences>
    ): PlayQueueRepository {
        return OfflinePlayQueueRepository(dataStore, queueDao)
    }


    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.Companion.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWorkManagerRepository(workManager: WorkManager): WorkerManagerRepository {
        return WorkerManagerRepositoryImpl(workManager)
    }

    @Provides
    @Singleton
    fun provideDynamicThemeRepository(imageLoader: ImageLoader): DynamicThemeRepository {
        return DynamicThemeRepositoryImpl(imageLoader)
    }

    @Provides
    @Singleton
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()

    @Provides
    @Singleton
    fun providePlaylistTracksDao(db: AppDatabase): PlaylistTracksDao = db.playlistTracksDao()


    @Provides
    @Singleton
    fun providePlaylistRepository(
        playlistDao: PlaylistDao,
        playlistTracksDao: PlaylistTracksDao,
        db: AppDatabase,
        @ApplicationContext context: Context
    ): PlaylistRepository {
        return PlaylistRepositoryImpl(playlistDao, playlistTracksDao, db, context)
    }

    @Provides
    @Singleton
    fun providePlaylistTracksRepository(
        playlistTracksDao: PlaylistTracksDao,
        playlistRepository: PlaylistRepository
    ): PlaylistTracksRepository {
        return PlaylistTracksRepositoryImpl(
            playlistTracksDao,
            playlistRepository
        )
    }

    @Provides
    @Singleton
    fun provideAreaDao(db: AppDatabase): AreaDao = db.areaDao()

    @Provides
    @Singleton
    fun provideAreaRepository(areaDao: AreaDao): AreaRepository {
        return AreaRepositoryImpl(areaDao)
    }


    @Provides
    @Singleton
    fun provideMetadataRepository(
        albumRepository: AlbumRepository,
        artistRepository: ArtistRepository,
        trackRepository: TrackRepository,
        trackMoodRepository: TrackMoodRepository,
        albumArtistRepository: AlbumArtistRepository,
        albumGenreRepository: AlbumGenreRepository,
        artistGenreRepository: ArtistGenreRepository
    ): MetadataRepository {
        return OfflineMetadataRepository(
            albumRepository,
            artistRepository,
            trackRepository,
            trackMoodRepository,
            albumArtistRepository,
            albumGenreRepository,
            artistGenreRepository
        )
    }

    @Provides
    @Singleton
    fun provideSequencerDao(db: AppDatabase): SequencerDao = db.sequencerDao()

    @Provides
    @Singleton
    fun provideSequencerRepository(sequencerDao: SequencerDao, playlistTracksDao: PlaylistTracksDao): SequencerRepository {
        return SequencerRepositoryImpl(sequencerDao, playlistTracksDao)
    }


}