package io.github.iur.arm.fragment.core.anim

import android.os.Parcel
import android.os.Parcelable
import io.github.iur.arm.fragment.core.R

class DefaultHorizontalAnimator() : FragmentAnimator() {
    init {
        enter = R.anim.h_fragment_enter
        exit = R.anim.h_fragment_exit
        popEnter = R.anim.h_fragment_pop_enter
        popExit = R.anim.h_fragment_pop_exit
    }

    constructor(parcel: Parcel) : this() {
        enter = parcel.readInt()
        exit = parcel.readInt()
        popEnter = parcel.readInt()
        popExit = parcel.readInt()
    }

    companion object CREATOR : Parcelable.Creator<DefaultHorizontalAnimator> {
        override fun createFromParcel(parcel: Parcel): DefaultHorizontalAnimator = DefaultHorizontalAnimator(parcel)

        override fun newArray(size: Int): Array<DefaultHorizontalAnimator?> = arrayOfNulls(size)
    }
}
