/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Fragment displaying comprehensive service status information including
 * Token status and configuration settings.
 */
package cariad.technology.metadataservice.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import technology.cariad.entertainment.metadataservice.service.DebugService
import technology.cariad.entertainment.metadataservice.service.MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED
import technology.cariad.entertainment.metadataservice.service.MSG_GET_SERVICE_LIST_CHECK_ENABLED
import cariad.technology.metadataservice.view.adapter.StatusAdapter
import cariad.technology.metadataservice.view.model.StatusGroup
import cariad.technology.metadataservice.view.model.StatusIndicator
import cariad.technology.metadataservice.view.model.StatusItem
import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Base64
import org.json.JSONObject

// SharedPreferences keys (mirrored from DemoConfigProvider)
private const val KEY_DEMO_MODE = "demo_mode"
private const val KEY_SERVICE_LIST_CHECK_ENABLED = "service_list_check_enabled"
private const val KEY_DISABLE_REASONS_CHECK_ENABLED = "disable_reasons_check_enabled"
private const val KEY_CONRAD_BACKEND_OVERRIDE_URL = "conrad_backend_override_url"
private const val KEY_CONRAD_TOKEN_OVERRIDE_ENABLED = "conrad_token_override_enabled"
private const val KEY_CONRAD_TOKEN = "conrad_token"

// ESM key (mirrored from ExtendedStorageManager)
private const val KEY_ESM_ENABLED = "esm_enabled"

/**
 * Fragment that displays comprehensive status information for the MetadataService.
 *
 * Shows grouped status items including:
 * - App Info (version, build type, demo mode)
 * - Token (override status, expiry)
 * - Configuration (ServiceList check, DisableReasons check, backend URL)
 * - Available Services
 *
 * Note: This fragment runs in the :demo process and cannot access the main
 * MetadataService flows directly. It reads configuration from SharedPreferences.
 */
class StatusFragment : Fragment() {

    private val log by logger()

    private lateinit var recyclerView: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var sharedPrefs: SharedPreferences

    // Live state queried from DebugService (main process) via IPC
    private var serviceListCheckEnabledLive: Boolean? = null
    private var tokenOverrideEnabledLive: Boolean? = null
    private var debugService: Messenger? = null

    private val statusMessageHandler = Messenger(object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_GET_SERVICE_LIST_CHECK_ENABLED -> {
                    serviceListCheckEnabledLive = msg.arg1 == 1
                    updateStatusGroups()
                }
                MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED -> {
                    tokenOverrideEnabledLive = msg.arg1 == 1
                    updateStatusGroups()
                }
            }
        }
    })

    private val debugServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            debugService = Messenger(binder)
            listOf(MSG_GET_SERVICE_LIST_CHECK_ENABLED, MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED).forEach { what ->
                runCatching {
                    debugService?.send(Message.obtain(null, what).also { it.replyTo = statusMessageHandler })
                }
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            debugService = null
        }
    }

    // Track expansion state across updates
    private val groupExpansionState = mutableMapOf(
        "app_info" to true,
        "token" to true,
        "config" to true,
        "services" to true
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        log.info(this::onCreateView.name) { "Status fragment is added to the view" }

        val view = inflater.inflate(R.layout.fragment_status, container, false)
        recyclerView = view.findViewById(R.id.statusRecyclerView)

        // Initialize SharedPreferences directly (not DemoConfigProvider to avoid appComponent access)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // Setup RecyclerView
        statusAdapter = StatusAdapter(emptyList()) { groupId ->
            groupExpansionState[groupId] = !(groupExpansionState[groupId] ?: true)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = statusAdapter

        return view
    }

    override fun onStart() {
        super.onStart()
        runCatching {
            requireActivity().bindService(
                Intent(requireActivity(), DebugService::class.java),
                debugServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onStop() {
        super.onStop()
        runCatching { requireActivity().unbindService(debugServiceConnection) }
        debugService = null
        serviceListCheckEnabledLive = null
        tokenOverrideEnabledLive = null
    }

    override fun onResume() {
        super.onResume()
        updateStatusGroups()
    }

    /**
     * Updates the status groups with current data from SharedPreferences.
     */
    private fun updateStatusGroups() {
        val groups = mutableListOf<StatusGroup>()

        // 1. App Info Group
        groups.add(buildAppInfoGroup())

        // 2. Token Group
        groups.add(buildTokenGroup())

        // 3. Configuration Group
        groups.add(buildConfigGroup())

        // 4. Services Group (Available ServiceIdentifiers)
        groups.add(buildServicesGroup())

        // Apply saved expansion state
        groups.forEach { group ->
            group.isExpanded = groupExpansionState[group.id] ?: true
        }

        statusAdapter.updateGroups(groups)
    }

    /**
     * Builds the App Info status group.
     */
    private fun buildAppInfoGroup(): StatusGroup {
        val items = mutableListOf<StatusItem>()

        items.add(StatusItem(
            label = "Version",
            value = BuildConfig.VERSION_NAME,
            indicator = StatusIndicator.INFO
        ))

        items.add(StatusItem(
            label = "Build Type",
            value = BuildConfig.BUILD_TYPE,
            indicator = StatusIndicator.INFO
        ))

        items.add(StatusItem(
            label = "Flavor",
            value = BuildConfig.FLAVOR,
            indicator = StatusIndicator.INFO
        ))

        val demoModeEnabled = sharedPrefs.getBoolean(KEY_DEMO_MODE, false)
        items.add(StatusItem(
            label = "Demo Mode",
            value = if (demoModeEnabled) "Enabled" else "Disabled",
            indicator = if (demoModeEnabled) StatusIndicator.WARNING else StatusIndicator.OK
        ))

        return StatusGroup(
            id = "app_info",
            title = "App Info",
            icon = "📱",
            items = items
        )
    }

    /**
     * Builds the Token status group.
     */
    private fun buildTokenGroup(): StatusGroup {
        val items = mutableListOf<StatusItem>()

        val tokenOverrideEnabled = tokenOverrideEnabledLive
            ?: sharedPrefs.getBoolean(KEY_CONRAD_TOKEN_OVERRIDE_ENABLED, false)
        items.add(StatusItem(
            label = "Token Source",
            value = when {
                tokenOverrideEnabledLive == null -> "Loading..."
                tokenOverrideEnabled -> "Dev Token (Override)"
                else -> "Dev Token (BuildConfig default)"
            },
            indicator = if (tokenOverrideEnabled) StatusIndicator.WARNING else StatusIndicator.OK
        ))

        if (tokenOverrideEnabled) {
            // Only show token details when using the dev token override
            // because the MagicAdapter token is not accessible from the :demo process
            val storedToken = sharedPrefs.getString(KEY_CONRAD_TOKEN, null)
            val token = if (storedToken.isNullOrEmpty()) BuildConfig.DEFAULT_CONRAD_TOKEN else storedToken
            val tokenInfo = parseJwtToken(token)

            items.add(StatusItem(
                label = "Token (truncated)",
                value = if (token.length > 50) "${token.take(25)}...${token.takeLast(10)}" else token,
                indicator = StatusIndicator.INFO
            ))

            tokenInfo?.let { info ->
                items.add(StatusItem(
                    label = "Token Expiry",
                    value = info.expiryFormatted,
                    indicator = if (info.isExpired) StatusIndicator.ERROR else StatusIndicator.OK
                ))

                items.add(StatusItem(
                    label = "Token Brand",
                    value = info.brand ?: "Unknown",
                    indicator = StatusIndicator.INFO
                ))

                items.add(StatusItem(
                    label = "Token Country",
                    value = info.country ?: "Unknown",
                    indicator = StatusIndicator.INFO
                ))

                items.add(StatusItem(
                    label = "Token Tenant",
                    value = info.tenant ?: "Unknown",
                    indicator = StatusIndicator.INFO
                ))
            }
        } else {
            // MagicAdapter token - not accessible from demo process
            items.add(StatusItem(
                label = "Token Details",
                value = "Token is provided by MagicAdapter at runtime",
                indicator = StatusIndicator.INFO
            ))

            items.add(StatusItem(
                label = "Note",
                value = "Enable 'Use Dev Token' in Settings to see token details here",
                indicator = StatusIndicator.INFO
            ))
        }

        return StatusGroup(
            id = "token",
            title = "Token",
            icon = "🔑",
            items = items
        )
    }

    /**
     * Builds the Configuration status group.
     */
    private fun buildConfigGroup(): StatusGroup {
        val items = mutableListOf<StatusItem>()

        val serviceListCheckEnabled = serviceListCheckEnabledLive
            ?: sharedPrefs.getBoolean(KEY_SERVICE_LIST_CHECK_ENABLED, true)
        items.add(StatusItem(
            label = "ServiceList Check",
            value = when {
                serviceListCheckEnabledLive == null -> "Loading..."
                serviceListCheckEnabled -> "Enabled (live from server)"
                else -> "Disabled (fake service list)"
            },
            indicator = if (serviceListCheckEnabled) StatusIndicator.OK else StatusIndicator.WARNING
        ))

        val disableReasonsCheckEnabled = sharedPrefs.getBoolean(KEY_DISABLE_REASONS_CHECK_ENABLED, true)
        items.add(StatusItem(
            label = "DisableReasons Check",
            value = if (disableReasonsCheckEnabled) "Enabled (MagicAdapter)" else "Disabled",
            indicator = if (disableReasonsCheckEnabled) StatusIndicator.OK else StatusIndicator.WARNING
        ))

        val backendUrl = sharedPrefs.getString(KEY_CONRAD_BACKEND_OVERRIDE_URL, "") ?: ""
        items.add(StatusItem(
            label = "Backend Override",
            value = if (backendUrl.isEmpty()) "None (from ServiceList)" else backendUrl,
            indicator = if (backendUrl.isEmpty()) StatusIndicator.OK else StatusIndicator.WARNING
        ))

        // Storage location (ESM = Extended Storage Manager for external partition)
        val esmEnabled = sharedPrefs.getBoolean(KEY_ESM_ENABLED, false)
        items.add(StatusItem(
            label = "Storage Location",
            value = if (esmEnabled) "External Partition (ESM)" else "Internal Storage (Local)",
            indicator = StatusIndicator.INFO
        ))

        return StatusGroup(
            id = "config",
            title = "Configuration",
            icon = "⚙️",
            items = items
        )
    }

    /**
     * Builds the Services status group showing available ServiceIdentifiers.
     */
    private fun buildServicesGroup(): StatusGroup {
        val items = mutableListOf<StatusItem>()

        ServiceIdentifier.entries.forEach { serviceId ->
            items.add(StatusItem(
                label = serviceId.serviceName,
                value = "Available (check Settings for live status)",
                indicator = StatusIndicator.INFO
            ))
        }

        items.add(StatusItem(
            label = "Note",
            value = "Live service status available via MetadataService API",
            indicator = StatusIndicator.INFO
        ))

        return StatusGroup(
            id = "services",
            title = "Available Services",
            icon = "📡",
            items = items
        )
    }

    /**
     * Parses a JWT token and extracts relevant information.
     */
    private fun parseJwtToken(token: String): TokenInfo? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)

            val exp = json.optLong("exp", 0)
            val expDate = Date(exp * 1000)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val isExpired = expDate.before(Date())

            val brand = if (json.has("brand")) json.getString("brand") else null
            val country = if (json.has("country")) json.getString("country") else null
            val tenant = if (json.has("tenant")) json.getString("tenant") else null

            TokenInfo(
                expiryTimestamp = exp,
                expiryFormatted = dateFormat.format(expDate),
                isExpired = isExpired,
                brand = brand,
                country = country,
                tenant = tenant
            )
        } catch (e: Exception) {
            log.error(this::parseJwtToken.name) { "Failed to parse JWT: ${e.message}" }
            null
        }
    }

    /**
     * Data class for parsed JWT token information.
     */
    private data class TokenInfo(
        val expiryTimestamp: Long,
        val expiryFormatted: String,
        val isExpired: Boolean,
        val brand: String?,
        val country: String?,
        val tenant: String?
    )
}

