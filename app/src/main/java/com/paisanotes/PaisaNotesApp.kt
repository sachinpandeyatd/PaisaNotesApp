package com.paisanotes

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PaisaNotesApp : Application(), Configuration.Provider {

    // Inject the factory provided by the hilt-work library
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Provide the configuration to the Android OS
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}