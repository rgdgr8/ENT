/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 */
package technology.cariad.entertainment.metadataservice.service.config

import android.content.SharedPreferences
import technology.cariad.entertainment.metadataservice.BuildConfig as metadataConfig
import technology.cariad.entertainment.metadataservice.common.log.PrivacyFlag.CATEGORY_UNDECIDED
import technology.cariad.entertainment.metadataservice.MetadataServiceApp
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider

private const val KEY_DEMO_MODE = "demo_mode"
private const val KEY_SERVICE_LIST_CHECK_ENABLED = "service_list_check_enabled"
private const val KEY_DISABLE_REASONS_CHECK_ENABLED = "disable_reasons_check_enabled"
private const val KEY_CONRAD_BACKEND_OVERRIDE_URL = "conrad_backend_override_url"
private const val KEY_CONRAD_BACKEND_SEEDED_DEFAULT = "conrad_backend_seeded_default"
private const val KEY_CONRAD_TOKEN_OVERRIDE_ENABLED = "conrad_token_override_enabled"
private const val KEY_CONRAD_TOKEN = "conrad_token"
private const val KEY_CONRAD_TOKEN_SEEDED_DEFAULT = "conrad_token_seeded_default"
private const val KEY_CONRAD_DELETE_BEARER_LIST = "conrad_delete_bearer_list"
private const val KEY_CONRAD_DELETE_METADATA = "conrad_delete_metadata"
private const val KEY_CONRAD_DELETE_LOGO = "conrad_delete_logo"
private const val KEY_CONRAD_DELETE_LOGO_SMALL = "conrad_delete_logo_small"
private const val KEY_CONRAD_DELETE_WEB_STREAMS = "conrad_delete_web_streams"
private const val KEY_CONRAD_EXPIRE_IN_SECONDS = "conrad_expire_in_seconds"
private const val KEY_CONRAD_MIN_DELAY = "conrad_min_delay"
private const val KEY_CONRAD_MAX_DELAY = "conrad_max_delay"
private const val KEY_CONRAD_SEND_BEARER_LIST = "conrad_send_bearer_list"
private const val KEY_CONRAD_SEND_METADATA = "conrad_send_metadata"
private const val KEY_CONRAD_SEND_LOGO = "conrad_send_logo"
private const val KEY_CONRAD_SEND_LOGO_SMALL = "conrad_send_logo_small"
private const val KEY_CONRAD_SEND_WEB_STREAMS = "conrad_send_web_streams"
private const val KEY_CONRAD_SEND_ERROR = "conrad_send_error"
private const val KEY_POSITION_OVERRIDE = "position_override"
private const val KEY_ESM_ENABLED = "esm_enabled"

class DemoConfigProvider(private val sharedPreferences: SharedPreferences) {

    val log by logger()

    var demoModeEnabled: Boolean = false
        set(enabled) {

            val editor = sharedPreferences.edit()

            if (field != enabled) {
                editor.putBoolean(KEY_DEMO_MODE, enabled)

                MetadataServiceApp.appComponent.getMutableConfigurationProvider().setOperationMode(
                    boolToDemoModeString(enabled)
                )
            }

            editor.apply()

            field = enabled
        }

    var serviceListCheckEnabled: Boolean = true
        set(enabled) {
            val editor = sharedPreferences.edit()

            if (field != enabled) {
                editor.putBoolean(KEY_SERVICE_LIST_CHECK_ENABLED, enabled)

                MetadataServiceApp.appComponent.getMutableConfigurationProvider()
                    .setServiceListCheckEnabled(enabled)
            }

            editor.apply()

            field = enabled
        }

    var disableReasonsCheckEnabled: Boolean = true
        set(enabled) {
            val editor = sharedPreferences.edit()

            if (field != enabled) {
                editor.putBoolean(KEY_DISABLE_REASONS_CHECK_ENABLED, enabled)
                MetadataServiceApp.appComponent.getMutableConfigurationProvider()
                    .setDisableReasonsCheckEnabled(enabled)
            }

            editor.apply()

            field = enabled
        }

    var conradBackendOverrideUrl: String = ""
        set(url) {
            val editor = sharedPreferences.edit()

            if (field != url) {
                editor.putString(KEY_CONRAD_BACKEND_OVERRIDE_URL, url)
                MetadataServiceApp.appComponent.getMutableConfigurationProvider()
                    .setConradBackendUrlOverride(url)
            }

            editor.apply()

            field = url
        }

    var conradTokenOverrideEnabled: Boolean = false
        set(enabled) {
            val editor = sharedPreferences.edit()

            if (field != enabled) {
                editor.putBoolean(KEY_CONRAD_TOKEN_OVERRIDE_ENABLED, enabled)
            }

            editor.apply()

            field = enabled
        }

    var conradToken: String = ""
        get() {
            val storedToken = sharedPreferences.getString(KEY_CONRAD_TOKEN, null)
            field = if (storedToken.isNullOrEmpty()) {
                log.info(this::conradToken.getter.name, CATEGORY_UNDECIDED) {
                    "return default token (stored was empty)"
                }
                metadataConfig.DEFAULT_CONRAD_TOKEN
            } else {
                log.info(this::conradToken.getter.name, CATEGORY_UNDECIDED) {
                    "return token from shared preferences: token=$storedToken"
                }
                storedToken
            }
            return field
        }
        set(token) {
            val editor = sharedPreferences.edit()

            if (field != token) {
                editor.putString(KEY_CONRAD_TOKEN, token)
            }

            editor.apply()

            field = token
        }

    var demoConfig: RadioDemoConfig = RadioDemoConfig()
        set(config) {

            val editor = sharedPreferences.edit()

            if (field.deleteBearerList != config.deleteBearerList) {
                editor.putBoolean(KEY_CONRAD_DELETE_BEARER_LIST, config.deleteBearerList)
            }

            if (field.deleteMetadata != config.deleteMetadata) {
                editor.putBoolean(KEY_CONRAD_DELETE_METADATA, config.deleteMetadata)
            }

            if (field.deleteLogo != config.deleteLogo) {
                editor.putBoolean(KEY_CONRAD_DELETE_LOGO, config.deleteLogo)
            }

            if (field.deleteSLogo != config.deleteSLogo) {
                editor.putBoolean(KEY_CONRAD_DELETE_LOGO_SMALL, config.deleteSLogo)
            }

            if (field.deleteWebStreams != config.deleteWebStreams) {
                editor.putBoolean(KEY_CONRAD_DELETE_WEB_STREAMS, config.deleteWebStreams)
            }

            if (field.expireInSeconds != config.expireInSeconds) {
                editor.putLong(KEY_CONRAD_EXPIRE_IN_SECONDS, config.expireInSeconds)
            }

            if (field.minDelay != config.minDelay) {
                editor.putInt(KEY_CONRAD_MIN_DELAY, config.minDelay)
            }

            if (field.maxDelay != config.maxDelay) {
                editor.putInt(KEY_CONRAD_MAX_DELAY, config.maxDelay)
            }

            if (field.sendBearerList != config.sendBearerList) {
                editor.putInt(KEY_CONRAD_SEND_BEARER_LIST, config.sendBearerList)
            }

            if (field.sendMetadata != config.sendMetadata) {
                editor.putInt(KEY_CONRAD_SEND_METADATA, config.sendMetadata)
            }

            if (field.sendLogo != config.sendLogo) {
                editor.putInt(KEY_CONRAD_SEND_LOGO, config.sendLogo)
            }

            if (field.sendSLogo != config.sendSLogo) {
                editor.putInt(KEY_CONRAD_SEND_LOGO_SMALL, config.sendSLogo)
            }

            if (field.sendWebStreams != config.sendWebStreams) {
                editor.putInt(KEY_CONRAD_SEND_WEB_STREAMS, config.sendWebStreams)
            }

            if (field.sendError != config.sendError) {
                editor.putInt(KEY_CONRAD_SEND_ERROR, config.sendError)
            }

            editor.apply()

            field = config
        }

    var positionOverride: String = ""
        set(position) {
            val editor = sharedPreferences.edit()

            if (field != position) {
                editor.putString(KEY_POSITION_OVERRIDE, position)
                MetadataServiceApp.appComponent.getMutableConfigurationProvider()
                    .setPositionOverride(position)
            }

            editor.apply()

            field = position
        }

    init {
        with(sharedPreferences) {

            demoModeEnabled = this.getBoolean(KEY_DEMO_MODE, false)
            val mode = boolToDemoModeString(demoModeEnabled)
            MetadataServiceApp.appComponent.getMutableConfigurationProvider().setOperationMode(mode)

            log.info("init") { "operation mode: $mode" }

            // Per-profile default: real MagicAdapter ServiceList (true) vs mock (false).
            // Persisted so tester overrides via the DEV/LIVE switch survive restarts.
            serviceListCheckEnabled =
                this.getBoolean(KEY_SERVICE_LIST_CHECK_ENABLED, metadataConfig.DEFAULT_USE_REAL_SERVICE_LIST)
            MetadataServiceApp.appComponent.getMutableConfigurationProvider()
                .setServiceListCheckEnabled(serviceListCheckEnabled)

            log.info("init") { "service list check enabled: $serviceListCheckEnabled" }

            // Per-profile default: real MagicAdapter DisableReasons (true) vs mock (false).
            disableReasonsCheckEnabled =
                this.getBoolean(KEY_DISABLE_REASONS_CHECK_ENABLED, metadataConfig.DEFAULT_USE_REAL_DISABLE_REASONS)
            MetadataServiceApp.appComponent.getMutableConfigurationProvider()
                .setDisableReasonsCheckEnabled(disableReasonsCheckEnabled)

            log.info("init") { "disable reasons check enabled: $disableReasonsCheckEnabled" }

            // Backend override URL.
            //
            // The override persists in SharedPreferences and survives reinstalls
            // (adb install -r). To make a changed baked default endpoint take effect after
            // reinstall, we remember which default this build last seeded. If the stored
            // override is still that previously-seeded default (i.e. the tester never picked
            // a custom URL), we follow the current baked default instead of the stale value.
            // A genuine user override is preserved.
            //
            // When the seeded-default key is absent (first launch after introducing this
            // migration) we cannot tell a stale default from a real override, so we follow
            // the current baked default. This purges any stale endpoint left by older builds;
            // a tester who needs a custom URL can simply re-enter it once.
            val currentDefaultBackendUrl = metadataConfig.DEFAULT_CONRAD_BACKEND_URL
            val seededDefault = this.getString(KEY_CONRAD_BACKEND_SEEDED_DEFAULT, null)
            val storedOverride = this.getString(KEY_CONRAD_BACKEND_OVERRIDE_URL, null).orEmpty()

            conradBackendOverrideUrl = when {
                storedOverride.isBlank() -> currentDefaultBackendUrl
                seededDefault == null -> currentDefaultBackendUrl
                storedOverride == seededDefault -> currentDefaultBackendUrl
                else -> storedOverride
            }

            // Record the default this build seeded so a future build can detect a change.
            this.edit().putString(KEY_CONRAD_BACKEND_SEEDED_DEFAULT, currentDefaultBackendUrl).apply()

            log.info("init") { "conrad override url: $conradBackendOverrideUrl" }

            // Per-profile default: token override on (baked test token) vs off
            // (legacy-icc real testbench, where the token comes from Magic/AccountManager).
            conradTokenOverrideEnabled =
                this.getBoolean(KEY_CONRAD_TOKEN_OVERRIDE_ENABLED, metadataConfig.DEFAULT_TOKEN_OVERRIDE_ENABLED)

            log.info("init") { "conrad token override enabled: $conradTokenOverrideEnabled" }

            // Token override value.
            //
            // SharedPreferences survive reinstalls and profile switches. Without migration,
            // a stale token can survive from a previous profile and cause invalid_token
            // errors. We track which default token was seeded by the previous build and
            // only preserve truly user-customized tokens.
            //
            // If the seeded-default key is missing (first launch after introducing this
            // migration), we prefer the current profile default token.
            val currentDefaultToken = metadataConfig.DEFAULT_CONRAD_TOKEN
            val seededDefaultToken = this.getString(KEY_CONRAD_TOKEN_SEEDED_DEFAULT, null)
            val storedToken = this.getString(KEY_CONRAD_TOKEN, null).orEmpty()

            conradToken = when {
                storedToken.isBlank() -> currentDefaultToken
                seededDefaultToken == null -> currentDefaultToken
                storedToken == seededDefaultToken -> currentDefaultToken
                else -> storedToken
            }

            // Persist both effective token and current seeded default for future migrations.
            this.edit()
                .putString(KEY_CONRAD_TOKEN, conradToken)
                .putString(KEY_CONRAD_TOKEN_SEEDED_DEFAULT, currentDefaultToken)
                .apply()

            demoConfig.deleteBearerList =
                this.getBoolean(KEY_CONRAD_DELETE_BEARER_LIST, demoConfig.deleteBearerList)

            demoConfig.deleteMetadata =
                this.getBoolean(KEY_CONRAD_DELETE_METADATA, demoConfig.deleteMetadata)

            demoConfig.deleteLogo =
                this.getBoolean(KEY_CONRAD_DELETE_LOGO, demoConfig.deleteLogo)

            demoConfig.deleteSLogo =
                this.getBoolean(KEY_CONRAD_DELETE_LOGO_SMALL, demoConfig.deleteSLogo)

            demoConfig.deleteWebStreams =
                this.getBoolean(KEY_CONRAD_DELETE_WEB_STREAMS, demoConfig.deleteWebStreams)

            demoConfig.expireInSeconds =
                this.getLong(KEY_CONRAD_EXPIRE_IN_SECONDS, demoConfig.expireInSeconds)

            demoConfig.minDelay =
                this.getInt(KEY_CONRAD_MIN_DELAY, demoConfig.minDelay)

            demoConfig.maxDelay =
                this.getInt(KEY_CONRAD_MAX_DELAY, demoConfig.maxDelay)

            demoConfig.sendBearerList =
                this.getInt(KEY_CONRAD_SEND_BEARER_LIST, demoConfig.sendBearerList)

            demoConfig.sendMetadata =
                this.getInt(KEY_CONRAD_SEND_METADATA, demoConfig.sendMetadata)

            demoConfig.sendLogo =
                this.getInt(KEY_CONRAD_SEND_LOGO, demoConfig.sendLogo)

            demoConfig.sendSLogo =
                this.getInt(KEY_CONRAD_SEND_LOGO_SMALL, demoConfig.sendSLogo)

            demoConfig.sendWebStreams =
                this.getInt(KEY_CONRAD_SEND_WEB_STREAMS, demoConfig.sendWebStreams)

            demoConfig.sendError =
                this.getInt(KEY_CONRAD_SEND_ERROR, demoConfig.sendError)

            positionOverride = this.getString(KEY_POSITION_OVERRIDE, "")!!
        }
    }

    private fun boolToDemoModeString(enabled: Boolean) = when (enabled) {
        true -> IConfigurationProvider.OPERATION_MODE_DEMO
        false -> IConfigurationProvider.OPERATION_MODE_LIVE
    }

    /**
     * Change the Esm value
     * @param state New state
     */
    fun setEsmActivationState(state: Boolean){
        sharedPreferences.edit()?.let {
            it.putBoolean(KEY_ESM_ENABLED, state)
            it.commit()
        }
    }

}

