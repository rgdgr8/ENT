/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Modifications:
 * - 2026-02-06: Made ASI Admin failure non-fatal for local builds without eso.fw
 */
package technology.cariad.entertainment.metadataservice.common.codingprovider.service

import android.content.Context
import de.esolutions.android.framework.clients.AsiAdmin
import de.esolutions.android.framework.clients.types.OneDimShortArrayWrapper
import technology.cariad.entertainment.metadataservice.stubs.fw.asi.StorageClientAdapter
import technology.cariad.entertainment.metadataservice.common.codingprovider.data.CodingConst
import technology.cariad.entertainment.metadataservice.common.log.logger

internal class StorageConnectionImpl(private val context: Context) : IStorageConnection {
    private val log by logger()

    private var isStarted = false
    private var serviceListener: StorageServiceListener? = null
    private var adapter: StorageClientAdapter? = null

    /**
     * Implementation of interface StorageConnection
     */
    override fun start(observer: IStorageConnectionObserver): Boolean {
        if (isStarted) {
            log.warn(this::start.name) {
                "already started"
            }
            return false
        }

        // LOCAL BUILD: Skip ASIAdmin - it conflicts with system framework on real devices
        // The system's de.esolutions.fw.android.stable_aidls.jar gets loaded instead of our stub
        val asiAdminStarted = startASIAdmin()
        if (!asiAdminStarted) {
            log.warn(this::start.name) {
                "startASIAdmin failed - continuing anyway for LOCAL BUILD"
            }
            // Don't return false - continue without ASI Admin for local builds
        }

        if (!startAdapter(observer)) {
            if (asiAdminStarted) {
                stopASIAdmin()
            }
            log.warn(this::start.name) {
                "startAdapter failed"
            }
            return false
        }

        log.info(this::start.name) {
            "connection requested"
        }
        isStarted = true
        return isStarted
    }

    override fun stop() {
        if (!isStarted) {
            log.warn(this::stop.name) {
                "was not started"
            }
            return
        }

        stopAdapter()
        stopASIAdmin()

        log.info(this::stop.name) {
            "connection stopped"
        }
        isStarted = false
    }

    override fun subscribeBlob(blobID: Int): Boolean {
        if (!isStarted) {
            log.warn(this::subscribeBlob.name) {
                "not started"
            }
            return false
        }

        return adapter?.let {
            try {
                it.api().subscribe(blobID)
            } catch (t: Throwable) {
                log.warn(this::subscribeBlob.name) {
                    "blobID: $blobID " +
                        "exception caught: $t"
                }
            }
            true
        } ?: run {
            log.warn(this@StorageConnectionImpl::subscribeBlob.name) {
                "adapter is set to null"
            }
            false
        }
    }

    override fun getBlob(blobID: Int): Boolean {
        if (!isStarted) {
            log.warn(this::getBlob.name) {
                "not started"
            }
            return false
        }

        return adapter?.let {
            try {
                it.api().get0(blobID)
            } catch (t: Throwable) {
                log.warn(this::getBlob.name) {
                    "blobID: $blobID " +
                        "exception caught: $t"
                }
            }
            true
        } ?: run {
            log.warn(this@StorageConnectionImpl::getBlob.name) {
                "adapter is set to null"
            }
            false
        }
    }

    override fun setBlob(blobID: Int, blobValue: ShortArray): Boolean {
        if (!isStarted) {
            log.warn(this::setBlob.name) {
                "not started"
            }
            return false
        }

        return adapter?.let {
            try {
                it.api().set(blobID, OneDimShortArrayWrapper(blobValue))
            } catch (t: Throwable) {
                log.warn(this::setBlob.name) {
                    "blobID: $blobID " +
                        "exception caught: $t"
                }
            }
            true
        } ?: run {
            log.warn(this@StorageConnectionImpl::setBlob.name) {
                "adapter is set to null"
            }
            false
        }
    }

    private fun startASIAdmin(): Boolean {
        try {
            AsiAdmin.start(context)
            return true
        } catch (t: Throwable) {
            log.error(this::startASIAdmin.name) {
                "exception received: $t"
            }
        }
        return false
    }

    private fun stopASIAdmin() {
        try {
            AsiAdmin.shutdown()
        } catch (t: Throwable) {
            log.error(this::stopASIAdmin.name) {
                "exception received: $t"
            }
        }
    }

    private fun startAdapter(observer: IStorageConnectionObserver): Boolean =
        run {
            serviceListener = StorageServiceListener(observer).also { listener ->
                adapter = StorageClientAdapter(
                    CodingConst.ASI_STORAGE_INSTANCE_ID,
                    listener
                ).apply {
                    connectService(listener)
                }
            }
            true
        }

    private fun stopAdapter() =
        run {
            adapter?.let {
                it.disconnectService()
            }
            adapter = null
            serviceListener = null
        }
}
