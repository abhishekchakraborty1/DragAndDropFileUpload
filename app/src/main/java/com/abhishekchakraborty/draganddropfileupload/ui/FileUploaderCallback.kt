package com.abhishekchakraborty.draganddropfileupload.ui

interface FileUploaderCallback {
        fun onError()
        fun onFinish()
        fun onProgressUpdate(currentpercent: Int, totalpercent: Int, filenumber: Int)
}