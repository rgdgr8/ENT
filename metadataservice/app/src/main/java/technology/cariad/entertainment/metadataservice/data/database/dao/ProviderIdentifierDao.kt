package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverters
import technology.cariad.entertainment.metadataservice.data.database.converter.OriginTypeConverter
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity.Companion.OriginType

@Dao
abstract class ProviderIdentifierDao : BaseDao<ProviderIdentifierEntry>() {

    internal fun createProviderIdentifierEntry(providerIdentifier: ProviderIdentifierEntity) =
        ProviderIdentifierEntry(
            origin = providerIdentifier.originType,
            externalId = providerIdentifier.id
        )

    internal suspend fun insertProviderIdentifier(
        providerIdentifier: ProviderIdentifierEntity
    ): Long {
        val providerEntry = createProviderIdentifierEntry(providerIdentifier)
        return selectOrInsertProvider(providerEntry)
    }

    internal suspend fun selectOrInsertProvider(providerIdentifier: ProviderIdentifierEntry): Long {
        var id = findIdByOriginAndExternalId(
            providerIdentifier.origin,
            providerIdentifier.externalId
        )
        if (id == null) {
            id = insert(providerIdentifier)
            assert(id != -1L)
        }
        return id
    }

    @TypeConverters(OriginTypeConverter::class)
    @Query("SELECT provider_id FROM provider_identifier WHERE origin = :origin AND " +
            "external_id = :externalId")
    internal abstract suspend fun findIdByOriginAndExternalId(
        origin: OriginType,
        externalId: String
    ): Long?

    @Query("SELECT * FROM provider_identifier WHERE provider_id = :providerId")
    internal abstract suspend fun findById(
        providerId: Long
    ): ProviderIdentifierEntry?
}

