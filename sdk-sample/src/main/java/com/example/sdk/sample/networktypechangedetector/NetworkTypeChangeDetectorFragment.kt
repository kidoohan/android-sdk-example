package com.example.sdk.sample.networktypechangedetector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import com.example.sdk.internal.NetworkType
import com.example.sdk.internal.common.NetworkTypeChangeDetector
import com.example.sdk.sample.databinding.FragmentNetworkTypeChangeDetectorBinding
import java.util.concurrent.atomic.AtomicReference

class NetworkTypeChangeDetectorFragment : Fragment() {
    private var _binding: FragmentNetworkTypeChangeDetectorBinding? = null
    private val binding get() = _binding!!

    @VisibleForTesting
    val currentNetworkType = AtomicReference<NetworkType>()
    private val callback = NetworkTypeChangeDetector.NetworkTypeChangeCallback { networkType ->
        currentNetworkType.set(networkType)
        _binding?.currentNetworkType?.text = networkType.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNetworkTypeChangeDetectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NetworkTypeChangeDetector.addCallback(callback)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
