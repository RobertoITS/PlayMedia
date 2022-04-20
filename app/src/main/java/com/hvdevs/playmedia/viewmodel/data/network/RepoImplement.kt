package com.hvdevs.playmedia.viewmodel.data.network

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.hvdevs.playmedia.viewmodel.vo.Resource
import kotlinx.coroutines.tasks.await

class RepoImplement: RepoInterface {
    override suspend fun getSessionsRepo(): Resource<Int> {
        val uid = Firebase.auth.currentUser?.uid
        val dbUser = FirebaseDatabase.getInstance().reference.child("users/$uid/sessions/quantity").get().await() //Este metodo se aplica sobre un Task, hasta que no traiga los datos, no pasa a la linea siguiente
        Log.d("ERRRRRROR", dbUser.value.toString())
        val sessions: Int = if (dbUser.value == null){
            0
        } else {
            Integer.parseInt(dbUser!!.value.toString())
        }
        //FIREBASE
        //Aca se retorna la informacion
        return Resource.Success(sessions)
    }
}