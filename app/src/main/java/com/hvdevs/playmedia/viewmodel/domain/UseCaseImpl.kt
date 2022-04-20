package com.hvdevs.playmedia.viewmodel.domain

import com.hvdevs.playmedia.viewmodel.vo.Resource
import com.hvdevs.playmedia.viewmodel.data.network.RepoInterface

class UseCaseImpl(private val repo: RepoInterface): UseCaseInterface {
    override suspend fun getSessions(): Resource<Int> = repo.getSessionsRepo()
}