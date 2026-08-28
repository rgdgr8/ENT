package technology.cariad.entertainment.metadataservice.common.log

/**
 * LOCAL BUILD STUB: Privacy flag enum replacing de.esolutions.fw.util.commons.tracing.stapi.attributes.PrivacyFlag
 * 
 * This enum is used to categorize log messages according to their privacy sensitivity.
 * In local builds, this is a no-op stub that maintains API compatibility.
 */
enum class PrivacyFlag {
    /** Default - not yet categorized */
    NOT_CATEGORIZED,
    
    /** GNSS/Location data - high privacy */
    GNSS,
    
    /** Category not yet decided */
    CATEGORY_UNDECIDED,
    
    /** Owner not yet decided */
    OWNER_UNDECIDED
}

