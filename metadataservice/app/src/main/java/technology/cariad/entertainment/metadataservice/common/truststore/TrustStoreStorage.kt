package technology.cariad.entertainment.metadataservice.common.truststore

import technology.cariad.entertainment.metadataservice.common.io.safeMkdirs
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.config.MODULE_CONFIG
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path

class TrustStoreStorage {

    private val log by logger()

    private val fileName = "truststore_gracenote.pem"
    private lateinit var fallbackDir : String
    private lateinit var fallbackPath : String
    private lateinit var otaDir : String
    private lateinit var otaPath : String


    private lateinit var fallbackTrustStoreFile : File
    private lateinit var otaTrustStoreFile : File

    private var workspacePrepared = false

    /**
     * Updates the TrustStore certificate for the gracenote service
     * @param newTrustStore content of the new certificate
     */

    fun updateTrustStore(newTrustStore: String?) {
        if(!workspacePrepared) { return }
        if (newTrustStore == null || newTrustStore == "") {
            log.info(this::updateTrustStore.name) {
                "New trustStore is null or demo, update cancelled"
            }
            IRCLogger.appGenericError("Truststore error","New trustStore is null")
            return
        }

        try {
            otaTrustStoreFile.createNewFile()
            otaTrustStoreFile.writeText(newTrustStore)
            log.info(this::updateTrustStore.name) {
                "New trustStore uploaded successfully"
            }
        } catch (e: Exception) {
            IRCLogger.appGenericError("Truststore error","Error creating truststore $e")
            log.info(this::updateTrustStore.name) {
                "An error has occur creating the new truststore $e"
            }
        }
    }

    /**
     * Prepares the workspace for saving the truststores on the unit
     * @param storageRoot Where to save the certificates
     */
    fun prepareWorkspace(storageRoot: String)  {
        otaDir = "${Paths.get(storageRoot, MODULE_CONFIG.gracenoteMagicTruststoreFolder)}"
        createDirectory(otaDir)
        otaPath = "${Paths.get(otaDir, fileName)}"
        otaTrustStoreFile = File(otaPath)

        fallbackDir = "${Paths.get(storageRoot, MODULE_CONFIG.gracenoteFallbackTruststoreFolder)}"
        fallbackPath = "${Paths.get(fallbackDir, fileName)}"
        fallbackTrustStoreFile = File(fallbackPath)

        workspacePrepared = true
    }

    private fun createDirectory(destDir: String) {
        with(File(destDir)) {
            if (!exists()) {
                safeMkdirs(Path(destDir))
            }
        }
    }
}
