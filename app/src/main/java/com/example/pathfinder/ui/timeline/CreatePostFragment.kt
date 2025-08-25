package com.example.pathfinder.ui.timeline

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Postagem
import com.example.pathfinder.data.models.Usuario
import com.example.pathfinder.util.FirebaseUtil
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CreatePostFragment : Fragment() {

    private lateinit var etContent: EditText
    private lateinit var ivImage: ImageView
    private lateinit var btnSelectImage: ImageButton
    private lateinit var btnPost: Button
    private var selectedImageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var imagePickLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_post, container, false)

        etContent = view.findViewById(R.id.et_post_content)
        ivImage = view.findViewById(R.id.iv_post_image)
        btnSelectImage = view.findViewById(R.id.btn_select_image)
        btnPost = view.findViewById(R.id.btn_post)

        setupImagePicker()
        setupListeners()

        return view
    }

    private fun setupImagePicker() {
        imagePickLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                ivImage.setImageURI(selectedImageUri)
                ivImage.visibility = View.VISIBLE
            }
        }

        btnSelectImage.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(512)
                .maxResultSize(512, 512)
                .createIntent { intent ->
                    imagePickLauncher.launch(intent)
                }
        }
    }

    private fun setupListeners() {
        btnPost.setOnClickListener {
            val content = etContent.text.toString()
            if (content.isBlank()) {
                Toast.makeText(requireContext(), "Digite algo para postar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createPost(content, selectedImageUri)
        }
    }

    private fun createPost(content: String, imageUri: Uri?) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)

                val post = Postagem(
                    usuarioPostagemId = currentUser.uid,
                    descricaoPostagem = content,
                    fotoPostagem = usuario?.fotoUsuario,
                    horaPostagem = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                )

                if (imageUri != null) {
                    post.fotoPostagem = FirebaseUtil.uriToBase64(requireContext(), imageUri)
                }

                firestore.collection("postagens")
                    .add(post)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Postagem criada!", Toast.LENGTH_SHORT).show()
                        etContent.text.clear()
                        ivImage.setImageURI(null)
                        ivImage.visibility = View.GONE
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao buscar usuário: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
