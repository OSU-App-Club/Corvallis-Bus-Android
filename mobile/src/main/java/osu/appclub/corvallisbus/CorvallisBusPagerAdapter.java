package osu.appclub.corvallisbus;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import osu.appclub.corvallisbus.alerts.AlertsFragment;
import osu.appclub.corvallisbus.browsestops.StopsFragment;
import osu.appclub.corvallisbus.favorites.FavoritesFragment;

/**
 * Created by rikkigibson on 2/7/16.
 */
public class CorvallisBusPagerAdapter extends FragmentPagerAdapter {
    final static int PAGE_COUNT = 3;
    Fragment[] fragments = new Fragment[PAGE_COUNT];

    public CorvallisBusPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if (fragments[position] == null) {
            fragments[position] = createFragment(position);
        }
        return fragments[position];
    }

    Fragment createFragment(int position) {
        switch(position) {
            case 0: // favorites
                return FavoritesFragment.newInstance();
            case 1: // browse stops
                return StopsFragment.newInstance();
            case 2: // service alerts
                return AlertsFragment.newInstance();
            default:
                throw new UnsupportedOperationException("Unsupported position was provided");
        }
    }
}
