package com.example.screenshotService.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.screenshotService.databinding.ActivityPreviewBinding

class PreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this@PreviewActivity).load(intent.getStringExtra("path").toString()).into(binding.imageView)
    }
}