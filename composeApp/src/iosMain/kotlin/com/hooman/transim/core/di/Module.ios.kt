package com.hooman.transim.core.di

import com.hooman.transim.domain.recorder.AudioMicPermissionControllerIOS
import com.hooman.transim.domain.recorder.AudioPlayer
import com.hooman.transim.domain.recorder.AudioRecorder
import com.hooman.transim.domain.recorder.AudioRecorderIOS
import com.hooman.transim.domain.recorder.MicPermissionController
import com.hooman.transim.presentation.IOSAudioPlayer
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<AudioRecorder> { AudioRecorderIOS() }

        factory<AudioPlayer> { IOSAudioPlayer() }

        single<MicPermissionController> { AudioMicPermissionControllerIOS() }

        single(named("ServerHost")){"localhost"}
    }