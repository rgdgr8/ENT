package de.esolutions.magicadapter.stapi

import android.content.Context
import android.util.Log

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.magicadapter.stapi.MagicAdapter
 * 
 * This is a stub implementation for local builds without the MagicAdapter framework.
 * All service list and disable reasons functionality is bypassed.
 */
class MagicAdapter {
    private val tag = "mds/MagicAdapterStub"
    
    /**
     * Platform stub - returns OI as default for ROW (Rest of World) build variant
     */
    val platform: Platform = Platform.OI

    fun init(context: Context) {
        Log.i(tag, "init() - STUB: MagicAdapter initialization bypassed")
    }

    fun shutdown() {
        Log.i(tag, "shutdown() - STUB: MagicAdapter shutdown bypassed")
    }
}
