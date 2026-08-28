/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace e.solutions framework dependencies.
 */
package de.esolutions.android.framework.clients

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.android.framework.clients.IServiceCallback
 */
interface IServiceCallback {
    fun connectionEstablished(serviceName: String, instanceId: Int)
    fun connectionDisconnected(serviceName: String, instanceId: Int)
    fun error(serviceName: String, instanceId: Int, message: String)
}
