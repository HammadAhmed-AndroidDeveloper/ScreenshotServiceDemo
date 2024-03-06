package com.example.screenshotService.repo

import android.os.FileObserver

class DirectoryObserver(
    directoryPath: String,
    private val onChange: () -> Unit
) : FileObserver(directoryPath) {

    override fun onEvent(event: Int, path: String?) {
        if (event == CREATE || event == DELETE) {
            onChange.invoke()
        }
    }
}
