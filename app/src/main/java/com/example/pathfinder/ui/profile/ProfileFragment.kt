package com.example.pathfinder.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.example.pathfinder.util.NavigationViewUtils
import com.example.pathfinder.util.FuncoesUteis
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import android.view.inputmethod.InputMethodManager
import com.example.pathfinder.data.repositories.UsuarioRepository


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
    var editarUsuarioBtn: Button? = null
    var progressBar: ProgressBar? = null
    var imageBase64: String? = null
    var usuarioAtual: Usuario? = null
    var senhaUsuario: String? = null // Armazena a senha atual do usuário

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        setUserData(view)
        NavigationViewUtils.esconderBottomNavigationView(requireActivity())
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

        editarUsuarioBtn = view.findViewById(R.id.btn_editar_profile)
        progressBar = view.findViewById(R.id.profile_progress_bar)

        val nomeUsuarioTxt = view.findViewById<TextView>(R.id.nomeUsuario)
        val emailTxt = view.findViewById<TextView>(R.id.email)
        val senhaTxt = view.findViewById<TextView>(R.id.senha)
        val idadeTxt = view.findViewById<TextView>(R.id.idade)

        val editarSenhaTxt = view.findViewById<TextView>(R.id.editarSenha)
        val editarEmailTxt = view.findViewById<TextView>(R.id.editarEmail)
        val editarIdadeTxt = view.findViewById<TextView>(R.id.editarIdade)
        val editarNomeUsuarioTxt = view.findViewById<TextView>(R.id.editarNomeUsuario)

        editarSenhaTxt.setOnClickListener {
            editarSenhaTxt.visibility = View.INVISIBLE
            val parent = senhaTxt.parent as ViewGroup
            val editText = FuncoesUteis.trocarTextViewPorEditText(parent, senhaTxt, "")
            //editarCampoTexto(editText, parent, senhaTxt, editarSenhaTxt)
            // Alterar para ser editada individualmente.
            AlterarSenhaUsuario(editText, parent, senhaTxt, editarSenhaTxt)
        }
        editarEmailTxt.setOnClickListener {
            editarEmailTxt.visibility = View.INVISIBLE
            val parent = emailTxt.parent as ViewGroup
            val editText = FuncoesUteis.trocarTextViewPorEditText(parent, emailTxt, emailTxt.text.toString())
            editarCampoTexto(editText, parent, emailTxt, editarEmailTxt)
        }
        editarIdadeTxt.setOnClickListener {
            editarIdadeTxt.visibility = View.INVISIBLE
            val parent = idadeTxt.parent as ViewGroup
            val editText = FuncoesUteis.trocarTextViewPorEditText(parent, idadeTxt, idadeTxt.text.toString())
            editarCampoTexto(editText, parent, idadeTxt, editarIdadeTxt)
        }
        editarNomeUsuarioTxt.setOnClickListener {
            editarNomeUsuarioTxt.visibility = View.INVISIBLE
            val parent = nomeUsuarioTxt.parent as ViewGroup
            val editText = FuncoesUteis.trocarTextViewPorEditText(parent, nomeUsuarioTxt, nomeUsuarioTxt.text.toString())
            editarCampoTexto(editText, parent, nomeUsuarioTxt, editarNomeUsuarioTxt)
        }

        // Botão de voltar com animação
        view.findViewById<ImageButton>(R.id.btn_voltar).setOnClickListener { btn ->
            btn.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .withEndAction {
                    btn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        .start()
                }
                .start()
        }

        view.findViewById<ImageView>(R.id.imageView).setOnClickListener {
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                .createIntent { intent ->
                    imagePickLauncher.launch(intent)
                }
        }

        view.findViewById<TextView>(R.id.btn_editar_profile).setOnClickListener {
            setInProgress(true)
            if(selectedImageUri != null) {
                imageBase64 = selectedImageUri?.let {
                    FirebaseUtil.uriToBase64(requireContext(), it)
                }
            }

            // Pega os dados atuais dos campos
            val nome = view.findViewById<TextView>(R.id.nomeUsuario).text.toString()
            val email = view.findViewById<TextView>(R.id.email).text.toString()
            //val senha = view.findViewById<TextView>(R.id.senha).text.toString()
            val idadeStr = view.findViewById<TextView>(R.id.idade).text.toString()

            // Converta idade para Timestamp se necessário (aqui mantido como String)
            val usuario = Usuario(
                idUsuario = auth.currentUser?.uid,
                nomeUsuario = nome,
                emailUsuario = email,
                senhaUsuario = senhaUsuario ?: usuarioAtual?.senhaUsuario ?: "",
                idadeUsuario = FuncoesUteis.parseDate(idadeStr), // ajuste se necessário
                fotoUsuario = imageBase64 ?: usuarioAtual?.fotoUsuario
            )
            authViewModel.alterar(usuario)

            val user = FirebaseAuth.getInstance().currentUser
            user?.updatePassword(senhaUsuario ?: usuarioAtual?.senhaUsuario ?: "")
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Erro ao alterar senha", Toast.LENGTH_SHORT).show()
                    }
                }
            /*user?.verifyBeforeUpdateEmail(email)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Email alterado com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Erro ao alterar email", Toast.LENGTH_SHORT).show()
                    }
                }*/



            // Observa o resultado e mostra Toast
            lifecycleScope.launch {
                authViewModel.authUiState.collectLatest { state ->
                    when (state) {
                        com.example.pathfinder.LoginUiState.SUCCESS -> {
                            Toast.makeText(requireContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            setInProgress(false)
                            setUserData(requireView())
                        }
                        com.example.pathfinder.LoginUiState.ERROR -> {
                            Toast.makeText(requireContext(), "Erro ao atualizar perfil.", Toast.LENGTH_SHORT).show()
                            setInProgress(false)
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

    private fun editarCampoTexto(editText: EditText, parent: ViewGroup, textView: TextView, clickedText: TextView) {
        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

        // Quando o usuário pressionar Enter ou sair do campo, transforma de volta em TextView
        editText.setOnEditorActionListener { v, actionId, event ->
            // Funciona corretamente
            FuncoesUteis.trocarEditTextPorTextView(parent, editText, textView)
            true
        }
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.postDelayed({
                    // Verifica se o editText ainda está no parent antes de tentar trocar
                    if (parent.indexOfChild(editText) != -1) {
                        FuncoesUteis.trocarEditTextPorTextView(parent, editText, textView)
                    }
                }, 100)
            }
            clickedText.visibility = View.VISIBLE
        }
        // Removido o uso de clearFocusOnKeyboardClose para evitar NPE
        editText.clearFocusOnKeyboardClose(requireActivity())
    }

    private fun setUserData(view: View) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        usuarioAtual = document.toObject(Usuario::class.java)
                        if (usuarioAtual != null) {
                            view.findViewById<TextView>(R.id.textView2).text = usuarioAtual?.nomeUsuario
                            view.findViewById<TextView>(R.id.textView3).text = usuarioAtual?.emailUsuario
                            //view.findViewById<TextView>(R.id.senha).text = usuarioAtual?.senhaUsuario // Ocultar senha
                            senhaUsuario = usuarioAtual?.senhaUsuario
                            view.findViewById<TextView>(R.id.email).text = usuarioAtual?.emailUsuario
                            view.findViewById<TextView>(R.id.idade).text = formatDate(usuarioAtual?.idadeUsuario)
                            view.findViewById<TextView>(R.id.nomeUsuario).text = usuarioAtual?.nomeUsuario
                            val imageView = view.findViewById<ImageView>(R.id.imageView)
                            if (!usuarioAtual?.fotoUsuario.isNullOrEmpty()) {
                                imageBase64 = usuarioAtual?.fotoUsuario
                                val bitmap = FirebaseUtil.base64ToBitmap(usuarioAtual?.fotoUsuario!!)
                                Glide.with(view.context)
                                    .load(bitmap)
                                    .circleCrop()
                                    .override(512, 512) // Ajuste o tamanho do círculo aqui (exemplo: 128x128 px)
                                    .into(imageView)
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

    override fun onDestroyView() {
        super.onDestroyView()
        NavigationViewUtils.mostrarBottomNavigationView(requireActivity())
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar?.visibility = View.VISIBLE
            editarUsuarioBtn?.visibility = View.GONE
        } else {
            progressBar?.visibility = View.GONE
            editarUsuarioBtn?.visibility = View.VISIBLE
        }
    }

    fun EditText.clearFocusOnKeyboardClose(activity: Activity) {
        val rootView = activity.window.decorView

        rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            private var isKeyboardVisible = false

            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val keypadHeight = screenHeight - rect.bottom

                val keyboardNowVisible = keypadHeight > screenHeight * 0.15

                if (isKeyboardVisible && !keyboardNowVisible) {
                    // Teclado foi fechado
                    this@clearFocusOnKeyboardClose.clearFocus()
                    hideKeyboard(activity, this@clearFocusOnKeyboardClose)
                }

                isKeyboardVisible = keyboardNowVisible
            }
        })
    }

    private fun hideKeyboard(activity: Activity, view: View) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // A função vai abrir um diálogo para o usuário inserir a nova senha e setar em senhaUsuario
    private fun AlterarSenhaUsuario(editText: EditText, parent: ViewGroup, textView: TextView, clickedText: TextView){
        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

        val usuarioRepository = UsuarioRepository()

        // Quando o usuário pressionar Enter ou sair do campo, transforma de volta em TextView
        editText.setOnEditorActionListener { v, actionId, event ->
            // Funciona corretamente
            FuncoesUteis.trocarEditTextPorTextView(parent, editText, textView)
            senhaUsuario = editText.text.toString()
            senhaUsuario = usuarioRepository.criptografarSenha(senhaUsuario?:"")
            true
        }
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.postDelayed({
                    // Verifica se o editText ainda está no parent antes de tentar trocar
                    if (parent.indexOfChild(editText) != -1) {
                        FuncoesUteis.trocarEditTextPorTextView(parent, editText, textView)
                        senhaUsuario = editText.text.toString()
                        senhaUsuario = usuarioRepository.criptografarSenha(senhaUsuario?:"")
                    }
                }, 100)
            }
            clickedText.visibility = View.VISIBLE
        }
        // Removido o uso de clearFocusOnKeyboardClose para evitar NPE
        editText.clearFocusOnKeyboardClose(requireActivity())
    }
}
