// Caminho: com/example/pathfinder/ui/rotas/RotaBottomSheetFragment.kt
package com.example.pathfinder.ui.rotas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Rota
import com.example.pathfinder.data.repositories.RotaRepository
import com.example.pathfinder.data.repositories.UsuarioRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RotaBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var rotaAdapter: RotaAdapter
    private val rotas = mutableListOf<Rota>()
    private val usuarioRepository = UsuarioRepository()
    private val rotaRepository = RotaRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rota_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        observarUsuarioLogado()
    }

    private fun setupRecyclerView(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_rotas)
        rotaAdapter = RotaAdapter(rotas) { rota ->
            val index = rotas.indexOf(rota)
            if (index != -1) {
                rotas.removeAt(index)
                rotaAdapter.notifyItemRemoved(index)
            }
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = rotaAdapter
    }

    private fun observarUsuarioLogado() {
        usuarioRepository.carregarUsuarioLogado()
        usuarioRepository.usuarioLogado.observe(viewLifecycleOwner) { usuario ->
            if (usuario?.idUsuario != null) {
                carregarRotasDoUsuario(usuario.idUsuario!!)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Usuário não encontrado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun carregarRotasDoUsuario(idUsuario: String) {
        rotaRepository.buscarRotasPorUsuario(
            idUsuario,
            onSuccess = { rotasCarregadas ->
                Log.d("RotaBottomSheetFragment", "Rotas carregadas: ${rotasCarregadas.size}")
                atualizarListaRotas(rotasCarregadas)
            },
            onFailure = { exception ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar rotas: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("RotaBottomSheetFragment", "Erro ao carregar rotas: ${exception.message}", exception)
            }
        )
    }

    private fun atualizarListaRotas(rotasCarregadas: List<Rota>) {
        rotas.clear()
        rotas.addAll(rotasCarregadas)
        rotaAdapter.notifyDataSetChanged()
    }
}
