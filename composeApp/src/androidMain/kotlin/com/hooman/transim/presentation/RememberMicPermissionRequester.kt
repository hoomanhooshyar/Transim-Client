package com.hooman.transim.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun rememberMicPermissionRequester(): suspend () -> Boolean {
    var continuation by remember {
        mutableStateOf<CancellableContinuation<Boolean>?>(null)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        continuation?.resume(granted)
        continuation = null
    }

    return suspend {
        suspendCancellableCoroutine { cont ->
            continuation = cont
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}