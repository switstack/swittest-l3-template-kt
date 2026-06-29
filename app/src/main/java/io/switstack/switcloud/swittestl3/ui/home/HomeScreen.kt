package io.switstack.switcloud.swittestl3.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.switstack.switcloud.swittestl3.R
import io.switstack.switcloud.swittestl3.Routes
import io.switstack.switcloud.swittestl3.data.ConnectionManager
import io.switstack.switcloud.swittestl3.data.LedState
import io.switstack.switcloud.swittestl3.data.PaymentProcessStatus
import io.switstack.switcloud.swittestl3.data.UserInfo
import io.switstack.switcloud.swittestl3.data.settings.CombinedSettings
import io.switstack.switcloud.swittestl3.ui.custom_snackbar.CustomSnackbarHost
import io.switstack.switcloud.swittestl3.ui.custom_snackbar.CustomSnackbarManager
import io.switstack.switcloud.swittestl3.ui.custom_snackbar.CustomSnackbarMessage
import io.switstack.switcloud.swittestl3.ui.settings.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController?,
    settingsViewModel: SettingsViewModel = viewModel(),
    snackbarManager: CustomSnackbarManager = viewModel(),
    connectionManager: ConnectionManager = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val currentSnackbar by snackbarManager.currentSnackbar.collectAsStateWithLifecycle()

    val isConnected by connectionManager.isConnected.collectAsStateWithLifecycle()
    val isConnecting by connectionManager.isConnecting.collectAsStateWithLifecycle()
    val message by connectionManager.message.collectAsStateWithLifecycle(null)

    val paymentProcessStatus by homeViewModel.paymentProcessStatus.collectAsStateWithLifecycle()
    val paymentProcessMessage by homeViewModel.paymentProcessMessage.collectAsStateWithLifecycle()

    val allCombinedSettings = remember { mutableStateOf<CombinedSettings?>(null) }

    val uirdHistory by homeViewModel.messageHistory.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    message?.let {
        snackbarManager.showSnackbar(
            CustomSnackbarMessage(
                message = it.first,
                type = it.second
            )
        )
        connectionManager.message.tryEmit(null)
    }

    LaunchedEffect(Unit) {
        launch {
            settingsViewModel.allCombinedSettings.filterNotNull().collect {
                allCombinedSettings.value = it
            }
        }
        delay(2000)
        homeViewModel.performFirstLaunchAction {
            allCombinedSettings.value?.let { connectionManager.connect(it) }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            snackbarManager.dismissCurrentSnackbar()
        }
    }

    HomeScreenContent(
        navController,
        paymentProcessStatus,
        paymentProcessMessage,
        isConnected,
        isConnecting,
        { allCombinedSettings.value?.let { connectionManager.connect(it) } },
        connectionManager::requestSocketClose,
        currentSnackbar,
        snackbarManager::dismissCurrentSnackbar,
        homeViewModel::cancelPolling,
        { showDialog = true },
        { showDialog = false },
        showDialog,
        uirdHistory
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    navController: NavController?,
    paymentProcessStatus: PaymentProcessStatus,
    paymentProcessMessage: UserInfo.UserMessage?,
    isConnected: Boolean,
    isConnecting: Boolean,
    connect: () -> Unit,
    requestCloseConnection: () -> Unit,
    customSnackbarMessage: CustomSnackbarMessage?,
    dismissSnackbar: () -> Unit,
    cancel: () -> Unit,
    onShowDialogClick: () -> Unit,
    onDismissDialogClick: () -> Unit,
    isShowDialog: Boolean,
    uirdHistory: List<String>
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        requestCloseConnection()
                        navController?.navigate(Routes.ABOUT)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.about)
                        )
                    }
                    IconButton(onClick = {
                        requestCloseConnection()
                        navController?.navigate(Routes.SETTINGS)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
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
                        .fillMaxHeight(0.3f)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = innerPadding.calculateTopPadding() + 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LedIndicator(
                            state = when (paymentProcessStatus) {
                                PaymentProcessStatus.Idle -> LedState.GREY
                                PaymentProcessStatus.Ready -> LedState.BLINKING_GREEN
                                else -> LedState.GREEN
                            }
                        )
                        LedIndicator(
                            state = when (paymentProcessStatus) {
                                PaymentProcessStatus.Idle,
                                PaymentProcessStatus.Ready,
                                PaymentProcessStatus.Step1Confirmation -> LedState.GREY

                                else -> LedState.GREEN
                            }
                        )
                        LedIndicator(
                            state = when (paymentProcessStatus) {
                                PaymentProcessStatus.Idle,
                                PaymentProcessStatus.Ready,
                                PaymentProcessStatus.Step1Confirmation,
                                PaymentProcessStatus.Step2Confirmation -> LedState.GREY

                                else -> LedState.GREEN
                            }
                        )
                        LedIndicator(
                            state = when (paymentProcessStatus) {
                                PaymentProcessStatus.Idle,
                                PaymentProcessStatus.Ready,
                                PaymentProcessStatus.Step1Confirmation,
                                PaymentProcessStatus.Step2Confirmation,
                                PaymentProcessStatus.Step3Confirmation -> LedState.GREY

                                else -> LedState.GREEN
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape = RoundedCornerShape(48.dp, 48.dp, 0.dp, 0.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(32.dp)
                                .clip(shape = RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(
                                    modifier = Modifier.padding(0.dp),
                                    onClick = onShowDialogClick
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = stringResource(R.string.message_history)
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row {
                                    Text(
                                        text = "UIRD",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Row(Modifier.padding(start = 8.dp)) {
                                    Column {
                                        Row {
                                            Text(
                                                text = "Status",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = paymentProcessMessage?.status ?: "",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Row {
                                            Text(
                                                text = "Message",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = paymentProcessMessage?.message ?: "",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(end = 16.dp),
                                        text = "OPS",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Row(Modifier.padding(start = 8.dp)) {
                                    Text(
                                        text = "Status",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = paymentProcessMessage?.ops ?: "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = cancel,
                                        enabled = when (paymentProcessStatus) {
                                            PaymentProcessStatus.Idle,
                                            PaymentProcessStatus.Completed,
                                            PaymentProcessStatus.Ready -> false

                                            PaymentProcessStatus.Step1Confirmation,
                                            PaymentProcessStatus.Step2Confirmation,
                                            PaymentProcessStatus.Step3Confirmation -> true
                                        }
                                    ) {
                                        Text("Cancel transaction")
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .alpha(
                                            if (isConnecting) {
                                                1.0f
                                            } else {
                                                0f
                                            }
                                        )
                                )
                                Button(
                                    onClick = { connect() },
                                    enabled = !isConnecting && !isConnected
                                ) {
                                    Text("Connect")
                                }
                                Button(
                                    onClick = { requestCloseConnection() },
                                    enabled = isConnecting || isConnected
                                ) {
                                    Text("Disconnect")
                                }
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
        if (isShowDialog) {
            ScrollableListDialog(uirdHistory, onDismissDialogClick)
        }
    }
}

@Composable
fun ScrollableListDialog(
    messages: List<String>,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f), // Limit height so it doesn't take the whole screen
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "UIRD history",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(messages.count()) { position ->
                        Text(
                            text = messages.get(position),
                            autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 14.sp)
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(onClick = onDismissRequest) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Preview
@Preview(device = TABLET)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        navController = null,
        paymentProcessStatus = PaymentProcessStatus.Step1Confirmation,
        paymentProcessMessage = UserInfo.UserMessage(
            null,
            defaultMessageStatus = "Welcome"
        ),
        isConnected = true,
        isConnecting = true,
        connect = {},
        requestCloseConnection = {},
        customSnackbarMessage = null,
        dismissSnackbar = {},
        cancel = {},
        onShowDialogClick = {},
        onDismissDialogClick = {},
        isShowDialog = true,
        listOf(
            """
            UIRD: df81161b00000000656e646566720000
                |_ Message ID: AUTHORIZING_PLEASE_WAIT (1b)
                |_ Status: NOT_READY (00)
                |_ Hold time: 0 
            """.trimIndent(),
            """
            UIRD: df81161e040000000000000000000000
                |_ Message ID: CLEAR_DISPLAY (1e)
                |_ Status: CARD_READ_SUCCESSFULLY (04)
                |_ Hold time: 0 
            """.trimIndent(),
            """
            UIRD: df811615020000000000000000000000
                |_ Message ID: PRESENT_CARD (15)
                |_ Status: READY_TO_READ (02)
                |_ Hold time: 0 
            """.trimIndent()
        )
    )
}
