package io.switstack.switcloud.swittestl3.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.switstack.switcloud.swittestl3.R
import io.switstack.switcloud.swittestl3.common.sanitize
import io.switstack.switcloud.swittestl3.data.DisplayedMessageTypeEnum
import io.switstack.switcloud.swittestl3.ui.custom_snackbar.CustomSnackbarHost
import io.switstack.switcloud.swittestl3.ui.custom_snackbar.CustomSnackbarManager
import io.switstack.switcloud.swittestl3.ui.custom_snackbar.CustomSnackbarMessage
import io.switstack.switcloud.swittestl3.ui.theme.Swittestl3Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel,
    snackbarManager: CustomSnackbarManager = viewModel()
) {
    val initialSettings by viewModel.allCombinedSettings.collectAsStateWithLifecycle()

    var serverAddress by rememberSaveable { mutableStateOf<String?>(null) }
    var deviceType by rememberSaveable { mutableStateOf<String?>(null) }
    var poiId by rememberSaveable { mutableStateOf<String?>(null) }
    var timeout by rememberSaveable { mutableStateOf<Int?>(null) }

    val errorList by viewModel.errorList.collectAsStateWithLifecycle()

    val currentSnackbar by snackbarManager.currentSnackbar.collectAsStateWithLifecycle()

    if (errorList.isNotEmpty()) {
        val fieldsInError = errorList.keys.map { "\u2022 " + stringResource(it) }.joinToString(separator = "\n")
        snackbarManager.showSnackbar(
            CustomSnackbarMessage(
                message = stringResource(R.string.wrong_formated_fields, fieldsInError),
                type = DisplayedMessageTypeEnum.Error
            )
        )
        viewModel.errorList.tryEmit(mapOf())
    }

    LaunchedEffect(initialSettings) {
        initialSettings?.let { settings ->
            serverAddress = settings.serverAddress
            deviceType = settings.deviceType
            poiId = settings.poiId.toString()
            timeout = settings.timeout
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.about)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        SettingsContent(
            innerPadding,
            serverAddress,
            deviceType,
            poiId,
            timeout,
            { serverAddress = it },
            { deviceType = it },
            { poiId = it },
            { timeout = it },
            {
                if (viewModel.onSaveSettings(
                        R.string.server_ip_address to serverAddress?.sanitize(),
                        R.string.device_type to deviceType?.sanitize(),
                        R.string.poi_id to poiId?.sanitize(),
                        R.string.timeout to timeout
                    ).isEmpty()
                ) {
                    navController.popBackStack()
                }
            },
            currentSnackbar,
            snackbarManager::dismissCurrentSnackbar
        )
    }
}

@Suppress("LongParameterList")
@Composable
fun SettingsContent(
    innerPadding: PaddingValues,
    serverAddress: String?,
    deviceType: String?,
    poiId: String?,
    timeout: Int?,
    onServerAddressChanged: (String) -> Unit,
    onDeviceTypeChanged: (String) -> Unit,
    onPoiIdChanged: (String) -> Unit,
    onTimeoutChanged: (Int) -> Unit,
    onSavedClicked: () -> Unit,
    customSnackbarMessage: CustomSnackbarMessage?,
    dismissSnackbar: () -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    Surface {
        Image(
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillHeight,
            painter = painterResource(
                if (isLandscape) {
                    R.drawable.bg_image_land
                } else {
                    R.drawable.bg_image_port
                }
            ),
            contentDescription = "Image background"
        )

        Column {
            Box(
                modifier = Modifier
                    .fillMaxHeight(
                        if (isLandscape) {
                            0.27f
                        } else {
                            0.32f
                        }
                    )
                    .fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(48.dp, 48.dp, 0.dp, 0.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = innerPadding.calculateBottomPadding())
                        .verticalScroll(scrollState)
                ) {
                    SettingsItem(
                        label = stringResource(R.string.server_ip_address),
                        value = serverAddress,
                        onValueChanged = onServerAddressChanged
                    )

                    SettingsItem(
                        label = stringResource(R.string.device_type),
                        value = deviceType,
                        onValueChanged = onDeviceTypeChanged
                    )

                    SettingsItem(
                        label = stringResource(R.string.poi_id),
                        value = poiId,
                        onValueChanged = onPoiIdChanged
                    )

                    SettingsItem(
                        label = stringResource(R.string.timeout),
                        value = timeout.toString(),
                        onValueChanged = {
                            onTimeoutChanged(it.toIntOrNull() ?: 0)
                        },
                        keyboardType = KeyboardType.Number
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = onSavedClicked) {
                            Text(stringResource(R.string.save_settings))
                        }
                    }
                }

                CustomSnackbarHost(
                    snackbarMessage = customSnackbarMessage,
                    onDismiss = dismissSnackbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun SettingsItem(modifier: Modifier = Modifier, label: String, value: String?, onValueChanged: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value ?: ""))
    }

    LaunchedEffect(value) {
        if (value != null && value != textFieldValueState.text) {
            textFieldValueState = textFieldValueState.copy(text = value)
        }
    }

    Column(modifier = modifier) {
        TextField(
            value = textFieldValueState,
            onValueChange = { newTextFieldValue ->
                // 1. Update the local TextFieldValue with the new value (which includes cursor position)
                textFieldValueState = newTextFieldValue

                // 2. Pass the raw text string to the external onValueChanged handler.
                // The handler will then perform parsing/validation and update the ViewModel if successful.
                onValueChanged(newTextFieldValue.text)
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Preview(device = TABLET)
@Composable
fun SettingsScreenPreview() {
    Swittestl3Theme {
        SettingsContent(
            PaddingValues(0.dp),
            "serverAddress",
            "poi",
            "17592525-d848646846",
            10,
            {}, {}, {}, {}, {},
            null,
            {}
        )
    }
}
