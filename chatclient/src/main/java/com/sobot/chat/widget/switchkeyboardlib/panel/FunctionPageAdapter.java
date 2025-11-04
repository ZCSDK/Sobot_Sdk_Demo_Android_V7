package com.sobot.chat.widget.switchkeyboardlib.panel;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.widget.recycler.GridSpacingItemDecoration;
import com.sobot.chat.widget.switchkeyboardlib.model.SobotPlusEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面适配器，每页显示一个网格布局
 */
public class FunctionPageAdapter extends RecyclerView.Adapter<FunctionPageAdapter.PageViewHolder> {
    private List<SobotPlusEntity> functionItems = new ArrayList<>();
    private int rows = 2;
    private int columns = 3;
    private Context mContext;
    private FunctionMenuPageView.OnFunctionItemClickListener itemClickListener;

    public FunctionPageAdapter(Context context, int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.mContext = context;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        // 获取屏幕宽度
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // 创建页面容器
        LinearLayout pageContainer = new LinearLayout(context);
        pageContainer.setOrientation(LinearLayout.VERTICAL);

        // 设置固定宽度为屏幕宽度
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,//外间距20*2
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        pageContainer.setLayoutParams(containerParams);

        // 创建网格布局的 RecyclerView
        RecyclerView gridView = new RecyclerView(context);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, columns);
        gridView.setLayoutManager(gridLayoutManager);
        gridView.setHasFixedSize(true);
        gridView.setNestedScrollingEnabled(false);

        // 设置 gridView 宽度为.MATCH_PARENT，使其撑满页面容器
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        gridView.setLayoutParams(gridParams);
        // 在创建 ViewHolder 时添加一次装饰器
        GridSpacingItemDecoration decoration = new GridSpacingItemDecoration(columns, ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 20), false);
        gridView.addItemDecoration(decoration);
        pageContainer.addView(gridView);

        return new PageViewHolder(pageContainer);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        // 计算当前页面应显示的项目
        int itemsPerPage = rows * columns;
        int start = position * itemsPerPage;
        int end = Math.min(start + itemsPerPage, functionItems.size());

        List<SobotPlusEntity> pageItems = new ArrayList<>();
        for (int i = start; i < end; i++) {
            pageItems.add(functionItems.get(i));
        }

        // 为页面设置子适配器
        FunctionGridAdapter gridAdapter = new FunctionGridAdapter(mContext, pageItems, itemClickListener);
        holder.gridView.setAdapter(gridAdapter);
    }

    @Override
    public int getItemCount() {
        if (functionItems.isEmpty()) return 0;
        int itemsPerPage = rows * columns;
        return (int) Math.ceil((double) functionItems.size() / itemsPerPage);
    }


    public List<SobotPlusEntity> getList() {
        if (functionItems == null) {
            functionItems = new ArrayList<>();
        }
        return functionItems;
    }

    public void setItems(List<SobotPlusEntity> items) {
        this.functionItems = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void setGridSize(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(FunctionMenuPageView.OnFunctionItemClickListener listener) {
        this.itemClickListener = listener;
        notifyDataSetChanged();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        RecyclerView gridView;

        PageViewHolder(@NonNull View itemView) {
            super(itemView);
            gridView = (RecyclerView) ((LinearLayout) itemView).getChildAt(0);
        }
    }
}

