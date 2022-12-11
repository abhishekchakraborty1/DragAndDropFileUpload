package com.abhishekchakraborty.draganddropfileupload.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UploadFile(
    @SerializedName("autoDelete")
    val autoDelete: Boolean,
    @SerializedName("created")
    val created: String,
    @SerializedName("downloads")
    val downloads: Int,
    @SerializedName("expires")
    val expires: String,
    @SerializedName("expiry")
    val expiry: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("link")
    val link: String,
    @SerializedName("maxDownloads")
    val maxDownloads: Int,
    @SerializedName("mimeType")
    val mimeType: String,
    @SerializedName("modified")
    val modified: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("size")
    val size: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("success")
    val success: Boolean
):Parcelable