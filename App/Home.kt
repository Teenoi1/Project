package com.example.app1952

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app1952.databinding.FragmentHomeBinding

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tempTextView.text = "--°C"
        binding.humidityTextView.text = "--%"
        binding.intensityTextView.text = "--"
    }

    fun updateData(humidity: Int, temperature: Double, intensity: Int) {
        _binding?.let {
            it.tempTextView.text = "$temperature°C"
            it.humidityTextView.text = "$humidity%"
            it.intensityTextView.text = "$intensity Lux"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
