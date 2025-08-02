package com.iur.arm.fragment.core.anim

import androidx.annotation.AnimRes

open class FragmentAnimator {
    @AnimRes
    protected var enter: Int = 0

    @AnimRes
    protected var exit: Int = 0

    @AnimRes
    protected var popEnter: Int = 0

    @AnimRes
    protected var popExit: Int = 0

    constructor()

    constructor(enter: Int, exit: Int) {
        this.enter = enter
        this.exit = exit
    }

    constructor(enter: Int, exit: Int, popEnter: Int, popExit: Int) {
        this.enter = enter
        this.exit = exit
        this.popEnter = popEnter
        this.popExit = popExit
    }

    fun copy(): FragmentAnimator = FragmentAnimator(getEnter(), getExit(), getPopEnter(), getPopExit())

    fun getEnter(): Int = enter

    fun setEnter(enter: Int): FragmentAnimator {
        this.enter = enter
        return this
    }

    fun getExit(): Int = exit

    /**
     * enter animation
     */
    fun setExit(exit: Int): FragmentAnimator {
        this.exit = exit
        return this
    }

    fun getPopEnter(): Int = popEnter

    fun setPopEnter(popEnter: Int): FragmentAnimator {
        this.popEnter = popEnter
        return this
    }

    fun getPopExit(): Int = popExit

    fun setPopExit(popExit: Int): FragmentAnimator {
        this.popExit = popExit
        return this
    }
}
