package com.iur.arm.fragment.core.anim;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.iur.arm.fragment.core.R;


public class DefaultVerticalAnimator extends FragmentAnimator implements Parcelable {

    public DefaultVerticalAnimator() {
        enter = R.anim.v_fragment_enter;
        exit = R.anim.v_fragment_exit;
        popEnter = R.anim.v_fragment_pop_enter;
        popExit = R.anim.v_fragment_pop_exit;
    }

    protected DefaultVerticalAnimator(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DefaultVerticalAnimator> CREATOR = new Creator<>() {
        @Override
        public DefaultVerticalAnimator createFromParcel(Parcel in) {
            return new DefaultVerticalAnimator(in);
        }

        @Override
        public DefaultVerticalAnimator[] newArray(int size) {
            return new DefaultVerticalAnimator[size];
        }
    };
}
