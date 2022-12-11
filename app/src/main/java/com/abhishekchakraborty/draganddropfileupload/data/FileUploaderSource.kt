package com.abhishekchakraborty.draganddropfileupload.data

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.abhishekchakraborty.draganddropfileupload.api.FileService
import com.abhishekchakraborty.draganddropfileupload.model.UploadFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import org.koin.dsl.module
import java.io.File
import java.io.FileInputStream
import java.io.IOException

@ExperimentalCoroutinesApi
val fileUploaderModule = module {
    single { FileUploaderSource(get()) }
}

class FileUploaderSource(private val service: FileService)
{
    //var fileUploaderCallback: FileUploaderCallback? = null
    private lateinit var files: Array<File?>
    var uploadIndex = -1
    private var uploadURL = ""
    private var totalFileLength: Long = 0
    private var totalFileUploaded: Long = 0


    class PRRequestBody(file: File?) : RequestBody() {
        private var mFile: File? = null

        init {
            if (file != null) {
                mFile = file
            }
        }

        override fun contentType(): MediaType? {
            // i want to upload only images
            return "image/*".toMediaTypeOrNull()
        }

        @Throws(IOException::class)
        override fun contentLength(): Long {
            return mFile?.length() ?: 0
        }

        @Throws(IOException::class)
        override fun writeTo(sink: BufferedSink) {
            val fileLength: Long = mFile?.length() ?: 0
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            val inputStream = FileInputStream(mFile)
            var uploaded: Long = 0
            inputStream.use { inputStream ->
                var read: Int
                val handler = Handler(Looper.getMainLooper())
                while (inputStream.read(buffer).also { read = it } != -1) {
                    // update progress on UI thread
                    handler.post(ProgressUpdater(uploaded, fileLength))
                    uploaded += read.toLong()
                    sink.write(buffer, 0, read)
                }
            }
        }

        companion object {
            private const val DEFAULT_BUFFER_SIZE = 2048
        }
    }

    suspend fun uploadFiles(
        files: Array<File?>
    ) : Result<UploadFile?> {
        this.files = files
        uploadIndex = -1
        this.totalFileUploaded = 0
        this.totalFileLength = 0
        uploadIndex = -1
        for (i in files.indices) {
            totalFileLength += files[i]?.length() ?: 0
        }
        return uploadNext()
    }

    private suspend fun uploadNext() : Result<UploadFile?> {
        var responseData: Result<UploadFile?>? = null
        if (files.isNotEmpty()) {
            if (uploadIndex != -1) totalFileUploaded += files[uploadIndex]?.length() ?: 0
            uploadIndex++
            if (uploadIndex < files.size) {
                return uploadSingleFile(uploadIndex)
            } else {
                //fileUploaderCallback!!.onFinish()
            }
        } else {
            //fileUploaderCallback!!.onFinish()
        }
        return Result.failure(Throwable("Failed"))
    }

    private suspend fun uploadSingleFile(index: Int): Result<UploadFile> {
        val fileBody = PRRequestBody(files[index])
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData("file", files[index]?.name, fileBody)
        val call = service.uploadFile(filePart)
        if(call.success){
            uploadNext()
        }else{
            Result.failure(Throwable("Failed"))
        }
        return Result.success(call)
    }

    class ProgressUpdater(private val mUploaded: Long, private val mTotal: Long) :Runnable {
        override fun run() {
            val currentPercent = (100 * mUploaded / mTotal).toInt()
            Log.d("Current Percent", "run: ".plus(currentPercent))
            /*val totalPercent: Int =
                (100 * (totalFileUploaded + mUploaded) / totalFileLength).toInt()
            fileUploaderCallback.onProgressUpdate(currentPercent, totalPercent, uploadIndex + 1)*/
        }
    }
}