package com.hooman.transim

import androidx.compose.ui.window.ComposeUIViewController
import com.hooman.transim.domain.recorder.AudioMicPermissionControllerIOS
import com.hooman.transim.domain.recorder.AudioRecorderIOS
import com.hooman.transim.presentation.main.MainScreenRoute
import com.hooman.transim.presentation.main.MainViewModel
import com.hooman.transim.presentation.main.MainViewModelFactory
import org.koin.compose.viewmodel.koinViewModel

fun MainViewController() = ComposeUIViewController {

    val viewModel: MainViewModel = koinViewModel()
    MainScreenRoute(viewModel)
}