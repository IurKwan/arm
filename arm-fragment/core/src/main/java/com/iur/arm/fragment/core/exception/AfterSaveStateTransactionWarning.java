package com.iur.arm.fragment.core.exception;

public class AfterSaveStateTransactionWarning extends RuntimeException {

    public AfterSaveStateTransactionWarning(String action) {
        super("Warning: Perform this " + action + " action after onSaveInstanceState!");
        Timber.d(getMessage());
    }

}
