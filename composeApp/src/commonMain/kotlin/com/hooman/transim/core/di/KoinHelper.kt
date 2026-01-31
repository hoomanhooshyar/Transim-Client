package com.hooman.transim.core.di

import com.hooman.transim.presentation.main.MainViewModel
import org.koin.core.component.KoinComponent

object KoinHelper: KoinComponent {
    fun getMainViewModel(): MainViewModel{
        return getKoin().get()
    }
}