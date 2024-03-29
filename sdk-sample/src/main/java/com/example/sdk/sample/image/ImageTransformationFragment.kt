package com.example.sdk.sample.image

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import com.example.sdk.internal.image.ImageCallback
import com.example.sdk.internal.image.ImageLoader.enqueue
import com.example.sdk.internal.image.ImageRequest
import com.example.sdk.internal.image.transform.BlendTransformation
import com.example.sdk.internal.image.transform.BlurTransformation
import com.example.sdk.sample.SampleContent
import com.example.sdk.sample.databinding.FragmentImageTransformationBinding
import kotlin.random.Random

const val ARG_IMAGE_TRANSFORMATION_TYPE = "image_transformation_type"

class ImageTransformationFragment : Fragment() {
    private var _binding: FragmentImageTransformationBinding? = null
    private val binding get() = _binding!!

    private var imageTransformationType: SampleContent.ImageTransformationType? = null
    private var originalBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageTransformationType = SampleContent.ImageTransformationType.parse(
                it.getInt(ARG_IMAGE_TRANSFORMATION_TYPE),
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentImageTransformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val (uri, transformation) = when (imageTransformationType) {
            SampleContent.ImageTransformationType.BLUR -> {
                Uri.parse(
                    "https://picsum.photos/828/464?dummy=${Random.nextDouble(1.0)}",
                ) to BlurTransformation(15, 10)
            }
            SampleContent.ImageTransformationType.BLEND -> {
                Uri.parse(
                    "https://picsum.photos/828/464?dummy=${Random.nextDouble(1.0)}",
                ) to BlendTransformation()
            }
            else -> {
                throw IllegalStateException("Not supported Image transformation type.")
            }
        }
        val originalImageRequest = ImageRequest.Builder(uri).densityFactory(2.0).build()
        val transformedImageRequest =
            ImageRequest.Builder(uri).densityFactory(2.0).transformation(transformation).build()

        originalImageRequest.enqueue(object : ImageCallback {
            override fun onResponse(request: ImageRequest, response: Bitmap) {
                originalBitmap = response
                BitmapDrawable(Resources.getSystem(), response).also { drawable ->
                    _binding?.originalImage?.setImageDrawable(drawable)
                }

                transformedImageRequest.enqueue(object : ImageCallback {
                    override fun onResponse(request: ImageRequest, response: Bitmap) {
                        _binding?.transformedImage?.let { transformedImageView ->
                            val drawable = run {
                                val tOriginalBitmap = originalBitmap
                                if (tOriginalBitmap != null) {
                                    val scaledBitmap = response.scale(tOriginalBitmap.width, tOriginalBitmap.height)
                                    BitmapDrawable(Resources.getSystem(), scaledBitmap)
                                } else {
                                    null
                                }
                            }
                            transformedImageView.setImageDrawable(drawable)
                        }
                    }

                    override fun onFailure(request: ImageRequest, e: Exception) {
                        // do nothing
                    }
                })
            }

            override fun onFailure(request: ImageRequest, e: Exception) {
                // do nothing
            }
        })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
