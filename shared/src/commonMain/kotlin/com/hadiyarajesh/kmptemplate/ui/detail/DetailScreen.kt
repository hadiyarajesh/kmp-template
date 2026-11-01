package com.hadiyarajesh.kmptemplate.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.hadiyarajesh.kmptemplate.data.database.entity.Image
import com.hadiyarajesh.kmptemplate.ui.components.ClickableUrlText
import com.hadiyarajesh.kmptemplate.ui.components.HorizontalSpacer
import com.hadiyarajesh.kmptemplate.ui.components.TopBarWithBackButton
import com.hadiyarajesh.kmptemplate.ui.components.VerticalSpacer
import com.hadiyarajesh.kmptemplate.ui.permission.rememberDownloadPermissionRequester
import com.hadiyarajesh.kmptemplate.ui.theme.AppTheme
import kmptemplate.shared.generated.resources.Res
import kmptemplate.shared.generated.resources.about
import kmptemplate.shared.generated.resources.author_label
import kmptemplate.shared.generated.resources.description
import kmptemplate.shared.generated.resources.detail
import kmptemplate.shared.generated.resources.dimensions
import kmptemplate.shared.generated.resources.download
import kmptemplate.shared.generated.resources.download_permission_denied
import kmptemplate.shared.generated.resources.downloading
import kmptemplate.shared.generated.resources.go_to_settings
import kmptemplate.shared.generated.resources.ic_download
import kmptemplate.shared.generated.resources.image_placeholder
import kmptemplate.shared.generated.resources.image_saved_success
import kmptemplate.shared.generated.resources.links
import kmptemplate.shared.generated.resources.ok
import kmptemplate.shared.generated.resources.orientation_landscape
import kmptemplate.shared.generated.resources.orientation_portrait
import kmptemplate.shared.generated.resources.orientation_square
import kmptemplate.shared.generated.resources.permission_dialog_message_settings
import kmptemplate.shared.generated.resources.permission_dialog_title
import kmptemplate.shared.generated.resources.permission_not_now
import kmptemplate.shared.generated.resources.save_to_device
import kmptemplate.shared.generated.resources.source
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DetailScreenRoute(
    image: Image,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = detailViewModel()
) {
    val uriHandler = LocalUriHandler.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val imageSavedMessage = stringResource(Res.string.image_saved_success)

    val permissionRequester = rememberDownloadPermissionRequester { granted ->
        if (granted) {
            viewModel.download()
        } else {
            viewModel.notifyPermissionDenied()
        }
    }

    LaunchedEffect(viewModel, image) { viewModel.start(image) }

    val event = uiState.event
    LaunchedEffect(event) {
        val message = when (event) {
            DownloadEvent.Success -> imageSavedMessage
            is DownloadEvent.Error -> event.message
            DownloadEvent.PermissionDenied, null -> return@LaunchedEffect
        }
        viewModel.onEventConsumed()
        // Consuming the event restarts this effect, so the snackbar must outlive it.
        snackbarScope.launch { snackbarHostState.showSnackbar(message) }
    }

    if (event == DownloadEvent.PermissionDenied) {
        PermissionDeniedDialog(
            canOpenSettings = permissionRequester.canOpenSettings(),
            onOpenSettings = {
                permissionRequester.openSettings()
                viewModel.onEventConsumed()
            },
            onDismiss = viewModel::onEventConsumed
        )
    }

    DetailScreenContent(
        image = image,
        isDownloading = uiState.isDownloading,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
        onDownloadClick = {
            if (permissionRequester.hasRequiredPermission()) {
                viewModel.download()
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
private fun PermissionDeniedDialog(
    canOpenSettings: Boolean,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_download),
                    contentDescription = null
                )
                HorizontalSpacer(size = 12)
                Text(
                    text = stringResource(Res.string.permission_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Text(
                text = stringResource(
                    if (canOpenSettings) {
                        Res.string.permission_dialog_message_settings
                    } else {
                        Res.string.download_permission_denied
                    }
                )
            )
        },
        confirmButton = {
            TextButton(onClick = if (canOpenSettings) onOpenSettings else onDismiss) {
                Text(
                    text = stringResource(
                        if (canOpenSettings) Res.string.go_to_settings else Res.string.ok
                    )
                )
            }
        },
        dismissButton = if (canOpenSettings) {
            {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(Res.string.permission_not_now))
                }
            }
        } else {
            null
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
        bottomBar = {
            DownloadBar(isDownloading = isDownloading, onDownloadClick = onDownloadClick)
        },
        topBar = {
            TopBarWithBackButton(
                title = stringResource(Res.string.detail),
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        ImageDetailView(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            image = image,
            onImageUrlClick = onImageUrlClick
        )
    }
}

@Composable
private fun DownloadBar(isDownloading: Boolean, onDownloadClick: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
    }
}

@Composable
private fun ImageDetailView(
    modifier: Modifier = Modifier,
    image: Image,
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
            onImageUrlClick = onImageUrlClick
        )
    }
}

@Composable
private fun WallpaperHero(modifier: Modifier = Modifier, image: Image) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(image.heroAspectRatio()),
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
private fun AuthorHeader(modifier: Modifier = Modifier, image: Image) {
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
private fun ImageMetadataChips(modifier: Modifier = Modifier, image: Image) {
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

            VerticalSpacer(size = 24)

            SectionTitle(
                text = stringResource(Res.string.links),
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

private fun String.initials(): String = trim()
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "A" }

/** Shows the photo at its own shape so it is not cropped, within sane bounds. */
private fun Image.heroAspectRatio(): Float =
    if (width > 0 && height > 0) (width.toFloat() / height).coerceIn(0.6f, 1.6f) else 1f

@Composable
private fun Image.orientationLabel(): String = stringResource(
    when {
        height > width -> Res.string.orientation_portrait
        width > height -> Res.string.orientation_landscape
        else -> Res.string.orientation_square
    }
)

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
                description = "Photo by Alejandro Escamilla"
            ),
            isDownloading = false,
            onBackClick = {},
            snackbarHostState = remember { SnackbarHostState() },
            onDownloadClick = {},
            onImageUrlClick = {}
        )
    }
}
