package com.abhishekchakraborty.draganddropfileupload.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abhishekchakraborty.draganddropfileupload.R
import com.abhishekchakraborty.draganddropfileupload.databinding.ActivityFilesListBinding
import com.abhishekchakraborty.draganddropfileupload.model.Event
import com.abhishekchakraborty.draganddropfileupload.model.FileResult
import com.abhishekchakraborty.draganddropfileupload.model.ListViewState
import com.abhishekchakraborty.draganddropfileupload.viewModel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@FlowPreview
@ExperimentalCoroutinesApi
class FileListActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, OnItemClickListener {

	companion object {
		private val TAG = FileListActivity::class.qualifiedName
		fun newInstance(): FileListActivity {
			return FileListActivity()
		}
	}

	private lateinit var binding: ActivityFilesListBinding

	private val filesAdapter: FilesRecyclerViewAdapter = FilesRecyclerViewAdapter(listener = this)

	// Lazy Inject ViewModel
	private val viewModel by viewModel<MainViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityFilesListBinding.inflate(layoutInflater)
		val mainConstraintLayout = binding.root
		setContentView(mainConstraintLayout)
		setupBinding()
		observeViewState()
		if (savedInstanceState == null) {
			lifecycleScope.launch {
				viewModel.onSuspendedEvent(Event.ScreenLoad)
			}
		}
	}


	private fun setupBinding() {
		binding.swiperefresh.setOnRefreshListener(this)
		binding.list.apply {
			layoutManager = LinearLayoutManager(context)
			addItemDecoration(VerticalSpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.list_item_decoration)))
			initAdapter()
		}
		binding.retryButton.setOnClickListener { Log.d("Abhishek", "click") }
	}

	private fun initAdapter() {
		binding.list.adapter = filesAdapter.withLoadStateHeaderAndFooter(
			header = FilesLoadStateAdapter { filesAdapter.retry() },
			footer = FilesLoadStateAdapter { filesAdapter.retry() }
		)
		filesAdapter.addLoadStateListener {
			Log.d("Abhishek", "loading state: ${it.toString()}")
			viewModel.onEvent(Event.LoadState(it))
		}
		setScrollToTopWHenRefreshedFromNetwork()
	}

	private fun setScrollToTopWHenRefreshedFromNetwork() {
		// Scroll to top when the list is refreshed from network.
		lifecycleScope.launch {
			filesAdapter.loadStateFlow
				// Only emit when REFRESH LoadState for RemoteMediator changes.
				.distinctUntilChangedBy { it.refresh }
				// Only react to cases where Remote REFRESH completes i.e., NotLoading.
				.filter { it.refresh is LoadState.NotLoading }
				.collect { binding.list.scrollToPosition(0) }
		}
	}

	private fun observeViewState() {
		viewModel.obtainState.observe(this) {
			Log.d(TAG, "observeViewState obtainState result: ${it.adapterList.size}")
			render(it)
		}
	}

	private fun render(state: ListViewState) {
		binding.swiperefresh.isRefreshing = false
		state.loadingStateVisibility?.let { binding.progressBar.visibility = it }
		lifecycleScope.launch {
			state.page?.let { filesAdapter.submitData(it) }
		}
		state.errorVisibility?.let {
			binding.mainListErrorMsg.visibility = it
			binding.retryButton.visibility = it
			state.errorMessage?.let { binding.mainListErrorMsg.text = state.errorMessage }
			state.errorMessageResource?.let { binding.mainListErrorMsg.text = getString(state.errorMessageResource) }
		}
	}

	override fun onRefresh() {
		lifecycleScope.launch {
			viewModel.onSuspendedEvent(Event.ScreenLoad)
		}
	}

	override fun onItemClick(item: FileResult) {
		// TODO: implement click
		//viewModel.event(Event.ListItemClicked(item))
	}
}
