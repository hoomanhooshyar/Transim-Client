package com.hooman.transim

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.hooman.transim.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class TransimApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin{
            androidContext(this@TransimApplication)
        }
    }

}