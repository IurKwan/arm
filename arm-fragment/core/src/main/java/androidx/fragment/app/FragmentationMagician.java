package androidx.fragment.app;


import android.util.Log;

import java.util.List;

/**
 * @author guanzhirui
 */
public class FragmentationMagician {

    private static final String TAG = "FragmentationMagician";

    public static boolean isStateSaved(FragmentManager fragmentManager) {
        if (!(fragmentManager instanceof FragmentManagerImpl)) {
            return false;
        }
        try {
            FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
            return fragmentManagerImpl.isStateSaved();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return false;
    }

    /**
     * Like {@link FragmentManager#popBackStack()}} but allows the commit to be executed after an
     * activity's state is saved.  This is dangerous because the action can
     * be lost if the activity needs to later be restored from its state, so
     * this should only be used for cases where it is okay for the UI state
     * to change unexpectedly on the user.
     */
    public static void popBackStackAllowingStateLoss(final FragmentManager fragmentManager) {
        FragmentationMagician.hookStateSaved(fragmentManager, fragmentManager::popBackStack);
    }

    /**
     * Like {@link FragmentManager#popBackStackImmediate()}} but allows the commit to be executed after an
     * activity's state is saved.
     */
    public static void popBackStackImmediateAllowingStateLoss(final FragmentManager fragmentManager) {
        FragmentationMagician.hookStateSaved(fragmentManager, fragmentManager::popBackStackImmediate);
    }

    /**
     * Like {@link FragmentManager#popBackStackImmediate(String, int)}} but allows the commit to be executed after an
     * activity's state is saved.
     */
    public static void popBackStackAllowingStateLoss(final FragmentManager fragmentManager, final String name, final int flags) {
        FragmentationMagician.hookStateSaved(fragmentManager, () -> fragmentManager.popBackStack(name, flags));
    }

    /**
     * Like {@link FragmentManager#executePendingTransactions()} but allows the commit to be executed after an
     * activity's state is saved.
     */
    public static void executePendingTransactionsAllowingStateLoss(final FragmentManager fragmentManager) {
        FragmentationMagician.hookStateSaved(fragmentManager, fragmentManager::executePendingTransactions);
    }

    public static List<Fragment> getActiveFragments(FragmentManager fragmentManager) {
        return fragmentManager.getFragments();
    }

    private static void hookStateSaved(FragmentManager fragmentManager, Runnable runnable) {
        if (!(fragmentManager instanceof FragmentManagerImpl)) {
            return;
        }
        if (!fragmentManager.isStateSaved()) {
            runnable.run();
        }
    }
}