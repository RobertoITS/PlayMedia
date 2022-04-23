package com.hvdevs.playmedia.mainlist.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hvdevs.playmedia.R.color
import com.hvdevs.playmedia.databinding.ActivityMainListViewBinding
import com.hvdevs.playmedia.exoplayer2.PlayerActivity
import com.hvdevs.playmedia.login.constructor.User
import com.hvdevs.playmedia.login.ui.LoginActivity
import com.hvdevs.playmedia.mainlist.adapters.ExpandedListAdapter
import com.hvdevs.playmedia.mainlist.constructor.ParentModel
import com.hvdevs.playmedia.mainlist.viewmodel.data.network.MainListRepoImplement
import com.hvdevs.playmedia.mainlist.viewmodel.domain.MainListUseCaseImplement
import com.hvdevs.playmedia.mainlist.viewmodel.presentation.viewmodel.MainListViewModel
import com.hvdevs.playmedia.mainlist.viewmodel.presentation.viewmodel.MainListViewModelFactory
import com.hvdevs.playmedia.resourse.Resource
import com.hvdevs.playmedia.utilities.Connectivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainListViewBinding //El viewBinding

    private var itemList: ArrayList<ParentModel> = arrayListOf() //Lista solo del parent
    private lateinit var expandedListAdapter: ExpandedListAdapter //El adaptador del expandedListView

    private lateinit var dbUser: DatabaseReference //Referencia a la bd del usuario
    private var userData: User? = null //El objeto del usuario
    private lateinit var uid: String //Uid del usuario
    private var testContent = false //Controla el contenido de prueba
    private var session = 0 //Sesiones activas del usuario

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            MainListViewModelFactory(MainListUseCaseImplement(MainListRepoImplement()))
        )[MainListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainListViewBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Obtenemos el bundle de la actividad anterior
        val bundle: Bundle? = intent.extras
        uid = bundle?.getString("uid").toString()
        session = bundle?.getInt("sessions")!!.toInt() //Obtenemos las sesiones pasadas por el putExtras
        Log.d("SESSIONS", session.toString())


        //Obtenemos los datos del usuario
        getUserData(uid)

        //Obtenemos los datos de la lista principal
        getListData()

        //El click listener de los child en la lista principal
        binding.mainList.setOnChildClickListener { expandableListView, view, parentPosition, childPosition, long ->
            val parentInfo = itemList[parentPosition]
            val childInfo = parentInfo.itemList[childPosition]
            val intent = Intent(this, PlayerActivity::class.java)
            //Pasamos la licencia y la uri para el reproductor
            intent.putExtra("licence", childInfo.drm_license_url)
            intent.putExtra("uri", childInfo.uri)
            intent.putExtra("uid", uid)

            //Comparamos las condiciones del usuario
            //En este caso el tipo de usuario
            when (userData?.type) {
                0 -> {
                    if (userData?.time!! < 2000){
                        Toast.makeText(this, "Su tiempo de prueba expiro", Toast.LENGTH_SHORT).show()
                    } else{
                        testContent = true
                        Toast.makeText(this, "Contenido de prueba", Toast.LENGTH_SHORT).show()
                        Log.d("TEST", userData?.time.toString())
                        //Pasamos el tiempo restante de prueba
                        intent.putExtra("time", userData?.time)
                        //pasamos que es contenido de prueba
                        intent.putExtra("testContent", testContent)
                        startActivity(intent)
                    }
                }
                1 -> {
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
                        //Pasamos si es contenido de prueba o no
                        intent.putExtra("testContent", testContent)
                        startActivity(intent)
                    //Caso contrario, no lo reproduce
                    } else {
                        Toast.makeText(this, "Su licencia expiró", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            false
        }

        //Le agregamos estilo a los colores del refresco
        binding.swipeLayout.setColorSchemeResources(
            color.s1,
            color.s2,
            color.s3,
            color.s4
        )

        //Accion de refresco
        binding.swipeLayout.setOnRefreshListener {
            getListData()
        }

        // boton LogOt, agrego una escucha al click
        binding.btnLogOut.setOnClickListener {
            showDialogSignOut()
        }

    }

    //Mostramos el dialogo de cierre de sesion:
    private fun showDialogSignOut() {
        //Creamos un dialog builder para el cierre de sesion
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder
            .setMessage("¿Desea cerrar su sesion?")
            .setCancelable(false) //No se puede cancelar el dialog
            .setPositiveButton("Si") { dialog, which ->
                logOut()
                dialog.dismiss()
            }
            .setNegativeButton ("No") { dialog, which ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Cierre de sesion")
        alert.show()
    }

    //Obtenemos los datos de la lista usando el viewModel y los corrutinas
    private fun getListData() {
        viewModel.fetchListData.observe(this@MainListActivity){ result ->
            if (Connectivity.isOnlineNet() == true){ //Comprobamos que haya conexion a internet:
                when (result){
                    is Resource.Loading -> { //Cuando carga los datos:
                        binding.progressBar.visibility = View.VISIBLE //Mostramos el progressBar
                    }
                    is Resource.Success -> { //Cuando se obtienen los datos
                        initExpandableListView(result.data) //Inicializamos el expandableListView
                        binding.progressBar.visibility = View.GONE //Ocultamos el progressBar
                        if (binding.swipeLayout.isRefreshing) {
                            binding.swipeLayout.isRefreshing = false //Paramos la animacion de refresco
                        }
                    }
                    is Resource.Failure -> { //En caso de fallar:
                        binding.progressBar.visibility = View.GONE //Paramos el progressBar
                        Toast.makeText(this, result.exception.toString(), Toast.LENGTH_SHORT).show()
                        Log.e("FIREBASE ERROR", result.exception.toString())
                    }
                }
            }
            else { //Si no hay conexion:
                Toast.makeText(this, "No hay conexion a intertet, conectese y acualice lista", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun initExpandableListView(data: ArrayList<ParentModel>) {
        itemList = data //Pasamos los datos obtenidos a la lista principal
        expandedListAdapter = ExpandedListAdapter() //Instanciamos el adaptador
        expandedListAdapter.getList(itemList) //Usamos las funciones que trae dentro para pasarle los datos
        expandedListAdapter.getContext(this@MainListActivity, binding.mainList) //Aca igual
        expandedListAdapter.notifyDataSetChanged()//Notificamos los cambios
        binding.mainList.setAdapter(expandedListAdapter) //Le instanciamos el adaptador al listView
    }

    // creo la funcion logOut y llamo a la shared preferences creado en LoginActivity
    // si el usuario clickea en el cierre de sesion, se dirige a la pantalla de Login y cambia es estado del login, pasa de estar activo a inactivo.
    private fun logOut() {
        dbUser = FirebaseDatabase.getInstance().reference
        dbUser.child("users/$uid/sessions/quantity")
            .setValue(session - 1)
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        val sp = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        with(sp.edit()){
            putString("active","false")
            apply()
        }
        //paso a la actividad Login, y termino esta actividad.
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    //Obtenemos los datos del usuario
    private fun getUserData(uid: String) {
        dbUser = FirebaseDatabase.getInstance().reference
        dbUser.child("users").child(uid).get().addOnSuccessListener {
            userData = it.getValue(User::class.java)
            Log.d("FIREBASE", "Got value ${userData?.time}")
        }.addOnFailureListener {
            Log.e("FIREBASE", "Errorr getting data", it)
        }
    }

    override fun onResume() {
        getUserData(uid)
        super.onResume()
    }
}