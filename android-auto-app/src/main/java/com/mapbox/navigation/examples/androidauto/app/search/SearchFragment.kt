package com.mapbox.navigation.examples.androidauto.app.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.navigation.examples.androidauto.R

class SearchFragment : Fragment() {

    private var dropInSearchComponent: DropInSearchComponent? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dropInSearchComponent = DropInSearchComponent(
            requireActivity().findViewById(R.id.mapView),
            AppSearchBottomSheet(
                view.findViewById(R.id.search_place_view),
                view.findViewById(R.id.root),
                savedInstanceState
            )
        ).also { lifecycle.addObserver(it) }
    }

    override fun onDestroyView() {
        dropInSearchComponent = null
        super.onDestroyView()
    }

    fun handleOnBackPressed(): Boolean =
        dropInSearchComponent?.handleOnBackPressed() ?: false

    override fun onSaveInstanceState(outState: Bundle) {
        dropInSearchComponent?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}
