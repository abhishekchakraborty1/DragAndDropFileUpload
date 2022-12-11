package com.abhishekchakraborty.draganddropfileupload.presentation.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abhishekchakraborty.draganddropfileupload.model.Lce
import com.abhishekchakraborty.draganddropfileupload.model.UploadFile
import kotlinx.coroutines.Job

abstract class BaseViewModel<ViewState : BaseViewState,
        ViewAction : BaseViewEffect,
        Event: BaseEvent,
        Result: BaseResult>(initialState: ViewState) :
    ViewModel() {

    var uploadAction: MutableLiveData<UploadFile> = MutableLiveData<UploadFile>()
    internal val viewStateLD = MutableLiveData<ViewState>()
    private val viewEffectLD = MutableLiveData<ViewAction>()
    val viewState: LiveData<ViewState> get() = viewStateLD
    val viewEffects: LiveData<ViewAction> get() = viewEffectLD

    var loadJob: Job? = null

    fun onEvent(event: Event) {
        Log.d("Abhishek","----- event ${event.javaClass.simpleName}")
        eventToResult(event)
    }

    suspend fun onSuspendedEvent(event: Event) {
        Log.d("Abhishek","----- suspend event ${event.javaClass.simpleName}")
        suspendEventToResult(event)
    }

    abstract fun eventToResult(event: Event)

    abstract suspend fun suspendEventToResult(event: Event)

    abstract fun resultToViewState(result: Lce<Result>)

    abstract fun resultToViewEffect(result: Lce<Result>)
}

interface BaseViewState

interface BaseViewEffect

interface BaseEvent

interface BaseResult