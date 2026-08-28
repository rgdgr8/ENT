package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.backend.lookup.result.DataObject

data class StationMetadata(
    val providerIdentifiers: List<ProviderIdentifier>,
    val shortName: String?,
    val mediumName: String?,
    val longName: String?,
    val description: String?,
    val genres: List<String>
) {

    companion object {
        /**
         * Create a [StationMetadata] object from the given [providerIdentifiers] and backend [data]
         */
        fun createFromBackendData(
            providerIdentifiers: List<ProviderIdentifier>,
            data: DataObject
        ): StationMetadata {
            return StationMetadata(
                providerIdentifiers,
                data.shortName,
                data.mediumName,
                data.longName,
                data.description,
                listOf()
            )
        }
    }
}