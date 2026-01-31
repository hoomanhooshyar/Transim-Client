package com.hooman.transim.domain.repository

import com.hooman.transim.domain.models.ServerMessage
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {

    /**
     * اتصال به وب‌سوکت سرور و دریافت جریان پیام‌ها
     */
    suspend fun connect(hostLang: String, targetLang: String, gender: String): Flow<ServerMessage>

    /**
     * ارسال تنظیمات اولیه (زبان مبدا، مقصد و جنسیت صدا)
     */
    suspend fun sendConfig(hostLang: String,targetLang: String,gender: String)

    /**
     * ارسال تکه صدای کاربر (به صورت Base64)
     */
    suspend fun sendAudio(base64Data: String)

    suspend fun signalEndOfTurn()

    /**
     * قطع اتصال
     */
    suspend fun disconnect()
}