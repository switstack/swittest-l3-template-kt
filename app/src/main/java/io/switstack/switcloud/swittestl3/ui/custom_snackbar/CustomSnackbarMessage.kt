package io.switstack.switcloud.swittestl3.ui.custom_snackbar

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.switstack.switcloud.swittestl3.data.DisplayedMessageTypeEnum

data class CustomSnackbarMessage(
    val message: String,
    val type: DisplayedMessageTypeEnum = DisplayedMessageTypeEnum.Info,
    val onDismissed: (() -> Unit)? = null,
    val elevation: Dp = 6.dp
)