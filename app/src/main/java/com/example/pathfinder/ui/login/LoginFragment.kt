package com.example.pathfinder.ui.login

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.pathfinder.LoginUiState
import com.example.pathfinder.R
import com.example.pathfinder.data.AuthViewModel
import com.example.pathfinder.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import android.content.Intent
import android.widget.CheckBox
import android.widget.TextView
import com.example.pathfinder.ui.MainActivity
import androidx.activity.OnBackPressedCallback
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.pathfinder.data.models.Usuario
import com.example.pathfinder.data.repositories.UsuarioRepository
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {
    private var binding: FragmentLoginBinding? = null
    private lateinit var navController: NavController
    private val vm: AuthViewModel by viewModels{ AuthViewModel.Factory }
    private lateinit var cbRememberMe: CheckBox
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()

        val usuarioRepository = UsuarioRepository()

        val loginInfoText = view.findViewById<TextView>(R.id.tvLoginInfo)
        cbRememberMe = view.findViewById(R.id.cbRememberMe)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)

        // Recupera o estado salvo
        val prefs = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val rememberMe = prefs.getBoolean("remember_me", false)
        cbRememberMe.isChecked = rememberMe
        if (rememberMe) {
            etEmail.setText(prefs.getString("saved_email", ""))
            etPassword.setText(prefs.getString("saved_password", ""))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

        lifecycleScope.launch {
            //Previne bugs
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                vm.authUiState.collect { state ->
                    when(state){
                        LoginUiState.LOADING -> {
                            //binding?.progressBar?.visibility = View.VISIBLE
                            //Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
                        }
                        LoginUiState.SUCCESS -> {
                            //binding?.progressBar?.visibility = View.GONE
                            //Toast.makeText(requireContext(), "Login efetuado com sucesso", Toast.LENGTH_SHORT).show()
                            loginInfoText.text = "Login efetuado com sucesso"
                            loginInfoText.setTextColor(resources.getColor(R.color.verde))
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        LoginUiState.ERROR -> {
                            //binding?.progressBar?.visibility = View.GONE
                            //Toast.makeText(requireContext(), "Erro ao efetuar login", Toast.LENGTH_SHORT).show()
                            loginInfoText.visibility = View.VISIBLE
                            loginInfoText.text = "Senha e/ou email incorretos"
                            loginInfoText.setTextColor(resources.getColor(R.color.red))
                        }
                    }
                }
            }
        }

        binding?.btnLogin?.setOnClickListener {
            // Use as referências obtidas via findViewById (etEmail / etPassword) para evitar mistura com binding?
            val email: String = etEmail.text?.toString()?.trim() ?: ""
            val password: String = etPassword.text?.toString() ?: ""

            val editor = prefs.edit()
            if (cbRememberMe.isChecked) {
                editor.putBoolean("remember_me", true)
                editor.putString("saved_email", email)
                editor.putString("saved_password", password)
            } else {
                editor.clear()
            }
            editor.apply()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Recupera o usuário do banco pelo e-mail
                usuarioRepository.buscarPorEmail(email).observe(viewLifecycleOwner) { usuario ->
                    if (usuario != null) {
                        val storedHash = usuario.senhaUsuario ?: ""
                        if (usuarioRepository.verificarSenha(password, storedHash)) {
                            lifecycleScope.launch {
                                vm.login(usuario)
                            }
                        } else {
                            loginInfoText.visibility = View.VISIBLE
                            loginInfoText.text = "Senha incorreta"
                            loginInfoText.setTextColor(resources.getColor(R.color.red))
                        }
                    } else {
                        loginInfoText.visibility = View.VISIBLE
                        loginInfoText.text = "Usuário não encontrado"
                        loginInfoText.setTextColor(resources.getColor(R.color.red))
                    }
                }
            } else {
                loginInfoText.visibility = View.VISIBLE
                loginInfoText.text = "Por favor preencha todos os campos"
                loginInfoText.setTextColor(resources.getColor(R.color.black))
            }

        }

        binding?.tvCreateAccount?.setOnClickListener{
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}
