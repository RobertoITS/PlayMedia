package com.hvdevs.playmedia.mainlist.viewmodel.domain

import com.hvdevs.playmedia.mainlist.constructor.ParentModel
import com.hvdevs.playmedia.mainlist.viewmodel.data.network.MainListRepoInterface
import com.hvdevs.playmedia.resourse.Resource

class MainListUseCaseImplement(private val repo: MainListRepoInterface): MainListUseCaseInterface {
    override suspend fun getList(): Resource<ArrayList<ParentModel>> = repo.getListRepo()
}