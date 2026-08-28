package de.esolutions.gracenoteservice.gntest

import android.content.Context
import android.content.res.AssetManager
import technology.cariad.entertainment.metadataservice.MetadataServiceApp
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

/**
 * This class is used to unpack tracing config files needed for esoTracing to required location.
 * Done in a similar way how it is done is a sample trace_core_app provided by FW
 * (e.g. //hcp3/common/sysint_esofwivi/main_sysint_esofwivi/UNIT/
 * /esofw_core_sdk-android-aarch64-clang-aosp/samples/android/trace_core_app/)
 */
class FrameworkConfigUnpacker {
    @Inject
    lateinit var context: Context

    private val log by logger(FrameworkConfigUnpacker::class)

    init {
        if (MetadataServiceApp.isInitialized) {
            MetadataServiceApp.appComponent.inject(this)
        }
    }

    /**
     * Unpacks the esofw assets from the asset manager and stores these to the local file system.
     *
     * @param assetMgr The android asset manager retrieved e.g. by getAssets()
     * @param rootPath Either set the root path to getApplicationInfo().dataDir
     * or ensure that this application has read and write access to this directory.
     */
    fun unpackConfigFiles(rootPath: String) {
        val assetMgr = context.assets;
        makeAssetAccessibleAsFile(
            assetMgr, "config/framework.json",
            "$rootPath/config/framework.json"
        )
        makeAssetAccessibleAsFile(
            assetMgr, "config/tracing.json",
            "$rootPath/config/tracing.json"
        )
        System.setProperty("ipl.config.dir", "$rootPath/config")
    }


    /**
     * Unpacks an asset to the file system
     *
     * @param assetMgr The android asset manager retrieved e.g. by getAssets()
     * @param assetPath The path name within the assets folder
     * @param filePath The path name where to unpack the asset to, e.g. getApplicationInfo().dataDir
     */
    private fun makeAssetAccessibleAsFile(
        assetMgr: AssetManager, assetPath: String, filePath: String
    ) {
        /* create directory */
        try {
            val child = File(filePath)
            val parentPath = child.parent
            if (parentPath != null) {
                val parent = File(parentPath)
                val dirCreated = parent.mkdirs()
                log.info(this::makeAssetAccessibleAsFile.name) {
                    "directory $parentPath if ($dirCreated) created else exists"
                }

            }
        } catch (secex: SecurityException) {
            log.error(this::makeAssetAccessibleAsFile.name) {
                "create directory failed on $filePath: $secex"
            }
        }

        /* copy file */
        var ins: InputStream? = null
        var outs: FileOutputStream? = null
        try {
            val buffer = ByteArray(512)
            ins = assetMgr.open(assetPath)
            outs = FileOutputStream(filePath)
            var numBytes = 0
            while (numBytes != -1) {
                numBytes = ins.read(buffer)
                if (numBytes > 0) {
                    outs.write(buffer, 0, numBytes)
                }
            }
            ins.close()
            outs.close()
        } catch (ioex: IOException) {
            log.error(this::makeAssetAccessibleAsFile.name) {
                "create file from asset failed on $assetPath, $filePath: $ioex.toString()"
            }

        } catch (secex: SecurityException) {
            log.error(this::makeAssetAccessibleAsFile.name) {
                "create directory failed on $assetPath, $filePath: $secex.toString()"
            }
        }
    }
}
