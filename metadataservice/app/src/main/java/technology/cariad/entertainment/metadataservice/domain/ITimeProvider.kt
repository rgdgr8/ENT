package technology.cariad.entertainment.metadataservice.domain

private const val MILLISECONDS_PER_SECOND = 1000L

interface ITimeProvider {

    /**
     * @return number of seconds between now and epoch (1970-01-01T00:00:00Z)
     */
    fun secondsSinceEpoch(): Long = (System.currentTimeMillis() / MILLISECONDS_PER_SECOND)

    /**s
     * @return number of milliseconds between now and epoch (1970-01-01T00:00:00.000Z)
     */
    fun millisecondsSinceEpoch(): Long = System.currentTimeMillis()
}
