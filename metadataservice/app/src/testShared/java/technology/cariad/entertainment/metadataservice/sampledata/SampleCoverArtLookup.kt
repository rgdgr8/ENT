package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.data.database.entity.CoverArtLookupEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.pojo.CoverArtLookupWithImageAndLifeCycleData

class SampleCoverArtLookup {
    companion object {
        val COVER_ART_LOOKUP_ENTRY_1 = CoverArtLookupEntry(
            coverArtLookupId = 0,
            albumId = 1,
            artistId = 8,
            trackName = "Love Song",
            imageId = 10,
            lifeCycleDataId = 20
        )

        val COVER_ART_LOOKUP_ENTRY_2 = CoverArtLookupEntry(
            artistId = 1L,
            albumId = 2L,
            imageId = 3L,
            trackName = "Cherish",
            coverArtLookupId = 4L,
            lifeCycleDataId = 10
        )

        val COVER_ART_LOOKUP_POJO = CoverArtLookupWithImageAndLifeCycleData(
            coverArt = null,
            cachedLookup = CoverArtLookupEntry(
                coverArtLookupId = 1L,
                albumId = 2L,
                artistId = 3L,
                trackName = "Cherish",
                imageId = 4L,
                lifeCycleDataId = 30
            ),
            lifeCycleData = LifeCycleDataEntry(
                lifeCycleDataId = 1,
                retryDelay = 9,
                retryCount = 3,
                nextRetry = 3,
                lastRetryStatus = null,
                validUntil = null,
                lastRequested = 2
            )
        )

        val COVER_ART_LOOKUP_POJO_FULL = CoverArtLookupWithImageAndLifeCycleData(
            coverArt = SampleImage.IMAGE_ENTRY_2,
            cachedLookup = CoverArtLookupEntry(
                coverArtLookupId = 1L,
                albumId = 2L,
                artistId = 3L,
                trackName = "Cherish",
                imageId = 4L,
                lifeCycleDataId = 10
            ),
            lifeCycleData = LifeCycleDataEntry(
                lifeCycleDataId = 10,
                lastRequested = 10,
                validUntil = 10,
                lastRetryStatus = null,
                nextRetry = 10,
                retryCount = 1,
                retryDelay = 20
            )
        )

        val COVER_ART_LOOKUP_POJO_WITH_ONLY_ALBUM = CoverArtLookupWithImageAndLifeCycleData(
            coverArt = SampleImage.IMAGE_ENTRY_2,
            cachedLookup = CoverArtLookupEntry(
                coverArtLookupId = 1L,
                albumId = 2L,
                imageId = 4L,
                lifeCycleDataId = 10
            ),
            lifeCycleData = LifeCycleDataEntry(
                lifeCycleDataId = 10,
                lastRequested = 10,
                validUntil = 10,
                lastRetryStatus = null,
                nextRetry = 10,
                retryCount = 1,
                retryDelay = 20
            )
        )

        val COVER_ART_LOOKUP_POJO_WITH_ARTIST_AND_TITLE = CoverArtLookupWithImageAndLifeCycleData(
            coverArt = SampleImage.IMAGE_ENTRY_2,
            cachedLookup = CoverArtLookupEntry(
                coverArtLookupId = 1L,
                artistId = 1L,
                trackName = "Frozen",
                imageId = 4L,
                lifeCycleDataId = 10
            ),
            lifeCycleData = LifeCycleDataEntry(
                lifeCycleDataId = 10,
                lastRequested = 10,
                validUntil = 10,
                lastRetryStatus = null,
                nextRetry = 10,
                retryCount = 1,
                retryDelay = 20
            )
        )
    }
}
