package technology.cariad.entertainment.metadataservice.common.permission

import android.Manifest

enum class Permission(val string: String) {
    ACCESS_FINE_LOCATION(string = Manifest.permission.ACCESS_FINE_LOCATION),
    ACCESS_BACKGROUND_LOCATION(string = Manifest.permission.ACCESS_BACKGROUND_LOCATION)
}

