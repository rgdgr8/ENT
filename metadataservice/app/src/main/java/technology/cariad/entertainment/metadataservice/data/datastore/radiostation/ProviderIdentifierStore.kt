package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.ProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity

class ProviderIdentifierStore(
    private val providerIdentifierDao: ProviderIdentifierDao,
    private val stationProviderIdentifierDao: StationProviderIdentifierDao,
) {

    /**
     * Creates a new provider identifier or returns the id of an existing one.
     */
    internal suspend fun getProviderIdentifier(providerIdentifier: ProviderIdentifierEntity): Long {
        val providerEntry =
            providerIdentifierDao.createProviderIdentifierEntry(providerIdentifier)
        return selectOrInsertProviderIdentifier(providerEntry)
    }

    internal suspend fun selectOrInsertProviderIdentifier(
        providerIdentifier: ProviderIdentifierEntry
    ): Long {
        var id = providerIdentifierDao.findIdByOriginAndExternalId(
            providerIdentifier.origin,
            providerIdentifier.externalId
        )
        if (id == null) {
            id = providerIdentifierDao.insert(providerIdentifier)
        }
        return id
    }

    /**
     * Inserts a new provider identifier and returns it's id.
     */
    internal suspend fun insertProviderIdentifier(
        providerIdentifier: ProviderIdentifierEntity
    ): Long {
        val providerEntry =
            providerIdentifierDao.createProviderIdentifierEntry(providerIdentifier)
        return providerIdentifierDao.selectOrInsertProvider(providerEntry)
    }

    /**
     * Inserts new provider identifier and returns their ids.
     */
    internal suspend fun insertProviderIdentifiers(
        stationId: Long,
        providerIdentifiers: List<ProviderIdentifierEntity>
    ): List<Long> {
        val stationProviderIdentifiers = providerIdentifiers.map {

            val providerIdentifierEntry = providerIdentifierDao.createProviderIdentifierEntry(it)
            val genreId = selectOrInsertProviderIdentifier(providerIdentifierEntry)

            stationProviderIdentifierDao.createStationProviderIdentifierEntry(stationId, genreId)
        }
        return stationProviderIdentifierDao.insert(stationProviderIdentifiers)
    }

    /**
     * Updates the list of provider identifiers for the station referred to by [stationId].
     * If providerIds are already stored they are replaced entirely with the new ids.
     * @param stationId Requested station
     * @param providerIdentifiers
     */
    internal suspend fun updateProviderIdentifiers(
        stationId: Long,
        providerIdentifiers: List<ProviderIdentifierEntity>
    ) {
        stationProviderIdentifierDao.deleteByStationId(stationId)
        if (providerIdentifiers.isNotEmpty()) {
            insertProviderIdentifiers(stationId, providerIdentifiers)
        }
    }
}

