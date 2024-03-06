package com.example.screenshotService.model

import android.os.Parcel
import android.os.Parcelable
import com.example.screenshotService.adapter.FileListAdapter.Companion.ITEM_VIEW_TYPE_FILE

data class FileItem(val path: String?, val viewType: Int = ITEM_VIEW_TYPE_FILE) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(path)
        dest.writeInt(viewType)
    }

    companion object CREATOR : Parcelable.Creator<FileItem> {
        override fun createFromParcel(parcel: Parcel): FileItem {
            return FileItem(parcel)
        }

        override fun newArray(size: Int): Array<FileItem?> {
            return arrayOfNulls(size)
        }
    }
}
