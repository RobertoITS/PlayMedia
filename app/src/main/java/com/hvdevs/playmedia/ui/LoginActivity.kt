package com.hvdevs.playmedia.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hvdevs.playmedia.databinding.ActivityLoginBinding
import com.raqueveque.foodexample.Utilities
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var db: DatabaseReference

    private var type = ""

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
//
//        //Usar la hora de servidor
//        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
//        val time = LocalDateTime.now()
//        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
//        val timeF = formatter.format(time)
////        Date(time)
//        val timeZone = TimeZone.getTimeZone("America/Argentina/Buenos_Aires")
//
//        simpleDateFormat.timeZone = timeZone
//        val dateTime = simpleDateFormat.format(Date())
//        Toast.makeText(this, "$dateTime - - $time", Toast.LENGTH_SHORT).show()
//
//        binding.userInput.setText(dateTime.toString())
//        binding.passwordInput.setText(timeF.toString())

    }

    private fun login(user: String, password: String){
        auth.signInWithEmailAndPassword(user, password)
            .addOnCompleteListener (this) { task: Task<AuthResult> ->

                if (task.isSuccessful){
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                    val uid = auth.currentUser?.uid
                    Log.d("FIREBASE", uid.toString())
                    db = FirebaseDatabase.getInstance().getReference("users/$uid/type")
                    db.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                for (ss in snapshot.children){
                                    type = ss.value.toString()
                                    Log.d("FIREBASE", type)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
//                    if (type == "0") Toast.makeText(this, "No tiene permisos para ingresar", Toast.LENGTH_SHORT).show()
//                    if (type == "1") Toast.makeText(this, "Tiene tiempo de prueba", Toast.LENGTH_SHORT).show()
//                    if (type == "2") {
//                        Toast.makeText(this, "Hasta que expire su licencia", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainListActivity::class.java))
//                    }
                } else {
                    val errorMsg = Objects.requireNonNull(task.exception)?.localizedMessage
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
    }

}