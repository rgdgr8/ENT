package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "station",
    indices = [
        Index(
            name = "index_station_data_life_cycle_data_id",
            value = ["life_cycle_data_id"],
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = LifeCycleDataEntry::class,
            parentColumns = ["life_cycle_data_id"],
            childColumns = ["life_cycle_data_id"]
        )
    ]
)
data class StationEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "station_id")
    val stationId: Long = 0,

    // TODO: this field will be mandatory later on, leave as optional for refactoring!
    @ColumnInfo(name = "life_cycle_data_id")
    val lifeCycleDataId: Long? = null
)

