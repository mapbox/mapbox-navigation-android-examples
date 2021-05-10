package com.mapbox.navigation.examples

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.navigation.examples.databinding.MapboxItemViewRecyclerBinding

class MapboxExamplesAdapter(
    private val examplesList: List<MapboxExample>,
    private val itemClickLambda: (position: Int) -> Unit
) : RecyclerView.Adapter<MapboxExamplesAdapter.MapboxExamplesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapboxExamplesViewHolder {
        val binding = MapboxItemViewRecyclerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MapboxExamplesViewHolder(binding)
    }

    override fun getItemCount(): Int = examplesList.size

    override fun onBindViewHolder(holder: MapboxExamplesViewHolder, position: Int) {
        with(holder) { bindItem(examplesList[position]) }
    }

    inner class MapboxExamplesViewHolder(
        private val viewBinding: MapboxItemViewRecyclerBinding
    ) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bindItem(example: MapboxExample) {
            viewBinding.itemImage.setImageDrawable(example.image)
            viewBinding.itemTitle.text = example.title
            viewBinding.itemDescription.text = example.description

            viewBinding.root.setOnClickListener {
                itemClickLambda(layoutPosition)
            }
        }
    }
}
