package com.hooman.transim.presentation

import com.hooman.transim.domain.recorder.AudioPlayer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.plus
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPCMFormatFloat32
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategory
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.setActive

@OptIn(ExperimentalForeignApi::class)
class IOSAudioPlayer: AudioPlayer {

    private val engine = AVAudioEngine()
    private val playerNode = AVAudioPlayerNode()

    // فرمت صدای ورودی (PCM 16-bit که از سرور می‌آید)
    // اما AVAudioEngine برای پخش راحت‌تر با Float32 کار می‌کند.
    // ما اینجا فرمتی تعریف می‌کنیم که می‌خواهیم پخش کنیم.
    private val outputFormat = AVAudioFormat(
        commonFormat = AVAudioPCMFormatFloat32,
        sampleRate = 16000.0,
        channels = 1u,
        interleaved = false
    )

    init {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayAndRecord,error = null)
            session.setActive(true,error = null)
            engine.attachNode(playerNode)

            // اتصال پلیر به خروجی اصلی (Speaker)
            engine.connect(
                playerNode,
                engine.mainMixerNode,
                outputFormat
            )

            engine.prepare()
            engine.startAndReturnError(null)
            playerNode.play()

        }catch (e: Exception){
            println("Error: ${e.message}")
        }
    }

    override fun play(data: ByteArray) {
        if(data.isEmpty()) return

        // 1. تبدیل ByteArray (PCM 16-bit) به فرمت Float32 که AVAudioEngine دوست دارد
        val frameCount = data.size / 2 // هر 2 بایت یک نمونه است

        // ساخت بافر صوتی
        val buffer = AVAudioPCMBuffer(outputFormat,frameCount.toUInt())
        buffer.frameLength = frameCount.toUInt()

        val channelData = buffer.floatChannelData?.get(0) ?: return

        // حلقه تبدیل
        for(i in 0 until frameCount){
            // ترکیب دو بایت برای ساخت Int16
            val low = data[i * 2].toInt() and 0xFF
            val high = data[i * 2 + 1].toInt()
            val sample = (high shl 8) or low
            val shortSample = sample.toShort()

            // نرمال‌سازی به Float [-1.0, 1.0]
            (channelData + i)!!.pointed.value = shortSample / 32768.0f
        }

        // 2. پخش بافر
        playerNode.scheduleBuffer(buffer,null)
    }

    override fun cleanup() {
        try {
            playerNode.stop()
            engine.stop()
            engine.reset()
        }catch (e: Exception){
            println("Error: ${e.message}")
        }
    }
}