package com.example.caiqu.demand.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.example.caiqu.demand.Fragments.ReceivedFragment;
import com.example.caiqu.demand.Fragments.SentFragment;

/**
 * Created by caiqu on 09/03/2017.
 */

public class FixedTabsPageAdapter extends FragmentPagerAdapter {
    final static int PAGE_COUNT = 2;
    private String tabTitle[] = {"RECEBIDAS", "ENVIADAS"};
    private Context context;

    public FixedTabsPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        //String urlRequest = "";
        switch (position) {
            case 0:
                Log.d("something","I'm passed by Frag #" + position);
               // urlRequest = "/demand/list-received/";
                return ReceivedFragment.newInstance(position + 1);
            case 1:
                Log.d("something","I'm passed by Frag #" + position);
                //urlRequest = "/demand/list-sent/";
                return SentFragment.newInstance(position + 1);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitle[position];
    }
}
