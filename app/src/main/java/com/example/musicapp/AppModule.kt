package com.example.musicapp

import android.content.Context
import android.util.Log
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideArtistDao(db: AppDatabase): ArtistDao = db.artistDao()

    @Provides
    @Singleton
    fun provideArtistRepository(
        artistDao: ArtistDao,
        musicbrainzApiService: MusicbrainzApiService,
        discogsApiService: DiscogsApiService,
        lastfmApiService: LastfmApiService): ArtistRepository {
        return OfflineArtistRepository(artistDao, musicbrainzApiService, discogsApiService, lastfmApiService)
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
        level = HttpLoggingInterceptor.Level.BODY
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
                               musicbrainzApiService: MusicbrainzApiService,
                               coverArtArchiveApiService: CoverArtArchiveApiService): AlbumRepository {
        return OfflineAlbumRepository(albumDao, musicbrainzApiService, coverArtArchiveApiService)
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