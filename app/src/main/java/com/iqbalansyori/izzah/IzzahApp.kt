package com.iqbalansyori.izzah

import android.app.Application

class IzzahApp : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
    }
}
