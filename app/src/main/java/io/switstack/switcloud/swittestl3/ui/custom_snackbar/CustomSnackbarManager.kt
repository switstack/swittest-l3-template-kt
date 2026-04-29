package io.switstack.switcloud.swittestl3.ui.custom_snackbar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CustomSnackbarManager : ViewModel() {
    private val _currentSnackbar = MutableStateFlow<CustomSnackbarMessage?>(null)
    val currentSnackbar: StateFlow<CustomSnackbarMessage?> = _currentSnackbar.asStateFlow()

    private var dismissJob: Job? = null

    /**
     * Shows a new snackbar message. If a message is already showing,
     * it will be immediately replaced by the new message.
     */
    fun showSnackbar(message: CustomSnackbarMessage) {
        // Invoke onDismissed for the previous message if it exists
        _currentSnackbar.value?.onDismissed?.invoke()
        _currentSnackbar.value = message
    }

    /**
     * Dismisses the currently displayed snackbar message.
     */
    fun dismissCurrentSnackbar() {
        _currentSnackbar.value?.onDismissed?.invoke()
        _currentSnackbar.value = null
        dismissJob?.cancel() // Ensure dismiss timer is cancelled
    }
}