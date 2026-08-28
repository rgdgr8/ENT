package de.esolutions.connectedradioclient.logger

/**
 * Local replacement for de.esolutions.fw.util.commons.tracing.stapi.attributes.PrivacyFlag
 * Used to categorize logged data for privacy purposes.
 */
enum class PrivacyFlag {
    NOT_CATEGORIZED,
    UNIQUE_ID,
    CATEGORY_UNDECIDED
}

/**
 * Wrapper class for PrivacyFlags for better maintainability
 */
enum class PrivacyFlags(val privacyFlag: PrivacyFlag) {

    /**
     * Should be used when the logged data does not match to any other PrivacyFlag.
     */
    SAFE(PrivacyFlag.NOT_CATEGORIZED),

    /**
     * Should be used when a token used for authentication is logged. A token is a unique identifier for
     * authentication with the backend. Therefore, it should be logged as UNIQUE_ID.
     */
    TOKEN(PrivacyFlag.UNIQUE_ID),

    /**
     * Should be used when a session is logged. A session is a unique identifier used to identify a session. Therefore,
     * it should be logged as UNIQUE_ID.
     */
    SESSION(PrivacyFlag.UNIQUE_ID),

    /**
     * Should be used if the logged data is unknown or the data has not been checked to which PrivacyFlag it belongs.
     */
    UNDECIDED(PrivacyFlag.CATEGORY_UNDECIDED)
}