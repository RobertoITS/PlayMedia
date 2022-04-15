package com.hvdevs.playmedia.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.hvdevs.playmedia.exoplayer2.PlayerActivity
import com.hvdevs.playmedia.adapters.HelperAdapter
import com.hvdevs.playmedia.constructor.ChildModel
import com.hvdevs.playmedia.constructor.ParentModel
import com.hvdevs.playmedia.constructor.User
import com.hvdevs.playmedia.databinding.ActivityMainListViewBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainListViewBinding //El viewBinding

    private lateinit var dbParent: DatabaseReference //Referencia a la bd del parent
    private lateinit var dbChild: DatabaseReference //Refecencia a la bd de los child

    private var parentItem:LinkedHashMap<String, ParentModel> = LinkedHashMap() //Lista del parent - child
    private var itemList: ArrayList<ParentModel> = arrayListOf() //Lista solo del parent
    private lateinit var helperAdapter: HelperAdapter //El adaptador del expandedListView

    private lateinit var dbUser: DatabaseReference //Referencia a la bd del usuario
    private var userData: User? = null //El objeto del usuario
    private lateinit var uid: String //Uid del usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainListViewBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Obtenemos el bundle de la actividad anterior
        val bundle: Bundle? = intent.extras
        uid = bundle?.getString("uid").toString()

        //Obtenemos los datos del usuario
        getUserData(uid)

        Log.d("USER", userData?.time.toString() + userData?.expire.toString() + userData?.type.toString())

        //Obtenemos los datos de la lista principal
        getParentData()

        //El click listener de los child en la lista principal
        binding.mainList.setOnChildClickListener { expandableListView, view, parentPosition, childPosition, long ->
            val parentInfo = itemList[parentPosition]
            val childInfo = parentInfo.itemList[childPosition]
//            Toast.makeText(baseContext, childInfo.name, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PlayerActivity::class.java)
            //Pasamos la licencia y la uri para el reproductor
            intent.putExtra("licence", childInfo.drm_license_url)
            intent.putExtra("uri", childInfo.uri)

            //Comparamos las condiciones del usuario
            //En este caso el tipo de usuario
            when (userData?.type) {
                0 -> { Toast.makeText(this, "No tiene acceso a este contenido", Toast.LENGTH_SHORT).show() }
                1 -> {
                    if (userData?.time == 0L){
                        Toast.makeText(this, "Su tiempo de prueba expiro", Toast.LENGTH_SHORT).show()
                    } else{
                        Toast.makeText(this, "Contenido de prueba", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                    }
                }
                2 -> {
                    //Formateador de las fechas por patron
                    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    //Parseamos la fecha obtenida de la db
                    val serverDate = LocalDate.parse(userData?.expire.toString(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    //La formateamos
                    val serverDateFormatted = formatter.format(serverDate)
                    //Obtenemos la fecha del dispositivo (local)
                    val localDate = LocalDate.now()
                    //La formateamos
                    val localDateFormatted = formatter.format(localDate)
                    //Si la fecha es mayor, reproduce el contenido
                    if (serverDateFormatted < localDateFormatted){
                        Toast.makeText(this, "Su licencia expira el $serverDateFormatted", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                    //Caso contrario, no lo reproduce
                    } else {
                        Toast.makeText(this, "Su licencia expiró", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("TIME", "$serverDateFormatted - - $localDateFormatted")

                }
            }
            false
        }
    }

    //Obtenemos los datos del usuario
    private fun getUserData(uid: String) {
        dbUser = FirebaseDatabase.getInstance().reference
        dbUser.child("users").child(uid).get().addOnSuccessListener {
            userData = it.getValue(User::class.java)
            Log.d("FIREBASE", "Got value ${userData?.type}")
        }.addOnFailureListener {
            Log.e("FIREBASE", "Errorr getting data", it)
        }
    }

    //Obtenemos los datos de los parent
    private fun getParentData() {
        //Usamos un contador, por comodidad en la forma en que se presentan los datos en la
        //base de datos. Los child, en la bd, estan enumerados del 0 al 9
        //por lo que es mas accesible para buscar los datos
        var j = 0
        dbParent = FirebaseDatabase.getInstance().getReference("channels")
        dbParent.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (parentSnapshot in snapshot.children){
                        val parent: String = parentSnapshot.child("name").value.toString()
                        Log.d("FIREBASE", parent)
                        //Una vez que obtenemos el parent, buscamos los child que le corresponden
                        //Pasamos el contador
                        getChildData(parent, j)
                        //Aumentamos el contador
                        j += 1
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE/DATABASE", error.toString())
            }

        })
    }

    //Obtenemos los datos de los child
    private fun getChildData(parent: String, j: Int) {
        //Instanciamos la base de datos y aqui es donde funciona el contador anterior
        dbChild = FirebaseDatabase.getInstance().getReference("channels/$j/samples")
        dbChild.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (childSnapshot in snapshot.children){
                        val child = childSnapshot.getValue(ChildModel::class.java)
                        Log.d("FIREBASE/CHILD", child?.name.toString())
                        //Pasamos los datos del parent y sus child (uno en uno, en este caso)
                        addItem(parent, child!!)

                        //Le pasamos los datos al adaptador
                        helperAdapter = HelperAdapter(this@MainListActivity, itemList, binding.mainList)
                        //Notificamos los cambios
                        helperAdapter.notifyDataSetChanged()
                        //Le instanciamos el adaptador al listView
                        binding.mainList.setAdapter(helperAdapter)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FIREBASE/DATABASE", error.toString())
            }

        })
    }

    //Funcion para agregar los items en la lista doble
    private fun addItem(parentItemList: String, subItemList: ChildModel): Int{
        val parentPosition: Int
        //Obtenemos la informacion del parent (en el contructor del objeto vemos que
        //tiene un ingreso de una string (el dato principal) y otro objeto (ChildModel)
        var parentInfo: ParentModel? = parentItem[parentItemList]
        //Si el parentInfo esta nulo, creamos uno nuevo y le comenzamos a ingresar los
        //datos de sus child
        if (parentInfo == null){
            parentInfo = ParentModel()
            parentInfo.name = parentItemList
            parentItem[parentItemList] = parentInfo
            itemList.add(parentInfo)
        }
        //Aca se comienzan a instanciar los datos de los child, con le index del parent
        val childItemList: ArrayList<ChildModel> = parentInfo.itemList
        var listSize = childItemList.size
        listSize++

        //Instanciamos todos los atributos del objeto ChildModel()
        val childInfo = ChildModel()
        childInfo.drm_license_url = subItemList.drm_license_url
        childInfo.drm_scheme = subItemList.drm_scheme
        childInfo.icon = subItemList.icon
        childInfo.name = subItemList.name
        childInfo.uri = subItemList.uri
        childItemList.add(childInfo)
        parentInfo.itemList = childItemList

        parentPosition = childItemList.indexOf(childInfo) //Por ultimo se colocan en la posicion del parent

        return parentPosition
    }
}