package com.hvdevs.playmedia.viewmodel.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hvdevs.playmedia.viewmodel.domain.UseCaseInterface

//Esta clase se genera para pasar las instancias al viewmodel
class MainViewModelFactory(private val useCase: UseCaseInterface): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(UseCaseInterface::class.java).newInstance(useCase)
    }
}