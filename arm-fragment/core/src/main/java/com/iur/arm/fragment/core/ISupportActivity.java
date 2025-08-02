package com.iur.arm.fragment.core;

import android.view.MotionEvent;

import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * @author guanzhirui
 */

public interface ISupportActivity {
    SupportActivityDelegate getSupportDelegate();

    ExtraTransaction extraTransaction();

    FragmentAnimator getFragmentAnimator();

    void setFragmentAnimator(FragmentAnimator fragmentAnimator);

    FragmentAnimator onCreateFragmentAnimator();

    void post(Runnable runnable);

    void onBackPressed();

    void onBackPressedSupport();

    boolean dispatchTouchEvent(MotionEvent ev);
}
