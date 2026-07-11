package com.paisanotes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PaisaNotesApp : Application() {
    // Hilt uses this to generate the entire dependency injection graph!
}