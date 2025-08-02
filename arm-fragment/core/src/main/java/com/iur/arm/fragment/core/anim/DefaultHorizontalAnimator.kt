package com.iur.arm.fragment.core.anim

import com.iur.arm.fragment.core.R

class DefaultHorizontalAnimator : FragmentAnimator() {
    init {
        enter = R.anim.h_fragment_enter
        exit = R.anim.h_fragment_exit
        popEnter = R.anim.h_fragment_pop_enter
        popExit = R.anim.h_fragment_pop_exit
    }
}
