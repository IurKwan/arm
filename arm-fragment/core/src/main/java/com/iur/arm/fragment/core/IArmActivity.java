package com.iur.arm.fragment.core;

import android.view.MotionEvent;

import com.iur.arm.fragment.core.anim.FragmentAnimator;


public interface IArmActivity {
    ArmActivityDelegate getSupportDelegate();

    ExtraTransaction extraTransaction();

    FragmentAnimator getFragmentAnimator();

    void setFragmentAnimator(FragmentAnimator fragmentAnimator);

    FragmentAnimator onCreateFragmentAnimator();

    void post(Runnable runnable);

    void onBackPressed();

    void onBackPressedSupport();

    boolean dispatchTouchEvent(MotionEvent ev);
}
