package com.hooman.transim.domain.recorder

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AudioRecorder {
    fun start(onAudioData:(ByteArray) -> Unit, onSilenceDetected: () -> Unit)
    fun stop()
    val amplitude: StateFlow<Float>
    val samples: Flow<List<Float>>
}