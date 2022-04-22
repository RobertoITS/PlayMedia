package com.hvdevs.playmedia.splashscreen.viewmodel.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.hvdevs.playmedia.resourse.Resource
import com.hvdevs.playmedia.splashscreen.viewmodel.domain.VersionUseCaseInterface
import kotlinx.coroutines.Dispatchers

class VersionViewModel(useCase: VersionUseCaseInterface): ViewModel() {
    val fetchVersion = liveData(Dispatchers.IO) {
        emit(Resource.Loading())
        try {
            val version = useCase.getVersion()
            emit(version)
        } catch (e: Exception){
            emit(Resource.Failure(e))
        }
    }
}