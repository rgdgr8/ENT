package technology.cariad.entertainment.metadataservice.domain.value

enum class RetryStrategy(val value: Int) {
    NONE(0),
    ONCE(1),
    DELAYED(2),
    EXPONENTIAL(3)
}
