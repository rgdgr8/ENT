// LOCAL BUILD STUB: IPositionInformation
package com.volkswagenag.nav.positioninformation

import android.os.IBinder

/**
 * Stub for IPositionInformation from navigation AIDL.
 * This is a placeholder for local builds without navigation AIDL files.
 */
interface IPositionInformation {
    fun registerPositionInformation(intervalMs: Long, callback: IPositionInformationCallback)
    fun unRegisterPositionInformation(callback: IPositionInformationCallback)

    /**
     * Stub companion object for AIDL interface conversion
     */
    object Stub {
        /**
         * Convert a binder to the interface.
         * LOCAL BUILD: Returns a stub implementation.
         */
        @JvmStatic
        fun asInterface(binder: IBinder?): IPositionInformation? {
            return if (binder != null) StubImpl() else null
        }
    }

    /**
     * Stub implementation that does nothing
     */
    private class StubImpl : IPositionInformation {
        override fun registerPositionInformation(intervalMs: Long, callback: IPositionInformationCallback) {
            // Stub: No-op in local build
            // Could optionally call callback.onError(0) to indicate unavailable
        }

        override fun unRegisterPositionInformation(callback: IPositionInformationCallback) {
            // Stub: No-op in local build
        }
    }
}
