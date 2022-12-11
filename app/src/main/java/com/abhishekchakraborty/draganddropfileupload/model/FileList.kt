package com.abhishekchakraborty.draganddropfileupload.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FileList(
    @SerializedName("count")
    val nextPage: Int,
    @SerializedName("success")
    val success: String,
    @SerializedName("nodes")
    val results: List<FileResult>
): Parcelable