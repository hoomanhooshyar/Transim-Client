package com.hooman.transim.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooman.transim.domain.models.ServerMessage
import com.hooman.transim.domain.recorder.AudioPlayer
import com.hooman.transim.domain.recorder.AudioRecorder
import com.hooman.transim.domain.recorder.MicPermissionController
import com.hooman.transim.domain.repository.TranslationRepository
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
    private val repository: TranslationRepository,
    private val micPermissionController: MicPermissionController
): ViewModel() {



    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var connectionJob: Job? = null
    private var visualizerJob: Job? = null



/*-----Mic-----*/
    fun start(){
        viewModelScope.launch {
            if(micPermissionController.ensurePermission()){
                _uiState.update { it.copy(isActive = true, lastTranscript = "Connecting")}

                // 1. اتصال به سرور و ارسال کانفیگ
                connectAndListen()

                // 2. شروع ضبط صدا و ارسال به سرور
                startCapture()
            }
        }
    }


    fun stop(){
        _uiState.update {
            it.copy(isActive = false, lastTranscript = "Disconnect")
        }


        connectionJob?.cancel()
        visualizerJob?.cancel()

        recorder.stop()
        player.cleanup()


        viewModelScope.launch(Dispatchers.IO) { repository.disconnect() }
    }

    private fun connectAndListen(){
        connectionJob?.cancel()
        connectionJob = viewModelScope.launch(Dispatchers.IO) {
            // الف) ارسال تنظیمات اولیه
            val gender = if(_uiState.value.selectedSound.contains("Female")) "FEMALE" else "MALE"

            // تبدیل نام زبان به کد (ساده‌سازی شده)
            val hostCode = _uiState.value.sourceLanguage
            val targetCode = _uiState.value.targetLanguage

            repository.sendConfig("English","Persian",gender)
            //repository.sendConfig(hostCode,targetCode,gender)

            // ب) دریافت پیام‌ها از سرور
            repository.connect(
                hostLang = hostCode,
                targetLang = targetCode,
                gender = gender
            ).collect { msg ->
                when(msg){
                    is ServerMessage.System -> {
                        // پیام سیستمی (مثل READY)
                        println("System message: ${msg.data}")
                        if (msg.data == "READY") {
                            _uiState.update { it.copy(lastTranscript = "Connected - Ready!") }
                        }
                    }
                    is ServerMessage.Audio ->{
                        // صدای ترجمه رسید -> پخش کن
                        try {
                            val audioBytes = msg.data.decodeBase64Bytes()
                            player.play(audioBytes)
                        }catch (e: Exception){
                            println("Audio decode Error: ${e.message}")
                        }
                    }
                    is ServerMessage.Text ->{
                        // متن رسید -> نمایش بده
                        _uiState.update {
                            it.copy(lastTranscript = msg.data)
                        }
                    }
                    is ServerMessage.Error->{
                        println("Server Error: ${msg.message}")
                        _uiState.update { it.copy(lastTranscript = "Error: ${msg.message}") }

                        //You can call stop() if you want
                    }
                }
            }

        }
    }

    private fun startCapture(){
        // الف) هندل کردن ویژوالایزر
        visualizerJob?.cancel()
        visualizerJob = viewModelScope.launch {
            recorder.samples.collect { newSample ->
                _uiState.update { current ->
                    current.copy(
                        waveForm = (current.waveForm + newSample).takeLast(60),
                        signal = newSample.lastOrNull() ?: 0f
                    )
                }
            }
        }

        // ب) شروع ضبط اصلی و ارسال به شبکه
        recorder.start(
            onAudioData = { audioBytes ->
                // این بلاک هر وقت میکروفون بافر پر کرد صدا زده می‌شود
                if(_uiState.value.isActive){
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            // تبدیل بایت به Base64 و ارسال
                            val base64String = audioBytes.encodeBase64()
                            repository.sendAudio(base64String)
                        }catch (e: Exception){
                            println("Error sending audio: ${e.message}")
                        }

                    }
                }
            },
            onSilenceDetected = {
                // 🔥 اینجا سکوت تشخیص داده شده، پیام تغییر نوبت را بفرستید
                viewModelScope.launch {
                    println("ViewModel: Silence detected, switching agent...")
                    repository.signalEndOfTurn()
                }
            }
        )
    }

    /*-----Language-----*/

    fun selectLanguage(
        type: LanguageType,
        language: String
    ){
        _uiState.update {
            when(type){
                LanguageType.SOURCE -> it.copy(sourceLanguage = language)
                LanguageType.TARGET -> it.copy(targetLanguage = language)
            }
        }
    }

    /*------Sound Profile------- */

    fun selectSound(sound: String){
        _uiState.update {
            it.copy(selectedSound = sound)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}