package io.github.iur.arm.mvi.rxjava

import io.github.iur.arm.mvi.common.MavericksState

/**
 * MvRx state exists solely for MvRx 1.x backwards compatibility.
 * [MavericksState] is a drop in replacement going forward.
 *
 * @see MavericksState
 */
interface MvRxState : MavericksState
