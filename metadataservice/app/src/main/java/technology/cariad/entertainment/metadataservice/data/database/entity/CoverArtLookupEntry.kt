package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cover_art_lookup",
    indices = [
        Index(
            name = "index_cover_art_lookup_album_id",
            value = ["album_id"]
        ),
        Index(
            name = "index_cover_art_lookup_artist_id",
            value = ["artist_id"]
        ),
        Index(
            name = "index_cover_art_lookup_image_id",
            value = ["image_id"],
            unique = true
        ),
        Index(
            name = "index_cover_art_lookup_life_cycle_data_id",
            value = ["life_cycle_data_id"],
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntry::class,
            parentColumns = ["album_id"],
            childColumns = ["album_id"]
        ),
        ForeignKey(
            entity = ImageEntry::class,
            parentColumns = ["image_id"],
            childColumns = ["image_id"]
        ),
        ForeignKey(
            entity = ArtistEntry::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"]
        ),
        ForeignKey(
            entity = LifeCycleDataEntry::class,
            parentColumns = ["life_cycle_data_id"],
            childColumns = ["life_cycle_data_id"]
        )
    ]
)
data class CoverArtLookupEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cover_art_lookup_id")
    val coverArtLookupId: Long = 0,

    @ColumnInfo(name = "album_id")
    val albumId: Long? = null,

    @ColumnInfo(name = "artist_id")
    val artistId: Long? = null,

    @ColumnInfo(name = "track_name")
    val trackName: String? = null,

    @ColumnInfo(name = "image_id")
    val imageId: Long? = null,

    @ColumnInfo(name = "life_cycle_data_id")
    val lifeCycleDataId: Long
)
