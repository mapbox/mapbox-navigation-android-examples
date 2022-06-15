package com.mapbox.navigation.examples.replay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.navigation.examples.R

typealias HistoryFileItemClicked = (ReplayPath) -> Unit

private const val ITEM_VIEW_TYPE_HEADER = 1
private const val ITEM_VIEW_TYPE_REPLAY_PATH = 2

class HistoryFileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var data: List<AdapterItem> = listOf()
    var itemClicked: HistoryFileItemClicked? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_REPLAY_PATH -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_files_list_item, parent, false)
                HistoryFileViewHolder(view)
            }
            ITEM_VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_files_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val dataValue = data[position]) {
            is ReplayPath -> {
                val historyHolder = holder as HistoryFileViewHolder
                historyHolder.textViewTop.text = dataValue.title
                historyHolder.textViewBottom.text = dataValue.description
                historyHolder.itemView.setOnClickListener {
                    itemClicked?.invoke(dataValue)
                }
            }
            is Header -> {
                (holder as HeaderViewHolder).textView.text = dataValue.title
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is ReplayPath -> ITEM_VIEW_TYPE_REPLAY_PATH
            is Header -> ITEM_VIEW_TYPE_HEADER
        }
    }

    override fun getItemCount() = data.size
}

class HistoryFileViewHolder(topView: View) : RecyclerView.ViewHolder(topView) {
    val textViewTop: TextView = topView.findViewById(R.id.textViewTop)
    val textViewBottom: TextView = topView.findViewById(R.id.textViewBottom)
}

class HeaderViewHolder(topView: View) : RecyclerView.ViewHolder(topView) {
    val textView: TextView = topView.findViewById(R.id.titleTextView)
}
