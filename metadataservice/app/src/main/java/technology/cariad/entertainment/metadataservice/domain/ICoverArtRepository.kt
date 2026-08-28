package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity

interface ICoverArtRepository {

    /**
     * @exception [technology.cariad.entertainment.metadataservice.common.util.ServiceDisabledException] is
     * thrown is a service entry is missing or a disable reason exists
     */
    suspend fun retrieveCoverArtImage(coverRequest: CoverArtRequestDataEntity): String?
}
