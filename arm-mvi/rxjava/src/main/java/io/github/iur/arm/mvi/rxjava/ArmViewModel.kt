package io.github.iur.arm.mvi.rxjava

import io.github.iur.arm.mvi.common.MavericksState

abstract class ArmViewModel<S : MavericksState>(
    initialState: S,
    logState: Boolean = true,
) : BaseMvRxViewModel<S>(initialState) {
    init {
        if (logState) logStateChanges()
    }
}
