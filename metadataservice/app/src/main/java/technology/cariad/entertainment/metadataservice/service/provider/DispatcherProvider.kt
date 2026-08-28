package technology.cariad.entertainment.metadataservice.service.provider

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface IDispatcherProvider {

    /**
     * @see [Dispatchers.Default]
     */
    fun default(): CoroutineDispatcher = Dispatchers.Default

    /**
     * @see [Dispatchers.Main]
     */
    fun main(): CoroutineDispatcher = Dispatchers.Main

    /**
     * @see [Dispatchers.IO]
     */
    fun io(): CoroutineDispatcher = Dispatchers.IO

    /**
     * @see [Dispatchers.Unconfined]
     */
    fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}

class DispatcherProvider : IDispatcherProvider

