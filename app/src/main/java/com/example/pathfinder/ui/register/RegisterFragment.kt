package com.example.pathfinder.ui.register

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
import androidx.navigation.fragment.findNavController
import com.example.pathfinder.LoginUiState
import com.example.pathfinder.R
import com.example.pathfinder.data.AuthViewModel
import com.example.pathfinder.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var binding: FragmentRegisterBinding? = null
    private val vm: AuthViewModel by viewModels{ AuthViewModel.Factory }

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
                            Toast.makeText(requireContext(), "Login efetuado com sucesso", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                        LoginUiState.ERROR -> {
                            //binding?.progressBar?.visibility = View.GONE
                            Toast.makeText(requireContext(), "Erro ao efetuar login", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding?.btnLogin?.setOnClickListener{
            val name: String = binding?.etName?.text.toString()
            val email: String = binding?.etEmail?.text.toString()
            val password: String = binding?.etPassword?.text.toString()
            val phoneNumber: String = binding?.etPhoneNumber?.text.toString()

            if(name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && phoneNumber.isNotEmpty()){
                lifecycleScope.launch {
                    vm.register(email, password)
                }
            } else{
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
