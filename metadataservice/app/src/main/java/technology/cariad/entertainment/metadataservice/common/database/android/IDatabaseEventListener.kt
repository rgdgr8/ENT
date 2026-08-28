package technology.cariad.entertainment.metadataservice.common.database.android

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database life cycle event listener.
 */
interface IDatabaseEventListener {

    /**
     * Return codes indicating if the event processing should be continued or not.
     */
    enum class EventProcessing {
        CONTINUE,
        HANDLED
    }

    /**
     * Called before the database is configured.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onConfigure
     * @param db The database.
     * @return [EventProcessing.HANDLED] if further event processing should be skipped,
     *         [EventProcessing.CONTINUE] otherwise
     */
    fun beforeConfigure(db: SupportSQLiteDatabase): EventProcessing = EventProcessing.CONTINUE

    /**
     * Called after the database was configured.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onConfigure
     * @param db The database.
     */
    fun afterConfigure(db: SupportSQLiteDatabase) {}

    /**
     * Called before the database is created.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onCreate
     * @param db The database.
     * @return [EventProcessing.HANDLED] if further event processing should be skipped,
     *         [EventProcessing.CONTINUE] otherwise
     */
    fun beforeCreate(db: SupportSQLiteDatabase): EventProcessing = EventProcessing.CONTINUE

    /**
     * Called after the database was created..
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onCreate
     * @param db The database.
     */
    fun afterCreate(db: SupportSQLiteDatabase) {}

    /**
     * Called before the database is upgraded.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onUpgrade
     * @param db The database.
     * @param oldVersion The database version before the upgrade.
     * @param newVersion The database version.after the upgrade.
     * @return [EventProcessing.HANDLED] if further event processing should be skipped,
     *         [EventProcessing.CONTINUE] otherwise
     */
    fun beforeUpgrade(
        db: SupportSQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ): EventProcessing = EventProcessing.CONTINUE

    /**
     * Called after the database was upgraded.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onUpgrade
     * @param db The database.
     * @param oldVersion The database version before the upgrade.
     * @param newVersion The database version.after the upgrade.
     */
    fun afterUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    /**
     * Called before the database is downgraded.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onDowngrade
     * @param db The database.
     * @param oldVersion The database version before the downgrade.
     * @param newVersion The database version.after the downgrade.
     * @return [EventProcessing.HANDLED] if further event processing should be skipped,
     *         [EventProcessing.CONTINUE] otherwise
     */
    fun beforeDowngrade(
        db: SupportSQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ): EventProcessing = EventProcessing.CONTINUE

    /**
     * Called after the database was downgraded.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onDowngrade
     * @param db The database.
     * @param oldVersion The database version before the downgrade.
     * @param newVersion The database version.after the downgrade.
     */
    fun afterDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    /**
     * Called before the database is opened.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onOpen
     * @param db The database.
     * @return [EventProcessing.HANDLED] if further event processing should be skipped,
     *         [EventProcessing.CONTINUE] otherwise
     */
    fun beforeOpen(db: SupportSQLiteDatabase): EventProcessing = EventProcessing.CONTINUE

    /**
     * Called after the database was opened.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onOpen
     * @param db The database.
     */
    fun afterOpen(db: SupportSQLiteDatabase) {}

    /**
     * Called before the database corruption is handled.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onCorruption
     * @param db The database.
     * @return [EventProcessing.HANDLED] if further event processing should be skipped,
     *         [EventProcessing.CONTINUE] otherwise
     */
    fun beforeCorruption(db: SupportSQLiteDatabase): EventProcessing = EventProcessing.CONTINUE

    /**
     * Called after the database corruption was handled.
     * @see androidx.sqlite.db.SupportSQLiteOpenHelper.Callback.onCorruption
     * @param db The database.
     */
    fun afterCorruption(db: SupportSQLiteDatabase) {}
}

