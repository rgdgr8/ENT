package technology.cariad.entertainment.metadataservice.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import technology.cariad.entertainment.metadataservice.common.log.PrivacyFlag
import technology.cariad.entertainment.metadataservice.MetadataServiceApp
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.position.FakePositionProvider
import technology.cariad.entertainment.metadataservice.common.position.PositionInformation
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.service.config.RadioDemoConfig
import technology.cariad.entertainment.metadataservice.util.deepCopy
import technology.cariad.entertainment.metadataservice.util.toBoolean
import technology.cariad.entertainment.metadataservice.util.toInt
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

/**
 * Key for first bundle argument.
 */
const val DATA_ARG_1 = "arg1"

/**
 * Key for second bundle argument.
 */
const val DATA_ARG_2 = "arg2"

/**
 * Key for first string in bundle passed as [Message.obj].
 */
const val OBJ_KEY_STRING_1 = "string1"

/**
 * Message to retrieve the current state of the debug mode from the service.
 * This message code is used for both the request and the response.
 * The client has to pass a response messenger in [Message.replyTo].
 * The service responds with [Message.arg1] set to the current debug mode state.
 * Allowed values for the state are 1 (enabled) and 0 (disabled)
 */
const val MSG_GET_DEBUG_MODE = 0

/**
 * Message to change the current state of the debug mode of the service.
 * This message code is used for both the request and the response.
 * The client provides the new state in [Message.arg1] and passes a response messenger in [Message.replyTo].
 * The service responds with [Message.arg1] set to the new state of the debug mode. If the service
 * wasn't able to change the state as requested, the old state is returned.
 * Allowed values for the state are 1 (enabled) and 0 (disabled)
 */
const val MSG_SET_DEBUG_MODE = 1

/**
 * Message to retrieve the current demo config of the connected radio client.
 * This message code is used for both the request and the response.
 * The client has to pass a response messenger in [Message.replyTo].
 * The service responds with the current demo mode config of type [RadioDemoConfig] obtainable
 * from [Message.getData] using key [DATA_ARG_1]".
 * The bundle key is "key" and the
 */
const val MSG_GET_CONRAD_DEMO_CONFIG = 2

/**
 * Message to change the current demo config of the connected radio client..
 * This message code is used for both the request and the response.
 * The client provides the new config of type [RadioDemoConfig] to [Message.setData] using key [DATA_ARG_1].
 * The client has to pass a response messenger in [Message.replyTo].
 * The service replies only with an acknowledgement, without any additional parameters.
 */
const val MSG_SET_CONRAD_DEMO_CONFIG = 3

/**
 * Message to retrieve the current state of the service list check.
 * This message code is used for both the request and the response.
 * The client has to pass a response messenger in [Message.replyTo].
 * The service responds with [Message.arg1] set to the current state.
 * Allowed values for the state are 1 (enabled) and 0 (disabled)
 */
const val MSG_GET_SERVICE_LIST_CHECK_ENABLED = 4

/**
 * Message to change the current state of the service list check.
 * This message code is used for both the request and the response.
 * The client provides the new state in [Message.arg1] and passes a response messenger in [Message.replyTo].
 * The service responds with [Message.arg1] set to the new state of the debug mode. If the service
 * wasn't able to change the state as requested, the old state is returned.
 * Allowed values for the state are 1 (enabled) and 0 (disabled)
 */
const val MSG_SET_SERVICE_LIST_CHECK_ENABLED = 5

/**
 * Message to retrieve the current state of the disable reasons check.
 * This message code is used for both the request and the response.
 * The client has to pass a response messenger in [Message.replyTo].
 * The service responds with [Message.arg1] set to the current state.
 * Allowed values for the state are 1 (enabled) and 0 (disabled)
 */
const val MSG_GET_DISABLE_REASONS_CHECK_ENABLED = 6

/**
 * Message to change the current state of the disable reasons check.
 * This message code is used for both the request and the response.
 * The client provides the new state in [Message.arg1] and passes a response messenger in [Message.replyTo].
 * The service responds with [Message.arg1] set to the new state of the debug mode. If the service
 * wasn't able to change the state as requested, the old state is returned.
 * Allowed values for the state are 1 (enabled) and 0 (disabled)
 */
const val MSG_SET_DISABLE_REASONS_CHECK_ENABLED = 7

/**
 * Message to delete the cache.
 */
const val MSG_DELETE_CACHE = 8

/**
 * Message to change the current conrad backend override url.
 * The client provides the new url in a [Bundle] under [OBJ_KEY_STRING_1] as [Message.obj].
 */
const val MSG_SET_CONRAD_BACKEND_OVERRIDE_URL = 9

/**
 * Message to retrieve the current conrad backend override url.
 * This message code is used for both the request and the response.
 * The client has to pass a response messenger in [Message.replyTo].
 * The service responds with [Message.obj] containing a [Bundle].
 * The url is stored within the bundle under [OBJ_KEY_STRING_1].
 */
const val MSG_GET_CONRAD_BACKEND_OVERRIDE_URL = 10

/**
 * Message to change the current state of the conrad token override.
 * The client provides the new state in [Message.arg1].
 * Allowed values for the state are 1 (enabled) and 0 (disabled)
 */
const val MSG_SET_CONRAD_TOKEN_OVERRIDE_ENABLED = 11

/**
 * Message to retrieve the current state of the conrad token override.
 * This message code is used for both the request and the response.
 * The client has to pass a response messenger in [Message.replyTo].
 * The service responds with [Message.arg1] set to the current state.
 * Allowed values for the state are 1 (enabled) and 0 (disabled)
 */
const val MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED = 12

/**
 * Message to set the current override position.
 * The client provides the new url in a [Bundle] under [OBJ_KEY_STRING_1] as [Message.obj].
 */
const val MSG_SET_POSITION_OVERRIDE = 13

/**
 * Message to retrieve the current override position.
 * This message code is used for both the request and the response.
 * The client has to pass a response messenger in [Message.replyTo].
 * The service responds with [Message.obj] containing a [Bundle].
 * The position is stored within the bundle under [OBJ_KEY_STRING_1].
 */
const val MSG_GET_POSITION_OVERRIDE = 14

/**
 * Message to disable  the ESM cache.
 */
const val MSG_DISABLE_ESM_CACHE = 15

/**
 * Message to enable  the ESM cache.
 */
const val MSG_ENABLE_ESM_CACHE = 16

/**
 * A bound service providing a simple [Messenger] based interface for dynamic configuration changes
 * of the MetadataService.
 */
class DebugService : Service() {

    val log by logger()

    /**
     * Definition of the [Handler] for incoming messages from clients.
     */
    internal class MessageHandler : Handler() {

        val log by logger(DebugService::class)

        /**
         * @param msg  Incoming message.
         * @see Handler.handleMessage
         */
        override fun handleMessage(msg: Message) {

            when (msg.what) {
                MSG_GET_DEBUG_MODE -> handleGetDebugMode(msg)
                MSG_SET_DEBUG_MODE -> handleSetDebugMode(msg)
                MSG_GET_CONRAD_DEMO_CONFIG -> handleGetDemoConfig(msg)
                MSG_SET_CONRAD_DEMO_CONFIG -> handleSetDemoConfig(msg)
                MSG_GET_SERVICE_LIST_CHECK_ENABLED -> handleGetServiceListCheckEnabled(msg)
                MSG_SET_SERVICE_LIST_CHECK_ENABLED -> handleSetServiceListCheckEnabled(msg)
                MSG_GET_DISABLE_REASONS_CHECK_ENABLED -> handleGetDisableReasonsCheckEnabled(msg)
                MSG_SET_DISABLE_REASONS_CHECK_ENABLED -> handleSetDisableReasonsCheckEnabled(msg)
                MSG_DELETE_CACHE -> handleDeleteCache(msg)
                MSG_GET_CONRAD_BACKEND_OVERRIDE_URL -> handleGetConradBackendOverrideUrl(msg)
                MSG_SET_CONRAD_BACKEND_OVERRIDE_URL -> handleSetConradBackendOverrideUrl(msg)
                MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED -> handleGetConradTokenOverrideEnabled(msg)
                MSG_SET_CONRAD_TOKEN_OVERRIDE_ENABLED -> handleSetConradTokenOverrideEnabled(msg)
                MSG_GET_POSITION_OVERRIDE -> handleGetPositionOveride(msg)
                MSG_SET_POSITION_OVERRIDE -> handleSetPositionOveride(msg)
                MSG_DISABLE_ESM_CACHE -> handleDisableESM(msg)
                MSG_ENABLE_ESM_CACHE -> handleEnableESM(msg)
                else -> super.handleMessage(msg)
            }
        }

        private fun handleGetDebugMode(msg: Message) {
            val enabled = MetadataServiceApp.config.demoModeEnabled

            val reply = Message.obtain(null, MSG_GET_DEBUG_MODE)
            reply.arg1 = enabled.toInt()
            msg.replyTo.send(reply)

            log.info(this::handleGetDebugMode.name) {
                "MSG_GET_DEBUG_MODE received. Reply with enabled=$enabled."
            }
        }

        private fun handleSetDebugMode(msg: Message) {
            log.info(this::handleSetDebugMode.name) { "MSG_SET_DEBUG_MODE: ${msg.arg1}" }

            val enabled = msg.arg1.toBoolean()

            MetadataServiceApp.config.demoModeEnabled = enabled

            val reply = Message.obtain(null, MSG_SET_DEBUG_MODE)
            reply.arg1 = msg.arg1
            msg.replyTo.send(reply)

            log.info(this::handleSetDebugMode.name) {
                "MSG_SET_DEBUG_MODE received. Reply with enabled=$enabled."
            }
        }

        private fun handleGetDemoConfig(msg: Message) {
            log.info(this::handleGetDemoConfig.name) { "MSG_GET_CONRAD_DEMO_CONFIG" }

            val reply = Message.obtain(null, MSG_GET_CONRAD_DEMO_CONFIG)
            reply.data.putParcelable(DATA_ARG_1, MetadataServiceApp.config.demoConfig.deepCopy())
            msg.replyTo.send(reply)
        }

        private fun handleSetDemoConfig(msg: Message) {
            log.info(this::handleSetDemoConfig.name) { "MSG_SET_CONRAD_DEMO_CONFIG" }

            msg.data.classLoader = javaClass.classLoader
            val config = msg.data.getParcelable<RadioDemoConfig>(DATA_ARG_1)

            config?.let {
                MetadataServiceApp.config.demoConfig = it
                MetadataServiceApp.domainComponent["demo"]!!.conRadClient()
                    .setDebugConfig(it.toDebugConfig())
            }

            val reply = Message.obtain(null, MSG_SET_CONRAD_DEMO_CONFIG)
            msg.replyTo.send(reply)

            log.info(this::handleSetDemoConfig.name) {
                "MSG_SET_CONRAD_DEMO_CONFIG received. Send ack."
            }
        }

        private fun handleGetServiceListCheckEnabled(msg: Message) {
            val enabled = MetadataServiceApp.config.serviceListCheckEnabled

            val reply = Message.obtain(null, MSG_GET_SERVICE_LIST_CHECK_ENABLED)
            reply.arg1 = enabled.toInt()
            msg.replyTo.send(reply)

            log.info(this::handleGetServiceListCheckEnabled.name) {
                "MSG_GET_SERVICE_LIST_CHECK_ENABLED received. Reply with enabled=$enabled."
            }
        }

        private fun handleSetServiceListCheckEnabled(msg: Message) {
            val enabled = msg.arg1.toBoolean()

            MetadataServiceApp.config.serviceListCheckEnabled = enabled

            runBlocking {
                MetadataServiceApp.appComponent.getServiceListClient()
                    .fakeServiceList(!enabled)
            }

            val reply = Message.obtain(null, MSG_SET_SERVICE_LIST_CHECK_ENABLED)
            reply.arg1 = msg.arg1
            msg.replyTo.send(reply)

            log.info(this::handleSetServiceListCheckEnabled.name) {
                "MSG_SET_SERVICE_LIST_CHECK_ENABLED received. Reply with enabled=$enabled."
            }
        }

        private fun handleGetDisableReasonsCheckEnabled(msg: Message) {
            val enabled = MetadataServiceApp.config.disableReasonsCheckEnabled

            val reply = Message.obtain(null, MSG_GET_DISABLE_REASONS_CHECK_ENABLED)
            reply.arg1 = enabled.toInt()
            msg.replyTo.send(reply)

            log.info(this::handleGetDisableReasonsCheckEnabled.name) {
                "MSG_GET_DISABLE_REASONS_CHECK_ENABLED received. Reply with enabled=$enabled."
            }
        }

        private fun handleSetDisableReasonsCheckEnabled(msg: Message) {
            val enabled = msg.arg1.toBoolean()

            MetadataServiceApp.config.disableReasonsCheckEnabled = enabled

            runBlocking {
                MetadataServiceApp.appComponent.getDisableReasonsClient()
                    .fakeDisabledReasons(!enabled)
            }

            val reply = Message.obtain(null, MSG_SET_DISABLE_REASONS_CHECK_ENABLED)
            reply.arg1 = msg.arg1
            msg.replyTo.send(reply)

            log.info(this::handleSetDisableReasonsCheckEnabled.name) {
                "MSG_SET_DISABLE_REASONS_CHECK_ENABLED received. Reply with enabled=$enabled."
            }
        }

        private fun handleDisableESM(msg: Message) {
            MetadataServiceApp.config.setEsmActivationState(false)
            handleDeleteCache(msg)
        }

        private fun handleEnableESM(msg: Message) {
            MetadataServiceApp.config.setEsmActivationState(true)
            handleDeleteCache(msg)
        }

        private fun handleDeleteCache(msg: Message) {
            val path = MetadataServiceApp.appComponent.getFileStorageRoot()
            val rootDir = File(path)
            var success = true
            val list = rootDir.list()
            list?.forEach {
                val file = File("$path/$it")
                if (file.isDirectory) {
                    success = success && file.deleteRecursively()
                } else file.delete()
            }
            exitProcess(0)
        }

        private fun handleGetConradBackendOverrideUrl(msg: Message) {
            val url = MetadataServiceApp.config.conradBackendOverrideUrl
            val bundle = Bundle().apply { putString(OBJ_KEY_STRING_1, url) }

            val reply = Message.obtain(null, MSG_GET_CONRAD_BACKEND_OVERRIDE_URL)
            reply.obj = bundle
            msg.replyTo.send(reply)

            log.info(this::handleGetConradBackendOverrideUrl.name) {
                "MSG_GET_CONRAD_BACKEND_OVERRIDE_URL received. Reply with url=$url."
            }
        }

        private fun handleSetConradBackendOverrideUrl(msg: Message) {
            val bundle = msg.obj as? Bundle
            val url = bundle?.getString(OBJ_KEY_STRING_1) ?: ""

            MetadataServiceApp.config.conradBackendOverrideUrl = url

            //trigger update of service list
            runBlocking {
                MetadataServiceApp.appComponent.getServiceListClient()
                    .fakeServiceList(!MetadataServiceApp.config.serviceListCheckEnabled)
            }

            log.info(this::handleSetConradBackendOverrideUrl.name) {
                "MSG_SET_CONRAD_BACKEND_OVERRIDE_URL received: url=$url"
            }
        }

        private fun handleGetConradTokenOverrideEnabled(msg: Message) {
            val enabled = MetadataServiceApp.config.conradTokenOverrideEnabled

            val reply = Message.obtain(null, MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED)
            reply.arg1 = enabled.toInt()
            msg.replyTo.send(reply)

            log.info(this::handleGetConradTokenOverrideEnabled.name) {
                "MSG_GET_CONRAD_TOKEN_OVERRIDE_ENABLED received. Reply with enabled=$enabled."
            }
        }

        private fun handleSetConradTokenOverrideEnabled(msg: Message) {
            val enabled = msg.arg1.toBoolean()

            with(MetadataServiceApp.config) {
                conradTokenOverrideEnabled = enabled
                val token = if (enabled) conradToken else null
                log.info(this@MessageHandler::handleSetConradTokenOverrideEnabled.name) {
                    "Setting token on ConRadClient: enabled=$enabled, token=${token?.take(20) ?: "null"}..."
                }
                MetadataServiceApp.domainComponent["live"]?.conRadClient()?.setToken(token)
                    ?: log.warn(this@MessageHandler::handleSetConradTokenOverrideEnabled.name) {
                        "domainComponent[live] or conRadClient() is null - token NOT applied!"
                    }
            }

            log.info(this::handleSetConradTokenOverrideEnabled.name) {
                "MSG_SET_CONRAD_TOKEN_OVERRIDE_ENABLED received. Reply with enabled=$enabled."
            }
        }

        private fun handleGetPositionOveride(msg: Message) {
            val pos = MetadataServiceApp.config.positionOverride
            val bundle = Bundle().apply { putString(OBJ_KEY_STRING_1, pos) }

            val reply = Message.obtain(null, MSG_GET_POSITION_OVERRIDE)
            reply.obj = bundle
            msg.replyTo.send(reply)

            log.info(this::handleGetPositionOveride.name, PrivacyFlag.GNSS) {
                "MSG_GET_POSITION_OVERRIDE received. Reply with pos=$pos."
            }
        }

        private fun handleSetPositionOveride(msg: Message) {
            val bundle = msg.obj as? Bundle
            val pos = bundle?.getString(OBJ_KEY_STRING_1) ?: ""

            MetadataServiceApp.config.positionOverride = pos

            //trigger update of position
            val fakePos = if (MetadataServiceApp.config.positionOverride.isNotEmpty()) {
                val (lat, long) = MetadataServiceApp.config.positionOverride
                    .split(";")
                    .map { it.toDouble() }
                PositionInformation(lat, long)
            } else {
                null
            }
            (MetadataServiceApp.appComponent.getPositionProvider() as? FakePositionProvider)
                ?.fakePosition?.tryEmit(Success(fakePos))

            log.info(this::handleSetPositionOveride.name, PrivacyFlag.GNSS) {
                "MSG_SET_POSITION_OVERRIDE received: pos=$pos"
            }
        }
    }

    private lateinit var messenger: Messenger

    override fun onCreate() {
        super.onCreate()

        log.debug(this::onCreate.name) { "called" }
    }

    override fun onBind(intent: Intent?): IBinder? {

        if (MetadataServiceApp.isInitialized) {

            messenger = Messenger(MessageHandler())

            log.debug(this::onBind.name) { "called" }

            return messenger.binder
        }

        log.debug(this::onBind.name) { "app not initialized yet" }

        return null
    }
}
