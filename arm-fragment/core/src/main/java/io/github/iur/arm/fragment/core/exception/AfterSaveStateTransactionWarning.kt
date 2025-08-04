package io.github.iur.arm.fragment.core.exception

import timber.log.Timber

class AfterSaveStateTransactionWarning : RuntimeException {
    constructor(action: String) : super("Warning: Perform this $action action after onSaveInstanceState!")

    init {
        Timber.d(message)
    }
}
