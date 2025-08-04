package io.github.iur.arm.mvi.common

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal Mavericks API. It is not intended for external use.",
)
annotation class InternalMavericksApi
