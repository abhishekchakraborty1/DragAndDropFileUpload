package com.abhishekchakraborty.draganddropfileupload.presentation.extensions

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import java.io.File

val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Uri.getImageFilePath(contentResolver: ContentResolver): String? {
    val file = File(this.path)
    val filePath: List<String> = file.path.split(":")
    val imageId = filePath[filePath.size - 1]
    val cursor: Cursor? = contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        null,
        MediaStore.Images.Media._ID + " = ? ",
        arrayOf(imageId),
        null
    )
    if (cursor != null) {
        cursor.moveToFirst()
        return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()
    }
    return null
}

fun getFilePathFromUri(context: Context, uri: Uri?): String? {
    uri ?: return null
    uri.path ?: return null

    var newUriString = uri.toString()
    newUriString = newUriString.replace(
        "content://com.android.providers.downloads.documents/",
        "content://com.android.providers.media.documents/"
    )
    newUriString = newUriString.replace(
        "/msf%3A", "/image%3A"
    )
    val newUri = Uri.parse(newUriString)

    var realPath = String()
    val databaseUri: Uri
    val selection: String?
    val selectionArgs: Array<String>?
    if (newUri.path?.contains("/document/image:") == true) {
        databaseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        selection = "_id=?"
        selectionArgs = arrayOf(DocumentsContract.getDocumentId(newUri).split(":")[1])
    } else {
        databaseUri = newUri
        selection = null
        selectionArgs = null
    }
    try {
        val column = "_data"
        val projection = arrayOf(column)
        val cursor = context.contentResolver.query(
            databaseUri,
            projection,
            selection,
            selectionArgs,
            null
        )
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                realPath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
    } catch (e: Exception) {
        Log.i("GetFileUri Exception:", e.message ?: "")
    }
    val path = realPath.ifEmpty {
        when {
            newUri.path?.contains("/document/raw:") == true -> newUri.path?.replace(
                "/document/raw:",
                ""
            )
            newUri.path?.contains("/document/primary:") == true -> newUri.path?.replace(
                "/document/primary:",
                "/storage/emulated/0/"
            )
            else -> return null
        }
    }
    return if (path.isNullOrEmpty()) null else path
}

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun decodeSampledBitmapFromUri(
    contentResolver: ContentResolver,
    uri: Uri,
    reqWidth: Int,
    reqHeight: Int
): Bitmap? {
    // First decode with inJustDecodeBounds=true to check dimensions
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        contentResolver.openInputStream(uri)?.let {
            BitmapFactory.decodeStream(it, null, this)
        }

        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false
        contentResolver.openInputStream(uri)?.let {
            BitmapFactory.decodeStream(it, null, this)
        }
    }
}
