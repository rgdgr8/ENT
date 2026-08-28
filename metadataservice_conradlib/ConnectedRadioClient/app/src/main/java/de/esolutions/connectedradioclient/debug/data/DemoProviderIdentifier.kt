package de.esolutions.connectedradioclient.debug.data

import de.esolutions.connectedradioclient.data.DEBUG_STRING
import de.esolutions.connectedradioclient.data.ProviderIdentifier
import de.esolutions.connectedradioclient.data.enums.Origin

class DemoProviderIdentifier {

    /**
     * Returns demo provider identifier.
     */
    fun getDemoObject(): ProviderIdentifier {
        return ProviderIdentifier(
            Origin.RADIOPLAYER,
            DEBUG_STRING
        )
    }
}