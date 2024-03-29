package com.example.sdk.sample.backgrounddetector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sdk.internal.Validate
import com.example.sdk.internal.common.BackgroundDetector
import com.example.sdk.sample.SampleLogsView
import com.example.sdk.sample.databinding.FragmentBackgroundDetectorBinding

class BackgroundDetectorFragment : Fragment() {
    private var _binding: FragmentBackgroundDetectorBinding? = null
    private val binding get() = _binding!!

    lateinit var sampleLogsView: SampleLogsView
        private set

    private val backgroundStateChangeCallback =
        BackgroundDetector.BackgroundStateChangeCallback { isBackground ->
            val text = if (isBackground) {
                Validate.checkState(BackgroundDetector.isInBackground())
                "Background"
            } else {
                Validate.checkState(!BackgroundDetector.isInBackground())
                "Foreground"
            }
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
            sampleLogsView.addLog(text)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBackgroundDetectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sampleLogsView = binding.sampleLogsView
        BackgroundDetector.addCallback(backgroundStateChangeCallback)
    }

    override fun onDestroyView() {
        _binding = null
        BackgroundDetector.removeCallback(backgroundStateChangeCallback)
        super.onDestroyView()
    }
}
