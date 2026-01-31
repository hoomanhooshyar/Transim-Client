package com.hooman.transim.domain.recorder

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.*
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.setActive
import kotlin.math.abs

class AudioRecorderIOS:AudioRecorder {
    private val _amplitude = MutableStateFlow(0f)

    override val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _samples = MutableSharedFlow<List<Float>>(extraBufferCapacity = 1)

    override val samples: Flow<List<Float>> = _samples
    private val engine = AVAudioEngine()


    // توجه: سخت‌افزار ممکن است دقیقا این را ندهد، اما برای سادگی اینجا درخواست می‌کنیم
    // در پروژه واقعی بهتر است Resampler دستی بنویسیم اگر سخت‌افزار 48k داد.
    private val desiredFormat = AVAudioFormat(
        commonFormat = AVAudioPCMFormatFloat32,
        sampleRate = 16000.0,
        channels = 1u,
        interleaved = true
    )

    @OptIn(ExperimentalForeignApi::class)
    override fun start(onAudioData:(ByteArray) -> Unit) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            AVAudioSessionModeMeasurement,
            AVAudioSessionCategoryOptionDefaultToSpeaker or AVAudioSessionCategoryOptionAllowBluetooth,
            error = null
        )

        session.setActive(true,error = null)
        val inputNode = engine.inputNode

        // تلاش می‌کنیم فرمت ورودی را بگیریم (معمولا 44.1 یا 48 است)
        val hardwareFormat = inputNode.inputFormatForBus(0u)

        // نصب Tap (شنود صدا)
        inputNode.installTapOnBus(
            bus = 0u,
            bufferSize = 1024u,
            format = hardwareFormat
        ){buffer, _ ->
            buffer ?: return@installTapOnBus

            val channelData = buffer.floatChannelData?.get(0) ?: return@installTapOnBus
            val frameLength = buffer.frameLength.toInt()

            // لیستی برای ویژوالایزر
            val floatsForUi = mutableListOf<Float>()

            // لیستی برای بایت‌های خروجی
            val byteArray = ByteArray(frameLength * 2)

            var maxAmp = 0f
            for(i in 0 until frameLength){
                val floatSample = channelData[i]

                // 1. دیتا برای ویژوالایزر
                val absVal = abs(floatSample)
                if(absVal > maxAmp)
                    maxAmp = absVal

                // (اختیاری) Downsample ساده برای UI: هر 8 تا یکی را برداریم
                if(i % 8 == 0)
                    floatsForUi.add(floatSample)

                // 2. تبدیل Float به PCM 16-bit برای سرور
                // Float [-1.0, 1.0] -> Int16 [-32768, 32767]
                val pcmValue = (floatSample * 32767).toInt().coerceIn(-32768, 32767)

                byteArray[i * 2] = (pcmValue and 0x00FF).toByte()
                byteArray[i * 2 + 1] = ((pcmValue shr 8) and 0x00FF).toByte()
            }

            _amplitude.value = maxAmp
            _samples.tryEmit(floatsForUi)
            onAudioData(byteArray)
        }

        engine.prepare()
        engine.startAndReturnError(null)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun stop() {
        engine.inputNode.removeTapOnBus(0UL)
        engine.stop()
        val session = AVAudioSession.sharedInstance()
        session.setActive(false, error = null)
    }
}