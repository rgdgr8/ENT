package technology.cariad.entertainment.metadataservice.data.database.pojo

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import technology.cariad.entertainment.metadataservice.data.database.entity.BearerEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ETagsEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationMetadataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StreamEntry

data class CompleteRadioStation(

    @Embedded
    val station: StationEntry,

    @Relation(
        entity = ETagsEntry::class,
        parentColumn = "station_id",
        entityColumn = "station_id"
    )
    val etags: ETagsEntry?,

    @Relation(
        entity = StationMetadataEntry::class,
        parentColumn = "station_id",
        entityColumn = "station_id"
    )
    val stationMetadata: StationMetadataEntry?,

    @Relation(
        entity = BearerEntry::class,
        parentColumn = "station_id",
        entityColumn = "station_id"
    )
    val bearers: List<BearerEntry>,

    @Relation(
        entity = ImageEntry::class,
        parentColumn = "station_id",
        entityColumn = "image_id",
        associateBy = Junction(
            StationImageEntry::class,
            parentColumn = "station_id",
            entityColumn = "image_id"
        )
    )
    val images: List<ImageEntry>,

    @Relation(
        entity = StreamEntry::class,
        parentColumn = "station_id",
        entityColumn = "station_id"
    )
    val streams: List<StreamEntry>,

    @Relation(
        entity = GenreEntry::class,
        parentColumn = "station_id",
        entityColumn = "genre_id",
        associateBy = Junction(
            StationGenreRankEntry::class,
            parentColumn = "station_id",
            entityColumn = "genre_id"
        )
    )
    val genres: List<GenreEntry>,

    @Relation(
        entity = StationGenreRankEntry::class,
        parentColumn = "station_id",
        entityColumn = "station_id",
        projection = ["genre_id", "position"]
    )
    val genreRanks: List<StationGenreRankEntry>,

    @Relation(
        entity = ProviderIdentifierEntry::class,
        parentColumn = "station_id",
        entityColumn = "provider_id",
        associateBy = Junction(
            StationProviderIdentifierEntry::class,
            parentColumn = "station_id",
            entityColumn = "provider_id"
        )
    )
    val providerIdentifiers: List<ProviderIdentifierEntry>,

    @Relation(
        entity = LifeCycleDataEntry::class,
        parentColumn = "life_cycle_data_id",
        entityColumn = "life_cycle_data_id"
    )
    val lifeCycleData: LifeCycleDataEntry
)

