package com.hooman.transim.data.repository

import com.hooman.transim.domain.models.ClientMessage
import com.hooman.transim.domain.models.ServerMessage
import com.hooman.transim.domain.repository.TranslationRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KtorTranslationRepository(
    private val client: HttpClient,
    private val host: String // این آدرس از بیرون تزریق می‌شود (مثلا از NetworkModule)
): TranslationRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val outgoingMessages = Channel<ClientMessage>(Channel.UNLIMITED)
    private val port = 8080
    private var packetCount = 0

    // تغییر ۱: اضافه کردن پارامترها به متد connect
    override suspend fun connect(
        hostLang: String,
        targetLang: String,
        gender: String
    ): Flow<ServerMessage> = flow {
        try {
            println("TransimRepo: Connecting to ws://$host:$port/relay with config: $hostLang -> $targetLang ($gender)")

            client.webSocket(
                method = HttpMethod.Get,
                host = host,
                port = port,
                path = "/relay"
            ) {
                println("TransimRepo: Socket Opened!")

                // تغییر ۲: ارسال کانفیگ (Handshake) به عنوان اولین پیام
                // نکته: مطمئن شوید کلاس ClientMessage.Config فیلد type="config" را دارد
                val configMsg = ClientMessage.Config(
                    hostLanguage = hostLang,
                    targetLanguage = targetLang,
                    voiceGender = gender
                )
                val configJson = json.encodeToString(configMsg)
                println("TransimRepo: Sending Handshake -> $configJson")
                send(Frame.Text(configJson))

                // ---------------------------------------------------------
                // از اینجا به بعد همه چیز مثل قبل است
                // ---------------------------------------------------------

                // 1. جاب برای ارسال پیام‌های بعدی (مثل صدا)
                val sendJob = launch {
                    for (msg in outgoingMessages) {
                        try {
                            // فقط پیام‌های غیر از کانفیگ را می‌فرستد (چون کانفیگ قبلا رفته)
                            val jsonStr = json.encodeToString(msg)
                            send(Frame.Text(jsonStr))
                        } catch (e: Exception) {
                            println("Error sending Message: ${e.message}")
                        }
                    }
                }

                // 2. دریافت پیام‌ها از سرور
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        try {
                            val text = frame.readText()
                            // چک کردن پیام READY از سرور (اختیاری ولی خوب است)
                            if (text.contains("READY")) {
                                println("TransimRepo: Server is READY!")
                            }

                            val message = json.decodeFromString<ServerMessage>(text)
                            emit(message)
                        } catch (e: Exception) {
                            println("Error parsing message: ${e.message}")
                        }
                    }
                }
                sendJob.cancel()
            }
        } catch (e: Exception) {
            println("TransimRepo: Connection Error -> ${e.message}")
            emit(ServerMessage.Error(0, "Connection failed: ${e.message}"))
        }
    }

    // این متد دیگر کاربردی ندارد چون در connect انجام دادیم
    // اما اگر در Interface هست، بدنه‌اش را خالی بگذارید یا حذف کنید
    override suspend fun sendConfig(hostLang: String, targetLang: String, gender: String) {
        // انجام شده در connect
    }

    override suspend fun sendAudio(base64Data: String) {
        packetCount++
        // لاگ‌ها را کمتر کردیم تا کنسول شلوغ نشود
        // if(packetCount % 50 == 0) println("Still sending audio...")

        val msg = ClientMessage.AudioChunk(data = base64Data)
        outgoingMessages.trySend(msg)
    }

    override suspend fun signalEndOfTurn() {
        println("TransimRepo: Sending Cycle Agent signal...")

        val msg = ClientMessage.CycleAgent()

        outgoingMessages.send(msg)
    }

    override suspend fun disconnect() {
        // بسته شدن خودکار توسط Flow
    }
}