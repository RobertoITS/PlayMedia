package com.hvdevs.playmedia.splashscreen.viewmodel.domain

import com.hvdevs.playmedia.resourse.Resource

interface VersionUseCaseInterface {
    suspend fun getVersion(): Resource<Int>
}