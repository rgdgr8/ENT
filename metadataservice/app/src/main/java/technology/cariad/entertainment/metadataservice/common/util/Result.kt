package technology.cariad.entertainment.metadataservice.common.util

/**
 * [Result] type to express [Success] or [Error] from an operation via type system.
 */
sealed class Result<out T>
data class Success<out T>(val result: T): Result<T>()
data class Error(val throwable: Throwable): Result<Nothing>()

