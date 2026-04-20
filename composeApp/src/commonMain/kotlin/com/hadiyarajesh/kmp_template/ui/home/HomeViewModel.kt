package com.hadiyarajesh.kmp_template.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hadiyarajesh.kmp_template.data.database.entity.Image
import com.hadiyarajesh.kmp_template.data.repository.HomeRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeScreenUiState {
    data object Initial : HomeScreenUiState
    data object Loading : HomeScreenUiState
    data class Success(val images: Flow<PagingData<Image>>) : HomeScreenUiState
    data class Error(val msg: String) : HomeScreenUiState
}

@Inject
class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Initial)
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    fun loadData() {
        if (_uiState.value is HomeScreenUiState.Success) return

        _uiState.value = HomeScreenUiState.Loading
        try {
            _uiState.value = HomeScreenUiState.Success(
                images = homeRepository.getImages().cachedIn(viewModelScope)
            )
        } catch (e: Exception) {
            _uiState.value = HomeScreenUiState.Error(msg = e.message ?: "Something went wrong")
        }
    }

    fun saveSelectedImage(image: Image) {
        viewModelScope.launch {
            homeRepository.saveSelectedImage(image)
        }
    }
}
