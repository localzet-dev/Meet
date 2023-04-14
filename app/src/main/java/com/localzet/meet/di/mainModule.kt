package com.localzet.meet.di

import com.localzet.meet.repository.MainRepository
import com.localzet.meet.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    single { MainRepository(get()) }
    viewModel { MainViewModel(get()) }

}
