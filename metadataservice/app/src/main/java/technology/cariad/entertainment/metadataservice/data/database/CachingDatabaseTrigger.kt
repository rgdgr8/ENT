package technology.cariad.entertainment.metadataservice.data.database

import androidx.sqlite.db.SupportSQLiteDatabase
import technology.cariad.entertainment.metadataservice.data.database.trigger.TRIGGER_COVER_ART_LOOKUP_ALBUM_AFTER_DELETE
import technology.cariad.entertainment.metadataservice.data.database.trigger.TRIGGER_COVER_ART_LOOKUP_ARTIST_AFTER_DELETE
import technology.cariad.entertainment.metadataservice.data.database.trigger.TRIGGER_COVER_ART_LOOKUP_IMAGE_AFTER_DELETE
import technology.cariad.entertainment.metadataservice.data.database.trigger.TRIGGER_COVER_ART_LOOKUP_LIFE_CYCLE_DATA_AFTER_DELETE
import technology.cariad.entertainment.metadataservice.data.database.trigger.TRIGGER_STATION_AFTER_DELETE
import technology.cariad.entertainment.metadataservice.data.database.trigger.TRIGGER_STATION_GENRE_RANK_AFTER_DELETE
import technology.cariad.entertainment.metadataservice.data.database.trigger.TRIGGER_STATION_IMAGE_AFTER_DELETE
import technology.cariad.entertainment.metadataservice.data.database.trigger.TRIGGER_STATION_PROVIDER_IDENTIFIER_AFTER_DELETE

private val triggers = listOf(
    TRIGGER_STATION_AFTER_DELETE,
    TRIGGER_STATION_IMAGE_AFTER_DELETE,
    TRIGGER_STATION_PROVIDER_IDENTIFIER_AFTER_DELETE,
    TRIGGER_STATION_GENRE_RANK_AFTER_DELETE,

    TRIGGER_COVER_ART_LOOKUP_LIFE_CYCLE_DATA_AFTER_DELETE,
    TRIGGER_COVER_ART_LOOKUP_IMAGE_AFTER_DELETE,
    TRIGGER_COVER_ART_LOOKUP_ALBUM_AFTER_DELETE,
    TRIGGER_COVER_ART_LOOKUP_ARTIST_AFTER_DELETE
)

/**
 * Crates database triggers form [triggers] list.
 */
fun createTriggers(database: SupportSQLiteDatabase) {
    triggers.forEach {
        val singleLine = it.trimIndent().replace("\n", " ")
        database.execSQL(singleLine)
    }
}

