/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace e.solutions framework dependencies.
 */
package technology.cariad.entertainment.metadataservice.stubs.fw.tracing

import android.util.Log

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.fw.util.commons.tracing.stapi.TraceLevels
 * 
 * Enum for trace levels - maps to Android Log levels.
 */
object TraceLevels {
    /** Verbose/Trace level - maps to Log.VERBOSE */
    const val TRACE = Log.VERBOSE
    
    /** Debug level - maps to Log.DEBUG */
    const val DEBUG = Log.DEBUG
    
    /** Info level - maps to Log.INFO */
    const val INFO = Log.INFO
    
    /** Warning level - maps to Log.WARN */
    const val WARN = Log.WARN
    
    /** Error level - maps to Log.ERROR */
    const val ERROR = Log.ERROR
    
    /** Fatal level - maps to Log.ASSERT (WTF) */
    const val FATAL = Log.ASSERT
}

