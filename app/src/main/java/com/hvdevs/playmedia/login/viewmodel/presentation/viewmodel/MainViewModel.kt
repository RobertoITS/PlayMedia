package com.hvdevs.playmedia.login.viewmodel.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
//import androidx.lifecycle.liveData
import com.hvdevs.playmedia.login.viewmodel.domain.UseCaseInterface
import com.hvdevs.playmedia.resourse.Resource
import kotlinx.coroutines.Dispatchers

//Aca escribimos la logica para sacar la info de firebase
//Pasamos la interfaz creada (IUseCase)
class MainViewModel(useCase: UseCaseInterface): ViewModel() {

    //¿Por que creamos tantas interfaces y clases, y no lo hacemos de un solo archivo?
    //Para independizar totalmente las clases. El ViewModel no sabe donde esta realmente la informacion
    //Es una buena practica de programacion, singleClass, openClose

    //Funciona de la misma forma que el destroy del main activity
    //Se puede guardar algon en cache por ejemplo
//    override fun onCleared() {
//        super.onCleared()
//    }

    //Esta extencion escucha cuando la informacion esta activa.
    //cuando la info pasa a inactiva, deja de escuchar
    /**El dispacher funciona para decir en que contexto ejecutamos nuestra operacion*/
    val fetchSessions = liveData(Dispatchers.IO) {
        //Aqui adentro ejecutamos las corrutinas
        //Retornar con un emit
        emit(Resource.Loading())

        try {
            //Aca le decimos el tipo de dato que le pasamos al emit
            val sessions = useCase.getSessions()
            emit(sessions)
        } catch (e: Exception){
            emit(Resource.Failure(e))
        }
    }
}