package de.esolutions.connectedradioclient.data.bearer

interface IBearer {

    /**
     * Returns bearer string, for example:
     * - dab:1e0.07d7.100d.0
     * - fm:1a0.110b.10710
     * - hd:040.00066.1
     */
    fun getBearerString(): String

    companion object {

        /**
         * Convert the given [bearerList] to a list of [IBearer] objects and return this list.
         * Check the bearer type and create [DabBearer], [FmBearer]
         * and [HdBearer] and add them to the list.
         */
        fun createListFromBackendData(mcc: String, bearerList: List<String>): List<IBearer> {
            return bearerList.map {
                val typeBearerSplit = it.split(":")
                if (typeBearerSplit.size != 2) {
                    return@map null
                }
                val type = typeBearerSplit[0]
                val bearerString = typeBearerSplit[1]
                return@map when (type) {
                    "dab" -> DabBearer.createFromString(mcc, bearerString)
                    "fm" -> FmBearer.createFromString(mcc, bearerString)
                    "hd" -> HdBearer.createFromString(bearerString)
                    else -> null
                }
            }.filterNotNull()
        }
    }
}