package technology.cariad.entertainment.metadataservice.service

import android.content.Context
import technology.cariad.entertainment.metadataservice.MetadataServiceApp
import technology.cariad.entertainment.metadataservice.common.position.FakePositionProvider
import technology.cariad.entertainment.metadataservice.common.position.PositionInformation
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.service.di.AppComponent
import technology.cariad.entertainment.metadataservice.service.di.DaggerAppComponent
import technology.cariad.entertainment.metadataservice.service.di.module.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

object AppComponentBuilder {

    /**
     * Constructs the Dagger AppComponent.
     */
    fun build(context: Context, storagePath: String): AppComponent {
        return DaggerAppComponent.builder()
            .appModule(AppModule(context, storagePath))
            .build()
    }

    /**
     * Performs the initialization of the Dagger AppComponent.
     */
    fun initialize(appComponent: AppComponent, scope: CoroutineScope) {

        appComponent.apply {
            if (!getConfigurationProvider().getServiceListCheckEnabled()) {
                runBlocking { getServiceListClient().fakeServiceList(true) }
            }

            if (!getConfigurationProvider().getDisableReasonsCheckEnabled()) {
                runBlocking { getDisableReasonsClient().fakeDisabledReasons(true) }
            }

            if (MetadataServiceApp.config.positionOverride.isNotEmpty()) {
                val (lat, long) = MetadataServiceApp.config.positionOverride
                    .split(";")
                    .map { it.toDouble() }
                (appComponent.getPositionProvider() as? FakePositionProvider)
                    ?.fakePosition?.tryEmit(Success(PositionInformation(lat, long)))
            }
        }

    }
}
