package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.backend.lookup.result.DataObject
import de.esolutions.connectedradioclient.data.backend.lookup.result.ProviderObject
import de.esolutions.connectedradioclient.data.enums.Origin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StationMetadataTest {

    @Test
    fun parse_correct_from_backend_data() {
        val providerIdentifiers = listOf(ProviderIdentifier(Origin.GRACENOTE, "externalId"))
        val providerObjects = listOf(ProviderObject("", ""))
        val dataObject =
            DataObject(
                "shotName",
                "mediumName",
                "longName",
                "description",
                providerObjects,
                listOf(),
                listOf()
            )
        val stationMetadata = StationMetadata.createFromBackendData(providerIdentifiers, dataObject)
        assertEquals(providerIdentifiers, stationMetadata.providerIdentifiers)
        assertEquals("shotName", stationMetadata.shortName)
        assertEquals("mediumName", stationMetadata.mediumName)
        assertEquals("longName", stationMetadata.longName)
        assertEquals("description", stationMetadata.description)
        assertEquals(0, stationMetadata.genres.size)
    }
}