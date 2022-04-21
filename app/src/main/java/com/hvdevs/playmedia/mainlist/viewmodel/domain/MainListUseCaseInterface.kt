package com.hvdevs.playmedia.mainlist.viewmodel.domain

import com.hvdevs.playmedia.mainlist.constructor.ParentModel
import com.hvdevs.playmedia.resourse.Resource

interface MainListUseCaseInterface {
    suspend fun getList(): Resource<ArrayList<ParentModel>>
}