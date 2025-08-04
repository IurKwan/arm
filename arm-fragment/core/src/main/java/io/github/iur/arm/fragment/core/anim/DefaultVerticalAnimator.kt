package io.github.iur.arm.fragment.core.anim

import android.os.Parcel
import android.os.Parcelable
import io.github.iur.arm.fragment.core.R

class DefaultVerticalAnimator() : FragmentAnimator() {
    init {
        enter = R.anim.v_fragment_enter
        exit = R.anim.v_fragment_exit
        popEnter = R.anim.v_fragment_pop_enter
        popExit = R.anim.v_fragment_pop_exit
    }

    constructor(parcel: Parcel) : this() {
        enter = parcel.readInt()
        exit = parcel.readInt()
        popEnter = parcel.readInt()
        popExit = parcel.readInt()
    }

    companion object CREATOR : Parcelable.Creator<DefaultVerticalAnimator> {
        override fun createFromParcel(parcel: Parcel): DefaultVerticalAnimator = DefaultVerticalAnimator(parcel)

        override fun newArray(size: Int): Array<DefaultVerticalAnimator?> = arrayOfNulls(size)
    }
}
