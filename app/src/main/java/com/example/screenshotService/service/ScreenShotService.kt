package com.example.screenshotService.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.screenshotService.R

class ScreenShotService : Service() {
    private var mpManager: MediaProjectionManager? = null
    private var mProjection: MediaProjection? = null

    private var virtualDisplay: VirtualDisplay? = null

    private var screenDensity = 0
    private var displayWidth = 0
    private var displayHeight = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                234, createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(234, createNotification())
        }

            mpManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            setDeviceDisplay()
        if (intent != null) {
            setUpMediaProjection(intent)
        }

        return START_STICKY
    }
    private fun createNotification(): Notification {
        val channelId = "CustomChannel"
        val channelName = "Foreground Service Channel"

        val actionIntent1 = Intent(this, MyBroadcastReceiver::class.java)
        actionIntent1.action = ACTION_SCREENSHOT
        val p1 = PendingIntent.getBroadcast(this, 0, actionIntent1, PendingIntent.FLAG_MUTABLE)

        val actionIntent2 = Intent(this, MyBroadcastReceiver::class.java)
        actionIntent2.action = ACTION_CANCEL
        val p2 = PendingIntent.getBroadcast(this, 0, actionIntent2, PendingIntent.FLAG_MUTABLE)

        val notificationBuilder =
            NotificationCompat.Builder(this, channelId).setContentTitle("Foreground Service")
                .setContentText("Tap to open app").setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(R.drawable.ic_launcher_foreground, "Screenshot", p1)
                .addAction(R.drawable.ic_launcher_foreground, "Cancel", p2)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }

    class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {

            when (intent?.action) {
                ACTION_SCREENSHOT -> {
                    Log.e("imageReader", "onReceive: image reader is $imageReader", )
                    saveScreenshotUtils = imageReader?.let { SaveScreenshotUtils(context, it) }
                    saveScreenshotUtils?.start()
                }

                ACTION_CANCEL -> {
                    context.stopService(Intent(context, ScreenShotService::class.java))
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this@ScreenShotService, "Destroyed", Toast.LENGTH_SHORT).show()
        stopSelf()
    }
    private fun setDeviceDisplay() {
        val displayMetrics = resources.displayMetrics
        screenDensity = displayMetrics.densityDpi / 2
        displayWidth = displayMetrics.widthPixels / 2
        displayHeight = displayMetrics.heightPixels / 2
    }
    private fun setUpMediaProjection(intent: Intent) {
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        mProjection = mpManager?.getMediaProjection(resultCode, intent)
        setUpVirtualDisplay()
    }

    @SuppressLint("WrongConstant")
    private fun setUpVirtualDisplay() {
        imageReader = ImageReader.newInstance(
            displayWidth, displayHeight, PixelFormat.RGBA_8888, 2
        )
        virtualDisplay = mProjection?.createVirtualDisplay(
            "ScreenCapture",
            displayWidth, displayHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }
    companion object {
        const val EXTRA_RESULT_CODE = "test"
        var saveScreenshotUtils: SaveScreenshotUtils? = null
        var imageReader: ImageReader? = null
        const val ACTION_SCREENSHOT = "action_screenshot"
        const val ACTION_CANCEL = "action_cancel"
    }
}