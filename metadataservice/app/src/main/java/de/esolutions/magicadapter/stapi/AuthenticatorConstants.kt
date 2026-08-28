/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace MagicAdapter dependencies for standalone builds.
 */
package de.esolutions.magicadapter.stapi

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.magicadapter.stapi.AuthenticatorConstants
 * 
 * Updated 2026-02-06: Changed to Cariad account types (technology.cariad.magic.oauth)
 * Updated 2026-02-06: Fixed bundle option keys - MagicAdapter expects "transactionTypeID" (uppercase ID)
 */
object AuthenticatorConstants {
    const val ACCOUNT_TYPE_OI_ONLINE = "de.esolutions.oi.online"
    
    // Account types and names - Updated for Cariad platform
    const val CORESERVICE_ACCOUNT_TYPE = "technology.cariad.magic.oauth"
    const val CORESERVICE_ACCOUNT_NAME = "vehicle"
    
    // Bundle options keys - Note: MagicAdapter expects uppercase "ID" suffix
    const val BUNDLE_OPTIONS_TRANSACTIONTYPEID = "transactionTypeID"
    const val BUNDLE_OPTIONS_OPERATIONTYPEID = "operationTypeID"
    const val BUNDLE_OPTIONS_SERVICEID = "serviceId"
    const val BUNDLE_OPTIONS_OPERATIONID = "operationId"
    const val BUNDLE_OPTIONS_OPERATIONID_DEFAULT = "default"
}
