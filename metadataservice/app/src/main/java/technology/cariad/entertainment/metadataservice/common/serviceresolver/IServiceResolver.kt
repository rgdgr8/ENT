package technology.cariad.entertainment.metadataservice.common.serviceresolver

import android.content.Intent

interface IServiceResolver {

    /**
     * Attempts to find a service offering the given action. The returned intent can be used to
     * bind the service.
     * @param action Intent filter action defined by the service.
     * @return an [Intent] with the component name of the service or null if no matching service was
     *         found
     */
    fun resolve(action: String): Intent?
}

