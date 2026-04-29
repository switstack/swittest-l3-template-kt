package io.switstack.switcloud.swittestl3.ui.custom_snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.switstack.switcloud.swittestl3.data.DisplayedMessageTypeEnum

@Composable
fun CustomSnackbarHost(
    snackbarMessage: CustomSnackbarMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = snackbarMessage != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val message = snackbarMessage ?: return@AnimatedVisibility

        // Determine colors and icon based on type
        val (backgroundColor, contentColor, icon) = when (message.type) {
            DisplayedMessageTypeEnum.Info -> Triple(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                Icons.Filled.Info
            )

            DisplayedMessageTypeEnum.Success -> Triple(
                Color(0xFF4CAF50), // Green
                Color.White,
                Icons.Filled.CheckCircle
            )

            DisplayedMessageTypeEnum.Warning -> Triple(
                Color(0xFFFFC107), // Amber
                Color.Black,
                Icons.Filled.Warning
            )

            DisplayedMessageTypeEnum.Error -> Triple(
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.onError,
                Icons.Filled.Error
            )
        }

        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = backgroundColor,
            shadowElevation = message.elevation
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = message.type.name,
                        tint = contentColor,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Text(
                        text = message.message,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCustomSnackbarHostSingle() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CustomSnackbarHost(
                snackbarMessage = CustomSnackbarMessage(
                    message = "Info: This is a simple informational message.",
                    type = DisplayedMessageTypeEnum.Info
                ),
                onDismiss = {}
            )
            CustomSnackbarHost(
                snackbarMessage = CustomSnackbarMessage(
                    message = "Success: Your action was completed successfully!",
                    type = DisplayedMessageTypeEnum.Success
                ),
                onDismiss = {}
            )
            CustomSnackbarHost(
                snackbarMessage = CustomSnackbarMessage(
                    message = "Warning: Something might be wrong. Please check your input carefully.",
                    type = DisplayedMessageTypeEnum.Warning
                ),
                onDismiss = {}
            )
            CustomSnackbarHost(
                snackbarMessage = CustomSnackbarMessage(
                    message = "Error: Failed to fetch data. Please try again later.",
                    type = DisplayedMessageTypeEnum.Error
                ),
                onDismiss = {}
            )
        }
    }
}