package technology.cariad.entertainment.metadataservice.service.di

import de.esolutions.connectedradioclient.handlers.media.ConRadClient

interface IDomainRegionComponent : IDomainComponent {

    /**
     * Returns the [ConRadClient] instance from the dependency graph.
     */
    fun conRadClient(): ConRadClient
}

