package com.abhishekchakraborty.draganddropfileupload.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.abhishekchakraborty.draganddropfileupload.api.FileService
import com.abhishekchakraborty.draganddropfileupload.model.FileResult
import retrofit2.HttpException
import java.io.IOException

private const val STARTING_PAGE_INDEX = 0


class FilePagingSource(
    private val service: FileService
) : PagingSource<Int, FileResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FileResult> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = service.getFiles(position)
            val files = response.results
            Log.d("Abhishek", "Service -> getFiles: ${files.size}")
            LoadResult.Page(
                data = files,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position,
                nextKey = if (files.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, FileResult>): Int? {
        TODO("Not yet implemented")
    }
}