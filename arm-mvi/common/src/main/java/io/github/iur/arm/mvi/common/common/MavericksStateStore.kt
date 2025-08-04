package io.github.iur.arm.mvi.common

import kotlinx.coroutines.flow.Flow

@InternalMavericksApi
interface MavericksStateStore<S : Any> {
    val state: S
    val flow: Flow<S>

    fun get(block: (S) -> Unit)

    fun set(stateReducer: S.() -> S)
}
