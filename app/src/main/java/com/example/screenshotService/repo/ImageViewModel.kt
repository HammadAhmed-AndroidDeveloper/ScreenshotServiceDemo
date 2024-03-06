package com.example.screenshotService.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.screenshotService.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(private val repository: ImageRepository) : ViewModel() {

    private val _imagesLiveData = MutableLiveData<List<FileItem>>()
    val imagesLiveData: LiveData<List<FileItem>>
        get() = _imagesLiveData

    private var directoryObserver: DirectoryObserver? = null

    fun startObservingDirectoryChanges(directoryPath: String) {
        directoryObserver = DirectoryObserver(directoryPath) {
            // Refresh the data when a change is detected
            getImagesFromDirectory(directoryPath)
        }
        directoryObserver?.startWatching()
    }

    fun stopObservingDirectoryChanges() {
        directoryObserver?.stopWatching()
    }

    fun getImagesFromDirectory(directoryPath: String) {
        val data = repository.getImagesFromDirectory(directoryPath)
        _imagesLiveData.postValue(data)
    }
}
