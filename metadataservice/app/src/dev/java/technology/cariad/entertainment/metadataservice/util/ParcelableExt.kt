package technology.cariad.entertainment.metadataservice.util

import android.os.Parcel
import android.os.Parcelable

/**
 * Replicates a Parcelable
 */
fun Parcelable.deepCopy(): Parcelable? =
    with(Parcel.obtain()) {
        try {
            writeParcelable(this@deepCopy, 0)
            setDataPosition(0)
            return readParcelable(this@deepCopy.javaClass.classLoader)
        } finally {
            recycle()
        }
    }

