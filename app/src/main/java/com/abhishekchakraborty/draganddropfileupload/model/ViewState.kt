package com.abhishekchakraborty.draganddropfileupload.model

import android.view.View
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import com.abhishekchakraborty.draganddropfileupload.presentation.base.BaseEvent
import com.abhishekchakraborty.draganddropfileupload.presentation.base.BaseResult
import com.abhishekchakraborty.draganddropfileupload.presentation.base.BaseViewEffect
import com.abhishekchakraborty.draganddropfileupload.presentation.base.BaseViewState

data class ListViewState(
    val page: PagingData<FileResult>? = null,
    val adapterList: List<FileResult> = emptyList(),
    val errorMessageResource: Int? = null,
    val errorMessage: String? = null,
    val loadingStateVisibility: Int? = View.GONE,
    val errorVisibility: Int? = View.GONE
): BaseViewState

sealed class ViewEffect: BaseViewEffect {
    data class TransitionToScreen(val photo: FileResult) : ViewEffect()
}

sealed class Event: BaseEvent {
    object SwipeToRefreshEvent: Event()
    data class LoadState(val state: CombinedLoadStates): Event()
    data class ListItemClicked(val item: FileResult): Event()
    // Suspended
    object ScreenLoad: Event()
    object UploadTrigger: Event()
}

sealed class Result: BaseResult {
    data class Error(val errorMessage: String?): Result()
    data class Content(val content: PagingData<FileResult>): Result()
    //data class ItemClickedResult(val item: Photo, val sharedElement: View) : Result()
}