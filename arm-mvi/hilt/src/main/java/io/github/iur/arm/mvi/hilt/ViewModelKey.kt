package io.github.iur.arm.mvi.hilt.hilt

import io.github.iur.arm.mvi.MavericksViewModel
import dagger.MapKey
import kotlin.reflect.KClass

/**
 * A [MapKey] for populating a map of ViewModels and their factories.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MapKey
annotation class ViewModelKey(
    val value: KClass<out MavericksViewModel<*>>,
)
