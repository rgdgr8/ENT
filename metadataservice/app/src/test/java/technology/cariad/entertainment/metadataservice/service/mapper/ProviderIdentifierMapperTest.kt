package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataserviceclient.api.ProviderIdentifier
import technology.cariad.entertainment.metadataservice.sampledata.SampleProviderIdentifier
import org.junit.Assert
import org.junit.Test

class ProviderIdentifierMapperTest {

    @Test
    fun mapValue_shouldConvertProviderIdentifierEntityToProviderIdentifier() {
        val providerIdentifier =
            ProviderIdentifierMapper().mapValue(SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1)

        Assert.assertEquals(
            SampleProviderIdentifier.PROVIDER_IDENTIFIER_GRACENOTE_1,
            providerIdentifier
        )
    }

    @Test
    fun mapList_shouldConvertProviderIdentifierEntitiesToProviderIdentifiers() {
        val providerIdentifierEntities = listOf(
            SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1,
            SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1
        )

        val providerIdentifiers = ProviderIdentifierMapper().mapList(providerIdentifierEntities)

        val expectedProviderIdentifiers =
            listOf(
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_GRACENOTE_1,
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_GRACENOTE_1
            )

        Assert.assertEquals(expectedProviderIdentifiers, providerIdentifiers)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val emptyList = ProviderIdentifierMapper().mapList(listOf())

        Assert.assertEquals(listOf<ProviderIdentifier>(), emptyList)
    }
}

