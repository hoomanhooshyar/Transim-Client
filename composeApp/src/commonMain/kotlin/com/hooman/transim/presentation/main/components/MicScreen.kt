package com.hooman.transim.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MicScreen(
    waveForm: List<Float>,
    modifier: Modifier = Modifier
) {
    AudioVisualizer(
        waveForm = waveForm,
        modifier = modifier
    )
}