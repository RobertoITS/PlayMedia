package com.hvdevs.playmedia.splashscreen.viewmodel.data.network

import com.hvdevs.playmedia.resourse.Resource

interface VersionRepoInterface {
    suspend fun getVersionRepo(): Resource<Int>
}