package io.github.iur.arm.mvi.rxjava.rxjava

import io.github.iur.arm.mvi.common.MavericksState
import io.github.iur.arm.mvi.rxjava.BaseMvRxViewModel

abstract class ArmViewModel<S : MavericksState>(
    initialState: S,
    logState: Boolean = true,
) : BaseMvRxViewModel<S>(initialState) {
    init {
        if (logState) logStateChanges()
    }
}
