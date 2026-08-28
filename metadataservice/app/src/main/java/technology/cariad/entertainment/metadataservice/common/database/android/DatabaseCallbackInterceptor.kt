package technology.cariad.entertainment.metadataservice.common.database.android

import android.database.sqlite.SQLiteException
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import technology.cariad.entertainment.metadataservice.common.database.android.IDatabaseEventListener.EventProcessing
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger.DB_CORRUPTION_ERROR
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger.DB_MIGRATION_ERROR
import technology.cariad.entertainment.metadataservice.common.log.logger

/**
 * Implementation of [SupportSQLiteOpenHelper.Callback].
 * Allows to set a custom [IDatabaseEventListener] invoked before and after each respective call is
 * forwarded to the default database callback.
 * Depending on the result returned from the custom event listener before hook, execution of the
 * delegated default callback and after hook is skipped. This allows to define a partial or full
 * custom handling for database events.
 * @property eventListener Custom event listener.
 * @property defaultCallback Default database callback.
 */
class DatabaseCallbackInterceptor private constructor(
    private val eventListener: IDatabaseEventListener,
    private val defaultCallback: SupportSQLiteOpenHelper.Callback
) : SupportSQLiteOpenHelper.Callback(defaultCallback.version) {

    private val log by logger()

    companion object {

        /**
         * Creates a new [DatabaseCallbackInterceptor]
         */
        fun create(
            eventListener: IDatabaseEventListener,
            defaultCallback: SupportSQLiteOpenHelper.Callback
        ): DatabaseCallbackInterceptor {
            return DatabaseCallbackInterceptor(eventListener, defaultCallback)
        }
    }

    override fun onConfigure(db: SupportSQLiteDatabase) {
        when (eventListener.beforeConfigure(db)) {
            EventProcessing.CONTINUE -> {
                log.info(this::onConfigure.name) { "run default database configure" }

                defaultCallback.onConfigure(db)
                eventListener.afterConfigure(db)
            }
            EventProcessing.HANDLED -> {
                log.info(this::onConfigure.name) { "skip default database configure" }
            }
        }
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        when (eventListener.beforeCreate(db)) {
            EventProcessing.CONTINUE -> {
                log.info(this::onCreate.name) { "run default database create" }

                defaultCallback.onCreate(db)
                eventListener.afterCreate(db)
            }
            EventProcessing.HANDLED -> {
                log.info(this::onCreate.name) { "skip default database create" }
            }
        }
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (eventListener.beforeUpgrade(db, oldVersion, newVersion)) {
            EventProcessing.CONTINUE -> {
                log.info(this::onUpgrade.name) {
                    "run default database upgrade from version $oldVersion to $newVersion"
                }
                try {
                    defaultCallback.onUpgrade(db, oldVersion, newVersion)
                } catch (e: SQLiteException) {
                    log.error(this::onUpgrade.name) {
                        "run default database upgrade from version $oldVersion to $newVersion " +
                                "went wrong. ${e.message}"
                    }
                    IRCLogger.appError(DB_MIGRATION_ERROR)
                }
                eventListener.afterUpgrade(db, oldVersion, newVersion)
            }
            EventProcessing.HANDLED -> {
                log.info(this::onUpgrade.name) {
                    "skip default database upgrade from version $oldVersion to $newVersion"
                }
            }
        }
    }

    override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when (eventListener.beforeDowngrade(db, oldVersion, newVersion)) {
            EventProcessing.CONTINUE -> {
                log.warn(this::onDowngrade.name) {
                    "run default database downgrade from version $oldVersion to $newVersion"
                }
                try {
                    defaultCallback.onDowngrade(db, oldVersion, newVersion)
                } catch (e: SQLiteException) {
                    log.error(this::onDowngrade.name) {
                        "run default database downgrade from version $oldVersion to $newVersion " +
                                "went wrong. ${e.message}"
                    }
                    IRCLogger.appError(DB_MIGRATION_ERROR)
                }
                eventListener.afterDowngrade(db, oldVersion, newVersion)
            }
            EventProcessing.HANDLED -> {
                log.warn(this::onDowngrade.name) {
                    "skip default database downgrade from version $oldVersion to $newVersion"
                }
            }
        }
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        when (eventListener.beforeOpen(db)) {
            EventProcessing.CONTINUE -> {
                log.info(this::onOpen.name) { "run default database open" }

                defaultCallback.onOpen(db)
                eventListener.afterOpen(db)
            }
            EventProcessing.HANDLED -> {
                log.info(this::onOpen.name) { "skip default database open" }
            }
        }
    }

    override fun onCorruption(db: SupportSQLiteDatabase) {
        when (eventListener.beforeCorruption(db)) {
            EventProcessing.CONTINUE -> {
                log.error(this::onCorruption.name) {
                    "run default database corruption handling: delete database"
                }
                IRCLogger.appError(DB_CORRUPTION_ERROR)
                defaultCallback.onCorruption(db)
                eventListener.afterCorruption(db)
            }
            EventProcessing.HANDLED -> {
                log.error(this::onCorruption.name) {
                    "skip default database corruption handling"
                }
            }
        }
    }
}

