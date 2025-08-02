package com.iur.arm.fragment.core.anim

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.AnimRes

open class FragmentAnimator() : Parcelable {
    @AnimRes
    var enter: Int = 0

    @AnimRes
    var exit: Int = 0

    @AnimRes
    var popEnter: Int = 0

    @AnimRes
    var popExit: Int = 0

    constructor(enter: Int, exit: Int) : this() {
        this.enter = enter
        this.exit = exit
    }

    constructor(enter: Int, exit: Int, popEnter: Int, popExit: Int) : this() {
        this.enter = enter
        this.exit = exit
        this.popEnter = popEnter
        this.popExit = popExit
    }

    fun copy(): FragmentAnimator = FragmentAnimator(enter, exit, popEnter, popExit)

    constructor(parcel: Parcel) : this() {
        enter = parcel.readInt()
        exit = parcel.readInt()
        popEnter = parcel.readInt()
        popExit = parcel.readInt()
    }

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeInt(enter)
        parcel.writeInt(exit)
        parcel.writeInt(popEnter)
        parcel.writeInt(popExit)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FragmentAnimator> {
        override fun createFromParcel(parcel: Parcel): FragmentAnimator = FragmentAnimator(parcel)

        override fun newArray(size: Int): Array<FragmentAnimator?> = arrayOfNulls(size)
    }
}
