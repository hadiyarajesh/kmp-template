package com.hadiyarajesh.kmptemplate.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.ui.components.ErrorItem
import com.hadiyarajesh.kmptemplate.ui.components.LoadingIndicator
import com.hadiyarajesh.kmptemplate.ui.components.items
import com.hadiyarajesh.kmptemplate.ui.theme.AppTheme
import kmptemplate.shared.generated.resources.Res
import kmptemplate.shared.generated.resources.app_name
import kmptemplate.shared.generated.resources.go_to_top
import kmptemplate.shared.generated.resources.ic_arrow_up
import kmptemplate.shared.generated.resources.placeholder_image
import kmptemplate.shared.generated.resources.retry
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeScreenRoute(
    onImageClick: (Image) -> Unit,
    viewModel: HomeViewModel = homeViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) { viewModel.start() }

    HomeScreenContent(uiState = uiState, onImageClick = onImageClick)
}

@Composable
private fun HomeScreenContent(uiState: HomeScreenUiState, onImageClick: (Image) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = {
            // Solid backing so cards never run behind 3-button navigation.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        when (uiState) {
            HomeScreenUiState.Initial -> Unit

            HomeScreenUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }

            is HomeScreenUiState.Success -> {
                ImageGrid(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding,
                    images = uiState.images.collectAsLazyPagingItems(),
                    onImageClick = onImageClick
                )
            }

            is HomeScreenUiState.Error -> {
                ErrorItem(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    text = uiState.msg
                )
            }
        }
    }
}

@Composable
private fun ImageGrid(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    images: LazyPagingItems<Image>,
    onImageClick: (Image) -> Unit
) {
    val statePadding = Modifier.padding(contentPadding)
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val showGoToTopButton by remember {
        derivedStateOf {
            gridState.canScrollBackward
        }
    }

    when (val refreshState = images.loadState.refresh) {
        LoadState.Loading -> {
            LoadingIndicator(modifier = modifier.then(statePadding))
        }

        is LoadState.Error -> {
            PagingErrorItem(
                modifier = modifier.then(statePadding),
                message = refreshState.error.message ?: "Something went wrong",
                onRetryClick = images::retry
            )
        }

        is LoadState.NotLoading -> {
            Box(modifier = modifier) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    columns = GridCells.Adaptive(150.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = contentPadding.calculateTopPadding() + 4.dp,
                        end = 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 16.dp
                    )
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
                        .padding(
                            end = 16.dp,
                            bottom = contentPadding.calculateBottomPadding() + 16.dp
                        ),
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
            .aspectRatio(1f)
            .clickable { onImageClick(image) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = image.thumbnailUrl(targetWidth = 480, targetHeight = 480),
                contentDescription = image.description,
                placeholder = painterResource(Res.drawable.placeholder_image),
                fallback = painterResource(Res.drawable.placeholder_image),
                error = painterResource(Res.drawable.placeholder_image),
                contentScale = ContentScale.Crop
            )

            Text(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
                    .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 8.dp),
                text = image.author,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
                                description = "Photo by Alejandro Escamilla"
                            )
                        )
                    )
                )
            ),
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
                description = "Photo by Alejandro Escamilla"
            ),
            onImageClick = {}
        )
    }
}
