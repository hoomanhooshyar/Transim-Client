package com.hooman.transim.presentation.main

data class MainUiState(
    val waveForm: List<Float> = List(100){0f},
    val languages: List<String> = listOf(
        "German",
        "English",
        "Persian",
        "Spanish",
        "French",
        "Arabic",
        "Russian",
        "Japanese",
        "Chinese",
        "Turkish",
        "Korean",
        "Italian",
        "Dutch",
        "Portuguese",
        "Hindi",
        "Bengali",
        "Tamil",
        "Telugu",
        "Gujarati",
        "Malayalam",
        "Marathi"
    ),
    val sourceLanguage: String = "English",
    val targetLanguage: String = "Persian",
    val selectedSound: String = "Male Profile",
    val isActive: Boolean = false,
    val signal: Float = 0.0f,
    val lastTranscript: String = "Ready..." // متن آخرین ترجمه
)
