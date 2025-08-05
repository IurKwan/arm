package io.github.iur.arm.mvi.rxjava

import io.github.iur.arm.mvi.Mavericks

/**
 * Exists for backwards compatibility.
 *
 * @see Mavericks
 */
object MvRx {
    /**
     * @see Mavericks.KEY_ARG
     */
    @Deprecated(
        message = "MvRx has been replaced with Mavericks",
        replaceWith = ReplaceWith("Mavericks.KEY_ARG"),
    )
    const val KEY_ARG = Mavericks.KEY_ARG
}
