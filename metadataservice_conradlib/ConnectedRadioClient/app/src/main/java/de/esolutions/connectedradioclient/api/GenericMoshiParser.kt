package de.esolutions.connectedradioclient.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.esolutions.connectedradioclient.irc.IRC
import de.esolutions.connectedradioclient.irc.IRCEntry
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import java.lang.reflect.ParameterizedType

object GenericMoshiParser {

    /**
     * Parse the given [data] to a json string.
     */
    inline fun <reified T : Any> parseToJson(data: T): String {
        try {
            Logger.d(PrivacyFlags.SAFE, null, "Converting data into Json: $data", LoggerTags.PARSING)
            val moshiAdapter: JsonAdapter<T> = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(T::class.java)
            val json = moshiAdapter.toJson(data)
            Logger.d(
                PrivacyFlags.SAFE,
                null,
                "Converted data to Json: $json",
                LoggerTags.PARSING,
                LoggerTags.JSON
            )
            return json
        } catch (e: Exception) {
            Logger.ex(
                PrivacyFlags.SAFE,
                null,
                "An error occurred during parseToJson.",
                e,
                LoggerTags.PARSING
            )
            return ""
        }
    }

    /**
     * Parse the given [json] string to the specified object [T].
     * If T a special object like a Map<String, T> you can give
     * the [parameterizedType] to the function.
     */
    inline fun <reified T : Any> parseFromJson(
        json: String,
        parameterizedType: ParameterizedType? = null
    ): T? {
        Logger.d(
            PrivacyFlags.SAFE,
            null,
            "Converting from Json to data: $json",
            LoggerTags.PARSING,
            LoggerTags.JSON
        )
        val moshiAdapter: JsonAdapter<T> = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(parameterizedType ?: T::class.java)
        try {
            val data = moshiAdapter.fromJson(json)
            Logger.d(PrivacyFlags.SAFE, null, "Converted Json to data: $data", LoggerTags.PARSING)
            return data
        } catch (e: Exception) {
            IRC.error(IRCEntry.EXCEPTIONS, "Parse=$json")
            Logger.ex(
                PrivacyFlags.SAFE,
                null,
                "Error converting Json to data",
                e,
                LoggerTags.PARSING
            )
        }
        return null
    }
}