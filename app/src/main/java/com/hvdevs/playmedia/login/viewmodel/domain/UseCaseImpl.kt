package com.hvdevs.playmedia.login.viewmodel.domain

import com.hvdevs.playmedia.resourse.Resource
import com.hvdevs.playmedia.login.viewmodel.data.network.RepoInterface

class UseCaseImpl(private val repo: RepoInterface): UseCaseInterface {
    override suspend fun getSessions(): Resource<Int> = repo.getSessionsRepo()
}