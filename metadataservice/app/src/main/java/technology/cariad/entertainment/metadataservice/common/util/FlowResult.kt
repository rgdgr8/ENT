package technology.cariad.entertainment.metadataservice.common.util

sealed class FlowResult<out T>
data class FlowValue<out T>(val value: T) : FlowResult<T>()
object FlowCompleted : FlowResult<Nothing>()

