package technology.cariad.entertainment.metadataservice.domain.common

/**
 * Defines an abstract mapper from type T to type U.
 * Subclass to implement how T is mapped to U.
 */
abstract class Mapper<in T,U> {

    /**
     * Maps type [T] to type [U]
     */
    abstract fun mapValue(value: T) : U

    /**
     * Maps a list of type [T] to a list of type [U]
     */
    fun mapList(list: List<T>) : List<U> = list.map {mapValue(it)}
}
