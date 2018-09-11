package com.zn.expirytracker.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.zn.expirytracker.data.model.Food;

import java.util.List;

public class DetailPagerAdapter extends FragmentStatePagerAdapter {

    private static final float DEFAULT_PAGE_WIDTH = 0.98f;

    private List<Food> mFoodsList;

    public DetailPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setFoodsList(List<Food> foodsList) {
        mFoodsList = foodsList;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return DetailFragment.newInstance(mFoodsList.get(position).get_id());
    }

    @Override
    public int getCount() {
        return mFoodsList != null ? mFoodsList.size() : 0;
    }

    @Override
    public float getPageWidth(int position) {
        return DEFAULT_PAGE_WIDTH;
    }
}
