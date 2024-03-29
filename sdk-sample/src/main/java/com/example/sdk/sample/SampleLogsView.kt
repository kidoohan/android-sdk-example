package com.example.sdk.sample

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.common.DimensionUtils
import com.example.sdk.sample.databinding.ViewSampleLogsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SampleLogsView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    enum class SampleLogLevel {
        INFO,
        WARN,
        ERROR,
    }

    data class SampleLog(
        val log: String,
        val logLevel: SampleLogLevel,
        val logDate: Date,
    ) {
        private val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

        fun toSpannableString(): SpannableString {
            val formattedLogDate = sdf.format(logDate)
            val formattedLog = "$formattedLogDate $log"

            val color = when (logLevel) {
                SampleLogLevel.INFO -> Color.parseColor("#FFFFFFFF")
                SampleLogLevel.WARN -> Color.parseColor("#FF9800")
                SampleLogLevel.ERROR -> Color.parseColor("#EF5350")
            }

            return SpannableString(formattedLog).apply {
                setSpan(
                    ForegroundColorSpan(color),
                    0,
                    formattedLog.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    formattedLogDate.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
    }

    private val binding = ViewSampleLogsBinding.inflate(LayoutInflater.from(context), this, true)

    private val logsContainer
        get() = binding.logsContainer
    private val logsTitleContainer
        get() = binding.logsTitleContainer
    private val logsScrollView
        get() = binding.logsScrollView
    private val logsTextView
        get() = binding.logsTextView
    private val logsExpandButton
        get() = binding.logsExpandButton

    private var expanded = false
        set(value) {
            if (field != value) {
                field = value

                logsTextView.text = ""

                val logsScrollLp = logsScrollView.layoutParams
                if (value) {
                    logsTitleContainer.visibility = View.VISIBLE
                    logsExpandButton.visibility = View.GONE

                    logsTextView.isSingleLine = false
                    logsTextView.ellipsize = null

                    logsScrollLp.height = DimensionUtils.dpToPixels(context, 160f)

                    sampleLogs.forEach { sampleLog ->
                        writeLog(sampleLog)
                    }
                    logsScrollView.post { logsScrollView.fullScroll(View.FOCUS_DOWN) }
                } else {
                    logsTitleContainer.visibility = View.GONE
                    logsExpandButton.visibility = View.VISIBLE

                    logsTextView.isSingleLine = true
                    logsTextView.ellipsize = TextUtils.TruncateAt.END

                    logsScrollLp.height = ViewGroup.LayoutParams.WRAP_CONTENT

                    sampleLogs.lastOrNull()?.let { firstSampleLog ->
                        writeLog(firstSampleLog)
                    }
                }
                logsScrollView.requestLayout()
            }
        }

    @VisibleForTesting
    val sampleLogs = mutableListOf<SampleLog>()

    init {
        logsContainer.setOnClickListener {
            expanded = !expanded
        }
        logsTextView.movementMethod = ScrollingMovementMethod()
    }

    fun addLog(log: String, logLevel: SampleLogLevel = SampleLogLevel.INFO) {
        val sampleLog = SampleLog(log, logLevel, Date())
        sampleLogs.add(sampleLog)

        writeLog(sampleLog)
        if (expanded) {
            logsScrollView.post { logsScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun writeLog(sampleLog: SampleLog) {
        val spannableLog = sampleLog.toSpannableString()
        if (expanded) {
            if (logsTextView.text.isNotBlank()) {
                logsTextView.append("\n")
                logsTextView.append(spannableLog)
            } else {
                logsTextView.text = spannableLog
            }
        } else {
            logsTextView.text = spannableLog
        }
    }
}
