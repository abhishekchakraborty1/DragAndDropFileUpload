package com.abhishekchakraborty.draganddropfileupload.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FileResult(
    @SerializedName("id")
    val id: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("link")
    val link: String,
    @SerializedName("created")
    val created: String
): Parcelable