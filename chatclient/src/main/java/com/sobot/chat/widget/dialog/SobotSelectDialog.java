package com.sobot.chat.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.adapter.SobotSelectAdapter;
import com.sobot.chat.api.model.SobotOptionModel;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.SobotEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * 单选列表
 * Created by Administrator on 2017/7/13.
 */
public class SobotSelectDialog extends Dialog {
    private ImageView iv_closes;
    private SobotSelectAdapter adapter;
    private ListView listView;
    private TextView sobot_tv_title;
    private ImageView sobot_iv_clear,sobot_iv_search;
    private SobotEditText sobot_et_search;
    private LinearLayout sobot_dialog_content,sobot_ll_search;
    private View v_search_line;
    private String title;
    private OnSelectListener listener;
    private List<SobotOptionModel> datas = new ArrayList<>();

    // 添加 Dialog 构造函数
    public SobotSelectDialog(Context context) {
        super(context);
        init();
    }

    public SobotSelectDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    public SobotSelectDialog(Context context, String title, List<SobotOptionModel> datas, OnSelectListener listener) {
        super(context, R.style.BottomDialogStyle);
        this.datas = datas;
        this.title = title;
        this.listener = listener;
        init();
    }

    private void init() {
        setContentView(R.layout.sobot_dialog_select);

        // 获取窗口并设置位置
        if (getWindow() != null) {
            getWindow().setGravity(android.view.Gravity.BOTTOM);
            // 设置宽度为全屏
            getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        initView();
    }

    protected void initView() {
        Drawable bgDrawable = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.sobot_bg_line_4, null);
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        sobot_ll_search = findViewById(R.id.sobot_ll_search);
        listView = (ListView) findViewById(R.id.sobot_activity_post_category_listview);
        sobot_dialog_content = findViewById(R.id.sobot_dialog_content);
        sobot_iv_search = findViewById(R.id.sobot_iv_search);
        v_search_line = findViewById(R.id.v_search_line);
        v_search_line.setVisibility(View.VISIBLE);
        iv_closes = findViewById(R.id.iv_closes);
        iv_closes.setVisibility(View.VISIBLE);
        iv_closes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        // 设置高度为屏幕的90%
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        sobot_dialog_content.setMinimumHeight((int) (screenHeight * 0.9));
        sobot_et_search = findViewById(R.id.sobot_et_search);
        sobot_iv_clear = findViewById(R.id.sobot_iv_clear);
        sobot_iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sobot_et_search.setText("");
                sobot_iv_clear.setVisibility(View.GONE);
            }
        });
//搜索
        sobot_et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter == null) {
                    return;
                }
                adapter.setSearchText(s.toString());
                if (s.length() > 0) {
                    sobot_iv_clear.setVisibility(View.VISIBLE);
                    sobot_iv_search.setVisibility(View.GONE);
                } else {
                    sobot_iv_clear.setVisibility(View.GONE);
                    sobot_iv_search.setVisibility(View.VISIBLE);
                }
                SobotSelectAdapter.MyFilter m = adapter.getFilter();
                m.filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        sobot_et_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    sobot_ll_search.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, ThemeUtils.getThemeColor(getContext())));
                } else {
                    sobot_ll_search.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.sobot_search_bg, null));
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onSelect(datas.get(position));
                }
                dismiss();
            }
        });
        sobot_tv_title.setText(title);
        adapter = new SobotSelectAdapter(getContext(), datas);
        listView.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        dismiss();
    }

    public interface OnSelectListener {
        void onSelect(SobotOptionModel optionModel);
    }

}