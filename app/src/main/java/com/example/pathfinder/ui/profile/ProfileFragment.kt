package com.example.pathfinder.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pathfinder.R
import com.example.pathfinder.data.AuthViewModel
import com.example.pathfinder.data.models.Usuario
import com.example.pathfinder.util.AndroidUtil
import com.example.pathfinder.util.FirebaseUtil
import com.example.pathfinder.util.funcoesUteis
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private val viewModel: ProfileViewModel by viewModels()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    private val authViewModel: AuthViewModel by activityViewModels { AuthViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        setUserData(view)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Utilizo a library de image picker para selecionar a foto do usuário
        imagePickLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if(data!= null && data.data != null) {
                    selectedImageUri = data.data
                    AndroidUtil.setProfilePic(
                        requireContext(),
                        selectedImageUri,
                        requireView().findViewById<ImageView>(R.id.imageView)
                    )
                }

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.imageView).setOnClickListener {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                .createIntent { intent ->
                    imagePickLauncher.launch(intent)
                }
        }

        view.findViewById<TextView>(R.id.btn_editar_profile).setOnClickListener {
            val imageBase64 = selectedImageUri?.let {
                FirebaseUtil.uriToBase64(requireContext(), it)
            }

            // Pega os dados atuais dos campos
            val nome = view.findViewById<TextView>(R.id.nomeUsuario).text.toString()
            val email = view.findViewById<TextView>(R.id.email).text.toString()
            val senha = view.findViewById<TextView>(R.id.senha).text.toString()
            val idadeStr = view.findViewById<TextView>(R.id.idade).text.toString()

            // Converta idade para Timestamp se necessário (aqui mantido como String)
            val usuario = Usuario(
                idUsuario = auth.currentUser?.uid,
                nomeUsuario = nome,
                emailUsuario = email,
                senhaUsuario = senha,
                idadeUsuario = funcoesUteis.parseDate(idadeStr), // ajuste se necessário
                fotoUsuario = imageBase64
            )

            authViewModel.alterar(usuario)

            // Observa o resultado e mostra Toast
            lifecycleScope.launch {
                authViewModel.authUiState.collectLatest { state ->
                    when (state) {
                        com.example.pathfinder.LoginUiState.SUCCESS -> {
                            Toast.makeText(requireContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                        com.example.pathfinder.LoginUiState.ERROR -> {
                            Toast.makeText(requireContext(), "Erro ao atualizar perfil.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }

        view.findViewById<TextView>(R.id.textView2).setOnClickListener {
            // Implementar ação para editar nome de usuário
        }
    }

    private fun setUserData(view: View) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val usuario = document.toObject(Usuario::class.java)
                        if (usuario != null) {
                            view.findViewById<TextView>(R.id.textView2).text = usuario.nomeUsuario
                            view.findViewById<TextView>(R.id.textView3).text = usuario.emailUsuario
                            view.findViewById<TextView>(R.id.senha).text = usuario.senhaUsuario // Ocultar senha
                            view.findViewById<TextView>(R.id.email).text = usuario.emailUsuario
                            view.findViewById<TextView>(R.id.idade).text = formatDate(usuario.idadeUsuario)
                            view.findViewById<TextView>(R.id.nomeUsuario).text = usuario.nomeUsuario
                            view.findViewById<TextView>(R.id.endereco).text = usuario.enderecoUsuario?.toString() ?: "Não informado"
                            val imageView = view.findViewById<ImageView>(R.id.imageView)
                            if (!usuario.fotoUsuario.isNullOrEmpty()) {
                                val bitmap = FirebaseUtil.base64ToBitmap(usuario.fotoUsuario!!)
                                imageView.setImageBitmap(bitmap)
                            } else {
                                imageView.setImageResource(R.drawable.ic_profile)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Erro ao carregar dados do usuário ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun formatDate(date: Timestamp?): String {
        return if (date != null) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateObj = date.toDate() // Corrigido: converte Timestamp para Date
            formatter.format(dateObj)
        } else {
            "Não informado"
        }
    }
}
