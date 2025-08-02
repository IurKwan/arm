package com.iur.arm.fragment.core.anim

import com.iur.arm.fragment.core.R

class DefaultVerticalAnimator : FragmentAnimator() {
    init {
        enter = R.anim.v_fragment_enter
        exit = R.anim.v_fragment_exit
        popEnter = R.anim.v_fragment_pop_enter
        popExit = R.anim.v_fragment_pop_exit
    }
}
