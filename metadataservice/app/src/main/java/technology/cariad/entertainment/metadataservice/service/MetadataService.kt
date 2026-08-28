package technology.cariad.entertainment.metadataservice.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.MainThread
import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.common.log.logger
import kotlinx.coroutines.ExperimentalCoroutinesApi

@MainThread
@ExperimentalCoroutinesApi
open class MetadataService : Service() {

    private val log by logger()

    lateinit var service: MetadataServiceImpl
    private lateinit var legacyService: LegacyMetadataServiceAdapter

    /**
     * @see [Service.onCreate]
     */
    override fun onCreate() {
        log.info(this::onCreate.name) { "instantiate service" }

        service = MetadataServiceImpl()
        legacyService = LegacyMetadataServiceAdapter(service)
    }

    /**
     * Note: called only once when first client binds
     * @see [Service.onBind]
     */
    override fun onBind(intent: Intent): IBinder? {
        log.info(this::onBind.name) { "client connected" }

        return if (intent.action == BuildConfig.LEGACY_METADATA_SERVICE_AIDL_ACTION) {
            log.info(this::onBind.name) { "serve legacy AIDL contract" }
            legacyService
        } else {
            service
        }
    }

    /**
     * Note: called only once when last client unbinds
     * @see [Service.onUnbind]
     */
    override fun onUnbind(intent: Intent): Boolean {
        log.info(this::onUnbind.name) { "last client disconnected" }

        return false
    }

    /**
     * @see [Service.onDestroy]
     */
    override fun onDestroy() {
        log.info(this::onDestroy.name) { "stop service - enter" }

        service.cancelRequests()

        log.info(this::onDestroy.name) { "stop service - exit" }
    }
}
