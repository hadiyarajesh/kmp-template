package com.hadiyarajesh.kmptemplate.testutil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 * Owns ViewModels created in a test, the way a screen's ViewModelStore does. Call [clear] before
 * `Dispatchers.resetMain()` so no work in `viewModelScope` outlives the test and fails a later one.
 */
internal class TestViewModelStore {
    private val store = ViewModelStore()
    private var count = 0

    @Suppress("UNCHECKED_CAST")
    fun <T : ViewModel> add(viewModel: T): T {
        val factory = object : ViewModelProvider.Factory {
            override fun <VM : ViewModel> create(
                modelClass: KClass<VM>,
                extras: CreationExtras
            ): VM = viewModel as VM
        }
        val provider = ViewModelProvider.create(store, factory)
        return provider["viewModel${count++}", viewModel::class as KClass<T>]
    }

    fun clear() = store.clear()
}
