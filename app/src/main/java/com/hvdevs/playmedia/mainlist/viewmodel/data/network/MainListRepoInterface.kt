package com.hvdevs.playmedia.mainlist.viewmodel.data.network

import com.hvdevs.playmedia.mainlist.constructor.ParentModel
import com.hvdevs.playmedia.resourse.Resource

interface MainListRepoInterface {
    suspend fun getListRepo(): Resource<ArrayList<ParentModel>>
}