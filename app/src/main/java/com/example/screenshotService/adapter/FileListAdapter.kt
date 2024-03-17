package com.example.screenshotService.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.screenshotService.R
import com.example.screenshotService.model.FileItem
import javax.inject.Inject

class FileListAdapter @Inject constructor() :
    ListAdapter<FileItem, RecyclerView.ViewHolder>(FileDiffCallback()) {

    private var listener: FileListener? = null
    fun setListener(listener: FileListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
       return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)
        when (holder.itemViewType) {

            ITEM_VIEW_TYPE_FILE -> {
                // Bind file view holder
                val fileViewHolder = holder as FileViewHolder
                Glide.with(fileViewHolder.imageView.context).load(item.path)
                    .into(fileViewHolder.imageView)

                fileViewHolder.itemView.setOnClickListener {
                    if (it.id != R.id.moreOptions) {
                        listener?.open(position)
                    }
                }
                fileViewHolder.moreOptions.setOnClickListener {
                    showPopupMenu(it, position)
                }
            }
        }
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: AppCompatImageView = itemView.findViewById(R.id.imageView)
        val moreOptions: AppCompatImageView = itemView.findViewById(R.id.moreOptions)
    }


    private fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.popup_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.openImage -> {
                    listener?.open(position = position)
                    popupMenu.dismiss()
                    true
                }

                R.id.shareImage -> {
                    listener?.share(position = position)
                    popupMenu.dismiss()
                    true
                }

                R.id.deleteImage -> {
                    listener?.delete(position = position)
                    popupMenu.dismiss()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    class FileDiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem.path == newItem.path
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 3 == 2) ITEM_VIEW_TYPE_AD else ITEM_VIEW_TYPE_FILE
    }

    companion object {
        const val ITEM_VIEW_TYPE_FILE = 0
        const val ITEM_VIEW_TYPE_AD = 1
    }

}

