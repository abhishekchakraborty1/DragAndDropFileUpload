package com.abhishekchakraborty.draganddropfileupload

import android.app.Application
import com.abhishekchakraborty.draganddropfileupload.api.apiModule
import com.abhishekchakraborty.draganddropfileupload.data.fileUploaderModule
import com.abhishekchakraborty.draganddropfileupload.data.repositoryModule
import com.abhishekchakraborty.draganddropfileupload.di.ViewModelsModule
import com.abhishekchakraborty.draganddropfileupload.networking.networkModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@ExperimentalCoroutinesApi
class DragAndDropFileUpload : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@DragAndDropFileUpload)
            modules(listOf(
                ViewModelsModule.modules,
                repositoryModule,
                fileUploaderModule,
                apiModule,
                networkModule
            ))
        }
    }
}