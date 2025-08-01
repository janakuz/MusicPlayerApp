package com.example.musicapp

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.HiltAndroidApp
import java.net.MulticastSocket

@HiltAndroidApp
class MusicPlayerApp : Application()