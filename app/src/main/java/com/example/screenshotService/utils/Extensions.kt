package com.example.screenshotService.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.permissionx.guolindev.PermissionX
import java.io.File

fun AppCompatActivity.checkPermissionsAndExecute(
    permissions: List<String>,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val notGrantedPermissions = permissions.filter {
        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }

    if (notGrantedPermissions.isNotEmpty()) {
        PermissionX.init(this)
            .permissions(*notGrantedPermissions.toTypedArray())
            .request { allGranted, _, _ ->
                if (allGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        onPermissionGranted()
                    }
                } else {
                    onPermissionDenied()
                }
            }
    } else {
        onPermissionGranted()
    }
}




fun Context.shareImage(path: String) {
    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    intent.type = "image/*"
    val uri = FileProvider.getUriForFile(this, this.packageName+".provider", File(path))
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    startActivity(Intent.createChooser(intent, "Share Via"));
}