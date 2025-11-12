package io.github.iur.arm.fragment.core.helper.internal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentationMagician;

import io.github.iur.arm.fragment.core.IArmFragment;

import java.util.List;

public class VisibleDelegate {
    private static final String FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE = "fragmentation_invisible_when_leave";
    private static final String FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE = "fragmentation_compat_replace";

    // SupportVisible相关
    private boolean mIsSupportVisible;
    private boolean mNeedDispatch = true;
    private boolean mInvisibleWhenLeave;
    private boolean mIsFirstVisible = true;
    private boolean mFirstCreateViewCompatReplace = true;
    private boolean mAbortInitVisible = false;
    private Runnable taskDispatchSupportVisible;

    private Handler mHandler;
    private Bundle mSaveInstanceState;

    private final IArmFragment mSupportF;
    private final Fragment mFragment;

    public VisibleDelegate(IArmFragment fragment) {
        this.mSupportF = fragment;
        this.mFragment = (Fragment) fragment;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSaveInstanceState = savedInstanceState;
            // setUserVisibleHint() may be called before onCreate()
            mInvisibleWhenLeave = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE);
            mFirstCreateViewCompatReplace = savedInstanceState.getBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_IS_INVISIBLE_WHEN_LEAVE, mInvisibleWhenLeave);
        outState.putBoolean(FRAGMENTATION_STATE_SAVE_COMPAT_REPLACE, mFirstCreateViewCompatReplace);
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (!mFirstCreateViewCompatReplace && mFragment.getTag() != null && mFragment.getTag().startsWith("android:switcher:")) {
            return;
        }

        if (mFirstCreateViewCompatReplace) {
            mFirstCreateViewCompatReplace = false;
        }

        initVisible();
    }

    private void initVisible() {
        if (!mInvisibleWhenLeave && !mFragment.isHidden() && mFragment.isResumed()) {
            if (mFragment.getParentFragment() == null || isFragmentVisible(mFragment.getParentFragment())) {
                mNeedDispatch = false;
                safeDispatchUserVisibleHint(true);
            }
        }
    }

    public void onResume() {
        if (!mIsFirstVisible) {
            if (!mIsSupportVisible && !mInvisibleWhenLeave && isFragmentVisible(mFragment)) {
                mNeedDispatch = false;
                dispatchSupportVisible(true);
            }
        } else {
            mAbortInitVisible = false;
            initVisible();
        }
    }

    public void onPause() {
        if (taskDispatchSupportVisible != null) {
            getHandler().removeCallbacks(taskDispatchSupportVisible);
            mAbortInitVisible = true;
            return;
        }

        if (mIsSupportVisible) {
            mNeedDispatch = false;
            mInvisibleWhenLeave = false;
            dispatchSupportVisible(false);
        } else {
            mInvisibleWhenLeave = true;
        }
    }

    public void onHiddenChanged(boolean hidden) {
        if (mFragment.getHost() == null) {
            mInvisibleWhenLeave = false;
            return;
        }
        if (!hidden && !mFragment.isResumed()) {
            //if fragment is shown but not resumed, ignore...
            onFragmentShownWhenNotResumed();
            return;
        }
        if (hidden) {
            safeDispatchUserVisibleHint(false);
        } else {
            enqueueDispatchVisible();
        }
    }

    private void onFragmentShownWhenNotResumed() {
        mInvisibleWhenLeave = false;
        dispatchChildOnFragmentShownWhenNotResumed();
    }

    private void dispatchChildOnFragmentShownWhenNotResumed() {
        FragmentManager fragmentManager = mFragment.getChildFragmentManager();
            List<Fragment> childFragments = FragmentationMagician.getActiveFragments(fragmentManager);
            for (Fragment child : childFragments) {
                if (child instanceof IArmFragment && !child.isHidden()) {
                    ((IArmFragment) child).getSupportDelegate().getVisibleDelegate().onFragmentShownWhenNotResumed();
                }
            }
    }

    public void onDestroyView() {
        mIsFirstVisible = true;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (mFragment.isResumed() || (!mFragment.isAdded() && isVisibleToUser)) {
            if (!mIsSupportVisible && isVisibleToUser) {
                safeDispatchUserVisibleHint(true);
            } else if (mIsSupportVisible && !isVisibleToUser) {
                dispatchSupportVisible(false);
            }
        }
    }

    private void safeDispatchUserVisibleHint(boolean visible) {
        if (mIsFirstVisible) {
            if (!visible) {
                return;
            }
            enqueueDispatchVisible();
        } else {
            dispatchSupportVisible(visible);
        }
    }

    private void enqueueDispatchVisible() {
        taskDispatchSupportVisible = () -> {
            taskDispatchSupportVisible = null;
            dispatchSupportVisible(true);
        };
        getHandler().post(taskDispatchSupportVisible);
    }

    private void dispatchSupportVisible(boolean visible) {
        if (visible && isParentInvisible()) {
            return;
        }

        if (mIsSupportVisible == visible) {
            mNeedDispatch = true;
            return;
        }

        mIsSupportVisible = visible;

        if (visible) {
            if (checkAddState()) {
                return;
            }
            mSupportF.onSupportVisible();

            if (mIsFirstVisible) {
                mIsFirstVisible = false;
                mSupportF.onLazyInitView(mSaveInstanceState);
            }
            dispatchChild(true);
        } else {
            dispatchChild(false);
            mSupportF.onSupportInvisible();
        }
    }

    private void dispatchChild(boolean visible) {
        if (!mNeedDispatch) {
            mNeedDispatch = true;
        } else {
            if (checkAddState()) {
                return;
            }
            FragmentManager fragmentManager = mFragment.getChildFragmentManager();
            List<Fragment> childFragments = FragmentationMagician.getActiveFragments(fragmentManager);
            for (Fragment child : childFragments) {
                if (child instanceof IArmFragment && !child.isHidden() && child.isResumed()) {
                    ((IArmFragment) child).getSupportDelegate().getVisibleDelegate().dispatchSupportVisible(visible);
                }
            }
        }
    }

    private boolean isParentInvisible() {
        Fragment parentFragment = mFragment.getParentFragment();

        if (parentFragment instanceof IArmFragment) {
            return !((IArmFragment) parentFragment).isSupportVisible();
        }

        return parentFragment != null && !parentFragment.isVisible();
    }

    private boolean checkAddState() {
        if (!mFragment.isAdded()) {
            mIsSupportVisible = !mIsSupportVisible;
            return true;
        }
        return false;
    }

    private boolean isFragmentVisible(Fragment fragment) {
        return !fragment.isHidden() && fragment.isResumed();
    }

    public boolean isSupportVisible() {
        return mIsSupportVisible;
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    public boolean ismIsFirstVisible() {
        return mIsFirstVisible;
    }
}
