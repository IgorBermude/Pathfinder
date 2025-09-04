package com.example.pathfinder.ui.register

import androidx.fragment.app.viewModels
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.pathfinder.LoginUiState
import com.example.pathfinder.R
import com.example.pathfinder.data.AuthViewModel
import com.example.pathfinder.data.models.Usuario
import com.example.pathfinder.data.repositories.UsuarioRepository
import com.example.pathfinder.databinding.FragmentRegisterBinding
import com.example.pathfinder.util.FuncoesUteis
import android.text.Editable
import android.text.TextWatcher
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var binding: FragmentRegisterBinding? = null
    private val vm: AuthViewModel by viewModels{ AuthViewModel.Factory }
    private var fotoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuarioRepository = UsuarioRepository()

        lifecycleScope.launch {
            //Previne bugs
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.authUiState.collect { state ->
                    when(state){
                        LoginUiState.LOADING -> {
                            //binding?.progressBar?.visibility = View.VISIBLE
                            Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                        }
                        LoginUiState.SUCCESS -> {
                            //binding?.progressBar?.visibility = View.GONE
                            Toast.makeText(requireContext(), "Login criado com sucesso", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                        LoginUiState.ERROR -> {
                            //binding?.progressBar?.visibility = View.GONE
                            Toast.makeText(requireContext(), "Erro ao criar login", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Máscara para data de nascimento (dd/MM/yyyy)
        binding?.etBirthDate?.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "##/##/####"
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                val str = s.toString().replace("[^\\d]".toRegex(), "")
                var formatted = ""
                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        if (i < str.length) formatted += m
                    } else {
                        if (i < str.length) formatted += str[i]
                        else break
                        i++
                    }
                }
                s?.replace(0, s.length, formatted)
                isUpdating = false
            }
        })

        binding?.btnRegister?.setOnClickListener {
            val name = binding?.etName?.text.toString()
            val email = binding?.etEmail?.text.toString()
            val password = binding?.etPassword?.text.toString()
            val ageInput = binding?.etBirthDate?.text.toString()

            if (!validarCampos(name, email, password, ageInput)) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val age = FuncoesUteis.parseDate(ageInput)
                if (age == null) {
                    Toast.makeText(
                        requireContext(),
                        "Formato de data inválido. Use dd/MM/yyyy.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val usuario = Usuario(
                    nomeUsuario = name,
                    emailUsuario = email,
                    senhaUsuario = usuarioRepository.criptografarSenha(password),
                    idadeUsuario = age,
                    fotoUsuario = null
                )

                try {
                    vm.register(usuario)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erro ao registrar usuário. Tente novamente.", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterFragment", "Erro genérico: ${e.message}", e)
                }
            }
        }

        binding?.tvCreateAccount?.setOnClickListener{
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun validarCampos(name: String, email: String, password: String, ageInput: String): Boolean {
        return name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && ageInput.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
