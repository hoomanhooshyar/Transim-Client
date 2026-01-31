package com.hooman.transim.presentation.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hooman.transim.core.presentation.grayLabelColor
import com.hooman.transim.core.presentation.greenLiveStatusColor

@Composable
fun RecordingIndicator(
    isRecording: Boolean = false,
    size: Dp = 16.dp
) {

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = if(isRecording) greenLiveStatusColor else grayLabelColor,
                shape = CircleShape
            )
    ){}
}