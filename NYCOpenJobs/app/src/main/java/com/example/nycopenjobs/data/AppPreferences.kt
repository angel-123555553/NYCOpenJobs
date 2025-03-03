package com.example.nycopenjobs.data

import android.content.Context
import android.content.SharedPreferences

interface AppSharedPreferences{
    fun getSharedPreferences(): SharedPreferences
}

class AppPreferences(private val context: Context) : AppSharedPreferences{
    private val prefsKey = "prefs"

    override fun getSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences(prefsKey, Context.MODE_PRIVATE)
    }



}