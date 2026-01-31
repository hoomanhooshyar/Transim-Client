package com.hooman.transim.presentation

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.hooman.transim.domain.recorder.AudioPlayer

class AndroidAudioPlayer: AudioPlayer {

    private var audioTrack: AudioTrack? = null

    // تنظیمات استاندارد Gemini (معمولاً 24kHz برای خروجی)
    // اگر صدا "کند" پخش شد، این عدد را زیاد کنید (مثلا 24000)
    // اگر صدا "تند" (نازک) پخش شد، این عدد را کم کنید (مثلا 16000)
    private val SAMPLE_RATE = 16000

    init {
        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    override fun play(data: ByteArray) {
        try {
            // نوشتن بایت‌ها در بافر AudioTrack برای پخش فوری
            audioTrack?.write(data,0,data.size)
        }catch (e: Exception){
            println("Error: ${e.message}")
        }
    }

    override fun cleanup() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        }catch (e: Exception){
            println("Error: ${e.message}")
        }
    }
}