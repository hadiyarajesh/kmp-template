package com.hadiyarajesh.kmp_template.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hadiyarajesh.kmp_template.data.database.entity.Image
import com.hadiyarajesh.kmp_template.ui.components.ClickableUrlText
import com.hadiyarajesh.kmp_template.ui.components.HorizontalSpacer
import com.hadiyarajesh.kmp_template.ui.components.TopBarWithBackButton
import com.hadiyarajesh.kmp_template.ui.components.VerticalSpacer
import com.hadiyarajesh.kmp_template.ui.permission.rememberDownloadPermissionRequester
import com.hadiyarajesh.kmp_template.ui.theme.AppTheme
import kmp_template.composeapp.generated.resources.Res
import kmp_template.composeapp.generated.resources.about
import kmp_template.composeapp.generated.resources.author_label
import kmp_template.composeapp.generated.resources.description
import kmp_template.composeapp.generated.resources.detail
import kmp_template.composeapp.generated.resources.dimensions
import kmp_template.composeapp.generated.resources.download
import kmp_template.composeapp.generated.resources.download_permission_denied
import kmp_template.composeapp.generated.resources.download_permission_denied_with_settings
import kmp_template.composeapp.generated.resources.downloading
import kmp_template.composeapp.generated.resources.failed_to_load_image
import kmp_template.composeapp.generated.resources.go_to_settings
import kmp_template.composeapp.generated.resources.image_placeholder
import kmp_template.composeapp.generated.resources.image_saved_success
import kmp_template.composeapp.generated.resources.save_to_device
import kmp_template.composeapp.generated.resources.source
import kmp_template.composeapp.generated.resources.welcome_message
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailScreenRoute(
    image: Image,
    viewModel: DetailViewModel,
    onBackClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val isDownloading by viewModel.isDownloading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val imageSavedMessage = stringResource(Res.string.image_saved_success)
    val permissionDeniedMessage = stringResource(Res.string.download_permission_denied)
    val permissionDeniedWithSettingsMessage =
        stringResource(Res.string.download_permission_denied_with_settings)
    val goToSettingsLabel = stringResource(Res.string.go_to_settings)

    val permissionRequester = rememberDownloadPermissionRequester { granted ->
        if (granted) {
            viewModel.downloadImage(image)
        } else {
            viewModel.notifyPermissionDenied()
        }
    }

    LaunchedEffect(
        viewModel,
        imageSavedMessage,
        permissionDeniedMessage,
        permissionDeniedWithSettingsMessage,
        goToSettingsLabel,
        permissionRequester,
        snackbarHostState
    ) {
        viewModel.events.collectLatest { event ->
            when (event) {
                DownloadEvent.Success -> snackbarHostState.showSnackbar(imageSavedMessage)

                DownloadEvent.PermissionDenied -> {
                    val canOpenSettings = permissionRequester.canOpenSettings()
                    val result = snackbarHostState.showSnackbar(
                        message = if (canOpenSettings) {
                            permissionDeniedWithSettingsMessage
                        } else {
                            permissionDeniedMessage
                        },
                        actionLabel = if (canOpenSettings) goToSettingsLabel else null,
                        withDismissAction = canOpenSettings
                    )
                    if (canOpenSettings && result == SnackbarResult.ActionPerformed) {
                        permissionRequester.openSettings()
                    }
                }

                is DownloadEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    DetailScreenContent(
        image = image,
        isDownloading = isDownloading,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
        onDownloadClick = {
            if (permissionRequester.hasRequiredPermission()) {
                viewModel.downloadImage(image)
            } else {
                permissionRequester.requestPermission()
            }
        },
        onImageUrlClick = { url ->
            runCatching { uriHandler.openUri(url) }
        }
    )
}

@Composable
private fun DetailScreenContent(
    image: Image,
    isDownloading: Boolean,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onDownloadClick: () -> Unit,
    onImageUrlClick: (String) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopBarWithBackButton(
                title = stringResource(Res.string.detail),
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageDetailView(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                image = image,
                isDownloading = isDownloading,
                onDownloadClick = onDownloadClick,
                onImageUrlClick = onImageUrlClick
            )
        }
    }
}

@Composable
private fun ImageDetailView(
    modifier: Modifier = Modifier,
    image: Image,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit,
    onImageUrlClick: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WallpaperHero(image = image)

        VerticalSpacer(size = 24)

        AuthorHeader(image = image)

        VerticalSpacer(size = 16)

        ImageMetadataChips(image = image)

        VerticalSpacer(size = 24)

        AboutCard(
            image = image,
            isDownloading = isDownloading,
            onDownloadClick = onDownloadClick,
            onImageUrlClick = onImageUrlClick
        )
    }
}

@Composable
private fun WallpaperHero(
    modifier: Modifier = Modifier,
    image: Image
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 14f),
            model = image.detailImageUrl(targetWidth = 1440),
            contentDescription = image.description,
            placeholder = painterResource(Res.drawable.image_placeholder),
            fallback = painterResource(Res.drawable.image_placeholder),
            error = painterResource(Res.drawable.image_placeholder),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun AuthorHeader(
    modifier: Modifier = Modifier,
    image: Image
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                text = image.author.initials(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        HorizontalSpacer(size = 12)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.author_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = image.author,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ImageMetadataChips(
    modifier: Modifier = Modifier,
    image: Image
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetadataChip(
            text = stringResource(Res.string.dimensions, image.width, image.height),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        MetadataChip(
            text = image.orientationLabel(),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun MetadataChip(
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    AssistChip(
        modifier = modifier,
        onClick = {},
        label = { Text(text = text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = null
    )
}

@Composable
private fun AboutCard(
    modifier: Modifier = Modifier,
    image: Image,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit,
    onImageUrlClick: (String) -> Unit
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

            SectionTitle(
                text = stringResource(Res.string.about),
                color = contentColor
            )

            VerticalSpacer(size = 10)

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${stringResource(Res.string.description)}: ")
                    }
                    append(image.description)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )

            VerticalSpacer(size = 14)

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDownloading,
                onClick = onDownloadClick
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    HorizontalSpacer(size = 10)
                    Text(text = stringResource(Res.string.downloading))
                } else {
                    Text(text = stringResource(Res.string.save_to_device))
                }
            }

            VerticalSpacer(size = 18)

            SectionTitle(
                text = stringResource(Res.string.source),
                color = contentColor
            )

            VerticalSpacer(size = 10)

            ClickableUrlText(
                label = stringResource(Res.string.source),
                url = image.sourceUrl,
                onClick = onImageUrlClick
            )

            VerticalSpacer(size = 12)

            ClickableUrlText(
                label = stringResource(Res.string.download),
                url = image.downloadUrl,
                onClick = onImageUrlClick
            )
        }
    }
}

@Composable
private fun SectionTitle(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

private fun String.initials(): String {
    return trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "A" }
}

private fun Image.orientationLabel(): String {
    return when {
        height > width -> "Portrait"
        width > height -> "Landscape"
        else -> "Square"
    }
}

@Preview
@Composable
private fun DetailScreenPreview() {
    AppTheme {
        DetailScreenContent(
            image = Image(
                imageId = "1",
                author = "Alejandro Escamilla",
                width = 5000,
                height = 3333,
                sourceUrl = "https://unsplash.com/photos/yC-Yzbqy7PY",
                downloadUrl = "https://picsum.photos/id/1/5000/3333",
                description = stringResource(Res.string.welcome_message),
                altText = stringResource(Res.string.failed_to_load_image)
            ),
            isDownloading = false,
            onBackClick = {},
            snackbarHostState = remember { SnackbarHostState() },
            onDownloadClick = {},
            onImageUrlClick = {}
        )
    }
}
