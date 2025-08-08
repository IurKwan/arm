package io.github.iur.arm.fragment.fragmentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import io.github.iur.arm.fragment.core.ArmActivityDelegate
import io.github.iur.arm.fragment.core.ExtraTransaction
import io.github.iur.arm.fragment.core.IArmActivity
import io.github.iur.arm.fragment.core.IArmFragment
import io.github.iur.arm.fragment.core.SupportHelper
import io.github.iur.arm.fragment.core.anim.FragmentAnimator

open class ArmActivity :
    AppCompatActivity(),
    IArmActivity {
    private val mDelegate = ArmActivityDelegate(this)

    override fun getSupportDelegate(): ArmActivityDelegate = mDelegate

    /**
     * Perform some extra transactions.
     * 额外的事务：自定义Tag，添加SharedElement动画，操作非回退栈Fragment
     */
    override fun extraTransaction(): ExtraTransaction = mDelegate.extraTransaction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDelegate.onCreate()
    }

    /**
     * Note： return mDelegate.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean = mDelegate.dispatchTouchEvent() || super.dispatchTouchEvent(ev)

    /**
     * 不建议复写该方法,请使用 [onBackPressedSupport] 代替
     */
    @SuppressLint("MissingSuperCall")
    final override fun onBackPressed() {
        mDelegate.onBackPressed()
    }

    /**
     * 该方法回调时机为,Activity回退栈内Fragment的数量 小于等于1 时,默认finish Activity
     * 请尽量复写该方法,避免复写onBackPress(),以保证SupportFragment内的onBackPressedSupport()回退事件正常执行
     */
    override fun onBackPressedSupport() {
        mDelegate.onBackPressedSupport()
    }

    /**
     * 获取设置的全局动画 copy
     *
     * @return FragmentAnimator
     */
    override fun getFragmentAnimator(): FragmentAnimator = mDelegate.fragmentAnimator

    /**
     * Set all fragments animation.
     * 设置Fragment内的全局动画
     */
    override fun setFragmentAnimator(fragmentAnimator: FragmentAnimator) {
        mDelegate.fragmentAnimator = fragmentAnimator
    }

    /**
     * Set all fragments animation.
     * 构建Fragment转场动画
     *
     * 如果是在Activity内实现,则构建的是Activity内所有Fragment的转场动画,
     * 如果是在Fragment内实现,则构建的是该Fragment的转场动画,此时优先级 > Activity的onCreateFragmentAnimator()
     *
     * @return FragmentAnimator对象
     */
    override fun onCreateFragmentAnimator(): FragmentAnimator = mDelegate.onCreateFragmentAnimator()

    override fun post(runnable: Runnable) {
        mDelegate.post(runnable)
    }

    /****************************************以下为可选方法(Optional methods)******************************************************/

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
     */
    fun loadRootFragment(
        containerId: Int,
        toFragment: IArmFragment,
    ) {
        mDelegate.loadRootFragment(containerId, toFragment)
    }

    fun loadRootFragment(
        containerId: Int,
        toFragment: IArmFragment,
        addToBackStack: Boolean,
        allowAnimation: Boolean,
    ) {
        mDelegate.loadRootFragment(containerId, toFragment, addToBackStack, allowAnimation)
    }

    /**
     * 加载多个同级根Fragment,类似Wechat, QQ主页的场景
     */
    fun loadMultipleRootFragment(
        containerId: Int,
        showPosition: Int,
        vararg toFragments: IArmFragment,
    ) {
        mDelegate.loadMultipleRootFragment(containerId, showPosition, *toFragments)
    }

    fun showHideFragment(showFragment: IArmFragment) {
        mDelegate.showHideFragment(showFragment)
    }

    /**
     * show一个Fragment,hide一个Fragment ; 主要用于类似微信主页那种 切换tab的情况
     */
    fun showHideFragment(
        showFragment: IArmFragment,
        hideFragment: IArmFragment,
    ) {
        mDelegate.showHideFragment(showFragment, hideFragment)
    }

    fun start(toFragment: IArmFragment) {
        mDelegate.start(toFragment)
    }

    /**
     * It is recommended to use [ArmFragment.start].
     *
     * @param launchMode Similar to Activity's LaunchMode.
     */
    fun start(
        toFragment: IArmFragment,
        @IArmFragment.LaunchMode launchMode: Int,
    ) {
        mDelegate.start(toFragment, launchMode)
    }

    /**
     * It is recommended to use [ArmFragment.startForResult].
     * Launch an fragment for which you would like a result when it poped.
     */
    fun startForResult(
        toFragment: IArmFragment,
        requestCode: Int,
    ) {
        mDelegate.startForResult(toFragment, requestCode)
    }

    /**
     * It is recommended to use [ArmFragment.startWithPop].
     * Start the target Fragment and pop itself
     */
    fun startWithPop(toFragment: IArmFragment) {
        mDelegate.startWithPop(toFragment)
    }

    /**
     * It is recommended to use [ArmFragment.startWithPopTo].
     *
     * @see popTo
     * +
     * @see start
     */
    fun startWithPopTo(
        toFragment: IArmFragment,
        targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean,
    ) {
        mDelegate.startWithPopTo(toFragment, targetFragmentClass, includeTargetFragment)
    }

    /**
     * It is recommended to use [ArmFragment.replaceFragment].
     */
    fun replaceFragment(
        toFragment: IArmFragment,
        addToBackStack: Boolean,
    ) {
        mDelegate.replaceFragment(toFragment, addToBackStack)
    }

    /**
     * Pop the fragment.
     */
    fun pop() {
        mDelegate.pop()
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     *
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    fun popTo(
        targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean,
    ) {
        mDelegate.popTo(targetFragmentClass, includeTargetFragment)
    }

    /**
     * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
     * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
     */
    fun popTo(
        targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable,
    ) {
        mDelegate.popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable)
    }

    fun popTo(
        targetFragmentClass: Class<*>,
        includeTargetFragment: Boolean,
        afterPopTransactionRunnable: Runnable,
        popAnim: Int,
    ) {
        mDelegate.popTo(
            targetFragmentClass,
            includeTargetFragment,
            afterPopTransactionRunnable,
            popAnim,
        )
    }

    /**
     * 当Fragment根布局 没有 设定background属性时,
     * Fragmentation默认使用Theme的android:windowbackground作为Fragment的背景,
     * 可以通过该方法改变其内所有Fragment的默认背景。
     */
    fun setDefaultFragmentBackground(
        @DrawableRes backgroundRes: Int,
    ) {
        mDelegate.setDefaultFragmentBackground(backgroundRes)
    }

    /**
     * 得到位于栈顶Fragment
     */
    fun getTopFragment(): IArmFragment? = SupportHelper.getTopFragment(supportFragmentManager)

    /**
     * 获取栈内的fragment对象
     */
    fun <T : IArmFragment> findFragment(fragmentClass: Class<T>): T? = SupportHelper.findFragment(supportFragmentManager, fragmentClass)
}
