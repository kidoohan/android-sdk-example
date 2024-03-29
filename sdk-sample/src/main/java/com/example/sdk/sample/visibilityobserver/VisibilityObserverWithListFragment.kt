package com.example.sdk.sample.visibilityobserver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sdk.sample.R

class VisibilityObserverWithListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_visibility_observer_with_list, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.list)

        // Set the adapter
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = VisibilityObserverWithListAdapter()
        }
        return view
    }
}
