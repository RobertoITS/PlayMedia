package com.hvdevs.playmedia.mainlist.viewmodel.data.network

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.hvdevs.playmedia.mainlist.adapters.ExpandedListAdapter
import com.hvdevs.playmedia.mainlist.constructor.ChildModel
import com.hvdevs.playmedia.mainlist.constructor.ParentModel
import com.hvdevs.playmedia.mainlist.ui.MainListActivity
import com.hvdevs.playmedia.resourse.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

class MainListRepoImplement: MainListRepoInterface {

    private var parentItem:LinkedHashMap<String, ParentModel> = LinkedHashMap() //Lista del parent - child
    private var itemList: ArrayList<ParentModel> = arrayListOf() //Lista solo del parent

    override suspend fun getListRepo(): Resource<ArrayList<ParentModel>> {
        //Usamos un contador, por comodidad en la forma en que se presentan los datos en la
        //base de datos. Los child, en la bd, estan enumerados del 0 al 9
        //por lo que es mas accesible para buscar los datos
        var j = 0
        val dbParent: DataSnapshot? = FirebaseDatabase.getInstance().getReference("channels").get().await()
        if (dbParent!!.exists()){
            for (snapshot in dbParent.children){
                val parent: String = snapshot.child("name").value.toString()
                //Una vez que obtenemos el parent, buscamos los child que le corresponden
                //Pasamos el contador
                getChildData(parent, j)
                //Aumentamos el contador
                j += 1
            }
        }
        return Resource.Success(itemList) //devuelve la lista
    }

    private suspend fun getChildData(parent: String, j: Int) {
        val dbChild: DataSnapshot? = FirebaseDatabase.getInstance().getReference("channels/$j/samples").get().await()
        if (dbChild!!.exists()){
            for (snapshot in dbChild.children){
                val child = snapshot.getValue(ChildModel::class.java)
                //Pasamos los datos del parent y sus child (uno en uno, en este caso)
                addItem(parent, child!!)
            }
        }
    }

    private fun addItem(parent: String, child: ChildModel): Int {
        val parentPosition: Int
        //Obtenemos la informacion del parent (en el contructor del objeto vemos que
        //tiene un ingreso de una string (el dato principal) y otro objeto (ChildModel)
        var parentInfo: ParentModel? = parentItem[parent]
        //Si el parentInfo esta nulo, creamos uno nuevo y le comenzamos a ingresar los
        //datos de sus child
        if (parentInfo == null){
            parentInfo = ParentModel()
            parentInfo.name = parent
            parentItem[parent] = parentInfo
            itemList.add(parentInfo)
        }
        //Aca se comienzan a instanciar los datos de los child, con le index del parent
        val childItemList: ArrayList<ChildModel> = parentInfo.itemList
        var listSize = childItemList.size
        listSize++

        //Instanciamos todos los atributos del objeto ChildModel()
        val childInfo = ChildModel()
        childInfo.drm_license_url = child.drm_license_url
        childInfo.drm_scheme = child.drm_scheme
        childInfo.icon = child.icon
        childInfo.name = child.name
        childInfo.uri = child.uri
        childItemList.add(childInfo)
        parentInfo.itemList = childItemList

        parentPosition = childItemList.indexOf(childInfo) //Por ultimo se colocan en la posicion del parent

        return parentPosition
    }
}