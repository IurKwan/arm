package com.iur.arm.fragment.core.anim

import android.os.Parcel
import android.os.Parcelable

class DefaultNoAnimator() : FragmentAnimator() {
    init {
        enter = 0
        exit = 0
        popEnter = 0
        popExit = 0
    }

    constructor(parcel: Parcel) : this() {
        enter = parcel.readInt()
        exit = parcel.readInt()
        popEnter = parcel.readInt()
        popExit = parcel.readInt()
    }

    companion object CREATOR : Parcelable.Creator<DefaultNoAnimator> {
        override fun createFromParcel(parcel: Parcel): DefaultNoAnimator = DefaultNoAnimator(parcel)

        override fun newArray(size: Int): Array<DefaultNoAnimator?> = arrayOfNulls(size)
    }
}
