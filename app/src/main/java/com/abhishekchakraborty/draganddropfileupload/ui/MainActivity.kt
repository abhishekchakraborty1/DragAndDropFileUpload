/*
 * Copyright (c) 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.abhishekchakraborty.draganddropfileupload.ui

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.view.View.DRAG_FLAG_GLOBAL
import android.view.View.DRAG_FLAG_GLOBAL_URI_READ
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.DragStartHelper
import androidx.draganddrop.DropHelper
import androidx.lifecycle.lifecycleScope
import com.abhishekchakraborty.draganddropfileupload.R
import com.abhishekchakraborty.draganddropfileupload.databinding.ActivityMainBinding
import com.abhishekchakraborty.draganddropfileupload.presentation.extensions.decodeSampledBitmapFromUri
import com.abhishekchakraborty.draganddropfileupload.presentation.extensions.getImageFilePath
import com.abhishekchakraborty.draganddropfileupload.presentation.extensions.px
import com.abhishekchakraborty.draganddropfileupload.viewModel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Nullable
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*


@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val model by viewModel<MainViewModel>()

    var files: ArrayList<String> = ArrayList()
    private var pDialog: ProgressDialog? = null

    companion object {
        private const val LOG_TAG = "DragDropSample"
        private const val MAX_LENGTH = 200
        fun logD(message: String) {
            Log.d(LOG_TAG, message)
        }

        fun logE(message: String) {
            Log.e(LOG_TAG, message)
        }
    }

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val mainConstraintLayout = binding.root
        setContentView(mainConstraintLayout)

        pDialog = ProgressDialog(this)

        binding.buttonSelectFiles.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    2
                )
            } else {
                val intent = Intent()
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
            }
        }

        binding.buttonNewTask.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            })
        }

        binding.buttonListFiles.setOnClickListener {
            // go to list view to list the file details
            startActivity(Intent(this, FileListActivity::class.java).apply {
                //addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
            })
        }


        dragImageViewPreparation(binding.imageDragItem,"earth.png")
        dragImageViewPreparation(binding.imageDragItemTwo,"bird.png")
        dragImageViewPreparation(binding.imageDragItemThree,"flowers.png")

        DropHelper.configureView(
            this,
            binding.textDropTarget,
            arrayOf(
                MIMETYPE_TEXT_PLAIN,
                "image/*",
                "application/x-arc-uri-list" // Support external items on Chrome OS Android 9
            ),
            DropHelper.Options.Builder()
                .setHighlightColor(getColor(R.color.purple_300))
                // Match the radius of the view's background drawable
                .setHighlightCornerRadiusPx(resources.getDimensionPixelSize(R.dimen.drop_target_corner_radius))
                .build()
        ) { _, payload ->
            resetDropTarget()

            // For the purposes of this demo, only handle the first ClipData.Item
            val item = payload.clip.getItemAt(0)
            val (_, remaining) = payload.partition { it == item }

            when {
                payload.clip.description.hasMimeType(MIMETYPE_TEXT_PLAIN) ->
                    handlePlainTextDrop(item)
                else ->
                    handleImageDrop(item)
            }

            // Allow the system to handle any remaining ClipData.Item objects if applicable
            remaining
        }

        binding.buttonClear.setOnClickListener {
            resetDropTarget()
        }
    }

    private fun dragImageViewPreparation(imageView: ImageView, name:String){
        DragStartHelper(imageView) { view, _ ->
            val imageFile = File(File(filesDir, "images"), name)

            if (!imageFile.exists()) {
                // Local file doesn't exist, create it
                File(filesDir, "images").mkdirs()
                // Write the file to local storage
                ByteArrayOutputStream().use { bos ->
                    (view as ImageView).drawable.toBitmap()
                        .compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
                    FileOutputStream(imageFile).use { fos ->
                        fos.write(bos.toByteArray())
                        fos.flush()
                    }
                }
            }

            val imageUri =
                FileProvider.getUriForFile(
                    this,
                    "com.abhishekchakraborty.draganddropfileupload.images",
                    imageFile
                )

            // Sets the appropriate MIME types automatically
            val dragClipData = ClipData.newUri(contentResolver, "Image", imageUri)

            // Set the visual look of the dragged object
            // Can be extended and customized. We use the default here.
            val dragShadow = View.DragShadowBuilder(view)

            // Starts the drag, note: global flag allows for cross-application drag
            view.startDragAndDrop(
                dragClipData,
                dragShadow,
                null,
                // Since this is a "content:" URI and not just plain text, we can use the
                // DRAG_FLAG_GLOBAL_URI_READ to allow other apps to read from our content provider.
                // Without it, other apps won't receive the drag events
                DRAG_FLAG_GLOBAL.or(DRAG_FLAG_GLOBAL_URI_READ)
            )
        }.attach()
    }

    private fun handlePlainTextDrop(item: ClipData.Item) {
        // The text is contained in the ClipData.Item
        if (item.text != null) {
            binding.textDropTarget.setTextSize(COMPLEX_UNIT_SP, 22f)
            binding.textDropTarget.text = getString(
                R.string.drop_text,
                item.text.substring(0, item.text.length.coerceAtMost(MAX_LENGTH))
            )
        } else {
            // The text is in a file pointed to by the ClipData.Item
            val parcelFileDescriptor: ParcelFileDescriptor? = try {
                contentResolver.openFileDescriptor(item.uri, "r")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                logE("FileNotFound")
                return
            }

            if (parcelFileDescriptor == null) {
                logE("Could not load file")
                binding.textDropTarget.text =
                    resources.getString(R.string.drop_error, item.uri.toString())
            } else {
                val fileDescriptor = parcelFileDescriptor.fileDescriptor
                val bytes = ByteArray(MAX_LENGTH)

                try {
                    FileInputStream(fileDescriptor).use {
                        it.read(bytes, 0, MAX_LENGTH)
                    }
                } catch (e: java.lang.Exception) {
                    logE("Unable to read file: ${e.message}")
                }

                binding.textDropTarget.setTextSize(COMPLEX_UNIT_SP, 15f)
                binding.textDropTarget.text = getString(R.string.drop_text, String(bytes))
            }
        }
    }

    private fun handleImageDrop(item: ClipData.Item) {
        item.uri?.let { uri ->
            val size = 72.px
            val filesToUpload = arrayOfNulls<File>(1)
            val imageFile = uri.lastPathSegment?.let { File(File(filesDir, "images"), it) }
            if (imageFile != null) {
                if (imageFile.exists()) {
                    val uri = Uri.parse("android.resource://$packageName/$imageFile")
                    filesToUpload[0] = uri.path?.let { File(it) }
                    lifecycleScope.launch {
                        model.doFileUpload(filesToUpload)
                        observeUploadState()
                        }
                    }
                }

            decodeSampledBitmapFromUri(
                contentResolver,
                uri,
                size,
                size
            )?.let { bitmap ->
                binding.textDropTarget.text =
                    getString(R.string.drop_image, bitmap.width, bitmap.height)
                val drawable = BitmapDrawable(resources, bitmap).apply {
                    val ratio =
                        intrinsicHeight.toFloat() / intrinsicWidth.toFloat()
                    setBounds(0, 0, size, (size * ratio).toInt())
                }
                binding.textDropTarget.setCompoundDrawables(
                    drawable,
                    null,
                    null,
                    null
                )
            }
        } ?: run {
            logE("Clip data is missing URI")
        }
    }

    private fun resetDropTarget() {
        binding.textDropTarget.setTextSize(COMPLEX_UNIT_SP, 22f)
        binding.textDropTarget.background =
            ContextCompat.getDrawable(this, R.drawable.bg_target_normal)
        binding.textDropTarget.text = resources.getString(R.string.drop_target)
        binding.textDropTarget.setCompoundDrawables(null, null, null, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && null != data) {
            if (data.clipData != null) {
                val count =
                    data.clipData!!.itemCount //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                for (i in 0 until count) {
                    val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                    //getFilePathFromUri(this,imageUri)?.let { files.add(it) }
                    imageUri.getImageFilePath(contentResolver)?.let { files.add(it) }
                }
            }

            if (files.size > 0) {
                val filesToUpload = arrayOfNulls<File>(files.size)
                for (i in files.indices) {
                    filesToUpload[i] = File(files[i])
                }
                lifecycleScope.launch {
                    model.doFileUpload(filesToUpload)
                    observeUploadState()
                }
            }
        }
    }

    fun updateProgress(value: Int, title: String?, msg: String?) {
        pDialog?.setTitle(title)
        pDialog?.setMessage(msg)
        pDialog?.setProgress(value)
    }

    private fun showProgress(str: String?) {
        try {
            pDialog?.setCancelable(false)
            pDialog?.setTitle("Please wait")
            //pDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            //pDialog?.setMax(100) // Progress Dialog Max Value
            pDialog?.setMessage(str)
            if (pDialog?.isShowing == true) pDialog?.dismiss()
            pDialog?.show()
        } catch (e: Exception) {
        }
    }

    private fun hideProgress() {
        try {
            if (pDialog?.isShowing == true) {
                pDialog?.dismiss()
                resetDropTarget()
            }
        } catch (e: Exception) {
        }
    }

    private fun observeUploadState() {
        showProgress("File is uploading...")
        model.uploadAction.observe(this) {
            Toast.makeText(this, it.id, Toast.LENGTH_SHORT).show()
            hideProgress()
        }
    }




}
