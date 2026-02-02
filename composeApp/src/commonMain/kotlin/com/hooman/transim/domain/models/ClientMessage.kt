package com.hooman.transim.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * پیام‌هایی که از موبایل به سمت سرور می‌روند
 */
@Serializable
sealed class ClientMessage {

    // ۱. پیام تنظیمات: وقتی کاربر دکمه اتصال را می‌زند
    @Serializable
    @SerialName("config")
    data class Config(
        val hostLanguage: String,
        val targetLanguage: String,
        val voiceGender: String
    ): ClientMessage()

    @Serializable
    @SerialName("audio_chunk")
    data class AudioChunk(
        val data: String // صدای کاربر به صورت Base64
    ): ClientMessage()

    // ۳. (اختیاری) پیام قطع صحبت: برای وقتی که VAD تشخیص سکوت داد
    @Serializable
    @SerialName("cycle_agent")
    data object CycleAgent : ClientMessage()

}