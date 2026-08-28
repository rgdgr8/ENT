/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Data model for grouped status items in the StatusFragment.
 */
package cariad.technology.metadataservice.view.model

/**
 * Represents a collapsible group of status items.
 *
 * @property id Unique identifier for the group
 * @property title Display title of the group
 * @property icon Emoji icon for the group header
 * @property items List of status items in this group
 * @property isExpanded Whether the group is currently expanded
 */
data class StatusGroup(
    val id: String,
    val title: String,
    val icon: String,
    val items: List<StatusItem>,
    var isExpanded: Boolean = true
)
