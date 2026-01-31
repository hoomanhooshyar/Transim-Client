package com.hooman.transim.domain.recorder

interface AudioPlayer {
    fun play(data: ByteArray)
    fun cleanup()
}