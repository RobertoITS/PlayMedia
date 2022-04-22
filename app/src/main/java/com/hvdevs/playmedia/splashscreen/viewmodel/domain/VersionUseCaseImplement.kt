package com.hvdevs.playmedia.splashscreen.viewmodel.domain

import com.hvdevs.playmedia.resourse.Resource
import com.hvdevs.playmedia.splashscreen.viewmodel.data.network.VersionRepoInterface

class VersionUseCaseImplement(private val repo: VersionRepoInterface): VersionUseCaseInterface {
    override suspend fun getVersion(): Resource<Int> = repo.getVersionRepo()
}