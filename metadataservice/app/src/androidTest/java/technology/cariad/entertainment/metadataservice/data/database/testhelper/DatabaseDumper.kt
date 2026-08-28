package technology.cariad.entertainment.metadataservice.data.database.testhelper

import androidx.room.RoomDatabase

// dumps a (in-memory) database to FS for testing/debugging
fun dumpDatabaseToFS(database: RoomDatabase) {
    val seconds = (System.currentTimeMillis() / 1000).toInt()
    database.query("VACUUM INTO '/sdcard/Download/metadata_cache_$seconds.db'", null)
        .moveToFirst()
}

