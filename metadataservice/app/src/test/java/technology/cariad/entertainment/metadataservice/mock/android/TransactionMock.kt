package technology.cariad.entertainment.metadataservice.mock.android

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import io.mockk.coEvery
import io.mockk.coInvoke
import io.mockk.mockkStatic

class TransactionMock(db: RoomDatabase) {

    init {
        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        coEvery { db.withTransaction<Any>(captureLambda()) } coAnswers {
            lambda<suspend () -> Any>().coInvoke()
        }
    }
}

