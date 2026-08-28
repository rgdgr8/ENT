/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Modifications:
 * - 2026-02-06: Added explicit token properties for PROD builds (MagicAdapter integration)
 */
package technology.cariad.entertainment.metadataservice.service.config

import android.content.SharedPreferences

/**
 * Production configuration provider.
 * 
 * In PROD builds, token authentication MUST come from the MagicAdapter/CoreService AccountManager.
 * No debug token override is available.
 * 
 * If the CoreService account is not registered on the device, the app will fail to authenticate
 * with the backend and log an error: "Can't receive accounts from AccountManager"
 */
data class DemoConfigProvider(private val sharedPreferences: SharedPreferences) {
    
    /**
     * In PROD builds, token override is always disabled.
     * Token must come from MagicAdapter/CoreService AccountManager.
     */
    val conradTokenOverrideEnabled: Boolean = false
    
    /**
     * In PROD builds, no debug token is available.
     * Token must come from MagicAdapter/CoreService AccountManager.
     * 
     * This empty string ensures the TokenHandler.debugToken remains null,
     * triggering the AccountManager token retrieval flow.
     */
    val conradToken: String = ""
}
