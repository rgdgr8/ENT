package technology.cariad.entertainment.metadataservice.service.config

val MODULE_CONFIG = ModuleConfig()

data class ModuleConfig(
    val databaseFile: String = "database/metadata_cache.db",
    val gracenoteDatabaseFolder: String = "gracenote",
    val gracenoteFallbackTruststoreFolder: String = "gracenote/truststore",
    val gracenoteMagicTruststoreFolder: String = "gracenote/truststore_ota",
    val gracenoteImageCacheFolder: String = "image_cache/gracenote",
    val gracenoteTempImageCacheFolder: String = "image_cache/gracenote_tmp",
    val conradImageCacheFolder: String = "image_cache/connected_radio"
)

