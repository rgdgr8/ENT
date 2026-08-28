package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.backend.lookup.result.ProviderObject
import de.esolutions.connectedradioclient.data.enums.Origin
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import java.util.Locale

data class ProviderIdentifier(val origin: Origin, val externalId: String) {

    companion object {

        /**
         * Create a [ProviderIdentifier] object from the given backend [providerObject].
         * Returns null if the provider enum is not parsable.
         */
        fun createFromBackendData(providerObject: ProviderObject): ProviderIdentifier? {
            val providerEnum = parseProviderEnum(providerObject.name) ?: return null

            return ProviderIdentifier(providerEnum, providerObject.id)
        }

        private fun parseProviderEnum(providerName: String): Origin? {
            // Add Local to UpperCase to prevent bugs (recommendation from intellij).
            // Use English because the names are Latin letter.
            val providerNameString = providerName.uppercase(Locale.ENGLISH)

            return try {
                Origin.valueOf(providerNameString)
            } catch (ex: IllegalArgumentException) {
                Logger.ex(
                    PrivacyFlags.SAFE,
                    null,
                    "Failed parsing ProviderIdentifier: $providerNameString",
                    ex,
                    LoggerTags.PARSING
                )

                null
            }
        }

        /**
         * Create a [List] of [ProviderIdentifier] objects from the given [providers] [List] from the backend.
         *
         * @see HCP3BUG-103049 Providers that are unknown should be ignored.
         */
        fun createListFromBackendData(providers: List<ProviderObject>): List<ProviderIdentifier> {
            return providers.mapNotNull {
                createFromBackendData(it)
            }
        }
    }
}