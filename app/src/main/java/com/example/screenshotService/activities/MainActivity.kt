package com.example.screenshotService.activities

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.screenshotService.R
import com.example.screenshotService.adapter.FileListAdapter
import com.example.screenshotService.adapter.FileListAdapter.Companion.ITEM_VIEW_TYPE_AD
import com.example.screenshotService.adapter.FileListener
import com.example.screenshotService.databinding.ActivityMainBinding
import com.example.screenshotService.model.FileItem
import com.example.screenshotService.repo.ImageViewModel
import com.example.screenshotService.service.ScreenShotService
import com.example.screenshotService.utils.checkPermissionsAndExecute
import com.example.screenshotService.utils.shareImage
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val viewModel: ImageViewModel by viewModels()

    @Inject
    lateinit var adapter: FileListAdapter

    private var imagesList: ArrayList<FileItem> = ArrayList()

    private var mpManager: MediaProjectionManager? = null
    private var REQUEST_CODE_SCREEN_SHOT = 4234
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    val REQUEST_CODE = 101
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Toast.makeText(
            this@MainActivity,
            "Developed By Hammad Ahmed on 27 Feb 11:16pm",
            Toast.LENGTH_SHORT
        ).show()

        mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        recyclerView = binding.recyclerView
        mSwipeRefreshLayout = binding.swipeToRefresh

        val gridLayoutManager = GridLayoutManager(this@MainActivity, 2)

        recyclerView.layoutManager = gridLayoutManager

        mSwipeRefreshLayout.setOnRefreshListener {
            getAllScreenshots()
            mSwipeRefreshLayout.isRefreshing = false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissionsAndExecute(listOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES
            ), onPermissionGranted = {
                getAllScreenshots()
                if (!Settings.canDrawOverlays(this)) {
                    val REQUEST_CODE = 101
                    val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    myIntent.setData(Uri.parse("package:$packageName"))
                    startActivityForResult(myIntent, REQUEST_CODE)
                }
//                else if (!isServiceRunning()) {
//                    val captureIntent = mpManager?.createScreenCaptureIntent()
//                    if (captureIntent != null) {
//                        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_SHOT)
//                    }
//
//                }
            }, onPermissionDenied = {

            })
        } else {
            checkPermissionsAndExecute(listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), onPermissionGranted = {
                getAllScreenshots()
                if (!Settings.canDrawOverlays(this)) {

                    val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    myIntent.setData(Uri.parse("package:$packageName"))
                    startActivityForResult(myIntent, REQUEST_CODE)
                } else if (!isServiceRunning()) {
                    val captureIntent = mpManager?.createScreenCaptureIntent()
                    if (captureIntent != null) {
                        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_SHOT)
                    }

                }
            }, onPermissionDenied = {

            })
        }
    }

    private fun getAllScreenshots() {
        imagesList.clear()
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            resources.getString(R.string.app_name)
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }

        viewModel.startObservingDirectoryChanges(dir.absolutePath)
        viewModel.imagesLiveData.observe(this) { images ->
            imagesList.clear()
            Log.e("getAllScreenshots", "getAllScreenshots: inside obs ${images.size}")
            if (!images.isNullOrEmpty()) {
                binding.pb.visibility = GONE
                binding.noFilesTv.visibility = GONE
                images.forEachIndexed { index, item ->
                    imagesList.add(item)
                    if ((index + 1) % 3 == 0) {
                        imagesList.add(FileItem(path = null, viewType = ITEM_VIEW_TYPE_AD))
                    }
                }
            } else {
                binding.pb.visibility = GONE
                binding.noFilesTv.visibility = VISIBLE
            }
            adapter.submitList(imagesList)
            recyclerView.adapter = adapter
        }

        viewModel.getImagesFromDirectory(dir.absolutePath)

        adapter.setListener(object : FileListener {
            override fun open(position: Int) {
                    Intent(this@MainActivity, PreviewActivity::class.java).apply {
                        putExtra("path", imagesList[position].path)
                        startActivity(this)
                    }
            }

            override fun share(position: Int) {
                imagesList[position].path?.let { shareImage(it) }
            }

            override fun delete(position: Int) {
                val fileToDelete = imagesList[position].path?.let { File(it) }

                Log.e("fileDelete", "delete: path of file is $fileToDelete")
                val deleted = fileToDelete?.delete()
                if (deleted == true) {
                    Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()
                    imagesList.removeAt(position)
                    adapter.submitList(imagesList)
                } else {
                    Toast.makeText(this@MainActivity, "Unable to Delete", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = manager.getRunningServices(Int.MAX_VALUE)
        for (service in services) {
            if (ScreenShotService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop observing directory changes
        viewModel.stopObservingDirectoryChanges()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCREEN_SHOT) {
            data?.let { startScreenShotService(resultCode, it) }
        } else {
            if (!isServiceRunning()) {
                val captureIntent = mpManager?.createScreenCaptureIntent()
                if (captureIntent != null) {
                    startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_SHOT)
                }
            }
        }
    }

    private fun startScreenShotService(resultCode: Int, captureIntent: Intent) {
        val intent = Intent(this, ScreenShotService::class.java)
        intent.putExtra(ScreenShotService.EXTRA_RESULT_CODE, resultCode)
        intent.putExtras(captureIntent)
        intent.action = "observeData"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}