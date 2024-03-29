package com.example.sdk.sample.executornodequeue

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import com.example.sdk.internal.Validate
import com.example.sdk.internal.common.TaskUtils
import com.example.sdk.sample.databinding.FragmentIoQueueBinding
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

class IoQueueFragment : Fragment() {
    private var _binding: FragmentIoQueueBinding? = null
    private val binding get() = _binding!!

    @VisibleForTesting
    val runningTask = AtomicInteger()

    @VisibleForTesting
    val successTask = AtomicInteger()

    @VisibleForTesting
    val failureTask = AtomicInteger()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentIoQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repeat(REPEAT_COUNT) {
            TaskUtils.callInBackgroundThread {
                requireActivity().runOnUiThread {
                    _binding?.runningTask?.text =
                        "Running deferred count ${runningTask.incrementAndGet()}"
                    Validate.checkLessThanOrEqualTo(runningTask.get(), 64)
                }

                val random = Random().nextInt(500).toLong() + 500
                Thread.sleep(random)
                if (random < 750) {
                    throw IllegalStateException("foo")
                }
            }.addOnCompleteListener {
                _binding?.runningTask?.text =
                    "Running task count ${runningTask.decrementAndGet()}"
                Validate.checkLessThanOrEqualTo(runningTask.get(), 64)
                if (it.isSuccessful) {
                    _binding?.successTask?.text =
                        "Success task count ${successTask.incrementAndGet()}"
                } else {
                    _binding?.failureTask?.text =
                        "Failure task count ${failureTask.incrementAndGet()}"
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val REPEAT_COUNT = 1000
    }
}
