package com.sobot.chat.adapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public abstract class SobotBaseTemplateAdapter extends PagerAdapter {
    protected ArrayList<View> mViewList = new ArrayList<>();
    protected Context context;

    public SobotBaseTemplateAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = mViewList.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mViewList.get(position));
    }

    public View getView(int position) {
        if (position >= 0 && position < mViewList.size()) {
            return mViewList.get(position);
        }
        return null;
    }
}
