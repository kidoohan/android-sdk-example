package com.example.sdk.sample

import android.os.Bundle
import com.example.sdk.sample.image.ARG_IMAGE_TRANSFORMATION_TYPE

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
}
