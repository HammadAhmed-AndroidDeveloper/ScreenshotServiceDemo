package com.example.screenshotService.repo

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.screenshotService.model.FileItem
import javax.inject.Inject

class ImageRepository @Inject constructor(private val context: Context) {

    val imagesList = arrayListOf<FileItem>()
    fun getImagesFromDirectory(directoryPath: String): List<FileItem> {
        imagesList.clear()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATA
        )

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            MediaStore.Images.Media.DATA + " like ? ", arrayOf("%$directoryPath%"),
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val size = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {

                val fileSize = cursor.getLong(size)
                val path = cursor.getString(data)

                if (checkFileSize(fileSize)) {
                    imagesList.add(FileItem(path))
                }
            }
        } ?: Log.e("ImageRepository", "Cursor is null or query failed.")
        Log.e("getImagesFromDirectory", "getImagesFromDirectory: my list size is ${imagesList.size}", )
        return imagesList
    }

    private fun checkFileSize(size: Long): Boolean {
        return size / 1024 > 5
    }
}
