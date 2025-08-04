package io.github.iur.arm.fragment.core;

import androidx.annotation.DrawableRes;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentationMagician;

import io.github.iur.arm.fragment.core.anim.DefaultVerticalAnimator;
import io.github.iur.arm.fragment.core.anim.FragmentAnimator;
import io.github.iur.arm.fragment.core.queue.Action;


public class ArmActivityDelegate {
    private final IArmActivity mSupport;
    private final FragmentActivity mActivity;

    boolean mPopMultipleNoAnim = false;
    boolean mFragmentClickable = true;

    private TransactionDelegate mTransactionDelegate;
    private FragmentAnimator mFragmentAnimator;
    private int mDefaultFragmentBackground = 0;

    public ArmActivityDelegate(IArmActivity support) {
        if (!(support instanceof FragmentActivity)) {
            throw new RuntimeException("Must extends FragmentActivity/AppCompatActivity");
        }
        this.mSupport = support;
        this.mActivity = (FragmentActivity) support;
    }

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    public ExtraTransaction extraTransaction() {
        return new ExtraTransaction.ExtraTransactionImpl<>((FragmentActivity) mSupport, getTopFragment(), getTransactionDelegate(), true);
    }

    public void onCreate() {
        mTransactionDelegate = getTransactionDelegate();
        mFragmentAnimator = mSupport.onCreateFragmentAnimator();
    }

    public TransactionDelegate getTransactionDelegate() {
        if (mTransactionDelegate == null) {
            mTransactionDelegate = new TransactionDelegate(mSupport);
        }
        return mTransactionDelegate;
    }

    /**
     * 获取设置的全局动画 copy
     *
     * @return FragmentAnimator
     */
    public FragmentAnimator getFragmentAnimator() {
        return mFragmentAnimator.copy();
    }

    /**
     * Set all fragments animation.
     * 设置Fragment内的全局动画
     */
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        this.mFragmentAnimator = fragmentAnimator;

        for (Fragment fragment : FragmentationMagician.getActiveFragments(getSupportFragmentManager())) {
            if (fragment instanceof IArmFragment) {
                IArmFragment iF = (IArmFragment) fragment;
                ArmFragmentDelegate delegate = iF.getSupportDelegate();
                if (delegate.mAnimByActivity) {
                    delegate.mFragmentAnimator = fragmentAnimator.copy();
                    if (delegate.mAnimHelper != null) {
                        delegate.mAnimHelper.notifyChanged(delegate.mFragmentAnimator);
                    }
                }
            }
        }
    }

    /**
     * Set all fragments animation.
     * 构建Fragment转场动画
     * <p/>
     * 如果是在Activity内实现,则构建的是Activity内所有Fragment的转场动画,
     * 如果是在Fragment内实现,则构建的是该Fragment的转场动画,此时优先级 > Activity的onCreateFragmentAnimator()
     *
     * @return FragmentAnimator对象
     */
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultVerticalAnimator();
    }

    /**
     * 当Fragment根布局 没有 设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变Fragment背景。
     */
    public void setDefaultFragmentBackground(@DrawableRes int backgroundRes) {
        mDefaultFragmentBackground = backgroundRes;
    }

    public int getDefaultFragmentBackground() {
        return mDefaultFragmentBackground;
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     * <p>
     * The runnable will be run after all the previous action has been run.
     * <p>
     * 前面的事务全部执行后 执行该Action
     */
    public void post(final Runnable runnable) {
        mTransactionDelegate.post(runnable);
    }

    /**
     * 不建议复写该方法,请使用 {@link #onBackPressedSupport} 代替
     */
    public void onBackPressed() {
        mTransactionDelegate.mActionQueue.enqueue(new Action(Action.ACTION_BACK) {
            @Override
            public void run() {
                if (!mFragmentClickable) {
                    mFragmentClickable = true;
                }

                // 获取activeFragment:即从栈顶开始 状态为show的那个Fragment
                IArmFragment activeFragment = SupportHelper.getActiveFragment(getSupportFragmentManager());
                if (mTransactionDelegate.dispatchBackPressedEvent(activeFragment)) {
                    return;
                }

                mSupport.onBackPressedSupport();
            }
        });
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    public void onBackPressedSupport() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            ActivityCompat.finishAfterTransition(mActivity);
        }
    }

    public boolean dispatchTouchEvent() {
        // 防抖动(防止点击速度过快)
        return !mFragmentClickable;
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     */
    public void loadRootFragment(int containerId, IArmFragment toFragment) {
        loadRootFragment(containerId, toFragment, true, false);
    }

    public void loadRootFragment(int containerId, IArmFragment toFragment, boolean addToBackStack, boolean allowAnimation) {
        mTransactionDelegate.loadRootTransaction(getSupportFragmentManager(), containerId, toFragment, addToBackStack, allowAnimation);
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    public void loadMultipleRootFragment(int containerId, int showPosition, IArmFragment... toFragments) {
        mTransactionDelegate.loadMultipleRootTransaction(getSupportFragmentManager(), containerId, showPosition, toFragments);
    }

    /**
     * show一个Fragment,hide其他同栈所有Fragment
     * 使用该方法时，要确保同级栈内无多余的Fragment,(只有通过loadMultipleRootFragment()载入的Fragment)
     * <p>
     * 建议使用更明确的{@link #showHideFragment(IArmFragment, IArmFragment)}
     *
     * @param showFragment 需要show的Fragment
     */
    public void showHideFragment(IArmFragment showFragment) {
        showHideFragment(showFragment, null);
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     *
     * @param showFragment 需要show的Fragment
     * @param hideFragment 需要hide的Fragment
     */
    public void showHideFragment(IArmFragment showFragment, IArmFragment hideFragment) {
        mTransactionDelegate.showHideFragment(getSupportFragmentManager(), showFragment, hideFragment);
    }

    public void start(IArmFragment toFragment) {
        start(toFragment, IArmFragment.STANDARD);
    }

    /**
     * @param launchMode Similar to Activity's LaunchMode.
     */
    public void start(IArmFragment toFragment, @IArmFragment.LaunchMode int launchMode) {
        mTransactionDelegate.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(), toFragment, 0, launchMode, TransactionDelegate.TYPE_ADD);
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
     */
    public void startForResult(IArmFragment toFragment, int requestCode) {
        mTransactionDelegate.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(), toFragment, requestCode, IArmFragment.STANDARD, TransactionDelegate.TYPE_ADD_RESULT);
    }

    /**
     * Start the target Fragment and pop itself
     */
    public void startWithPop(IArmFragment toFragment) {
        mTransactionDelegate.startWithPop(getSupportFragmentManager(), getTopFragment(), toFragment);
    }

    public void startWithPopTo(IArmFragment toFragment, Class<?> targetFragmentClass, boolean includeTargetFragment) {
        mTransactionDelegate.startWithPopTo(getSupportFragmentManager(), getTopFragment(), toFragment, targetFragmentClass.getName(), includeTargetFragment);
    }

    public void replaceFragment(IArmFragment toFragment, boolean addToBackStack) {
        mTransactionDelegate.dispatchStartTransaction(getSupportFragmentManager(), getTopFragment(), toFragment, 0, IArmFragment.STANDARD, addToBackStack ? TransactionDelegate.TYPE_REPLACE : TransactionDelegate.TYPE_REPLACE_DONT_BACK);
    }

    /**
     * Pop the child fragment.
     */
    public void pop() {
        mTransactionDelegate.pop(getSupportFragmentManager());
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     * <p>
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment) {
        popTo(targetFragmentClass, includeTargetFragment, null);
    }

    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable) {
        popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, TransactionDelegate.DEFAULT_POPTO_ANIM);
    }

    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim) {
        mTransactionDelegate.popTo(targetFragmentClass.getName(), includeTargetFragment, afterPopTransactionRunnable, getSupportFragmentManager(), popAnim);
    }

    private FragmentManager getSupportFragmentManager() {
        return mActivity.getSupportFragmentManager();
    }

    private IArmFragment getTopFragment() {
        return SupportHelper.getTopFragment(getSupportFragmentManager());
    }
}
