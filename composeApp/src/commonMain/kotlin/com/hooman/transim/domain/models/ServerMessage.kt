package com.hooman.transim.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * پیام‌هایی که از سرور به موبایل می‌آیند
 */

@Serializable
sealed class ServerMessage {

    // ۰. پیام سیستمی (مثل READY)
    @Serializable
    @SerialName("system")
    data class System(
        val data: String
    ): ServerMessage()

    // ۱. تکه صدای ترجمه شده (برای پخش)
    @Serializable
    @SerialName("audio")
    data class Audio(
        val data: String //Base64
    ): ServerMessage()

    // ۲. متن ترجمه یا صحبت کاربر (برای نمایش در UI)
    @Serializable
    @SerialName("text")
    data class Text(
        val data: String
    ): ServerMessage()

    @Serializable
    @SerialName("error")
    data class Error(
        val code: Int,
        val message: String
    ): ServerMessage()
}