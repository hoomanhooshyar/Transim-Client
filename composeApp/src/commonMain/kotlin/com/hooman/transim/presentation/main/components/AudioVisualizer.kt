package com.hooman.transim.presentation.main.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AudioVisualizer(
    waveForm: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Black)
    ){
        if(waveForm.isEmpty())
            return@Canvas

        val gain = 4f
        val centerY = size.height / 2
        val stepX = size.width / waveForm.size
        val path = Path()
        path.moveTo(0f,centerY)
        waveForm.forEachIndexed { index, sample ->
            val x = index * stepX
            val y = centerY - (sample * gain).coerceIn(-1f,1f) * centerY
            path.lineTo(x,y)
        }
        drawPath(
            path,
            color = Color.Green,
            style = Stroke(width = 2f)
        )
    }
}