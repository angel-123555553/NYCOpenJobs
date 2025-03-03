package com.example.nycopenjobs.data

import android.content.Context
import android.util.Log
import com.example.nycopenjobs.api.AppRemoteApis
import com.example.nycopenjobs.util.TAG

interface AppContainer {
    val appRepository: AppRepository
}

    class DefaultAppContainer(private val context: Context): AppContainer {



        override val appRepository: AppRepository by lazy{
            Log.i(TAG, "initializing app repository")
            AppRepositoryImpl(
                AppRemoteApis().getNycOpenDataApi(),
                AppPreferences(context).getSharedPreferences(),
                LocalDatabase.getDatabase(context).jobPostDao()
            )
        }

    }
