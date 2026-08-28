package technology.cariad.entertainment.metadataservice.data.database.pojo

import androidx.room.Embedded
import androidx.room.Relation
import technology.cariad.entertainment.metadataservice.data.database.entity.CoverArtLookupEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry

data class CoverArtLookupWithImageAndLifeCycleData(

    @Embedded
    val cachedLookup: CoverArtLookupEntry,

    @Relation(
        entity = ImageEntry::class,
        parentColumn = "image_id",
        entityColumn = "image_id"
    )
    val coverArt: ImageEntry?,

    @Relation(
        entity = LifeCycleDataEntry::class,
        parentColumn = "life_cycle_data_id",
        entityColumn = "life_cycle_data_id"
    )
    val lifeCycleData: LifeCycleDataEntry
)

