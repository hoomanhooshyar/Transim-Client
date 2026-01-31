package com.hooman.transim.presentation.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hooman.transim.core.presentation.blackBorderColor
import com.hooman.transim.core.presentation.blackPrimaryBackground
import com.hooman.transim.core.presentation.darkGrayColor
import com.hooman.transim.core.presentation.grayLabelColor
import com.hooman.transim.core.presentation.purpleInitialColor
import com.hooman.transim.core.presentation.redShutdownColor
import com.hooman.transim.core.presentation.whiteMainFontColor
import com.hooman.transim.presentation.main.components.DropDownMenu
import com.hooman.transim.presentation.main.components.MicScreen
import com.hooman.transim.presentation.main.components.RecordingIndicator
import com.hooman.transim.presentation.main.components.SmallButton
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import transim.composeapp.generated.resources.Res
import transim.composeapp.generated.resources.export_session
import transim.composeapp.generated.resources.initiate_sync
import transim.composeapp.generated.resources.lat
import transim.composeapp.generated.resources.live
import transim.composeapp.generated.resources.mic_signal
import transim.composeapp.generated.resources.pause
import transim.composeapp.generated.resources.relay_telemetry
import transim.composeapp.generated.resources.shutdown
import transim.composeapp.generated.resources.stop
import transim.composeapp.generated.resources.vad_streaming

@Composable
fun MainScreenRoute(
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit){
        onDispose {
            viewModel.stop()
        }
    }

    MainScreen(
        uiState = uiState,
        onLanguageSelected = viewModel::selectLanguage,
        onSoundSelected = viewModel::selectSound,
        onInitiateSync = {
            viewModel.start()

        },
        onPauseClick = {
            viewModel.stop()
        },
        onStopClick = {
            viewModel.stop()

        }
        )
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    onLanguageSelected: (LanguageType, String) -> Unit,
    onSoundSelected: (String) -> Unit,
    onInitiateSync: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(blackPrimaryBackground)
    ) {

        val sounds = listOf("Female Profile", "Male Profile")

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                DropDownMenu(
                    items = uiState.languages,
                    onItemSelected = {item ->
                        onLanguageSelected(LanguageType.SOURCE, item)
                    },
                    selectedItem = uiState.sourceLanguage,
                    modifier = Modifier.weight(1f)
                    )

                DropDownMenu(
                    items = uiState.languages,
                    selectedItem = uiState.sourceLanguage,
                    onItemSelected = {item ->
                        onLanguageSelected(LanguageType.TARGET, item)
                    },
                    modifier = Modifier.weight(1f)
                )
            }//Select Languages Row

            DropDownMenu(
                items = sounds,
                selectedItem = sounds.get(0),
                onItemSelected = { sound ->
                    onSoundSelected(sound)
                },
                modifier = Modifier.padding(top = 16.dp)
            )//Select Voice DropDown
            if (!uiState.isActive) {
                Button(
                    onClick = {
                        onInitiateSync()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = purpleInitialColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.initiate_sync),
                        color = Color.White
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SmallButton(
                        onClick = {onPauseClick()},
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 32.dp),
                        color = whiteMainFontColor,
                        text = stringResource(Res.string.live),
                        icon = Icons.Default.Pause
                    )
                    SmallButton(
                        onClick = {onStopClick()},
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 32.dp),
                        color = redShutdownColor,
                        text = stringResource(Res.string.shutdown),
                        icon = Icons.Default.HighlightOff
                    )

                }//Pause or Stop Roe
            }
        }//Select Languages Column
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                width = 1.dp,
                color = blackBorderColor
            ),
            colors = CardDefaults.cardColors(
                containerColor = blackPrimaryBackground
            )
        ){
            Column(
                Modifier.fillMaxWidth()
            ){
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(blackPrimaryBackground),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    RecordingIndicator(
                        isRecording = uiState.isActive,
                    )
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                    Text(
                        text = stringResource(Res.string.vad_streaming),
                        color = grayLabelColor
                    )
                }//Record Indicator Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ){
                    MicScreen(
                        uiState.waveForm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)


                    )
                }// MicScreen Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ){
                    Text(
                        text = stringResource(Res.string.mic_signal),
                        color = darkGrayColor
                    )
                    Text(
                        text = "${uiState.signal}%",
                        color = darkGrayColor
                    )
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                    Text(
                        text = stringResource(Res.string.lat),
                        color = darkGrayColor
                    )
                }// Sound features Row
            }// Record Section Column
        }// Mic Visualizer Card

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .background(blackPrimaryBackground)
        ){
            Text(
                text = "What you say!!!!",
                color = darkGrayColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        } //Speech to Text Box
        HorizontalDivider(
            thickness = 1.dp,
            color = blackBorderColor
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = stringResource(Res.string.relay_telemetry),
                color = darkGrayColor
            )
            SmallButton(
                onClick = {},
                color = darkGrayColor,
                text = stringResource(Res.string.export_session),
                icon = Icons.Default.Download,
                modifier = Modifier.padding(start = 42.dp, end = 16.dp)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ){

        }
    }// Main Column
}