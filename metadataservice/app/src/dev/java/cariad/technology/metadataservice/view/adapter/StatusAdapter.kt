/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * RecyclerView Adapter for displaying grouped status items with collapsible headers.
 */
package cariad.technology.metadataservice.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cariad.technology.metadataservice.view.model.StatusGroup
import cariad.technology.metadataservice.view.model.StatusIndicator
import cariad.technology.metadataservice.view.model.StatusItem
import technology.cariad.entertainment.metadataservice.R

/**
 * Sealed class representing different item types in the status list.
 */
sealed class StatusListItem {
    data class Header(val group: StatusGroup) : StatusListItem()
    data class Item(val item: StatusItem, val groupId: String) : StatusListItem()
}

/**
 * Adapter for displaying status groups with collapsible headers and status items.
 */
class StatusAdapter(
    private var groups: List<StatusGroup>,
    private val onGroupToggle: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private var flattenedList: List<StatusListItem> = buildFlattenedList()

    private fun buildFlattenedList(): List<StatusListItem> {
        val list = mutableListOf<StatusListItem>()
        for (group in groups) {
            list.add(StatusListItem.Header(group))
            if (group.isExpanded) {
                group.items.forEach { item ->
                    list.add(StatusListItem.Item(item, group.id))
                }
            }
        }
        return list
    }

    /**
     * Update the groups and refresh the list.
     */
    fun updateGroups(newGroups: List<StatusGroup>) {
        groups = newGroups
        flattenedList = buildFlattenedList()
        notifyDataSetChanged()
    }

    /**
     * Toggle expansion state of a group.
     */
    fun toggleGroup(groupId: String) {
        groups.find { it.id == groupId }?.let { group ->
            group.isExpanded = !group.isExpanded
            flattenedList = buildFlattenedList()
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (flattenedList[position]) {
            is StatusListItem.Header -> VIEW_TYPE_HEADER
            is StatusListItem.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_status_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_status_row, parent, false)
                ItemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = flattenedList[position]) {
            is StatusListItem.Header -> (holder as HeaderViewHolder).bind(item.group)
            is StatusListItem.Item -> (holder as ItemViewHolder).bind(item.item)
        }
    }

    override fun getItemCount(): Int = flattenedList.size

    /**
     * ViewHolder for group headers.
     */
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconTextView: TextView = itemView.findViewById(R.id.headerIcon)
        private val titleTextView: TextView = itemView.findViewById(R.id.headerTitle)
        private val expandIndicator: ImageView = itemView.findViewById(R.id.expandIndicator)

        fun bind(group: StatusGroup) {
            iconTextView.text = group.icon
            titleTextView.text = group.title

            expandIndicator.rotation = if (group.isExpanded) 180f else 0f

            itemView.setOnClickListener {
                onGroupToggle(group.id)
                toggleGroup(group.id)
            }
        }
    }

    /**
     * ViewHolder for status items.
     */
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val indicatorView: View = itemView.findViewById(R.id.statusIndicator)
        private val labelTextView: TextView = itemView.findViewById(R.id.statusLabel)
        private val valueTextView: TextView = itemView.findViewById(R.id.statusValue)

        fun bind(item: StatusItem) {
            labelTextView.text = item.label
            valueTextView.text = item.value

            // Set indicator color based on status
            val indicatorColor = when (item.indicator) {
                StatusIndicator.OK -> Color.parseColor("#4CAF50")      // Green
                StatusIndicator.WARNING -> Color.parseColor("#FF9800") // Orange
                StatusIndicator.ERROR -> Color.parseColor("#F44336")   // Red
                StatusIndicator.INFO -> Color.parseColor("#9E9E9E")    // Gray
            }
            indicatorView.setBackgroundColor(indicatorColor)
        }
    }
}

