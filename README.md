# MusicApp (Personal Android Music Player)

A self-hosted, local-first Android music player focused on using your own files, rich metadata (Last.fm / MusicBrainz / Discogs), smart playlists, and a reorderable play queue. Built with Jetpack Compose, Room, Hilt, Media3/ExoPlayer, and offline-first design.

> Currently intended for personal use only (APK side-loaded, not published).

## Features (work-in-progress)

- Local library scanning (MediaStore / file URIs)
- Artist / Album / Track data stored in Room with rich metadata
- Reorderable play queue with persistence
- Now playing screen with playback controls
- DTOs and relations for efficient UI views
- Hilt dependency injection
- Scrobbling / metadata enrichment (planned)
- Smart playlists, moods, and tagging (planned)

## Tech stack

- Kotlin (preferred version in `libs.versions.toml`)
- Jetpack Compose UI
- Room for local database
- Hilt for dependency injection
- Media3 / ExoPlayer for playback
- Coil for image loading
- Coroutines + Flow for reactive state
- Navigation Compose for in-app navigation

## Getting Started

### Prerequisites

- Android Studio (Giraffe or later recommended)
- Android SDK matching `compileSdk` / `targetSdk` (see `build.gradle`)
- Emulator or physical device (grant `READ_MEDIA_AUDIO` or legacy storage permission)
- Git (for cloning)

### Setup

1. Clone the repo:
   ```bash
   git clone <your-repo-url>
   cd <your-repo>
   ```

2. Open in Android Studio. Let it sync Gradle.
    
3. (Optional) If using a device/emulator, push a few audio files for initial testing:
	 ```bash	
	adb push /path/to/song.mp3 /sdcard/Music/
	```

4. Grant necessary runtime permissions on the device/emulator when prompted:
	- On Android 13+: `READ_MEDIA_AUDIO`
	- On older versions: `READ_EXTERNAL_STORAGE`
	
### Running

- Build and run the app from Android Studio.
    
- Use the “Scan Library” button to populate artists/tracks from local storage.
    
- Navigate to the artists/albums/tracks screens. Playback and queue logic should work with the scanned data.