package com.hadiyarajesh.kmp_template.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.hadiyarajesh.kmp_template.data.database.entity.Image
import com.hadiyarajesh.kmp_template.ui.components.ErrorItem
import com.hadiyarajesh.kmp_template.ui.components.ImagePlaceholder
import com.hadiyarajesh.kmp_template.ui.components.LoadingIndicator
import com.hadiyarajesh.kmp_template.ui.components.items
import com.hadiyarajesh.kmp_template.ui.theme.AppTheme
import kmp_template.composeapp.generated.resources.Res
import kmp_template.composeapp.generated.resources.app_name
import kmp_template.composeapp.generated.resources.failed_to_load_image
import kmp_template.composeapp.generated.resources.go_to_top
import kmp_template.composeapp.generated.resources.ic_arrow_up
import kmp_template.composeapp.generated.resources.placeholder_image
import kmp_template.composeapp.generated.resources.retry
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel,
    onNavigateClick: (Image) -> Unit
) {
    val homeScreenUiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = homeScreenUiState,
        loadData = { viewModel.loadData() },
        onImageClick = { image ->
            viewModel.saveSelectedImage(image)
            onNavigateClick(image)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeScreenUiState,
    loadData: () -> Unit,
    onImageClick: (Image) -> Unit
) {
    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.app_name)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                HomeScreenUiState.Initial -> Unit

                HomeScreenUiState.Loading -> {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                }

                is HomeScreenUiState.Success -> {
                    ImageGrid(
                        modifier = Modifier.fillMaxSize(),
                        images = uiState.images.collectAsLazyPagingItems(),
                        onImageClick = onImageClick
                    )
                }

                is HomeScreenUiState.Error -> {
                    ErrorItem(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        text = uiState.msg
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageGrid(
    modifier: Modifier = Modifier,
    images: LazyPagingItems<Image>,
    onImageClick: (Image) -> Unit
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val showGoToTopButton by remember {
        derivedStateOf {
            gridState.canScrollBackward
        }
    }

    when (val refreshState = images.loadState.refresh) {
        LoadState.Loading -> {
            LoadingIndicator(modifier = modifier)
        }

        is LoadState.Error -> {
            PagingErrorItem(
                modifier = modifier,
                message = refreshState.error.message ?: "Something went wrong",
                onRetryClick = images::retry
            )
        }

        is LoadState.NotLoading -> {
            Box(modifier = modifier) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    columns = GridCells.Adaptive(104.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(
                        items = images,
                        key = { image -> image.imageId }
                    ) { image ->
                        WallpaperImageCard(
                            image = image,
                            onImageClick = onImageClick
                        )
                    }

                    appendLoadStateItems(images)
                }

                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    visible = showGoToTopButton,
                    enter = fadeIn() + scaleIn(initialScale = 0.82f),
                    exit = fadeOut() + scaleOut(targetScale = 0.82f)
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                gridState.animateScrollToItem(0)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_up),
                            contentDescription = stringResource(Res.string.go_to_top)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperImageCard(
    modifier: Modifier = Modifier,
    image: Image,
    onImageClick: (Image) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clickable { onImageClick(image) },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(),
            model = image.thumbnailUrl(targetWidth = 360, targetHeight = 640),
            contentDescription = image.description,
            placeholder = painterResource(Res.drawable.placeholder_image),
            fallback = painterResource(Res.drawable.placeholder_image),
            error = painterResource(Res.drawable.placeholder_image),
            contentScale = ContentScale.Crop
        )
    }
}

private fun LazyGridScope.appendLoadStateItems(images: LazyPagingItems<Image>) {
    when (val appendState = images.loadState.append) {
        LoadState.Loading -> {
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                )
            }
        }

        is LoadState.Error -> {
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                PagingErrorItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                    message = appendState.error.message ?: "Something went wrong",
                    onRetryClick = images::retry
                )
            }
        }

        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun PagingErrorItem(
    modifier: Modifier = Modifier,
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ErrorItem(text = message)

        OutlinedButton(onClick = onRetryClick) {
            Text(text = stringResource(Res.string.retry))
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreenContent(
            uiState = HomeScreenUiState.Success(
                images = flowOf(
                    PagingData.from(
                        listOf(
                            Image(
                                imageId = "1",
                                author = "Alejandro Escamilla",
                                width = 5000,
                                height = 3333,
                                sourceUrl = "https://unsplash.com/photos/yC-Yzbqy7PY",
                                downloadUrl = "https://picsum.photos/id/1/5000/3333",
                                description = "Photo by Alejandro Escamilla",
                                altText = stringResource(Res.string.failed_to_load_image)
                            )
                        )
                    )
                )
            ),
            loadData = {},
            onImageClick = {}
        )
    }
}

@Preview
@Composable
private fun WallpaperImageCardPreview() {
    AppTheme {
        WallpaperImageCard(
            image = Image(
                imageId = "1",
                author = "Alejandro Escamilla",
                width = 5000,
                height = 3333,
                sourceUrl = "https://unsplash.com/photos/yC-Yzbqy7PY",
                downloadUrl = "https://picsum.photos/id/1/5000/3333",
                description = "Photo by Alejandro Escamilla",
                altText = stringResource(Res.string.failed_to_load_image)
            ),
            onImageClick = {}
        )
    }
}

@Preview
@Composable
private fun ImagePlaceholderPreview() {
    AppTheme {
        ImagePlaceholder(
            modifier = Modifier.fillMaxSize(),
            contentDescription = stringResource(Res.string.failed_to_load_image)
        )
    }
}
