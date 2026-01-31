package com.hooman.transim.core.di

import com.hooman.transim.data.repository.KtorTranslationRepository
import com.hooman.transim.domain.recorder.MicPermissionController
import com.hooman.transim.domain.repository.TranslationRepository
import com.hooman.transim.presentation.main.MainViewModel
import com.hooman.transim.presentation.main.MainViewModelFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {

    single {
        HttpClient{
            install(WebSockets)
            install(ContentNegotiation){
                json(Json { ignoreUnknownKeys = true } )
            }
        }
    }



    single<TranslationRepository> {
        KtorTranslationRepository(
            client = get(),
            host = get(named("ServerHost"))
        )
    }



    viewModel {
        MainViewModel(
            recorder = get(),
            player = get(),
            repository = get(),
            micPermissionController = get()
        )
    }
}