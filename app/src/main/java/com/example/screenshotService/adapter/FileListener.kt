package com.example.screenshotService.adapter

interface FileListener {
    fun open(position: Int)
    fun share(position: Int)
    fun delete(position: Int)
}