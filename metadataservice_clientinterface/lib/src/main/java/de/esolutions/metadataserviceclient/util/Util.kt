package de.esolutions.metadataserviceclient.util

import android.os.Parcel
import android.os.Parcelable

internal inline fun <reified T> defaultCreator(crossinline creatorBlock: (source: Parcel) -> T) =
    object : Parcelable.Creator<T> {
        override fun createFromParcel(source: Parcel): T = creatorBlock(source)
        override fun newArray(size: Int): Array<T?> = arrayOfNulls<T>(size)
    }

internal fun Parcel.wStrings(vararg strings: String?): Parcel {
    strings.forEach { this.writeString(it) }
    return this
}

internal fun Parcel.wInts(vararg ints: Int): Parcel {
    ints.forEach { this.writeInt(it) }
    return this
}

internal fun Parcel.wInt(value: Int): Parcel {
    this.writeInt(value)
    return this
}

internal fun Parcel.wIntList(intList: List<Int>): Parcel {
    writeInt(intList.size)
    intList.forEach(this::writeInt)
    return this
}

internal fun Parcel.rIntList(): List<Int> {
    val size = readInt()
    val intList = ArrayList<Int>(size)
    for (i in 0 until size) {
        intList.add(readInt())
    }
    return intList
}

internal fun Parcel.wString(value: String?): Parcel {
    this.writeString(value)
    return this
}

internal fun Parcel.wStringList(strings: List<String>?): Parcel {
    this.writeStringList(strings)
    return this
}

internal inline fun <reified T : Parcelable> Parcel.rParcelable(): T? =
    this.readParcelable(T::class.java.classLoader)

internal inline fun <reified T : Parcelable> Parcel.wParcelable(parcelable: T?): Parcel {
    this.writeParcelable(parcelable, parcelable?.describeContents() ?: 0)
    return this
}

internal inline fun <reified T : Parcelable> Parcel.rParcelableList(): List<T>? =
    this.readParcelableArray(T::class.java.classLoader)?.map { it as T }

internal inline fun <reified T : Parcelable> Parcel.wParcelableList(parcelables: List<T>): Parcel {
    this.writeParcelableArray(
        parcelables.toTypedArray(),
        parcelables.firstOrNull()?.describeContents() ?: 0
    )
    return this
}


internal fun Parcel.end(): Unit = Unit