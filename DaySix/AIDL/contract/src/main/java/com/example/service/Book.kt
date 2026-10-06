package com.example.service

import android.os.Parcel
import android.os.Parcelable

// Thu tu doc phai trung thu tu ghi o ca Client va Server.
// Implement thu cong de quan sat marshalling ma khong can plugin Parcelize.
data class Book(
    val id: Int,
    val name: String,
    val author: String,
    val price: Double
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        parcel.readInt(),
        requireNotNull(parcel.readString()),
        requireNotNull(parcel.readString()),
        parcel.readDouble()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(name)
        dest.writeString(author)
        dest.writeDouble(price)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Book> = object : Parcelable.Creator<Book> {
            override fun createFromParcel(source: Parcel): Book = Book(source)
            override fun newArray(size: Int): Array<Book?> = arrayOfNulls(size)
        }
    }
}
