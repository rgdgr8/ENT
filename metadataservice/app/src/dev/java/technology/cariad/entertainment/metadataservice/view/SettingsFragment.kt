package technology.cariad.entertainment.metadataservice.view

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import technology.cariad.entertainment.metadataservice.R
import technology.cariad.entertainment.metadataservice.common.log.PrivacyFlag
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.databinding.FragmentSettingsBinding
import technology.cariad.entertainment.metadataservice.model.OnlineStateModel
import technology.cariad.entertainment.metadataservice.service.DATA_ARG_1
import technology.cariad.entertainment.metadataservice.service.DebugService
import technology.cariad.entertainment.metadataservice.service.MSG_DELETE_CACHE
import technology.cariad.entertainment.metadataservice.service.MSG_DISABLE_ESM_CACHE
import technology.cariad.entertainment.metadataservice.service.MSG_ENABLE_ESM_CACHE
import technology.cariad.entertainment.metadataservice.service.MSG_GET_CONRAD_BACKEND_OVERRIDE_URL
import technology.cariad.entertainment.metadataservice.service.MSG_GET_CONRAD_DEMO_CONFIG
import technology.cariad.entertainment.metadataservice.service.MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED
import technology.cariad.entertainment.metadataservice.service.MSG_GET_DEBUG_MODE
import technology.cariad.entertainment.metadataservice.service.MSG_GET_DISABLE_REASONS_CHECK_ENABLED
import technology.cariad.entertainment.metadataservice.service.MSG_GET_POSITION_OVERRIDE
import technology.cariad.entertainment.metadataservice.service.MSG_GET_SERVICE_LIST_CHECK_ENABLED
import technology.cariad.entertainment.metadataservice.service.MSG_SET_CONRAD_BACKEND_OVERRIDE_URL
import technology.cariad.entertainment.metadataservice.service.MSG_SET_CONRAD_DEMO_CONFIG
import technology.cariad.entertainment.metadataservice.service.MSG_SET_CONRAD_TOKEN_OVERRIDE_ENABLED
import technology.cariad.entertainment.metadataservice.service.MSG_SET_DEBUG_MODE
import technology.cariad.entertainment.metadataservice.service.MSG_SET_DISABLE_REASONS_CHECK_ENABLED
import technology.cariad.entertainment.metadataservice.service.MSG_SET_POSITION_OVERRIDE
import technology.cariad.entertainment.metadataservice.service.MSG_SET_SERVICE_LIST_CHECK_ENABLED
import technology.cariad.entertainment.metadataservice.service.OBJ_KEY_STRING_1
import technology.cariad.entertainment.metadataservice.service.config.RadioDemoConfig
import java.lang.ref.WeakReference
import kotlin.system.exitProcess
import de.esolutions.connectedradioclient.BuildConfig as conradConfig
import technology.cariad.entertainment.metadataservice.BuildConfig as metadataConfig
import technology.cariad.entertainment.metadataserviceclient.BuildConfig as clientConfig

/*
* The client sets current debug mode state via activity UI and
* service responds with [Message.arg1] set to the current debug mode state.
* Allowed values for the state are 1 (enabled) and 0 (disabled)
*/
const val DISABLED = 0
const val ENABLED = 1
private const val KEY_ESM_ENABLED = "esm_enabled"

/**
 * A fragment hosting a client communicating with a bound service via a simple [Messenger] based interface.
 * Fragment provides a simple UI to the user to change MetadataService configuration dynamically.
 */
class SettingsFragment : Fragment() {

    private val log by logger()

    private lateinit var binding: FragmentSettingsBinding

    private var debugService: Messenger? = null
    private lateinit var debugSwitch: Switch
    private lateinit var debugModeCheckedChangeListener: CompoundButton.OnCheckedChangeListener
    private lateinit var demoConfig: RadioDemoConfig

    private lateinit var onlineStateSwitch: Switch
    private lateinit var onlineStateCheckedChangeListener: CompoundButton.OnCheckedChangeListener

    private var onlineStateModel: OnlineStateModel? = null

    private lateinit var conradBackendOverrideSpinner: Spinner
    private lateinit var conradBackendOverrideSelectionListener: AdapterView.OnItemSelectedListener
    private lateinit var conradBackendCustomUrlEditText: EditText

    private val backendList = listOf(
        " -- Service List --",
        "ODP Demo HCP3",
        "ODP Approval HCP3",
        "ODP Live HCP3"
    )

    private val backendUrlList = listOf(
        "",
        metadataConfig.DEFAULT_CONRAD_BACKEND_URL,
        "https://connectedradio-v2.tui.car-cloud.org",
        "https://connectedradio-v2.approval.car-cloud.org",
        "https://connectedradio-v2.live.car-cloud.org"
    )

    private lateinit var conradTokenOverrideSwitch: Switch
    private lateinit var conradTokenOverrideChangeListener: CompoundButton.OnCheckedChangeListener
    private lateinit var saveConradSettingsButton: Button

    private lateinit var conradVariantTextView: TextView
    private lateinit var runtimeModeSummaryTextView: TextView
    private lateinit var apkVersionTextView: TextView
    private lateinit var conRadLibVersionTextView: TextView
    private lateinit var clientLibVersionTextView: TextView
    private lateinit var graceNoteStatusTextView: TextView

    private lateinit var simulateDevEnvironmentSwitch: Switch
    private lateinit var simulateDevEnvironmentChangeListener: CompoundButton.OnCheckedChangeListener

    private var pendingConradBackendOverrideUrl: String = metadataConfig.DEFAULT_CONRAD_BACKEND_URL
    private var pendingConradTokenOverrideEnabled: Boolean = false
    private var pendingConradToken: String = ""
    private var pendingConradCustomBackendUrl: String = ""
    private var pendingRuntimeModeIsDev: Boolean = false
    private var suppressConradListeners: Boolean = false


    private val positionList = linkedMapOf(
        "--- Custom Position ---" to ";",
        "Peking, China" to "39.865482;116.423232",
        "Seoul, South-Korea" to "37.556629;126.977870",
        "Shanghai, China" to "31.2301453;121.4779433",
        "Tokio, Japan" to "35.717550;139.749712",
        "Hangzhou, China" to "30.295063;120.157512",
        "Hongkong, China" to "22.396428;114.109497"
    )

    private lateinit var positionItemSelectedListener: AdapterView.OnItemSelectedListener

    /**
     * Definition of the [Handler] for incoming messages from services.
     * @param fragment Outer class instance. Note: Storing this reference directly can cause the [Handler] to be leaked!
     */
    internal class MessageHandler(fragment: SettingsFragment) : Handler() {

        private val log by logger(SettingsFragment::class)

        //avoid leaking the handler by only holding a weak reference to the outer class instance
        private val fragmentRef = WeakReference(fragment)

        /**
         * @param msg  Incoming message.
         * @see Handler.handleMessage
         */
        override fun handleMessage(msg: Message) {

            val fragment = fragmentRef.get() ?: return

            when (msg.what) {
                MSG_GET_DEBUG_MODE -> {
                    val debugState = (msg.arg1 == ENABLED)
                    fragment.updateViewDemoMode(debugState)

                    log.info(this::handleMessage.name) {
                        "Debug mode information is get from service: ${msg.arg1}"
                    }
                }
                MSG_SET_DEBUG_MODE -> {
                    log.info(this::handleMessage.name) {
                        "Debug mode is set to service: ${msg.arg1}"
                    }
                }
                MSG_GET_CONRAD_DEMO_CONFIG -> {
                    msg.data.classLoader = javaClass.classLoader
                    val demoConfig = msg.data.getParcelable<RadioDemoConfig>(DATA_ARG_1) ?: return
                    fragment.demoConfig = demoConfig
                    fragment.updateViewDemoConfig(demoConfig)
                }
                MSG_SET_CONRAD_DEMO_CONFIG -> {
                }
                MSG_GET_SERVICE_LIST_CHECK_ENABLED -> {
                    val serviceListCheckEnabled = msg.arg1 == ENABLED
                    fragment.pendingRuntimeModeIsDev = !serviceListCheckEnabled
                    fragment.updateViewSimulateDevEnvironment(fragment.pendingRuntimeModeIsDev)
                }
                MSG_DELETE_CACHE -> {
                }
                MSG_GET_CONRAD_BACKEND_OVERRIDE_URL -> {
                    val bundle = msg.obj as? Bundle
                    val url = bundle?.getString(OBJ_KEY_STRING_1) ?: ""
                    fragment.pendingConradBackendOverrideUrl = url
                    fragment.updateViewConradBackendOverrideUrl(url)
                }
                MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED -> {
                    fragment.pendingConradTokenOverrideEnabled = msg.arg1 == ENABLED
                    fragment.updateViewConradTokenOverride(fragment.pendingConradTokenOverrideEnabled)
                }
                MSG_GET_POSITION_OVERRIDE -> {
                    val bundle = msg.obj as? Bundle
                    val pos = bundle?.getString(OBJ_KEY_STRING_1) ?: ""
                    fragment.updateViewPositionOverride(pos)
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val messageHandler = Messenger(MessageHandler(this))

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            debugService = Messenger(binder)

            log.info(this::onServiceConnected.name) { "DebugService is connected" }

            getDebugMode()
            getDemoConfig()
            getServiceListCheckEnabled()
            getConradBackendOverrideUrl()
            getConradTokenOverrideEnabled()
            getPositionOverride()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            debugService = null

            log.info(this::onServiceDisconnected.name) { "DebugService is disconnected" }
        }
    }

    private fun doBindService() {
        try {
            val intent = Intent(activity, DebugService::class.java)
            activity?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

            log.info(this::doBindService.name) { "service connection established" }
        } catch (e: RemoteException) {
            log.warn(this::doBindService.name) { "bindService failed with error $e" }
        }
    }

    private fun doUnbindService() {
        activity?.unbindService(serviceConnection)

        log.info(this::doUnbindService.name) { "service connection closed" }
    }

    private fun setDebugMode(mode: Int) {
        val msg = Message.obtain(null, MSG_SET_DEBUG_MODE)
        msg.arg1 = mode
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::setDebugMode.name) {
                "Sending SET_DEBUG_MODE message is successful. Sent mode value: ${msg.arg1}"
            }
        } catch (e: RemoteException) {
            log.warn(this::setDebugMode.name) { "Sending SET_DEBUG_MODE message failed with error $e" }
        }
    }

    private fun getDebugMode() {
        val msg = Message.obtain(null, MSG_GET_DEBUG_MODE)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::getDebugMode.name) {
                "Sending GET_DEBUG_MODE message is successful. Received mode value: ${msg.arg1}"
            }
        } catch (e: RemoteException) {
            log.warn(this::getDebugMode.name) { "Sending GET_DEBUG_MODE message failed with error $e" }
        }
    }

    private fun getDemoConfig() {
        val msg = Message.obtain(null, MSG_GET_CONRAD_DEMO_CONFIG)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::getDemoConfig.name) {
                "Sending MSG_GET_CONRAD_DEMO_CONFIG message is successful. Received mode value:" +
                        " ${msg.arg1}"
            }
        } catch (e: RemoteException) {
            log.warn(this::getDemoConfig.name) {
                "Sending MSG_GET_CONRAD_DEMO_CONFIG message failed error $e"
            }
        }
    }

    private fun setDemoConfig() {

        demoConfig.sendBearerList = binding.bearerListPercentageSeekBar.progress * 10
        demoConfig.sendMetadata = binding.metadataPercentageSeekBar.progress * 10
        demoConfig.sendLogo = binding.logoPercentageSeekBar.progress * 10
        demoConfig.sendSLogo = binding.logoSmallPercentageSeekBar.progress * 10
        demoConfig.sendWebStreams = binding.streamsPercentageSeekBar.progress * 10
        demoConfig.sendError = binding.errorPercentageSeekBar.progress * 10

        demoConfig.deleteBearerList = binding.deleteBearerListCheckBox.isChecked
        demoConfig.deleteMetadata = binding.deleteMetadataCheckBox.isChecked
        demoConfig.deleteLogo = binding.deleteLogoCheckBox.isChecked
        demoConfig.deleteSLogo = binding.deleteSmallLogoCheckBox.isChecked
        demoConfig.deleteWebStreams = binding.deleteStreamsCheckBox.isChecked

        val minDelayString = binding.minDelayEditText.text.toString()
        val minDelay = minDelayString.toIntOrNull() ?: demoConfig.minDelay
        demoConfig.minDelay = minDelay
        if (minDelayString != minDelay.toString()) binding.minDelayEditText.setText(minDelay.toString())

        val maxDelayString = binding.maxDelayEditText.text.toString()
        val maxDelay = maxDelayString.toIntOrNull() ?: demoConfig.maxDelay
        demoConfig.maxDelay = maxDelay
        if (maxDelayString != maxDelay.toString()) binding.maxDelayEditText.setText(maxDelay.toString())

        val lifetimeString = binding.lifetimeEditText.text.toString()
        val lifetime = lifetimeString.toLongOrNull() ?: demoConfig.expireInSeconds
        demoConfig.expireInSeconds = lifetime
        if (lifetimeString != lifetime.toString()) binding.lifetimeEditText.setText(lifetime.toString())

        val msg = Message.obtain(null, MSG_SET_CONRAD_DEMO_CONFIG)
        msg.data.putParcelable(DATA_ARG_1, demoConfig)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)
            log.info(this::setDemoConfig.name) {
                "Sending MSG_SET_CONRAD_DEMO_CONFIG message is successful."
            }
        } catch (e: RemoteException) {
            log.warn(this::setDemoConfig.name) { "Sending MSG_SET_CONRAD_DEMO_CONFIG message failed error $e" }
        }
    }

    private fun setDefaultDemoConfig() {
        demoConfig = RadioDemoConfig()
        updateViewDemoConfig(demoConfig)

        val msg = Message.obtain(null, MSG_SET_CONRAD_DEMO_CONFIG)
        msg.data.putParcelable(DATA_ARG_1, demoConfig)
        msg.replyTo = messageHandler

        try {
            debugService?.send(msg)
            log.info(this::setDefaultDemoConfig.name) {
                "Sending MSG_SET_CONRAD_DEMO_CONFIG message is successful."
            }
        } catch (e: RemoteException) {
            log.warn(this::setDefaultDemoConfig.name) { "Sending MSG_SET_CONRAD_DEMO_CONFIG message failed error $e" }
        }
        simulateDevEnvironmentSwitch.isChecked = false
        onlineStateSwitch.isChecked = true
        debugSwitch.isChecked = false
        conradTokenOverrideSwitch.isChecked = false
        conradBackendOverrideSpinner.setSelection(0)
        binding.positionSpinner.setSelection(0)

        disableESM()
    }

    private fun deleteCache() {

        val msg = Message.obtain(null, MSG_DELETE_CACHE)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)
            log.info(this::deleteCache.name) { "Sending message successful" }
            restartApplication(requireActivity())
        } catch (e: RemoteException) {
            log.warn(this::deleteCache.name) { "Sending MSG_DELETE_CACHE message failed $e" }
        }
    }

    private fun enableESM() {

        val msg = Message.obtain(null, MSG_ENABLE_ESM_CACHE)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)
            log.info(this::enableESM.name) { "Sending message successful" }
            restartApplication(requireActivity())
        } catch (e: RemoteException) {
            log.warn(this::enableESM.name) { "Sending MSG_ENABLE_ESM_CACHE message failed error $e" }
        }
    }

    private fun disableESM() {

        val msg = Message.obtain(null, MSG_DISABLE_ESM_CACHE)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)
            log.info(this::disableESM.name) { "Sending message successful" }
            restartApplication(requireActivity())
        } catch (e: RemoteException) {
            log.warn(this::disableESM.name) { "Sending MSG_DISABLE_ESM_CACHE message failed error $e" }
        }
    }

    private fun restartApplication(context: Activity) {
        val intent = Intent(context, DemoActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        exitProcess(0)
    }

    private fun setServiceListCheckEnabled(mode: Int) {
        val msg = Message.obtain(null, MSG_SET_SERVICE_LIST_CHECK_ENABLED)
        msg.arg1 = mode
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::setDebugMode.name) {
                "Sending MSG_SET_SERVICE_LIST_CHECK_ENABLED message is successful. " +
                        "Sent mode value: ${msg.arg1}"
            }
        } catch (e: RemoteException) {
            log.warn(this::setDebugMode.name) {
                "Sending MSG_SET_SERVICE_LIST_CHECK_ENABLED message failed error $e"
            }
        }
    }

    private fun getServiceListCheckEnabled() {
        val msg = Message.obtain(null, MSG_GET_SERVICE_LIST_CHECK_ENABLED)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::getServiceListCheckEnabled.name) {
                "Sending MSG_GET_SERVICE_LIST_CHECK_ENABLED message is successful. " +
                        "Received mode value: ${msg.arg1}"
            }
        } catch (e: RemoteException) {
            log.warn(this::getServiceListCheckEnabled.name) {
                "Sending MSG_GET_SERVICE_LIST_CHECK_ENABLED message failed error $e"
            }
        }
    }

    private fun setDisableReasonsCheckEnabled(mode: Int) {
        val msg = Message.obtain(null, MSG_SET_DISABLE_REASONS_CHECK_ENABLED)
        msg.arg1 = mode
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::setDisableReasonsCheckEnabled.name) {
                "Sending MSG_SET_DISABLE_REASONS_CHECK_ENABLED message is successful. " +
                        "Sent mode value: ${msg.arg1}"
            }
        } catch (e: RemoteException) {
            log.warn(this::setDisableReasonsCheckEnabled.name) {
                "Sending MSG_SET_DISABLE_REASONS_CHECK_ENABLED message failed error $e"
            }
        }
    }

    private fun getDisableReasonsCheckEnabled() {
        val msg = Message.obtain(null, MSG_GET_DISABLE_REASONS_CHECK_ENABLED)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::getDisableReasonsCheckEnabled.name) {
                "Sending MSG_GET_DISABLE_REASONS_CHECK_ENABLED message is successful. " +
                        "Received mode value: ${msg.arg1}"
            }
        } catch (e: RemoteException) {
            log.warn(this::getDisableReasonsCheckEnabled.name) {
                "Sending MSG_GET_DISABLE_REASONS_CHECK_ENABLED message failed error $e"
            }
        }
    }

    private fun setConradBackendOverrideUrl(url: String) {
        val bundle = Bundle().apply { putString(OBJ_KEY_STRING_1, url) }
        val msg = Message.obtain(null, MSG_SET_CONRAD_BACKEND_OVERRIDE_URL)
        msg.obj = bundle
        try {
            debugService?.send(msg)

            log.info(this::setConradBackendOverrideUrl.name) {
                "Sending MSG_SET_CONRAD_BACKEND_OVERRIDE_URL message is successful. " +
                        "Set url: $url"
            }
        } catch (e: RemoteException) {
            log.warn(this::setConradBackendOverrideUrl.name) {
                "Sending MSG_SET_CONRAD_BACKEND_OVERRIDE_URL message failed error $e"
            }
        }
    }

    private fun getConradBackendOverrideUrl() {
        val msg = Message.obtain(null, MSG_GET_CONRAD_BACKEND_OVERRIDE_URL)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::getConradBackendOverrideUrl.name) {
                "Sending MSG_GET_CONRAD_BACKEND_OVERRIDE_URL message is successful."
            }
        } catch (e: RemoteException) {
            log.warn(this::getConradBackendOverrideUrl.name) {
                "Sending MSG_GET_CONRAD_BACKEND_OVERRIDE_URL message failed error $e"
            }
        }
    }

    private fun setConradTokenOverrideEnabled(mode: Int) {
        val msg = Message.obtain(null, MSG_SET_CONRAD_TOKEN_OVERRIDE_ENABLED)
        msg.arg1 = mode
        try {
            debugService?.send(msg)

            log.info(this::setConradTokenOverrideEnabled.name) {
                "Sending MSG_SET_CONRAD_TOKEN_OVERRIDE_ENABLED message is successful. " +
                        "Sent mode value: ${msg.arg1}"
            }
        } catch (e: RemoteException) {
            log.warn(this::setConradTokenOverrideEnabled.name) {
                "Sending MSG_SET_CONRAD_TOKEN_OVERRIDE_ENABLED message failed error $e"
            }
        }
    }

    private fun getConradTokenOverrideEnabled() {
        val msg = Message.obtain(null, MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::getConradTokenOverrideEnabled.name) {
                "Sending MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED message is successful."
            }
        } catch (e: RemoteException) {
            log.warn(this::getConradTokenOverrideEnabled.name) {
                "Sending MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED message failed error $e"
            }
        }
    }

    private fun setPositionOverride(lat: String, long: String) {
        var latLong = "${lat};${long}"
        if (lat.isEmpty() || long.isEmpty()) {
            latLong = ""
        }
        val bundle = Bundle().apply { putString(OBJ_KEY_STRING_1, latLong) }
        val msg = Message.obtain(null, MSG_SET_POSITION_OVERRIDE)
        msg.obj = bundle
        try {
            debugService?.send(msg)

            log.info(this::setPositionOverride.name, PrivacyFlag.GNSS) {
                "Sending MSG_SET_POSITION_OVERRIDE successful: lat=$lat long=$long"
            }
        } catch (e: RemoteException) {
            log.warn(this::setPositionOverride.name) {
                "Sending MSG_SET_POSITION_OVERRIDE failed error $e"
            }
        }
    }

    private fun getPositionOverride() {
        val msg = Message.obtain(null, MSG_GET_POSITION_OVERRIDE)
        msg.replyTo = messageHandler
        try {
            debugService?.send(msg)

            log.info(this::getPositionOverride.name) {
                "Sending MSG_GET_POSITION_OVERRIDE successful."
            }
        } catch (e: RemoteException) {
            log.warn(this::getPositionOverride.name) {
                "Sending MSG_GET_POSITION_OVERRIDE failed error $e"
            }
        }
    }

    private fun updateViewDemoConfig(config: RadioDemoConfig) {
        binding.bearerListPercentageSeekBar.progress = config.sendBearerList / 10
        binding.metadataPercentageSeekBar.progress = config.sendMetadata / 10
        binding.logoPercentageSeekBar.progress = config.sendLogo / 10
        binding.logoSmallPercentageSeekBar.progress = config.sendSLogo / 10
        binding.streamsPercentageSeekBar.progress = config.sendWebStreams / 10

        binding.minDelayEditText.setText(config.minDelay.toString())
        binding.maxDelayEditText.setText(config.maxDelay.toString())

        binding.lifetimeEditText.setText(config.expireInSeconds.toString())

        binding.errorPercentageSeekBar.progress = config.sendError / 10

        binding.deleteBearerListCheckBox.isChecked = config.deleteBearerList
        binding.deleteMetadataCheckBox.isChecked = config.deleteMetadata
        binding.deleteLogoCheckBox.isChecked = config.deleteLogo
        binding.deleteSmallLogoCheckBox.isChecked = config.deleteSLogo
        binding.deleteStreamsCheckBox.isChecked = config.deleteWebStreams
    }

    private fun updateViewDemoMode(enabled: Boolean) {
        checkSwitch(debugSwitch, debugModeCheckedChangeListener, enabled)

        val disableChildren =
            { group: ViewGroup -> group.children.forEach { it.isEnabled = enabled } }

        binding.applyDemoConfigButton.isEnabled = enabled
        disableChildren(binding.leftColumnLinearLayout)
        disableChildren(binding.middleColumnLinearLayout)
        disableChildren(binding.rightColumnLinearLayout)
    }

    private fun updateViewSimulateDevEnvironment(enabled: Boolean) {
        checkSwitch(simulateDevEnvironmentSwitch, simulateDevEnvironmentChangeListener, enabled)
        updateRuntimeModeSummary()
    }

    private fun updateViewConradBackendOverrideUrl(url: String) {
        val pos = backendUrlList.indexOf(url).takeIf { it >= 0 } ?: 0
        val customUrl = if (backendUrlList.contains(url)) "" else url
        selectSpinnerItem(conradBackendOverrideSpinner, conradBackendOverrideSelectionListener, pos)
        conradBackendCustomUrlEditText.setText(customUrl)
        updateRuntimeModeSummary()
    }

    private fun updateViewConradTokenOverride(enabled: Boolean) {
        checkSwitch(conradTokenOverrideSwitch, conradTokenOverrideChangeListener, enabled)
        binding.conradTokenEditText.isEnabled = enabled
        updateRuntimeModeSummary()
    }

    private fun updateRuntimeModeSummary() {
        val tokenSource = if (pendingConradTokenOverrideEnabled) "token override" else "MagicAdapter"
        val backendUrl = when {
            pendingRuntimeModeIsDev && pendingConradCustomBackendUrl.isNotBlank() -> pendingConradCustomBackendUrl
            pendingConradBackendOverrideUrl.isBlank() -> "Service List"
            else -> pendingConradBackendOverrideUrl
        }
        val serviceListMode = if (pendingRuntimeModeIsDev) "fake" else "real"
        val disableReasonsMode = if (pendingRuntimeModeIsDev) "fake-none" else "real"
        val esmEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(KEY_ESM_ENABLED, false)
        val esmSummary = if (pendingRuntimeModeIsDev) "off on save" else if (esmEnabled) "enabled" else "disabled"

        runtimeModeSummaryTextView.text =
            "Token: $tokenSource | Backend: $backendUrl | ServiceList: $serviceListMode | DisableReasons: $disableReasonsMode | ESM: $esmSummary"
    }

    private fun markConradSettingsDirty() {
        saveConradSettingsButton.isEnabled = true
    }

    private fun applyRuntimePresetToUi(isDev: Boolean) {
        pendingRuntimeModeIsDev = isDev
        pendingConradBackendOverrideUrl = if (isDev) {
            metadataConfig.DEFAULT_CONRAD_BACKEND_URL
        } else {
            ""
        }
        pendingConradCustomBackendUrl = ""
        pendingConradTokenOverrideEnabled = isDev
        if (pendingConradToken.isBlank()) {
            pendingConradToken = metadataConfig.DEFAULT_CONRAD_TOKEN
        }

        suppressConradListeners = true
        updateViewSimulateDevEnvironment(isDev)
        updateViewConradBackendOverrideUrl(pendingConradBackendOverrideUrl)
        updateViewConradTokenOverride(pendingConradTokenOverrideEnabled)
        binding.conradTokenEditText.setText(
            if (pendingConradTokenOverrideEnabled) pendingConradToken else ""
        )
        suppressConradListeners = false
        updateRuntimeModeSummary()
        markConradSettingsDirty()
    }

    private fun saveRuntimeSettings() {
        val tokenFromUi = binding.conradTokenEditText.text.toString().trim()
        val customBackendUrl = conradBackendCustomUrlEditText.text.toString().trim()
        if (tokenFromUi.isNotEmpty()) {
            pendingConradToken = tokenFromUi
        }
        pendingConradCustomBackendUrl = customBackendUrl

        val backendUrlToApply = if (pendingRuntimeModeIsDev && pendingConradCustomBackendUrl.isNotBlank()) {
            pendingConradCustomBackendUrl
        } else {
            pendingConradBackendOverrideUrl
        }

        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putString("conrad_token", pendingConradToken)
            .apply()

        setServiceListCheckEnabled(if (pendingRuntimeModeIsDev) DISABLED else ENABLED)
        setDisableReasonsCheckEnabled(if (pendingRuntimeModeIsDev) DISABLED else ENABLED)
        setConradBackendOverrideUrl(backendUrlToApply)
        setConradTokenOverrideEnabled(if (pendingConradTokenOverrideEnabled) ENABLED else DISABLED)

        val esmEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(KEY_ESM_ENABLED, false)
        if (pendingRuntimeModeIsDev && esmEnabled) {
            disableESM()
            return
        }

        saveConradSettingsButton.isEnabled = false
    }

    private fun updateViewPositionOverride(position: String) {
        var index = positionList.values.indexOf(position)
        if (index == -1) {
            index = 0
        }
        selectSpinnerItem(binding.positionSpinner, positionItemSelectedListener, index)

        var pos = position
        if (position.isEmpty()) {
            pos = ";"
        }
        val latLong = pos.split(";")
        binding.latitudeEditText.setText(latLong[0])
        binding.latitudeEditText.error = null
        binding.longitudeEditText.setText(latLong[1])
        binding.longitudeEditText.error = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)

        debugSwitch = binding.debugModeSwitch
        onlineStateSwitch = binding.onlineStateSwitch

        simulateDevEnvironmentSwitch = binding.simulateDevEnvironmentSwitch
        saveConradSettingsButton = binding.saveConradSettingsButton

        conradBackendOverrideSpinner = binding.conradBackendOverrideSpinner
        conradBackendCustomUrlEditText = binding.conradBackendCustomUrlEditText
        val backendAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, backendList)
        backendAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        conradBackendOverrideSpinner.adapter = backendAdapter
        conradBackendOverrideSpinner.setSelection(0, false);

        conradTokenOverrideSwitch = binding.conradTokenOverrideSwitch

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        pendingConradToken = prefs.getString("conrad_token", metadataConfig.DEFAULT_CONRAD_TOKEN).orEmpty()
        binding.conradTokenEditText.setText(pendingConradToken)
        binding.conradTokenEditText.isEnabled = prefs.getBoolean("conrad_token_override_enabled", false)

        val positionAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, positionList.keys.toList()
        )
        val positionSpinner: Spinner = binding.positionSpinner
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        positionSpinner.adapter = positionAdapter

        conradVariantTextView = binding.conradVariantTextView
        runtimeModeSummaryTextView = binding.runtimeModeSummaryTextView
        apkVersionTextView = binding.apkVersionTextView
        conRadLibVersionTextView = binding.conRadLibVersionTextView
        clientLibVersionTextView = binding.clientLibVersionTextView
        graceNoteStatusTextView = binding.graceNoteStatusTextView

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        onlineStateModel = (activity as DemoActivity).onlineStateModel

        doBindService()

        debugModeCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            val debugModeVal = if (isChecked) ENABLED else DISABLED
            setDebugMode(debugModeVal)
            updateViewDemoMode(isChecked)
        }
        debugSwitch.setOnCheckedChangeListener(debugModeCheckedChangeListener)

        onlineStateCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            onlineStateModel?.changeOnlineLookupState(
                isChecked
            )
        }

        onlineStateModel?.addObserver { _, arg ->
            if (arg is Boolean) {
                onlineStateSwitch.setOnCheckedChangeListener(null)
                onlineStateSwitch.isChecked = arg
                onlineStateSwitch.setOnCheckedChangeListener(onlineStateCheckedChangeListener)
            }
        }

        onlineStateSwitch.setOnCheckedChangeListener(onlineStateCheckedChangeListener)

        onlineStateModel?.let {
            checkSwitch(
                onlineStateSwitch,
                onlineStateCheckedChangeListener,
                it.getOnlineLookupState()
            )
        }

        binding.applyDemoConfigButton.setOnClickListener {
            setDemoConfig()
            val imm = getSystemService(requireActivity(), InputMethodManager::class.java)
            imm?.hideSoftInputFromWindow(requireView().windowToken, 0)
        }

        binding.defaultDemoConfigButton.setOnClickListener {
            setDefaultDemoConfig()
            val imm = getSystemService(requireActivity(), InputMethodManager::class.java)
            imm?.hideSoftInputFromWindow(requireView().windowToken, 0)
        }

        binding.deleteCacheButton.setOnClickListener {
            deleteCache()
        }

        binding.disableESM.setOnClickListener {
            disableESM()
        }

        binding.enableESM.setOnClickListener {
            enableESM()
        }

        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(context)
        val isEsmActive = sharedPreference.getBoolean(KEY_ESM_ENABLED, false)

        if (isEsmActive) {
            binding.disableESM.isEnabled = true
            binding.enableESM.isEnabled = false
        } else {
            binding.disableESM.isEnabled = false
            binding.enableESM.isEnabled = true
        }

        simulateDevEnvironmentChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (suppressConradListeners) {
                return@OnCheckedChangeListener
            }

            applyRuntimePresetToUi(isChecked)
        }
        simulateDevEnvironmentSwitch.setOnCheckedChangeListener(simulateDevEnvironmentChangeListener)

        conradBackendOverrideSelectionListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (suppressConradListeners) {
                    return
                }
                pendingConradBackendOverrideUrl = backendUrlList[position]
                pendingConradCustomBackendUrl = ""
                conradBackendCustomUrlEditText.setText("")
                updateRuntimeModeSummary()
                markConradSettingsDirty()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        conradBackendOverrideSpinner.onItemSelectedListener = conradBackendOverrideSelectionListener

        conradTokenOverrideChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (suppressConradListeners) {
                return@OnCheckedChangeListener
            }

            pendingConradTokenOverrideEnabled = isChecked
            updateViewConradTokenOverride(isChecked)
            markConradSettingsDirty()
        }
        conradTokenOverrideSwitch.setOnCheckedChangeListener(conradTokenOverrideChangeListener)

        binding.conradTokenEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                pendingConradToken = binding.conradTokenEditText.text.toString().trim()
                updateRuntimeModeSummary()
                markConradSettingsDirty()
            }
        }

        conradBackendCustomUrlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                pendingConradCustomBackendUrl = conradBackendCustomUrlEditText.text.toString().trim()
                updateRuntimeModeSummary()
                markConradSettingsDirty()
            }
        }

        saveConradSettingsButton.setOnClickListener {
            saveRuntimeSettings()
        }
        saveConradSettingsButton.isEnabled = false

        positionItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val position = positionList.values.toList()[position]
                updateViewPositionOverride(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.positionSpinner.onItemSelectedListener = positionItemSelectedListener

        binding.latitudeEditText.doAfterTextChanged(
            validateDoubleRangeInput(
                binding.latitudeEditText,
                -90.0,
                90.0
            )
        )
        binding.longitudeEditText.doAfterTextChanged(
            validateDoubleRangeInput(
                binding.longitudeEditText,
                -180.0,
                180.0
            )
        )

        binding.setPositionButton.setOnClickListener {
            if (binding.latitudeEditText.error == null && binding.longitudeEditText.error == null) {
                setPositionOverride(
                    "${binding.latitudeEditText.text}",
                    "${binding.longitudeEditText.text}"
                )
                val latLong = "${binding.latitudeEditText.text};${binding.longitudeEditText.text}"
                if (!positionList.values.contains(latLong)) {
                    selectSpinnerItem(binding.positionSpinner, positionItemSelectedListener, 0)
                }
            }
        }

        binding.clearPositionButton.setOnClickListener {
            setPositionOverride("", "")
            updateViewPositionOverride("")
        }

        val conradVariantLabel = when (metadataConfig.MDS_PROFILE) {
            "legacy-emu" -> "Legacy (emu-generic)"
            "legacy-icc" -> "Legacy (icc)"
            "legacy-no-gn" -> "Legacy (icc, no Gracenote license)"
            else -> "Legacy"
        }

        conradVariantTextView.text = conradVariantLabel
        apkVersionTextView.text = metadataConfig.VERSION_NAME
        clientLibVersionTextView.text = clientConfig.VERSION_NAME
        conRadLibVersionTextView.text = conradConfig.VERSION_NAME
        graceNoteStatusTextView.text = if (metadataConfig.GN_LICENSE_ENABLED) "Available (Licensed)" else "Not Available"
        updateRuntimeModeSummary()
    }

    /**
     * Re-queries the current configuration from [DebugService] and repopulates the UI.
     *
     * Called when the Settings tab becomes the selected page. Switching tabs can leave the
     * fragment showing stale values because the service state may have changed while the tab
     * was off-screen; this forces a refresh. If the service is not yet bound the bind is
     * (re)initiated and [serviceConnection] performs the queries once connected.
     */
    fun refreshSettings() {
        if (!isAdded || view == null) {
            return
        }
        if (debugService != null) {
            getDebugMode()
            getDemoConfig()
            getServiceListCheckEnabled()
            getConradBackendOverrideUrl()
            getConradTokenOverrideEnabled()
            getPositionOverride()
        } else {
            doBindService()
        }
    }

    override fun onPause() {
        super.onPause()

        onlineStateModel?.deleteObservers()

        binding.applyDemoConfigButton.setOnClickListener(null)
        binding.defaultDemoConfigButton.setOnClickListener(null)
        saveConradSettingsButton.setOnClickListener(null)

        debugSwitch.setOnCheckedChangeListener(null)
        simulateDevEnvironmentSwitch.setOnCheckedChangeListener(null)

        conradBackendOverrideSpinner.onItemSelectedListener = null
        conradTokenOverrideSwitch.setOnCheckedChangeListener(null)
        conradBackendCustomUrlEditText.setOnFocusChangeListener(null)

        binding.positionSpinner.onItemSelectedListener = null
        binding.setPositionButton.setOnClickListener(null)
        binding.clearPositionButton.setOnClickListener(null)

        doUnbindService()
    }

    private fun checkSwitch(
        switch: Switch,
        listener: CompoundButton.OnCheckedChangeListener,
        checked: Boolean
    ) {
        switch.setOnCheckedChangeListener(null)
        switch.isChecked = checked
        switch.setOnCheckedChangeListener(listener)
    }

    private fun selectSpinnerItem(
        spinner: Spinner,
        listener: AdapterView.OnItemSelectedListener,
        position: Int
    ) {
        spinner.onItemSelectedListener = null
        spinner.setSelection(position, false)
        spinner.onItemSelectedListener = listener
    }

    private fun validateDoubleRangeInput(
        editText: EditText,
        min: Double,
        max: Double
    ) = { editable: Editable? ->
        val text = editable.toString()
        var double = 0.0
        try {
            if (text.isNotEmpty() && text != "-") {
                double = text.toDouble()
            }
        } catch (e: NumberFormatException) {
            editText.error = "invalid format"
        }

        if (double < min || double > max) {
            editText.error = "Out of range (${min.toInt()} to ${max.toInt()})"
        }
    }
}

