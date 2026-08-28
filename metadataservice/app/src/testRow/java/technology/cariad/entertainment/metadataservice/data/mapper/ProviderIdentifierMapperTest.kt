package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.ProviderIdentifier
import de.esolutions.connectedradioclient.data.enums.Origin
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity.Companion.OriginType
import org.junit.Assert
import org.junit.Test

class ProviderIdentifierMapperTest {

    private val providerIdentifierMapper = ProviderIdentifierMapper()

    @Test
    fun mapValue_shouldConvertProviderIdentifierToEntity() {

        val gracenote = ProviderIdentifier(Origin.GRACENOTE, "gracenoteId")
        val gracenoteEntity = providerIdentifierMapper.mapValue(gracenote)

        Assert.assertEquals("gracenoteId", gracenoteEntity.id)
        Assert.assertEquals(OriginType.GRACENOTE, gracenoteEntity.originType)

        val radioplayer = ProviderIdentifier(Origin.RADIOPLAYER, "radioplayerId")
        val radioplayerEntity = providerIdentifierMapper.mapValue(radioplayer)

        Assert.assertEquals("radioplayerId", radioplayerEntity.id)
        Assert.assertEquals(OriginType.RADIOPLAYER, radioplayerEntity.originType)

        val radiodns = ProviderIdentifier(Origin.RADIODNS, "radiodnsId")
        val radiodnsEntity = providerIdentifierMapper.mapValue(radiodns)

        Assert.assertEquals("radiodnsId", radiodnsEntity.id)
        Assert.assertEquals(OriginType.RADIODNS, radiodnsEntity.originType)
    }

    @Test
    fun mapList_shouldConvertProviderIdentifiersToEntities() {

        val ids = listOf(
            ProviderIdentifier(Origin.GRACENOTE, "gracenoteId"),
            ProviderIdentifier(Origin.RADIOPLAYER, "radioplayerId"),
            ProviderIdentifier(Origin.RADIODNS, "radiodnsId")
        )

        val idEntries = providerIdentifierMapper.mapList(ids)

        Assert.assertEquals(
            listOf(
                ProviderIdentifierEntity(OriginType.GRACENOTE, "gracenoteId"),
                ProviderIdentifierEntity(OriginType.RADIOPLAYER, "radioplayerId"),
                ProviderIdentifierEntity(OriginType.RADIODNS, "radiodnsId")
            ), idEntries
        )
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        
        val empty = providerIdentifierMapper.mapList(listOf())

        Assert.assertEquals(listOf<ProviderIdentifierEntity>(), empty)
    }
}

