package technology.cariad.entertainment.metadataservice.contentprovider

import android.content.Context
import technology.cariad.entertainment.metadataservice.R
import org.xmlpull.v1.XmlPullParser

private const val XML_ELEM_FILES_PATH = "files-path"
private const val XML_ATTR_NAME = "name"
private const val XML_ATTR_PATH = "path"

class PathConfigParser internal constructor() {

    companion object {

        /**
         * Parses the [R.xml.logo_path] xml resource defining the uri path mapping to the file
         * system paths for the various image folders.
         * @param context Valid Context that can be used to access xml resources
         * @return A map from the Uri path to the corresponding relative file system path.
         */
        fun getPathConfig(context: Context): Map<String, String> {

            val uriPathMapping = mutableMapOf<String, String>()

            val xmlParser = context.resources.getXml(R.xml.logo_path)
            xmlParser.use { parser ->
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == XML_ELEM_FILES_PATH) {
                                val name = parser.getAttributeValue(null, XML_ATTR_NAME)
                                val path = parser.getAttributeValue(null, XML_ATTR_PATH)
                                if (name != null && path != null) {
                                    uriPathMapping[name] = path
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
            return uriPathMapping.toMap()
        }
    }
}

