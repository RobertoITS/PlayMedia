package com.hvdevs.playmedia.login.viewmodel.data.network

import com.hvdevs.playmedia.resourse.Resource

interface RepoInterface {
    suspend fun getSessionsRepo(): Resource<Int>
}