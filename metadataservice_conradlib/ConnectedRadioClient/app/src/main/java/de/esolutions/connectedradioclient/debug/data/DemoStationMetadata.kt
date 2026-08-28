package de.esolutions.connectedradioclient.debug.data

import de.esolutions.connectedradioclient.data.ProviderIdentifier
import de.esolutions.connectedradioclient.data.StationMetadata

class DemoStationMetadata {

    /**
     * Returns demo station metadata.
     */
    fun getDemoObject(
        providerIdentifiers: List<ProviderIdentifier>,
        bearerString: String
    ): StationMetadata {
        return StationMetadata(
            providerIdentifiers,
            "$bearerString - shortName",
            "$bearerString - mediumName",
            "$bearerString - longName",
            "$bearerString - description",
            listOf("genre1", "genre2")
        )
    }
}