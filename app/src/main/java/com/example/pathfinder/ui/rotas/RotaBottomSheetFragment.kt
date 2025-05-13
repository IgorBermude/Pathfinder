package com.example.pathfinder.ui.rotas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Rota
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RotaBottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RotaBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var rotaAdapter: RotaAdapter
    private val rotas = mutableListOf<Rota>()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rota_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAdd = view.findViewById<Button>(R.id.btn_add_rota)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_rotas)

        rotaAdapter = RotaAdapter(rotas) { rota ->
            rotas.remove(rota)
            rotaAdapter.notifyItemRemoved(rotas.indexOf(rota))
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = rotaAdapter

        btnAdd.setOnClickListener {
            // Lógica para cadastrar rota
        }

        loadUserRoutes()
    }

    private fun loadUserRoutes() {
        currentUser?.let { user ->
            db.collection("rotas")
                .whereEqualTo("criadorRota.id", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    rotas.clear()
                    for (document in documents) {
                        val rota = document.toObject(Rota::class.java)
                        rotas.add(rota)
                    }
                    rotaAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Erro ao carregar rotas: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
