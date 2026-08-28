package de.esolutions.metadataserviceclient.util

import android.os.Parcelable

abstract class DefaultParcelable : Parcelable {
    override fun describeContents(): Int = 0
}