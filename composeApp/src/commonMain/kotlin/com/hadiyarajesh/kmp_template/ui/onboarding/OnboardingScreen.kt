package com.hadiyarajesh.kmp_template.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.hadiyarajesh.kmp_template.ui.theme.AppTheme
import kmp_template.composeapp.generated.resources.Res
import kmp_template.composeapp.generated.resources.app_name
import kmp_template.composeapp.generated.resources.onboarding_background_cd
import kmp_template.composeapp.generated.resources.onboarding_feature_cross_platform
import kmp_template.composeapp.generated.resources.onboarding_feature_fast_feed
import kmp_template.composeapp.generated.resources.onboarding_feature_hd_images
import kmp_template.composeapp.generated.resources.onboarding_get_started
import kmp_template.composeapp.generated.resources.onboarding_subtitle
import kmp_template.composeapp.generated.resources.onboarding_title
import kmp_template.composeapp.generated.resources.placeholder_image
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = Res.getUri("drawable/onboarding_background_image.webp"),
            contentDescription = stringResource(Res.string.onboarding_background_cd),
            placeholder = painterResource(Res.drawable.placeholder_image),
            fallback = painterResource(Res.drawable.placeholder_image),
            error = painterResource(Res.drawable.placeholder_image),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.38f))
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = stringResource(Res.string.onboarding_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(Res.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeaturePill(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.onboarding_feature_hd_images)
                    )
                    FeaturePill(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.onboarding_feature_fast_feed)
                    )
                    FeaturePill(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.onboarding_feature_cross_platform)
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onGetStarted,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.onboarding_get_started),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(
    modifier: Modifier = Modifier,
    text: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(
            onGetStarted = {}
        )
    }
}
