package com.hvdevs.playmedia.viewmodel.data.network

import com.hvdevs.playmedia.viewmodel.vo.Resource

interface RepoInterface {
    suspend fun getSessionsRepo(): Resource<Int>
}