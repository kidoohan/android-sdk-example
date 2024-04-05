package com.example.sdk.sample

import android.os.Bundle
import com.example.sdk.internal.webview.mraid.MraidPlacementType
import com.example.sdk.sample.image.ARG_IMAGE_TRANSFORMATION_TYPE
import com.example.sdk.sample.mraid.ARG_AD_WEB_VIEW_SIZE_HEIGHT
import com.example.sdk.sample.mraid.ARG_AD_WEB_VIEW_SIZE_WIDTH
import com.example.sdk.sample.mraid.ARG_HTML_FILE_NAME
import com.example.sdk.sample.mraid.ARG_IS_INLINE_PLACEMENT_TYPE

object SampleContent {
    val SAMPLES = listOf(
        // region Image Samples
        SectionItem("Image Samples"),
        ContentItem("Blur", SampleType.ImageTransformation(ImageTransformationType.BLUR)),
        ContentItem("Blend", SampleType.ImageTransformation(ImageTransformationType.BLEND)),
        // endregion

        // region Background Detector Samples
        SectionItem("Background Detector Samples"),
        ContentItem(
            "Background Detector",
            SampleType.BackgroundDetector,
        ),
        // endregion

        // region Network Type Change Detector Samples
        SectionItem("Network Type Change Detector Samples"),
        ContentItem(
            "Network Type Change Detector",
            SampleType.NetworkTypeChangeDetector,
        ),
        // endregion

        // region Visibility Observer Samples
        SectionItem("Visibility Observer Samples"),
        ContentItem(
            "List",
            SampleType.VisibilityObserverWithList,
        ),
        ContentItem(
            "ScrollView",
            SampleType.VisibilityObserverWithScrollViewBothDirection,
        ),
        // endregion

        // region ExecutorNodeQueue Samples
        SectionItem("ExecutorNodeQueue Samples"),
        ContentItem("IO_QUEUE", SampleType.IoQueue),
        // endregion

        // region
        SectionItem("MRAID Samples"),
        ContentItem(
            "Default",
            SampleType.Mraid(MraidType.DEFAULT),
        ),
        ContentItem(
            "Resize",
            SampleType.Mraid(MraidType.RESIZE),
        ),
        ContentItem(
            "Resize Error",
            SampleType.Mraid(MraidType.RESIZE_ERROR),
        ),
        ContentItem(
            "Expand",
            SampleType.Mraid(MraidType.EXPAND),
        ),
        ContentItem(
            "Expand Two Part",
            SampleType.Mraid(MraidType.EXPAND_TWO_PART),
        ),
        // endregion
    )

    sealed interface Item

    data class SectionItem(
        val title: String,
    ) : Item

    data class ContentItem(
        val title: String,
        val type: SampleType,
    ) : Item

    sealed class SampleType(val resId: Int, open val extras: Bundle) {
        class ImageTransformation(
            imageTransformationType: ImageTransformationType,
        ) : SampleType(
            R.id.action_SampleListFragment_to_ImageTransformationFragment,
            imageTransformationType.toBundle(),
        )

        object BackgroundDetector :
            SampleType(R.id.action_SampleListFragment_to_BackgroundDetectorFragment, Bundle())

        object NetworkTypeChangeDetector : SampleType(
            R.id.action_SampleListFragment_to_NetworkTypeChangeDetectorFragment,
            Bundle(),
        )

        object VisibilityObserverWithList :
            SampleType(
                R.id.action_SampleListFragment_to_VisibilityObserverWithListFragment,
                Bundle(),
            )

        object VisibilityObserverWithScrollViewBothDirection :
            SampleType(
                R.id.action_SampleListFragment_to_VisibilityObserverWithScrollViewBothDirectionFragment,
                Bundle(),
            )

        object IoQueue : SampleType(
            R.id.action_SampleListFragment_to_IoQueueFragment,
            Bundle(),
        )

        data class Mraid(
            val mraidType: MraidType,
        ) : SampleType(R.id.action_SampleListFragment_to_MraidFragment, mraidType.toBundle())
    }

    enum class ImageTransformationType(val key: Int) {
        BLUR(0),
        BLEND(1),
        ;

        fun toBundle(): Bundle {
            return Bundle().apply {
                putInt(
                    ARG_IMAGE_TRANSFORMATION_TYPE,
                    key,
                )
            }
        }

        companion object {
            fun parse(key: Int): ImageTransformationType? {
                return values().find {
                    it.key == key
                }
            }
        }
    }

    enum class MraidType(
        private val placementType: MraidPlacementType,
        private val adWebViewWidth: Int,
        private val adWebViewHeight: Int,
        private val onePartHtmlFileName: String,
    ) {
        DEFAULT(MraidPlacementType.INLINE, 300, 150, "mraid/mraid_default.txt"),
        RESIZE(MraidPlacementType.INLINE, 320, 50, "mraid/mraid_resize.txt"),
        RESIZE_ERROR(MraidPlacementType.INLINE, -1, -1, "mraid/mraid_resize_err.txt"),
        EXPAND(MraidPlacementType.INLINE, 250, 60, "mraid/mraid_expand.txt"),
        EXPAND_TWO_PART(MraidPlacementType.INLINE, 320, 50, "mraid/mraid_twopart_expand_part1.txt"),
        ;

        fun toBundle(): Bundle {
            return Bundle().apply {
                putBoolean(
                    ARG_IS_INLINE_PLACEMENT_TYPE,
                    placementType == MraidPlacementType.INLINE,
                )
                putInt(
                    ARG_AD_WEB_VIEW_SIZE_WIDTH,
                    adWebViewWidth,
                )
                putInt(
                    ARG_AD_WEB_VIEW_SIZE_HEIGHT,
                    adWebViewHeight,
                )
                putString(
                    ARG_HTML_FILE_NAME,
                    onePartHtmlFileName,
                )
            }
        }
    }
}
