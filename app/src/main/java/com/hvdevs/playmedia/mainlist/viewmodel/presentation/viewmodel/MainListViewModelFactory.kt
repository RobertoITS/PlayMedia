package com.hvdevs.playmedia.mainlist.viewmodel.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hvdevs.playmedia.mainlist.viewmodel.domain.MainListUseCaseInterface

class MainListViewModelFactory(private val useCase: MainListUseCaseInterface): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(MainListUseCaseInterface::class.java).newInstance(useCase)
    }
}