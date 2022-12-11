/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abhishekchakraborty.draganddropfileupload.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abhishekchakraborty.draganddropfileupload.R
import com.abhishekchakraborty.draganddropfileupload.model.FileResult

/**
 * View Holder for a [Files] RecyclerView list item.
 */
class FilesViewHolder(view: View, private val listener: OnItemClickListener) : RecyclerView.ViewHolder(view) {
    private val name: TextView = view.findViewById(R.id.primary_text)
    private val description: TextView = view.findViewById(R.id.sub_text)
    private val media: ImageView = view.findViewById(R.id.media_image)
    private var files: FileResult? = null

    init {
        view.setOnClickListener {
            files?.link?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }
    }

    fun bind(news: FileResult?) {
        if (news == null) {
            val resources = itemView.resources
            name.text = resources.getString(R.string.loading)
            description.text = resources.getString(R.string.loading)
        } else {
            showData(news)
        }
    }

    private fun showData(files: FileResult) {
        this.files= files
        name.text = files.name
        description.text = files.created    }

    companion object {
        fun create(parent: ViewGroup, listener: OnItemClickListener): FilesViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.file_list_item, parent, false)
            return FilesViewHolder(view, listener)
        }
    }
}
