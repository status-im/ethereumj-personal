package org.ethereum.android_app;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import org.ethereum.android.EthereumManager;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    int tabsCount;
    private String[] tabTitles = { "Console", "Tests" };
    private EthereumManager ethereumManager;

    public TabsPagerAdapter(FragmentManager fragmentManager, EthereumManager ethereumManager) {

        super(fragmentManager);
        this.tabsCount = tabTitles.length;
        this.ethereumManager = ethereumManager;
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                ConsoleFragment consoleFragment = new ConsoleFragment();
                consoleFragment.setEthereumManager(ethereumManager);
                return consoleFragment;
            case 1:
                TestsFragment testsFragment = new TestsFragment();
                return testsFragment;
        }

        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return tabTitles[position];
    }


    @Override
    public int getCount() {

        return tabsCount;
    }
}
