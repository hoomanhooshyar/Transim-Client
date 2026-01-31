package com.hooman.transim.domain.recorder

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.math.abs

class AudioRecorderAndroid : AudioRecorder {

    // --- StateFlows ---
    private val _amplitude = MutableStateFlow(0f)
    override val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _samples = MutableSharedFlow<List<Float>>(extraBufferCapacity = 1)
    override val samples: Flow<List<Float>> = _samples

    // --- Audio Components ---
    private var audioRecorder: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var automaticGainControl: AutomaticGainControl? = null
    private var job: Job? = null

    // --- VAD Settings (تنظیمات تشخیص سکوت) ---
    // آستانه بلندی صدا برای اینکه بگوییم کاربر دارد حرف می‌زند (بین 500 تا 3000 تنظیم کنید)
    private val VAD_THRESHOLD = 2000
    // مدت زمان سکوت لازم برای اینکه نوبت تمام شود (1.5 ثانیه)
    private val SILENCE_DURATION_MS = 1500L

    // ترد جداگانه برای ضبط صدا
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val recordContext = newSingleThreadContext("AudioRecorderThread")

    private val SAMPLE_RATE = 16000

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun start(
        onAudioData: (ByteArray) -> Unit,
        onSilenceDetected: () -> Unit // 🔥 کال‌بک جدید برای پایان صحبت
    ) {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = maxOf(minBufferSize, 8192)

        try {
            audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val audioSessionId = audioRecorder!!.audioSessionId
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(audioSessionId)
                noiseSuppressor?.enabled = true
                Log.d("AudioRecorder", "NoiseSuppressor Enabled")
            }
            if (AutomaticGainControl.isAvailable()) {
                automaticGainControl = AutomaticGainControl.create(audioSessionId)
                automaticGainControl?.enabled = true
                Log.d("AudioRecorder", "AutomaticGainControl Enabled")
            }

            audioRecorder?.startRecording()

            job = CoroutineScope(recordContext).launch(Dispatchers.IO) {
                val readSize = bufferSize / 2
                val shortBuffer = ShortArray(readSize)

                var lastUiUpdate = 0L

                // متغیرهای VAD
                var isSpeaking = false
                var lastSpeakingTime = System.currentTimeMillis()

                while (isActive) {
                    val readCount = audioRecorder?.read(shortBuffer, 0, readSize) ?: 0

                    if (readCount > 0) {
                        val bytes = ByteArray(readCount * 2)
                        var maxVal = 0

                        for (i in 0 until readCount) {
                            val s = shortBuffer[i].toInt()
                            // تبدیل Short به بایت برای ارسال
                            bytes[i * 2] = (s and 0x00FF).toByte()
                            bytes[i * 2 + 1] = ((s shr 8) and 0x00FF).toByte()

                            val absVal = abs(s)
                            if (absVal > maxVal) maxVal = absVal
                        }

                        // --- 1. لاجیک VAD (تشخیص سکوت) ---
                        if (maxVal > VAD_THRESHOLD) {
                            // صدای بلند تشخیص داده شد، تایمر سکوت ریست می‌شود
                            lastSpeakingTime = System.currentTimeMillis()
                            if (!isSpeaking) {
                                Log.d("VAD", "User Started Speaking")
                                isSpeaking = true
                            }
                        } else {
                            // صدا کم است (سکوت)
                            if (isSpeaking) {
                                val silenceDuration = System.currentTimeMillis() - lastSpeakingTime
                                if (silenceDuration > SILENCE_DURATION_MS) {
                                    Log.d("VAD", "End of Turn Detected! ($silenceDuration ms silence)")

                                    // 🔥 خبر دادن به ViewModel برای تغییر ایجنت
                                    onSilenceDetected()

                                    isSpeaking = false // ریست وضعیت
                                }
                            }
                        }

                        // --- 2. ارسال صدا به سرور ---
                        // نویز گیت: فقط اگر صدا از حد خیلی کمی بیشتر بود بفرست تا پهنای باند هدر نرود
                        if (maxVal > 500) {
                            onAudioData(bytes)
                        }

                        // --- 3. آپدیت UI ---
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUiUpdate > 50) {
                            lastUiUpdate = currentTime
                            _amplitude.value = maxVal / 32768f

                            if (_samples.subscriptionCount.value > 0) {
                                val downSampledList = mutableListOf<Float>()
                                val step = 20
                                for (i in 0 until readCount step step) {
                                    downSampledList.add(shortBuffer[i] / 32768f)
                                }
                                _samples.tryEmit(downSampledList)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    override fun stop() {
        job?.cancel()
        try {
            audioRecorder?.stop()
            audioRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecorder = null
            noiseSuppressor?.release()
            automaticGainControl?.release()
            noiseSuppressor = null
            automaticGainControl = null
        }
    }
}