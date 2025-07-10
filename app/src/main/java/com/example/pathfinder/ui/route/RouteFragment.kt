package com.example.pathfinder.ui.route

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pathfinder.databinding.FragmentRouteBinding


class RouteFragment : Fragment() {

    private companion object {
        fun newInstance() = RouteFragment()
    }
    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

}