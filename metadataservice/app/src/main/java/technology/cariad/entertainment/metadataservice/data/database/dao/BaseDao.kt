package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

abstract class BaseDao<T> {

    @Insert
    internal abstract suspend fun insert(obj: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    internal abstract suspend fun insertIgnore(obj: T): Long

    @Insert
    internal abstract suspend fun insert(obj: List<T>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    internal abstract suspend fun insertIgnore(obj: List<T>): List<Long>

    @Update
    internal abstract suspend fun update(obj: T)

    @Update
    internal abstract suspend fun update(objs: List<T>)

    @Delete
    internal abstract suspend fun delete(obj: T)

    internal open suspend fun upsert(obj: T): Unit {
        val id = insertIgnore(obj)
        if (id == -1L) {
            update(obj)
        }
    }
}

