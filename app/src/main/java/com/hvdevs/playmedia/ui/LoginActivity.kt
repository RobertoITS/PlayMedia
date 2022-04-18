package com.hvdevs.playmedia.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hvdevs.playmedia.constructor.User
import com.hvdevs.playmedia.databinding.ActivityLoginBinding
import com.raqueveque.foodexample.Utilities
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var db: DatabaseReference

    private var userData: User? = null

    private lateinit var uid: String

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.userInput.setOnFocusChangeListener { view, b ->
            if (!b){
                if ("@" !in binding.userInput.text!!) binding.user.error = "Usuario Incorrecto"
                else binding.user.error = null
            } else binding.user.error = null
        }

        //Define el comportamiento del endIcon del input,
        // en este caso, limpia el text
        binding.user.setEndIconOnClickListener {
            binding.userInput.text = null
        }

        binding.userLogin.setOnClickListener {
            if (binding.userInput.text.isNullOrEmpty() || binding.passwordInput.text.isNullOrEmpty())
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show()
            else {
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

        val sp = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

        checkLogIn(sp)


        binding.userLogin.setOnClickListener { rememberUser(sp) }

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

    private fun checkLogIn(sp: SharedPreferences){

        if (sp.getString("active","") =="true"){

            startActivity(Intent(this,MainListActivity::class.java))
            finish()
        } else{

            if (sp.getString("remember","") == "true"){

                binding.userInput.setText(sp.getString("user",""))
                binding.passwordInput.setText(sp.getString("password",""))
            }
        }
    }

    private fun login(user: String, password: String){
        auth.signInWithEmailAndPassword(user, password)
            .addOnCompleteListener (this) { task: Task<AuthResult> ->
                if (task.isSuccessful){
                    uid = auth.currentUser?.uid.toString()
                    Log.d("FIREBASE", uid)
                    val intent = Intent(this, MainListActivity::class.java)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = Objects.requireNonNull(task.exception)?.localizedMessage
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
    }
}