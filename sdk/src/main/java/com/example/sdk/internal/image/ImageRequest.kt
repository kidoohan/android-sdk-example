package com.example.sdk.internal.image

import android.net.Uri
import android.os.Bundle
import com.example.sdk.internal.concurrent.ExecutorNodeItem
import com.example.sdk.internal.image.transform.Transformation
import com.example.sdk.internal.inspector.MapSerializable
import com.google.android.gms.tasks.CancellationToken

/**
 * An value that represents a request for an image.
 *
 * @property uri the uri of the image.
 * @property densityFactor the density factor of the image.
 * @property transformation the instance for making transformations to an image's pixel data.
 * @property extra used to set extra parameters. for example, set tag to identify the image.
 * @property cancellationToken the token that propagates notification that operations should be canceled.
 */
data class ImageRequest
@JvmOverloads
constructor(
    val uri: Uri,
    val densityFactor: Double = DEFAULT_DENSITY_FACTOR,
    val transformation: Transformation? = null,
    val extra: Bundle? = null,
    override val cancellationToken: CancellationToken? = null,
) : ExecutorNodeItem(cancellationToken), MapSerializable<Any> {
    /** the key without transformation of this request */
    val keyWithoutTransformation: String = uri.toString() + densityFactor

    /** the key of this request */
    val key: String = keyWithoutTransformation + (transformation?.key ?: "")

    /** Converts this to a Map<String, Any> */
    override fun toMap(): Map<String, Any> {
        return mapOf(
            "request" to mapOf(
                "uri" to uri,
                "densityFactor" to densityFactor,
                "extra" to extra,
            ),
        )
    }

    /** Builder for [ImageRequest] instances. */
    class Builder(private val uri: Uri) {
        private var densityFactor: Double? = null
        private var transformation: Transformation? = null
        private var extra: Bundle? = null
        private var cancellationToken: CancellationToken? = null

        /** Sets the density factory. */
        fun densityFactory(densityFactor: Double) = apply {
            this.densityFactor = densityFactor
        }

        /** Sets the transformation. */
        fun transformation(transformation: Transformation) = apply {
            this.transformation = transformation
        }

        /** Sets the extra. */
        fun extra(extra: Bundle) = apply {
            this.extra = extra
        }

        /** Sets the cancellation token. */
        fun cancellationToken(cancellationToken: CancellationToken) = apply {
            this.cancellationToken = cancellationToken
        }

        /** Returns an instance of [ImageRequest] created from the fields set on this builder. */
        fun build(): ImageRequest {
            return ImageRequest(
                uri = uri,
                densityFactor = densityFactor ?: DEFAULT_DENSITY_FACTOR,
                transformation = transformation,
                extra = extra,
                cancellationToken = cancellationToken,
            )
        }
    }

    companion object {
        /** Default density factor. */
        const val DEFAULT_DENSITY_FACTOR: Double = 1.0
    }
}
