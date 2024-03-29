package com.example.sdk.sample.visibilityobserver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sdk.internal.visibilityobserver.VisibilityObserver
import com.example.sdk.internal.visibilityobserver.VisibilityObserver.Companion.addExposureChangeObserver
import com.example.sdk.sample.databinding.FragmentVisibilityObserverWithScrollViewBothDirectionBinding

class VisibilityObserverWithScrollViewBothDirection : Fragment() {
    private var _binding: FragmentVisibilityObserverWithScrollViewBothDirectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var visibilityObserver: VisibilityObserver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentVisibilityObserverWithScrollViewBothDirectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visibilityObserver = binding.targetView.addExposureChangeObserver { _, newEntry ->
            val intersectingPercent = (newEntry.intersectingRatio * 100).toFloat()
            val intersectingWidth = newEntry.intersectingRect?.width() ?: 0
            val intersectingHeight = newEntry.intersectingRect?.height() ?: 0
            val intersectingPx = newEntry.intersectingPx

            binding.sampleLogsView.addLog(
                "intersectingPercent: $intersectingPercent, " +
                    "intersectingWidth: $intersectingWidth, " +
                    "intersectingHeight: $intersectingHeight, " +
                    "intersectingPx: $intersectingPx",
            )
        }.apply {
            observe()
        }
    }

    override fun onDestroyView() {
        visibilityObserver.disconnect()
        _binding = null
        super.onDestroyView()
    }
}
