package com.hvdevs.playmedia.splashscreen.viewmodel.data.network

import com.google.firebase.database.FirebaseDatabase
import com.hvdevs.playmedia.resourse.Resource
import kotlinx.coroutines.tasks.await

class VersionRepoImplement: VersionRepoInterface {
    override suspend fun getVersionRepo(): Resource<Int>{
        val dbVersion = FirebaseDatabase.getInstance().reference.child("version_code/version").get().await()
        val version: Int = if (dbVersion.value == null){
            0
        } else {
            Integer.parseInt(dbVersion!!.value.toString())
        }
        return Resource.Success(version)
    }
}