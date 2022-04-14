package com.hvdevs.playmedia.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.hvdevs.playmedia.PlayerActivity
import com.hvdevs.playmedia.adapters.HelperAdapter
import com.hvdevs.playmedia.constructor.ChildModel
import com.hvdevs.playmedia.constructor.ParentModel
import com.hvdevs.playmedia.constructor.User
import com.hvdevs.playmedia.databinding.ActivityMainListViewBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainListViewBinding

    private lateinit var dbParent: DatabaseReference
    private lateinit var dbChild: DatabaseReference

    private var parentItem:LinkedHashMap<String, ParentModel> = LinkedHashMap()
    private var itemList: ArrayList<ParentModel> = arrayListOf()
    private lateinit var helperAdapter: HelperAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainListViewBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val userTime = intent.extras?.getLong("time")
        val userExpire = intent.extras?.getString("time")
        val userType = intent.extras?.getInt("type")

        Log.d("USER", userTime.toString() + userExpire.toString() + userType.toString())

        val date = LocalDate.parse(userExpire, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        Log.d("TIME", date.toString())
        Toast.makeText(this, date.toString(), Toast.LENGTH_SHORT).show()

        getParentData()

        binding.mainList.setOnChildClickListener { expandableListView, view, parentPosition, childPosition, long ->
            val parentInfo = itemList[parentPosition]
            val childInfo = parentInfo.itemList[childPosition]
            Toast.makeText(baseContext, childInfo.name, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("licence", childInfo.drm_license_url)
            intent.putExtra("uri", childInfo.uri)
            when (userType) {
                0 -> { Toast.makeText(this, "No tiene acceso a este contenido", Toast.LENGTH_SHORT).show() }
                1 -> {
                    if (userTime == 0L){
                        Toast.makeText(this, "Su tiempo de prueba expiro", Toast.LENGTH_SHORT).show()
                    } else{
                        Toast.makeText(this, "Contenido de prueba", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                    }
                }
                2 -> {
                    Toast.makeText(this, "Su licencia expira el $userExpire", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }
            }
            false
        }

        binding.mainList.setOnGroupClickListener { expandableListView, view, parentPosition, long ->
            val parentInfo = itemList[parentPosition]
            Toast.makeText(baseContext, parentInfo.name, Toast.LENGTH_SHORT).show()
            false
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
                        helperAdapter = HelperAdapter(this@MainListActivity, itemList)
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