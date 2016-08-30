package net.ouwan.umipay.android.api;


import android.support.v4.app.Fragment;

import net.ouwan.umipay.android.fragment.BaseFragment;

/**
 * Created by mink on 16-8-17.
 */
public interface  FragmentNavigationDelegate {
	 void replaceFragmentToActivityFragmentManager(final BaseFragment fragment);

	Fragment getTopFragment();
}
