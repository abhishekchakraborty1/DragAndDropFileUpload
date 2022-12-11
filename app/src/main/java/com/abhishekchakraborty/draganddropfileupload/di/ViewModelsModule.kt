package com.abhishekchakraborty.draganddropfileupload.di

import com.abhishekchakraborty.draganddropfileupload.viewModel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ViewModelsModule {
	companion object{
		@ExperimentalCoroutinesApi
		val modules = module {
			viewModel { MainViewModel(get(),get()) }
		}
	}
}
