package com.abhishekchakraborty.draganddropfileupload.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.abhishekchakraborty.draganddropfileupload.api.FileService
import com.abhishekchakraborty.draganddropfileupload.model.FileResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import org.koin.dsl.module

@ExperimentalCoroutinesApi
@FlowPreview
val repositoryModule = module {
    single { FileRepository(get()) }
}

class FileRepository(private val service: FileService)  {

    companion object {
        private const val NETWORK_PAGE_SIZE = 1
    }

    /**
     * Fetch Files, expose them as a stream of data that will emit
     * every time we get more data from the network.
     */
    fun getFiles(): Flow<PagingData<FileResult>> {
        Log.d("FilesRepository", "New page")
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = true
            ),
            pagingSourceFactory = { FilePagingSource(service) }
        ).flow
   }

}
