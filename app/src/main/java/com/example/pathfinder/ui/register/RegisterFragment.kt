package com.example.pathfinder.ui.register

import androidx.fragment.app.viewModels
import android.app.Activity
import android.content.Intent
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
import androidx.navigation.fragment.findNavController
import com.example.pathfinder.LoginUiState
import com.example.pathfinder.R
import com.example.pathfinder.data.AuthViewModel
import com.example.pathfinder.data.models.Usuario
import com.example.pathfinder.databinding.FragmentRegisterBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

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

        binding?.btnSelecionarFoto?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        binding?.btnLogin?.setOnClickListener {
            val name = binding?.etName?.text.toString()
            val email = binding?.etEmail?.text.toString()
            val password = binding?.etPassword?.text.toString()
            val ageInput = binding?.etAge?.text.toString()
            val localFotoUri = fotoUri // Uri da imagem selecionada do dispositivo

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && ageInput.isNotEmpty() && localFotoUri != null) {
                try {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val parsedDate = dateFormat.parse(ageInput) ?: throw IllegalArgumentException("Data inválida")
                    val age = Timestamp(parsedDate)

                    // Gere um nome único para a imagem
                    val fotoNome = UUID.randomUUID().toString() + ".jpg"
                    val storageRef = FirebaseStorage.getInstance().reference.child("usuarios/$fotoNome")

                    // 1. Upload da imagem para o Firebase Storage
                    storageRef.putFile(localFotoUri)
                        .addOnSuccessListener {
                            // 2. Obtem a URL pública da imagem
                            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                val usuario = Usuario(
                                    nomeUsuario = name,
                                    emailUsuario = email,
                                    senhaUsuario = password,
                                    idadeUsuario = age,
                                    fotoUsuario = downloadUrl.toString() // Salva a URL da imagem
                                )
                                // 3. Chama a função de registro com a imagem correta
                                lifecycleScope.launch {
                                    vm.register(usuario)
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Erro ao fazer upload da foto", Toast.LENGTH_SHORT).show()
                            Log.e("RegisterFragment", "Erro ao fazer upload da foto: ${it.message}")
                        }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Formato de data inválido. Use dd/MM/yyyy.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Preencha todos os campos e selecione uma foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            fotoUri = data?.data
            binding?.ivFotoUsuario?.setImageURI(fotoUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
