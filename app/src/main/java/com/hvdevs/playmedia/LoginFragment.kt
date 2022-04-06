package com.hvdevs.playmedia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.hvdevs.playmedia.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
//        return inflater.inflate(R.layout.fragment_login, container, false)
        //Cambiar el icono Start
//        binding.user.setStartIconDrawable(R.drawable.ic_email)

        //Observador del edittext
        //Observa lo que se ingresa al textField
//        binding.userInput.addTextChangedListener(object : TextWatcher{
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                if (binding.userInput.text!!.isEmpty()) binding.user.error = null
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if ("@" !in binding.userInput.text!!){
//                    binding.user.error = "Usuario Incorrecto"
//                } else {
//                    binding.user.error = null
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                if (binding.userInput.text!!.isEmpty()) binding.user.error = null
//            }
//        })

        //Comprueba que cuando pierda el foco, o lo gane, si no cumple con la condicion,
        //que muestre el mensaje de error
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
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }

        return binding.root
    }
}