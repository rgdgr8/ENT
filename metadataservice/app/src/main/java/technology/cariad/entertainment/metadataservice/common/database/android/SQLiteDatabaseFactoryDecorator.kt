package technology.cariad.entertainment.metadataservice.common.database.android

import androidx.sqlite.db.SupportSQLiteOpenHelper

/**
 * Specialized [SupportSQLiteOpenHelper.Factory] allowing to register an [IDatabaseEventListener] to
 * receive life cycle events of the database.
 */
class SQLiteDatabaseFactoryDecorator(
    private val delegate: SupportSQLiteOpenHelper.Factory,
    private val databaseEventHandler: IDatabaseEventListener
) : SupportSQLiteOpenHelper.Factory {

    /**
     * @see SupportSQLiteOpenHelper.Factory.create
     */
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration)
            : SupportSQLiteOpenHelper {
        val decoratedConfiguration = SupportSQLiteOpenHelper.Configuration
            .builder(configuration.context)
            .name(configuration.name)
            .callback(
                DatabaseCallbackInterceptor.create(databaseEventHandler, configuration.callback)
            )
            .build()
        return delegate.create(decoratedConfiguration)
    }
}

