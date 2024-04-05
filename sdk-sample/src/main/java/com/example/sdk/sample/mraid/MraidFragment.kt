package com.example.sdk.sample.mraid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sdk.internal.webview.AdWebViewController
import com.example.sdk.internal.webview.AdWebViewControllerListener
import com.example.sdk.internal.webview.AdWebViewErrorCode
import com.example.sdk.internal.webview.AdWebViewSize
import com.example.sdk.internal.webview.DefaultAdWebViewController
import com.example.sdk.internal.webview.mraid.MraidPlacementType
import com.example.sdk.sample.SampleLogsView
import com.example.sdk.sample.databinding.FragmentMraidBinding
import com.example.sdk.testutils.TestUtils

const val ARG_IS_INLINE_PLACEMENT_TYPE = "is_inline_placement_type"
const val ARG_AD_WEB_VIEW_SIZE_WIDTH = "ad_web_view_width"
const val ARG_AD_WEB_VIEW_SIZE_HEIGHT = "ad_web_view_height"
const val ARG_HTML_FILE_NAME = "html_file_name"

class MraidFragment : Fragment() {
    private var _binding: FragmentMraidBinding? = null
    private val binding get() = _binding!!

    private var isInlinePlacementType: Boolean = true
    private var adWebViewWidth: Int? = null
    private var adWebViewHeight: Int? = null
    private var onePartHtmlFileName: String? = null

    private var adWebViewController: AdWebViewController? = null

    lateinit var sampleLogsView: SampleLogsView
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isInlinePlacementType = it.getBoolean(ARG_IS_INLINE_PLACEMENT_TYPE, true)
            adWebViewWidth = it.getInt(ARG_AD_WEB_VIEW_SIZE_WIDTH)
            adWebViewHeight = it.getInt(ARG_AD_WEB_VIEW_SIZE_HEIGHT)
            onePartHtmlFileName = it.getString(ARG_HTML_FILE_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMraidBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sampleLogsView = binding.sampleLogsView

        adWebViewController = DefaultAdWebViewController.Factory(
            baseUrl = BASE_URL,
            mraidPlacementType = if (isInlinePlacementType) {
                MraidPlacementType.INLINE
            } else {
                MraidPlacementType.INTERSTITIAL
            },
        ).create(requireContext(), AdWebViewSize(adWebViewWidth!!, adWebViewHeight!!)).apply {
            setControllerListener(object : AdWebViewControllerListener {
                override fun onAdLoaded() {
                    sampleLogsView.addLog("onAdLoaded")
                }

                override fun onAdClicked() {
                    sampleLogsView.addLog("onAdClicked")
                }

                override fun onAdResize() {
                    sampleLogsView.addLog("onAdResize")
                }

                override fun onAdUnloaded() {
                    sampleLogsView.addLog("onAdUnloaded")
                    adWebViewController?.destroy()
                }

                override fun onAdError(errorCode: AdWebViewErrorCode) {
                    sampleLogsView.addLog("onAdError errorCode: ${errorCode.message}")
                }
            })
            fillContent(TestUtils.getString(requireActivity(), "$onePartHtmlFileName"))
            binding.adContainer.addView(adWebViewContainer)
        }
    }

    override fun onDestroyView() {
        _binding = null
        adWebViewController?.destroy()
        super.onDestroyView()
    }

    companion object {
        private const val BASE_URL = "file:///android_asset/mraid/"
    }
}
