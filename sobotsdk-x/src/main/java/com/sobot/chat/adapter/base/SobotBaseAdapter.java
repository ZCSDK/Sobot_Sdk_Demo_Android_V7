package com.sobot.chat.adapter.base;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class SobotBaseAdapter<T> extends BaseAdapter {

    protected List<T> list;
    protected Context context;

    public SobotBaseAdapter(Context context, List<T> list) {
        super();
        this.list = list;
        this.context = context;
        if (this.list == null) {
            this.list=new ArrayList<>();
        }
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (list != null) {
            return list.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public List<T> getDatas() {
        return list;
    }


    public Context getContext() {
        return context;
    }
}