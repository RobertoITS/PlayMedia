package com.hvdevs.playmedia.splashscreen.viewmodel.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hvdevs.playmedia.splashscreen.viewmodel.domain.VersionUseCaseInterface

class VersionViewModelFactory(private val useCase: VersionUseCaseInterface): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(VersionUseCaseInterface::class.java).newInstance(useCase)
    }
}