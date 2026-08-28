/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace e.solutions framework dependencies.
 */
package technology.cariad.entertainment.metadataservice.stubs.fw.asi

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.fw.android.comm.asi.diag.pers.OptionalValue
 */
data class OptionalValue(
    val identifier: Int = 0,
    val result: OpResult = OpResult.op_result_ok,
    val value: ShortArray = ShortArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OptionalValue

        if (identifier != other.identifier) return false
        if (result != other.result) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = identifier
        result1 = 31 * result1 + result.hashCode()
        result1 = 31 * result1 + value.contentHashCode()
        return result1
    }
}

