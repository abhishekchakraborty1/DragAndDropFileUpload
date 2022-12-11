package com.abhishekchakraborty.draganddropfileupload.ui

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.abhishekchakraborty.draganddropfileupload.model.FileResult

class FilesRecyclerViewAdapter(
    private val listener: OnItemClickListener
) : PagingDataAdapter<FileResult, FilesViewHolder>(FILES_COMPARATOR) {

    companion object {
        private val FILES_COMPARATOR = object : DiffUtil.ItemCallback<FileResult>() {
            override fun areItemsTheSame(oldItem: FileResult, newItem: FileResult): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(oldItem: FileResult, newItem: FileResult): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {
        return FilesViewHolder.create(parent, listener)
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        getItem(position)?.let { files ->
            holder.bind(files)
        }
    }
}
