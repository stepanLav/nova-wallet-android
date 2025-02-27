package io.novafoundation.nova.common.mixin.actionAwaitable

import io.novafoundation.nova.common.R
import io.novafoundation.nova.common.base.BaseFragment
import io.novafoundation.nova.common.view.dialog.dialog

class ConfirmationDialogInfo(val title: Int, val message: Int, val positiveButton: Int, val negativeButton: Int) {

    constructor(title: Int, message: Int) : this(title, message, R.string.common_enable, R.string.common_cancel)
}

fun BaseFragment<*>.setupConfirmationDialog(awaitableMixin: ConfirmationAwaitable<ConfirmationDialogInfo>) {
    awaitableMixin.awaitableActionLiveData.observeEvent { action ->
        dialog(requireContext(), R.style.AccentAlertDialogTheme) {
            setTitle(action.payload.title)
            setMessage(action.payload.message)
            setPositiveButton(action.payload.positiveButton) { _, _ -> action.onSuccess(Unit) }
            setNegativeButton(action.payload.negativeButton) { _, _ -> action.onCancel() }
            setOnCancelListener { action.onCancel() }
        }
    }
}

fun BaseFragment<*>.setupConfirmationOrDenyDialog(awaitableMixin: ConfirmOrDenyAwaitable<ConfirmationDialogInfo>) {
    awaitableMixin.awaitableActionLiveData.observeEvent { action ->
        dialog(requireContext(), R.style.AccentAlertDialogTheme) {
            setTitle(action.payload.title)
            setMessage(action.payload.message)
            setPositiveButton(action.payload.positiveButton) { _, _ -> action.onSuccess(true) }
            setNegativeButton(action.payload.negativeButton) { _, _ -> action.onSuccess(false) }
            setOnCancelListener { action.onCancel() }
        }
    }
}
