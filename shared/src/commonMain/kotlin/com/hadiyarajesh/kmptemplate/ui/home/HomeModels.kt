package com.hadiyarajesh.kmptemplate.ui.home

import androidx.paging.PagingData
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import kotlinx.coroutines.flow.Flow

internal sealed interface HomeScreenUiState {
    data object Initial : HomeScreenUiState
    data object Loading : HomeScreenUiState
    data class Success(val images: Flow<PagingData<Image>>) : HomeScreenUiState
    data class Error(val msg: String) : HomeScreenUiState
}

internal sealed interface HomeAction {
    data object Start : HomeAction
}
