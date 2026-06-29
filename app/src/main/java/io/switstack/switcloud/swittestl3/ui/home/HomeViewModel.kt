package io.switstack.switcloud.swittestl3.ui.home

import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.switstack.switcloud.switcloudclt.data.BipEvent
import io.switstack.switcloud.switcloudclt.data.InitiationData
import io.switstack.switcloud.switcloudclt.data.OutcomeParameterSet
import io.switstack.switcloud.switcloudclt.domain.SwitcloudClt
import io.switstack.switcloud.swittestl3.common.CustomTonesGenerator
import io.switstack.switcloud.swittestl3.common.TlvUtils.parseUirdTlv
import io.switstack.switcloud.swittestl3.data.PaymentProcessStatus
import io.switstack.switcloud.swittestl3.data.PaymentProcessStatus.Completed
import io.switstack.switcloud.swittestl3.data.PaymentProcessStatus.Idle
import io.switstack.switcloud.swittestl3.data.PaymentProcessStatus.Ready
import io.switstack.switcloud.swittestl3.data.PaymentProcessStatus.Step1Confirmation
import io.switstack.switcloud.swittestl3.data.PaymentProcessStatus.Step2Confirmation
import io.switstack.switcloud.swittestl3.data.PaymentProcessStatus.Step3Confirmation
import io.switstack.switcloud.swittestl3.data.ResetEvent
import io.switstack.switcloud.swittestl3.data.StartPaymentEvent
import io.switstack.switcloud.swittestl3.data.UserInfo
import io.switstack.switcloud.swittestl3.domain.ResponseResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import timber.log.Timber

class HomeViewModel : ViewModel(), KoinComponent {
    private val _paymentProcessStatus = MutableStateFlow<PaymentProcessStatus>(Idle)
    val paymentProcessStatus: StateFlow<PaymentProcessStatus> = _paymentProcessStatus.asStateFlow()
    private val _paymentProcessMessage = MutableStateFlow<UserInfo.UserMessage?>(null)
    val paymentProcessMessage = _paymentProcessMessage.asStateFlow()

    val messageHistory = MutableStateFlow<List<String>>(listOf())

    private var hasFirstLaunchRun = false

    private lateinit var toneGenerator: CustomTonesGenerator

    init {
        viewModelScope.launch(Dispatchers.IO) {
            toneGenerator = CustomTonesGenerator()
            merge(
                SwitcloudClt.userInfo,
                SwitcloudClt.bipEvent,
                ResponseResolver.initiateResponse,
                ResponseResolver.readyStatus,
                ResponseResolver.resetStatus,
                ResponseResolver.startPayment
            ).collect {
                when (it) {
                    is BipEvent -> {
                        Timber.d("HVM bipEvent")
                        if (it.success) {
                            toneGenerator.playSuccess()
                        } else {
                            toneGenerator.playAlert()
                        }
                    }

                    is InitiationData -> { // InitiateResponse
                        var holdTime = 5000
                        // message to display
                        val uird = it.userInterfaceRequestData?.also { uird ->
                            Timber.d("HVM urid $uird")
                            if (uird.holdTime > 0) {
                                holdTime = uird.holdTime * 100
                            }
                        }
                        // ops to display if available
                        val ops = it.outcomeParameterSet?.status
                        Timber.d("HVM ops ${it.outcomeParameterSet}")

                        val paymentMessage = UserInfo.UserMessage(uird, ops)
                        _paymentProcessMessage.update {
                            paymentMessage
                        }

                        addMessageToHistory(paymentMessage)

                        _paymentProcessStatus.update {
                            when (ops) {
                                OutcomeParameterSet.Status.APPROVED,
                                OutcomeParameterSet.Status.DECLINED,
                                OutcomeParameterSet.Status.ONLINE_REQUEST,
                                OutcomeParameterSet.Status.SELECT_NEXT -> Completed

                                // should never happend in L3 level
                                OutcomeParameterSet.Status.TRY_AGAIN -> Step1Confirmation

                                null,
                                OutcomeParameterSet.Status.NA,
                                OutcomeParameterSet.Status.TRY_ANOTHER_INTERFACE,
                                OutcomeParameterSet.Status.END_APPLICATION -> Idle
                            }
                        }

                        // reset to Ready state and clear message after holdTime delay
                        android.os.Handler(Looper.getMainLooper()).postDelayed({
                            when (_paymentProcessStatus.value) {
                                Completed,
                                Idle -> {
                                    resetToReady()
                                }

                                else -> {
                                    // no action if Status already changed
                                }
                            }
                        }, holdTime.toLong())
                    }

                    is ByteArray -> { // userInfo
                        Timber.d("HVM userInfo ${it.toHexString()}")
                        parseUirdTlv(it).forEach {
                            when (it) {
                                is UserInfo.LedLevel -> {
                                    val status = when (it.level) {
                                        1 -> Step1Confirmation
                                        2 -> Step2Confirmation
                                        3 -> Step3Confirmation
                                        4 -> Completed
                                        else -> null
                                    }
                                    status?.let { updatedStatus ->
                                        _paymentProcessStatus.update { updatedStatus }
                                    }
                                }

                                is UserInfo.UserMessage -> {
                                    it.let { userMessage ->
                                        _paymentProcessMessage.update { userMessage }
                                    }
                                    addMessageToHistory(it)
                                }
                            }
                        }
                    }

                    is Boolean -> { // readyStatus
                        Timber.d("HVM readyStatus $it")
                        if (it) {
                            resetToReady()
                        } else {
                            _paymentProcessStatus.update { Idle }
                            _paymentProcessMessage.update { null }
                        }
                    }

                    is ResetEvent -> { // reset status on
                        Timber.d("HVM reset to Ready")
                        resetToReady()
                    }

                    is StartPaymentEvent -> {
                        messageHistory.update { listOf() }
                    }
                }
            }
        }
    }

    private fun resetToReady() {
        _paymentProcessStatus.update { Ready }
        _paymentProcessMessage.update { UserInfo.UserMessage(null, defaultMessageStatus = "Welcome") }
    }

    fun cancelPolling() {
        SwitcloudClt.setPollingCancelled(true)
        // cleanup is done in startPayment() finally clause
    }

    fun addMessageToHistory(userMessage: UserInfo.UserMessage) {
        userMessage.run {
            """
            UIRD: df8116${uird?.data?.toHexString()}
                |_ Message ID: $message
                |_ Status: $status
                |_ Hold time: ${uird?.holdTime ?: ""} 
            """.trimIndent()
        }.let { formattedMessage ->
            Timber.d("HVM formattedMessage $formattedMessage")
            messageHistory.update {
                mutableListOf(formattedMessage).apply {
                    addAll(messageHistory.value)
                }
            }
        }
    }

    fun performFirstLaunchAction(action: () -> Unit) {
        if (!hasFirstLaunchRun) {
            action()
            hasFirstLaunchRun = true
        }
    }
}