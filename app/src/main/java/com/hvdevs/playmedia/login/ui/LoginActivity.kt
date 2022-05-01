package com.hvdevs.playmedia.login.ui

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.hvdevs.playmedia.databinding.ActivityLoginBinding
import com.hvdevs.playmedia.mainlist.ui.MainListActivity
import com.hvdevs.playmedia.login.viewmodel.data.network.RepoImplement
import com.hvdevs.playmedia.login.viewmodel.domain.UseCaseImpl
import com.hvdevs.playmedia.login.viewmodel.presentation.viewmodel.MainViewModel
import com.hvdevs.playmedia.login.viewmodel.presentation.viewmodel.MainViewModelFactory
import com.hvdevs.playmedia.resourse.Resource
import com.hvdevs.playmedia.utilities.Utilities
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.*


@OptIn(DelicateCoroutinesApi::class)
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var uid: String

    private val viewModel by lazy {
        ViewModelProvider(this,
            MainViewModelFactory(UseCaseImpl(RepoImplement()))
        )[MainViewModel::class.java]
    } //La variable del viewModel

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.userInput.setOnKeyListener { v, keyCode, event -> //Escuchador de teclas
            when (keyCode) { //Si la keyCode es igual a enter, pasa al siguiente editText
                KeyEvent.KEYCODE_ENTER -> {
                    binding.passwordInput.requestFocus()
                    return@setOnKeyListener true
                }
                else -> {
                    return@setOnKeyListener false
                }
            }
        }

        binding.passwordInput.setOnKeyListener { v, keyCode, event -> //Lo mismo para el editText de password
            when (keyCode) {
                KeyEvent.KEYCODE_ENTER -> {
                    binding.userLogin.requestFocus()
                    return@setOnKeyListener true
                }
                else -> {
                    return@setOnKeyListener false
                }
            }
        }

        binding.userInput.setOnFocusChangeListener { view, b ->
            if (!b){
                if ("@" !in binding.userInput.text!!) {
                    binding.user.error = "Usuario Incorrecto"
                    binding.user.startAnimation(shakeError())
                }
                else binding.user.error = null
            } else {
                binding.user.error = null
            }
        }

        binding.passwordInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) binding.password.error = null
        }

        //Define el comportamiento del endIcon del input,
        // en este caso, limpia el text
        binding.user.setEndIconOnClickListener {
            binding.userInput.text = null
        }

        //Listener del boton login
        binding.userLogin.setOnClickListener {
            binding.user.error = null
            binding.password.error = null
            //Si los campos estan vacios:
            if (binding.userInput.text.isNullOrEmpty() || binding.passwordInput.text.isNullOrEmpty()){
                binding.user.error = "Complete los campos"
                binding.user.startAnimation(shakeError())
                binding.password.startAnimation(shakeError())
            }
            //Si no se cumple la condicion anterior, pasamos a logear
            else {
                revealLayoutAnimation(binding.progressBarLayout, false)//Mostramos el progressBar
                val user = binding.userInput.text.toString()
                val password = binding.passwordInput.text.toString()
                login(user, password)
            }
        }

        //Escondemos el teclado al tocar cualquier view
        Utilities.setupUI(binding.root, this)

        /**El usuario puede cambiar la hora y pasar por alto la comprobacion*/

        /**Aplicamos SharedPreferences para guardar usuario y contraseña en la app. Una vez logueado se recuerdan estos datos
         * y no es necesario volverlos a colocar*/

//        val sp = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

//        checkLogIn(sp)


//        binding.userLogin.setOnClickListener { rememberUser(sp) }

    }

    // Recuperamos el contenido de los textField

    private fun rememberUser(sp: SharedPreferences) {

        val user = binding.userInput.text.toString()
        val password = binding.passwordInput.text.toString()

        // Verificamos si los campos no son vacíos

        if (user.isNotEmpty() && password.isNotEmpty()) {

            // crearemos cuatro valores con el uso de la shared preferences instancia que se pasa como parámetro de la función. Los pares clave valor son el usuario, password, el inicio de sesión del usuario a traves de "active" y por último, "remember", indicándonos que el usuario inicio sesión presionando el boton de ingresar. Esto será útil ya que cada vez que el usuario salga de la sesión, la próxima vez que entre, los datos de email y password serán recordados.

            with(sp.edit()) {

                putString("user", user)
                putString("password", password)
                putString("active", "true")
                putString("remember", "true")

                apply()
            }

        } else {
            Toast.makeText(this, "Usiario incorrecto, intente nuevamente", Toast.LENGTH_SHORT)
                .show()
        }
        // al hacer click en el boton ingresar, el usuario pasa al fragment con el listado de canales

        startActivity(Intent(this, MainListActivity::class.java))
        finish()


    }

    //Si el usuario ha iniciado sesión y por alguna razón sale de la aplicación y luego quisiera volver a entrar a la app, ya no será necesario enviarlo a la pantalla de Login, sino que directamente lo llevaremos a la lista de canales. Para eso crearemos otra función que será la encargada de verificar el estado de la sesión del usuario.

//    private fun checkLogIn(sp: SharedPreferences){
//        if (sp.getString("active","") =="true"){
//            startActivity(Intent(this,MainListActivity::class.java))
//            finish()
//        } else{
//            if (sp.getString("remember","") == "true"){
//                binding.userInput.setText(sp.getString("user",""))
//                binding.passwordInput.setText(sp.getString("password",""))
//            }
//        }
//    }

    private fun login(user: String, password: String){
        binding.passwordInput.isEnabled = false
        binding.userInput.isEnabled = false
        binding.userLogin.isClickable = false
        auth.signInWithEmailAndPassword(user, password)
            .addOnCompleteListener (this) { task: Task<AuthResult> ->
                if (task.isSuccessful){
                    uid = auth.currentUser?.uid.toString()
                    startNewSession()
                    Log.d("FIREBASE", uid)
                } else {
                    binding.passwordInput.isEnabled = true
                    binding.userInput.isEnabled = true
                    binding.userLogin.isClickable = true
                    revealLayoutAnimation(binding.progressBarLayout, true)
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthWeakPasswordException) {
//                        binding.user.error = "Usuario Incorrecto"
                        binding.password.error = "Contraseña Incorrecta"
                        binding.user.startAnimation(shakeError())
                        binding.password.startAnimation(shakeError())
//                        binding.userInput.requestFocus()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        binding.password.error = "Usuario o contraseña Incorrectos"
                        binding.user.startAnimation(shakeError())
                        binding.password.startAnimation(shakeError())
                        binding.userInput.requestFocus()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        val errorMsg = Objects.requireNonNull(task.exception)?.localizedMessage
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e(TAG, e.message!!)
                        val errorMsg = Objects.requireNonNull(task.exception)?.localizedMessage
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun startNewSession() {
        viewModel.fetchSessions.observe(this@LoginActivity) { result ->
            when (result){
                is Resource.Loading -> {} //Cuando comienza a cargarse la informacion
                is Resource.Success -> {
                    if (result.data < 2) {
                        val newSession = result.data + 1
                        val dbUser = FirebaseDatabase.getInstance().reference
                        dbUser
                            .child("users/$uid/sessions/quantity")
                            .setValue(newSession)
                        val intent = Intent(this, MainListActivity::class.java)
                        intent.putExtra("uid", uid)
                        intent.putExtra("sessions", newSession)
                        revealLayoutAnimation(
                            binding.progressBarLayout,
                            true
                        )//Ocultamos el progressBar
                        startActivity(intent)
                        finish()
                        Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        revealLayoutAnimation(binding.progressBarLayout, true)
                        showDialog()
                    }
                } //Cuando la busqueda se completa
                is Resource.Failure -> {
                    revealLayoutAnimation(binding.progressBarLayout, true)//Ocultamos el progressBar
                } //Cuando la busqueda falla
        }
        }
    }

    //Esta funcion se encarga solamente de animar la aparicion del progressBar
    private fun revealLayoutAnimation(layout: ConstraintLayout, isRevealed: Boolean) {

        // based on the boolean value the
        // reveal layout should be toggled
        if (!isRevealed) {

            // get the right and bottom side
            // lengths of the reveal layout
            val x: Int = layout.right / 2
            val y: Int = layout.bottom / 2

            // here the starting radius of the reveal
            // layout is 0 when it is not visible
            val startRadius = 0

            // make the end radius should
            // match the while parent view
            val endRadius = kotlin.math.hypot(
                layout.width.toDouble(),
                layout.height.toDouble()
            ).toInt()

            // create the instance of the ViewAnimationUtils to
            // initiate the circular reveal animation
            val anim = ViewAnimationUtils.createCircularReveal(
                layout, x, y,
                startRadius.toFloat(), endRadius.toFloat()
            )

            // make the invisible reveal layout to visible
            // so that upon revealing it can be visible to user
            layout.visibility = View.VISIBLE
            // now start the reveal animation
            anim.start()

        } else {

            // get the right and bottom side lengths
            // of the reveal layout
            val x: Int = layout.right / 2
            val y: Int = layout.bottom / 2

            // here the starting radius of the reveal layout is its full width
            val startRadius: Int = kotlin.math.max(layout.width, layout.height)

            // and the end radius should be zero at this
            // point because the layout should be closed
            val endRadius = 0

            // create the instance of the ViewAnimationUtils
            // to initiate the circular reveal animation
            val anim = ViewAnimationUtils.createCircularReveal(
                layout, x, y,
                startRadius.toFloat(), endRadius.toFloat()
            )

            // now as soon as the animation is ending, the reveal
            // layout should also be closed
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    layout.visibility = View.GONE

                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })

            // start the closing animation
            anim.start()
        }
    }

    //Creamos el dialogo
    private fun showDialog(){
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder
            .setMessage("Existe mas de dos (2) sesiones activas")
            .setCancelable(false) //No se puede cancelar el dialog
            .setPositiveButton("Ok") { dialog, which ->
                auth.signOut()
                finish()
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Error de autenticacion")
        alert.show()
    }

    //Una animacion de sacudida para los text field
    private fun shakeError(): TranslateAnimation {
        val shake = TranslateAnimation(0f, 10f, 0f, 0f)
        shake.duration = 500
        shake.interpolator = CycleInterpolator(7f)
        return shake
    }
}