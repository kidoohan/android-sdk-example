package com.example.sdk.sample

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sdk.sample.databinding.FragmentSampleItemContentBinding
import com.example.sdk.sample.databinding.FragmentSampleItemSectionBinding

class SampleListAdapter(
    private val items: List<SampleContent.Item>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SampleContent.SectionItem -> -1
            is SampleContent.ContentItem -> CONTENT_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CONTENT_VIEW_TYPE -> {
                ContentViewHolder(
                    FragmentSampleItemContentBinding.inflate(
                        inflater,
                        parent,
                        false,
                    ),
                )
            }
            else -> {
                SectionViewHolder(
                    FragmentSampleItemSectionBinding.inflate(
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
            is SampleContent.SectionItem -> {
                (holder as SectionViewHolder).bind(item)
            }
            is SampleContent.ContentItem -> {
                (holder as ContentViewHolder).bind(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class SectionViewHolder(
        binding: FragmentSampleItemSectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val titleView: TextView = binding.listItemSectionTitle

        fun bind(item: SampleContent.SectionItem) {
            titleView.text = item.title
        }

        override fun toString(): String {
            return super.toString() + " '" + titleView.text + "'"
        }
    }

    inner class ContentViewHolder(
        binding: FragmentSampleItemContentBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val titleView: TextView = binding.title

        fun bind(item: SampleContent.ContentItem) {
            titleView.text = item.title
            itemView.setOnClickListener { v ->
                v.findNavController().navigate(item.type.resId, item.type.extras)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + titleView.text + "'"
        }
    }

    companion object {
        private const val CONTENT_VIEW_TYPE = 0
    }
}
