package com.hooman.transim.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.hooman.transim.domain.recorder.AudioMicPermissionControllerAndroid
import com.hooman.transim.domain.recorder.MicPermissionController
import com.hooman.transim.presentation.rememberMicPermissionRequester
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun AndroidMainScreen() {
    // ۱. دریافت کنترلر اختصاصی اندروید از Koin
    // ما تایپ دقیق اندرویدی را می‌خواهیم تا به متد bind دسترسی داشته باشیم
    val permissionController = koinInject<MicPermissionController>()
            as AudioMicPermissionControllerAndroid

    val requestPermission = rememberMicPermissionRequester()

    LaunchedEffect(Unit) {
        permissionController.bind(requestPermission)
    }

    val viewModel: MainViewModel = koinViewModel()

    MainScreenRoute(viewModel)
}