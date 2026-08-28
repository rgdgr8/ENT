package technology.cariad.entertainment.metadataservice.common.serviceresolver

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger

class ServiceResolver(private val context: Context) : IServiceResolver {

    private val log by logger()

    /**
     * @see IServiceResolver.resolve
     */
    override fun resolve(action: String): Intent? {
        val implicit = Intent(action)
        val service = context.packageManager.resolveService(implicit, 0)
        return if (service != null) {
            val serviceInfo = service.serviceInfo

            Intent(implicit).apply {
                component = ComponentName(serviceInfo.applicationInfo.packageName, serviceInfo.name)
            }.also {
                log.info(this::resolve.name) {
                    "found service for action=${it.action}: component=${it.component}"
                }
            }
        } else {
            log.error(this::resolve.name) { "no service found for action=$action" }
            IRCLogger.appGenericError(
                "no service found",
                "found no service offering the given action=$action"
            )
            null
        }
    }
}

