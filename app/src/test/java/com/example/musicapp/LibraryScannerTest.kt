package com.example.musicapp

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.TrackRepository


class LibraryScannerTest {
    val libraryScanner: LibraryScanner = LibraryScanner(
        artistRepository = mockk<ArtistRepository>(),
        albumRepository = mockk<AlbumRepository>(),
        albumArtistRepository = mockk<AlbumArtistRepository>(),
        trackRepository = mockk<TrackRepository>()
    )

    @Test
    fun testNormalizeTrackNumberSmall() {
        assertAll ( "normalize numbers direct",
            { assertEquals(1, LibraryScanner.normalizeTrackNumber(1)) },
            { assertEquals(10, LibraryScanner.normalizeTrackNumber(10)) },
            { assertEquals(123, LibraryScanner.normalizeTrackNumber(123))}
        );
    }

    @Test
    fun testNormalizeTrackNumberLarge() {
        assertAll ( "normalize numbers normalized",
            { assertEquals(1, LibraryScanner.normalizeTrackNumber(1001)) },
            { assertEquals(5, LibraryScanner.normalizeTrackNumber(1005)) },
            { assertEquals(10, LibraryScanner.normalizeTrackNumber(1010)) },
            { assertEquals(7, LibraryScanner.normalizeTrackNumber(2007)) },
            { assertEquals(11, LibraryScanner.normalizeTrackNumber(2011)) },
        );
    }

    @Test
    fun testNormalizeTrackNumberNull() {
        assertAll ( "normalize numbers null or 0",
            { assertEquals(0, LibraryScanner.normalizeTrackNumber(0)) },
            { assertEquals(0, LibraryScanner.normalizeTrackNumber(1000)) },
            { assertNull(LibraryScanner.normalizeTrackNumber(null)) },
        );
    }

}