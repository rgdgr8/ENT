package de.esolutions.connectedradioclient.debug.data

import de.esolutions.connectedradioclient.data.bearer.IBearer

class DemoBearer {

    /**
     * Returns a list with the passed bearer.
     */
    fun getDemoList(bearer: IBearer): List<IBearer> {
        return listOf(bearer)
    }
}