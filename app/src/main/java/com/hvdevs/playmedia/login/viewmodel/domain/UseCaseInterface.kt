package com.hvdevs.playmedia.login.viewmodel.domain

import com.hvdevs.playmedia.resourse.Resource

interface UseCaseInterface {
    //Es un metodo que no se sabe cuando va a terminar.
    //Va a ir a la BD a buscar la info.
    //cuando retorne, va a continuar con lo otro
    suspend fun getSessions(): Resource<Int>
}