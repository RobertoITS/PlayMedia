package com.hvdevs.playmedia.mainlist.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.hvdevs.playmedia.R.*
import com.hvdevs.playmedia.databinding.ActivityMainListViewBinding
import com.hvdevs.playmedia.exoplayer2.PlayerActivity
import com.hvdevs.playmedia.exoplayer2.leanbackforandroidtv.LeanbackActivity
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
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import android.os.Build.VERSION_CODES as VERSION_CODES1

class MainListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainListViewBinding //El viewBinding

    private lateinit var itemList: ArrayList<ParentModel> //Lista solo del parent
    private lateinit var expandedListAdapter: ExpandedListAdapter //El adaptador del expandedListView

    private lateinit var dbUser: DatabaseReference //Referencia a la bd del usuario
    private var userData: User? = null //El objeto del usuario
    private lateinit var uid: String //Uid del usuario
    private var testContent = false //Controla el contenido de prueba
    private var session = 0 //Sesiones activas del usuario
//    private var isTv: Boolean = false

    private val viewModel by lazy { //Instanciamos el view model con sus implementos usando el Factory
        ViewModelProvider(
            this,
            MainListViewModelFactory(MainListUseCaseImplement(MainListRepoImplement()))
        )[MainListViewModel::class.java]
    }

    private lateinit var runnable: Runnable //Un handler y un runnable para hacer una repeticion de funcionalidad
    private val handler = Handler()

    @RequiresApi(VERSION_CODES1.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainListViewBinding.inflate(layoutInflater)
        AndroidThreeTen.init(this)
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
            if (userData?.active!!) {
                /** esto mismo hacer en el player activity*/
                val parentInfo = itemList[parentPosition]
                val childInfo = parentInfo.itemList[childPosition]

                //Shared Preferences
                //Para el fragment que lo reproduce, solo le pasamos esta informacion, la actividad recibe el resto
                val sp = getSharedPreferences("channel", Context.MODE_PRIVATE)!! //Las SP
                with(sp.edit()) {

                    putString("uri", childInfo.uri)
                    putString("licence", childInfo.drm_license_url)
                    putInt("parentPosition", parentPosition)
                    putInt("childPosition", childPosition)
                    apply()
                }

                //Pasamos el intent dependiendo del dispositivo
                val intent: Intent = if (isTv()/*No necesitamos crear un variable!! */)
                    Intent(this, LeanbackActivity::class.java)
                else Intent(this, PlayerActivity::class.java)

                //Pasamos la licencia y la uri para el reproductor
                intent.putExtra("licence", childInfo.drm_license_url)
                intent.putExtra("uri", childInfo.uri)
                intent.putExtra("uid", uid)

                //Comparamos las condiciones del usuario
                //En este caso el tipo de usuario
                when (userData?.type) {
                    0 -> {
                        if (userData?.time!! < 2000) {
                            Toast.makeText(this, "Su tiempo de prueba expiro", Toast.LENGTH_SHORT)
                                .show()
                        } else {
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
                        //Obtenemos la fecha del dispositivo (local)
                        val localDate = LocalDate.now()
                        //Si la fecha es mayor, reproduce el contenido
                        if (serverDate > localDate){
                            //Pasamos si es contenido de prueba o no
                            intent.putExtra("testContent", testContent)
                            startActivity(intent)
                            //Caso contrario, no lo reproduce
                        } else {
                            Toast.makeText(this, "Su licencia expiró", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                //Creamos un dialog builder para el cierre de sesion
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder
                    .setMessage("Su cuenta a sido desactivada.")
                    .setCancelable(false) //No se puede cancelar el dialog
                    .setPositiveButton("Salir") { dialog, which ->
                        logOut()
                        dialog.dismiss()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("Error de sesion")
                alert.show()
            }
            false
        }

        binding.swipeLayout.setColorSchemeResources( //Le agregamos estilo a los colores del refresco
            color.s1,
            color.s2,
            color.s3,
            color.s4
        )

        binding.swipeLayout.setOnRefreshListener { //Accion de refresco
            itemList = arrayListOf() //Vaciamos la lista
            initExpandableListView(itemList) //Pasamos la lista vacia para vaciar el listView
            binding.anim.visibility = View.GONE
            handler.removeCallbacksAndMessages(null) //Removemos las acciones del handler
            getUserData(uid)
            getListData()
        }

        binding.btnLogOut.setOnClickListener { // boton LogOt, agrego una escucha al click
            showDialogSignOut()
        }
    }

    private fun showDialogSignOut() { //Mostramos el dialogo de cierre de sesion:
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

    private fun getListData() { //Obtenemos los datos de la lista usando el viewModel y las corrutinas
        viewModel.fetchListData.observe(this@MainListActivity){ result ->
            if (Connectivity.isOnlineNet()){ //Comprobamos que haya conexion a internet:
                when (result){
                    is Resource.Loading -> { //Cuando carga los datos:
                        binding.progressBar.visibility = View.VISIBLE //Mostramos el progressBar
                    }
                    is Resource.Success -> { //Cuando se obtienen los datos
                        initExpandableListView(result.data) //Inicializamos el expandableListView
                        //Pasamos la lista al shared preferences:
                        saveListSP(result.data)
                        binding.progressBar.visibility = View.GONE //Ocultamos el progressBar
                        if (binding.swipeLayout.isRefreshing) {
                            binding.swipeLayout.isRefreshing = false //Paramos la animacion de refresco
                        }
                    }
                    is Resource.Failure -> { //En caso de fallar:
                        loadingError()
                    }
                }
            }
            else { //Si no hay conexion:
                loadingError()
            }
        }
    }

    private fun saveListSP(list: ArrayList<ParentModel>) { //Guardamos la lista en las SP
        val sp = getSharedPreferences("channel", Context.MODE_PRIVATE)!! //Las SP
        val listToJson = Gson().toJson(list) //Pasamos la lista a String
        with(sp.edit()){ //Las SP
            putString("list", listToJson) //La colocamos en las SP, luego la obtenemos
            apply()
        }

    }

    private fun initExpandableListView(data: ArrayList<ParentModel>) { //Inicializamos el listView
        itemList = arrayListOf()
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

    private fun getUserData(uid: String) { //Obtenemos los datos del usuario
        dbUser = FirebaseDatabase.getInstance().reference
        dbUser.child("users").child(uid).get().addOnSuccessListener {
            userData = it.getValue(User::class.java)
            Log.d("FIREBASE", "Got value ${userData?.time}")
        }.addOnFailureListener {
            Log.e("FIREBASE", "Errorr getting data", it)
        }
    }

    private fun isTv(): Boolean { //Comprobamos si es tv o celular
        return (packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK))
    }

    override fun onResume() {
        if (Connectivity.isOnlineNet()){
            getUserData(uid)
        } else loadingError()
        super.onResume()
    }

    private fun swipeAnimation(image: LottieAnimationView, animation: Int){
        image.setAnimation(animation)
        image.playAnimation()
    }

    private fun shakeError(): TranslateAnimation { //Animacion de sacudida
        val shake = TranslateAnimation(0f, 10f, 0f, 0f)
        shake.duration = 500
        shake.interpolator = CycleInterpolator(7f)
        return shake
    }

    private fun loadingError() {
        binding.progressBar.visibility = View.GONE
        binding.swipeLayout.isRefreshing = false //Paramos la animacion de refresco
        binding.anim.visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(this, anim.bounce) //Cargamos la animacion de rebote
        binding.tvBounce.animation = anim
        binding.tvBounce.animate()
        runnable = Runnable {
            swipeAnimation(binding.animation, raw.swipe_down)
            binding.tvBounce.startAnimation(shakeError())
            handler.postDelayed(runnable, 3000) //Pasamos que se repita cada 2 segundos
        }
        handler.postDelayed(runnable, 3000) //Dejamos una espera de 1 segundo
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        super.onStop()
    }
}