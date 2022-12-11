package com.abhishekchakraborty.draganddropfileupload.api

import com.abhishekchakraborty.draganddropfileupload.model.FileList
import com.abhishekchakraborty.draganddropfileupload.model.UploadFile
import okhttp3.MultipartBody
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.http.*


val apiModule = module {
    single {
        val retrofit: Retrofit = get()
        retrofit.create(FileService::class.java)
    }
}

interface FileService {

    @GET("/")
    suspend fun getFiles(@Query("offset") page: Int): FileList

    @Multipart
    @POST("/")
    suspend fun uploadFile(@Part file: MultipartBody.Part?): UploadFile
}
