package com.example.sdk.sample.visibilityobserver

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sdk.internal.visibilityobserver.VisibilityObserver
import com.example.sdk.internal.visibilityobserver.VisibilityObserver.Companion.addExposureChangeObserver
import com.example.sdk.sample.R
import com.example.sdk.sample.databinding.FragmentVisibilityObserverItemNonTargetBinding
import com.example.sdk.sample.databinding.FragmentVisibilityObserverItemTargetBinding
import java.lang.StringBuilder

class VisibilityObserverWithListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    sealed class Item(val content: String)

    class TargetItem(content: String) : Item(content)
    class NonTargetItem(content: String) : Item(content)

    private val items: List<Item>

    init {
        val tItems = mutableListOf<Item>()
        repeat(100) {
            if (it % 5 == 0) {
                tItems.add(TargetItem("Target ${(it / 5) + 1}"))
            }
            tItems.add(NonTargetItem("NonTarget ${it + 1}"))
        }
        items = tItems
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TargetItem -> TARGET_VIEW_TYPE
            is NonTargetItem -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TARGET_VIEW_TYPE -> {
                TargetViewHolder(
                    FragmentVisibilityObserverItemTargetBinding.inflate(
                        inflater,
                        parent,
                        false,
                    ),
                )
            }
            else -> {
                NonTargetViewHolder(
                    FragmentVisibilityObserverItemNonTargetBinding.inflate(
                        inflater,
                        parent,
                        false,
                    ),
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TargetItem -> {
                (holder as TargetViewHolder).bind(item)
            }
            is NonTargetItem -> {
                (holder as NonTargetViewHolder).bind(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class TargetViewHolder(
        binding: FragmentVisibilityObserverItemTargetBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val content = binding.content
        private var visibilityObserver: VisibilityObserver? = null

        fun bind(item: TargetItem) {
            if (visibilityObserver == null) {
                visibilityObserver = itemView.addExposureChangeObserver { _, newEntry ->
                    itemView.setBackgroundResource(
                        if (newEntry.intersectingRatio >= 0.5) {
                            R.color.success
                        } else if (newEntry.intersectingRatio >= 0.3) {
                            R.color.warning
                        } else {
                            R.color.error
                        },
                    )

                    val intersectingPercent = (newEntry.intersectingRatio * 100).toFloat()
                    val intersectingWidth = newEntry.intersectingRect?.width() ?: 0
                    val intersectingHeight = newEntry.intersectingRect?.height() ?: 0
                    val intersectingPx = newEntry.intersectingPx

                    content.text = StringBuilder().apply {
                        append("intersectingPercent: $intersectingPercent\n")
                        append("intersectingWidth: $intersectingWidth\n")
                        append("intersectingHeight: $intersectingHeight\n")
                        append("intersectingPx: $intersectingPx\n")
                    }
                }.apply {
                    observe()
                }
            }
            content.text = item.content
        }
    }

    inner class NonTargetViewHolder(
        binding: FragmentVisibilityObserverItemNonTargetBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val content: TextView = binding.content

        fun bind(item: NonTargetItem) {
            content.text = item.content
        }
    }

    companion object {
        private const val TARGET_VIEW_TYPE = 0
    }
}
