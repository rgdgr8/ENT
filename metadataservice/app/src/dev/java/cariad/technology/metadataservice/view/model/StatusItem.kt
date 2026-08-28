/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Data model for individual status items displayed in the StatusFragment.
 */
package cariad.technology.metadataservice.view.model

/**
 * Status indicator for visual feedback
 */
enum class StatusIndicator {
    OK,      // Green - everything working
    WARNING, // Orange - degraded or partial functionality
    ERROR,   // Red - not working
    INFO     // Gray - neutral information
}

/**
 * Represents a single status item to be displayed in the status list.
 *
 * @property label The label/title of the status item
 * @property value The current value as string
 * @property indicator Visual status indicator (OK, WARNING, ERROR, INFO)
 * @property details Optional additional details shown on expansion
 */
data class StatusItem(
    val label: String,
    val value: String,
    val indicator: StatusIndicator = StatusIndicator.INFO,
    val details: String? = null
)
