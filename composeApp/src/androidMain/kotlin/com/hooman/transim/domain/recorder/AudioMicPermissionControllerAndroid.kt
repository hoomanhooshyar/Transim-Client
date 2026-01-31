package com.hooman.transim.domain.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class AudioMicPermissionControllerAndroid(
    private val context: Context
): MicPermissionController {

    private var requestPermissionLauncher: (suspend() -> Boolean)? = null

    fun bind(launcher: (suspend() -> Boolean)){
        this.requestPermissionLauncher = launcher
    }

    override suspend fun ensurePermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        return if(hasPermission) true
        else requestPermissionLauncher?.invoke() ?: false
    }
}