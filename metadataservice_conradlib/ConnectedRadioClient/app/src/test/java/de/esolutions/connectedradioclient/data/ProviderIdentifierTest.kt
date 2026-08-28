package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.backend.lookup.result.ProviderObject
import de.esolutions.connectedradioclient.data.enums.Origin
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ProviderIdentifierTest {

    @Test
    fun parse_correct_from_backend_data() {
        val providerObject = ProviderObject("gracenote", "id")
        val providerIdentifier = ProviderIdentifier.createFromBackendData(providerObject)!!

        assertEquals(Origin.GRACENOTE, providerIdentifier.origin)
        assertEquals("id", providerIdentifier.externalId)
    }

    @Test
    @DisplayName("parsing_returns_null_if_provider_is_unknown[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-283709,OI_Global_CLU51:OIG-55568#]")
    fun parsing_returns_null_if_provider_is_unknown() {
        val providerObject = ProviderObject("::unknown_provider::", "id")
        val providerIdentifier = ProviderIdentifier.createFromBackendData(providerObject)

        assertNull(providerIdentifier)
    }

    @Test
    fun parse_correct_list_from_backend_data() {
        val providerObjects = listOf(ProviderObject("gracenote", "id"))
        val providerIdentifiers = ProviderIdentifier.createListFromBackendData(providerObjects)

        providerIdentifiers.forEach {
            assertEquals(Origin.GRACENOTE, it.origin)
            assertEquals("id", it.externalId)
        }
    }

    @Test
    @DisplayName("parsing_returns_null_if_provider_is_unknown[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-283709,OI_Global_CLU51:OIG-55568#]")
    fun parsing_returns_empty_list_if_providers_are_unknown() {
        val providerObjects = listOf(ProviderObject("::unknown_provider::", "id"))
        val providerIdentifiers = ProviderIdentifier.createListFromBackendData(providerObjects)

        assertTrue(providerIdentifiers.isEmpty())
    }
}