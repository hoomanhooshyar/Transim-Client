package com.hooman.transim.presentation.main

import com.hooman.transim.domain.recorder.AudioPlayer
import com.hooman.transim.domain.recorder.AudioRecorder
import com.hooman.transim.domain.recorder.MicPermissionController
import com.hooman.transim.domain.repository.TranslationRepository

class MainViewModelFactory(
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
    private val repository: TranslationRepository,
    private val micPermissionController: MicPermissionController
) {
    fun create(): MainViewModel {
        return MainViewModel(
            recorder = recorder,
            player = player,
            repository = repository,
            micPermissionController = micPermissionController)
    }
}