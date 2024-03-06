package com.example.screenshotService.service

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import com.example.screenshotService.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class SaveScreenshotUtils internal constructor(
    private val context: Context,
    private val imgReader: ImageReader,
) : Thread(), Runnable {

    override fun run() {
        CoroutineScope(Dispatchers.Main).launch {
            val image = imgReader.acquireLatestImage()

            Log.e("prepareImageReader", "run: --> image is $image")
            if (image != null) {
                val planes = image.planes
                val pixelStride = planes?.get(0)?.pixelStride
                val rowPadding =
                    planes?.get(0)?.rowStride?.minus(pixelStride?.times(imgReader.width) ?: 0)
                val width = imgReader.width + (pixelStride?.let { rowPadding?.div(it) } ?: 0)
                val bitmap = Bitmap.createBitmap(width, imgReader.height, Bitmap.Config.ARGB_8888)
                planes?.get(0)?.let { bitmap.copyPixelsFromBuffer(it.buffer) }
                captureAndSaveImage(context, bitmap)
                image.close()
            }
        }


    }
    private fun captureAndSaveImage(context: Context, result: Bitmap) {
        val time = System.currentTimeMillis()
        val contentValues = ContentValues().apply {
            put("mime_type", "image/png")
            put("date_added", time)
            put(MediaStore.Images.Media.DISPLAY_NAME, "Screenshot_${Date().time}")
        }

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            context.resources.getString(R.string.app_name) + "/"
        )
        CoroutineScope(Dispatchers.IO).launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                contentValues.put("datetaken", time)
                contentValues.put(
                    "relative_path", "Pictures/" + context.getString(R.string.app_name)
                )
                contentValues.put("is_pending", true)
                val insert = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )
                if (insert != null) {
                    try {
                        val os = context.contentResolver.openOutputStream(insert)
                        if (os != null) {
                            result.compress(
                                Bitmap.CompressFormat.PNG, 100, os
                            )

                            os.close()
                            contentValues.put("is_pending", false)
                            context.contentResolver.update(insert, contentValues, null, null)

                            MediaScannerConnection.scanFile(
                                context, arrayOf(insert.toFile().absolutePath), null
                            ) { p0, p1 ->
                                Log.d(
                                    "onScanCompleted",
                                    "onScanCompleted: $p1 $p0"
                                )

                            };

                        }
                    } catch (e: Exception) {
                        Log.e("captureAndSaveImage", "captureAndSaveImage: ${e.message}")
                    }
                }
            } else {

                if (!file.exists()) {
                    file.mkdirs()
                }
                val file2 = File(file, "Screenshot_${Date().time}.png")
                try {
                    val os = FileOutputStream(file2)

                    result.compress(Bitmap.CompressFormat.PNG, 50, os)
                    os.close()
                    contentValues.put("_data", file2.absolutePath)
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                    )
                    MediaScannerConnection.scanFile(
                        context, arrayOf(file2.absolutePath), null
                    ) { p0, p1 -> Log.d("onScanCompleted", "onScanCompleted: $p1 $p0")

                    };
                } catch (e: Exception) {
                    Log.e("captureAndSaveImage", "saveBitmapImage: ${e.message}")
                }
            }
        }
    }
}