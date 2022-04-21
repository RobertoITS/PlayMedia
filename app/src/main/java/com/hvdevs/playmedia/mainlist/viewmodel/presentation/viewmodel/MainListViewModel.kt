package com.hvdevs.playmedia.mainlist.viewmodel.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.hvdevs.playmedia.mainlist.viewmodel.domain.MainListUseCaseInterface
import com.hvdevs.playmedia.resourse.Resource
import kotlinx.coroutines.Dispatchers

class MainListViewModel(useCase: MainListUseCaseInterface): ViewModel() {
    val fetchListData = liveData(Dispatchers.IO) {
        emit(Resource.Loading())

        try {
            val list = useCase.getList()
            emit(list)
        } catch (e: Exception){
            emit(Resource.Failure(e))
        }
    }
}