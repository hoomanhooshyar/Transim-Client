package com.hooman.transim.core.di

import com.hooman.transim.domain.recorder.AudioMicPermissionControllerAndroid
import com.hooman.transim.domain.recorder.AudioPlayer
import com.hooman.transim.domain.recorder.AudioRecorder
import com.hooman.transim.domain.recorder.AudioRecorderAndroid
import com.hooman.transim.domain.recorder.MicPermissionController
import com.hooman.transim.presentation.AndroidAudioPlayer
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<AudioRecorder> { AudioRecorderAndroid() }

        factory<AudioPlayer> { AndroidAudioPlayer() }

        // تعریف کنترلر مجوز برای اندروید (Single باشد تا همان نمونه در UI استفاده شود)
        single<MicPermissionController> { AudioMicPermissionControllerAndroid(get()) }

        //10.0.2.2
        single(named("ServerHost")){"10.228.64.59"}
    }