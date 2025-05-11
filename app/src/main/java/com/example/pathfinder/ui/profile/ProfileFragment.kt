package com.example.pathfinder.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private val viewModel: ProfileViewModel by viewModels()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        setUserData(view)
        return view
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
                            usuario.fotoUsuario?.let {
                                Glide.with(view.context)
                                    .load(it)
                                    .into(imageView)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // Tratar erro ao buscar dados do Firestore
                }
        }
    }

    private fun formatDate(date: Date?): String {
        return if (date != null) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(date)
        } else {
            "Não informado"
        }
    }
}
