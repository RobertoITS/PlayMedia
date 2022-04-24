package com.hvdevs.playmedia.splashscreen.ui

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hvdevs.playmedia.BuildConfig
import com.hvdevs.playmedia.R
import com.hvdevs.playmedia.databinding.ActivitySplashScreenBinding
import com.hvdevs.playmedia.login.ui.LoginActivity
import com.hvdevs.playmedia.mainlist.ui.MainListActivity
import com.hvdevs.playmedia.resourse.Resource
import com.hvdevs.playmedia.splashscreen.viewmodel.data.network.VersionRepoImplement
import com.hvdevs.playmedia.splashscreen.viewmodel.domain.VersionUseCaseImplement
import com.hvdevs.playmedia.splashscreen.viewmodel.presentation.viewmodel.VersionViewModel
import com.hvdevs.playmedia.splashscreen.viewmodel.presentation.viewmodel.VersionViewModelFactory
import com.hvdevs.playmedia.tv.TvMainActivity

class SplashScreen : AppCompatActivity() {
    // creamos la variable auth que gestionara los metodos de inicio de sesion usando el modulo de Auth de Firebase.
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySplashScreenBinding
    private var session = 0
    private var actualVersion = 0

    private var isTV: Boolean = false

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            VersionViewModelFactory(
                VersionUseCaseImplement(VersionRepoImplement()))
        )[VersionViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val anim2: Animation = AnimationUtils.loadAnimation(this, R.anim.translation_up_to_down)
        binding.image.animation = anim2

        binding.container.alpha = 0f
        binding.container.animate().alpha(1f).setDuration(1200).setStartDelay(600).start()


        //MSO: Chequeamos si es Android TV

        isTV = isTv()

        // obtenemos instancia de FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // decimos que si current user esta vacio, el usuario no ha iniciado sesion, con lo cual lo mandamos la pagina de registro o login, usando la funcion debounce.
        if (auth.currentUser == null){
            val intent = Intent(this, LoginActivity::class.java)
            debounce(intent)
        }
        // si no es nulo, es decir que ya inicio sesion, por lo cual sus datos estan guardados, si ese es el caso lo mandamos a la pagina inicial.
        else {
            val uid = auth.currentUser!!.uid
            val dbUser = FirebaseDatabase.getInstance().reference.child("users/$uid/sessions/quantity").get()
            dbUser.addOnSuccessListener {
                session = Integer.parseInt(it.value.toString())
            }

                val intent = Intent(this, MainListActivity::class.java)
                intent.putExtra("uid", uid)
                debounce(intent)

        }

    }
    // creamos una funcion privada para que la pantalla splash dure 3 segundos. Usamos la funcion handler.
    // pasamos la funcion startActivity y le pasamos el intent del main activity, para que cuando haya transcurrido los 3 segundos
    // navegue a esa pagina
    private fun debounce(intent: Intent){
        Handler(Looper.getMainLooper()).postDelayed({
            intent.putExtra("sessions", session)
            if (checkForUpdate()) update()
            else {
                startActivity(intent)
                finish()
            }
        }, 3000) // 3000 milisegundos = 3 segundos
    }

    private fun update() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder
            .setMessage("Existe una version nueva de esta app. Por favor contactese con su proveedor para actualizar")
            .setCancelable(false) //No se puede cancelar el dialog
            .setPositiveButton("Salir") { dialog, which ->
                dialog.dismiss()
                finish()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Actualizacion disponible")
        alert.show()
    }

    private fun checkForUpdate(): Boolean{
        viewModel.fetchVersion.observe(this@SplashScreen){ result ->
            when (result){
                is Resource.Loading -> {}
                is Resource.Success -> {
                    actualVersion = result.data
                }
                is Resource.Failure -> {
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder
                        .setMessage("Algo salio mal al comprobar la version")
                        .setCancelable(false) //No se puede cancelar el dialog
                        .setPositiveButton("Salir") { dialog, which ->
                            dialog.dismiss()
                            finish()
                        }
                    val alert = dialogBuilder.create()
                    alert.setTitle("Error de autenticacion")
                    alert.show()
                }
            }

        }
        val currentVersion = BuildConfig.VERSION_CODE
        return currentVersion < actualVersion
    }

    private fun isTv(): Boolean {
        return (packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK))
    }
}