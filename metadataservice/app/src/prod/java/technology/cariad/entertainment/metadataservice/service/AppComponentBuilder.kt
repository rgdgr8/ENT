package technology.cariad.entertainment.metadataservice.service

import android.content.Context
import technology.cariad.entertainment.metadataservice.service.di.AppComponent
import technology.cariad.entertainment.metadataservice.service.di.DaggerAppComponent
import technology.cariad.entertainment.metadataservice.service.di.module.AppModule
import kotlinx.coroutines.CoroutineScope

class AppComponentBuilder private constructor() {

    companion object {

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
        }
    }
}


