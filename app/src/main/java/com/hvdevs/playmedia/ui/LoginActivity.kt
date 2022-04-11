package com.hvdevs.playmedia.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hvdevs.playmedia.databinding.ActivityLoginBinding
import com.raqueveque.foodexample.Utilities

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.user.addOnEditTextAttachedListener {
//            Toast.makeText(context, "Escribiendo...", Toast.LENGTH_SHORT).show()
        }
        binding.user.addOnEndIconChangedListener { textInputLayout, previousIcon ->
//            textInputLayout.endIconMode = ContextCompat.getDrawable(requireContext(), R.drawable.ic_email)
        }

        binding.userLogin.setOnClickListener {
            startActivity(Intent(this, MainListActivity::class.java))
        }

        //Escondemos el teclado al tocar cualquier view
        Utilities.setupUI(binding.root, this)

    }
}