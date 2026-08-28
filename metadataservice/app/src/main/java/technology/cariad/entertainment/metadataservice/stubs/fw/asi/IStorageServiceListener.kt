/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace e.solutions framework dependencies.
 */
package technology.cariad.entertainment.metadataservice.stubs.fw.asi

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.fw.android.comm.asi.diag.pers.IStorageServiceListener
 * 
 * This is a stub interface for the ASI Storage Service Listener.
 * In local builds, no actual ASI communication occurs.
 */
interface IStorageServiceListener {
    fun publishValue(value: OptionalValue?)
    fun setReply(blobId: Int, result: OpResult?)
    fun publishValues(values: OptionalValueOneDimArrayObjectWrapper?)
    fun getReply(blobId: Int, value: OptionalValue?)
    fun get0Reply(value: OptionalValue?)
    fun get1Reply(values: OptionalValueOneDimArrayObjectWrapper?)

    /**
     * Stub class that provides a default implementation
     */
    abstract class Stub : IStorageServiceListener {
        override fun publishValue(value: OptionalValue?) {}
        override fun setReply(blobId: Int, result: OpResult?) {}
        override fun publishValues(values: OptionalValueOneDimArrayObjectWrapper?) {}
        override fun getReply(blobId: Int, value: OptionalValue?) {}
        override fun get0Reply(value: OptionalValue?) {}
        override fun get1Reply(values: OptionalValueOneDimArrayObjectWrapper?) {}
    }
}

