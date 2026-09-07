package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotTimeZoneAdapter;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotTimezone;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义字段 --日期时间--时区
 */
public class SobotZoneActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private ArrayList<SobotTimezone> list;
    private SobotTimeZoneAdapter adapter;
    private SobotTimezone selectStauts;
    private RecyclerView rv_list;

    //搜索框
    private LinearLayout ll_search;
    private EditText et_search;//搜索
    private ImageView iv_clear, sobot_iv_search;
    private TextView tv_nodata;
    private TextView sobot_tv_title;
    private SobotCusFieldConfig cusFieldConfig;//当前自定义字段
    //键盘避让：白色内容面板（SobotMHLinearLayout）及其布局监听
    private View dialogPanel;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;

    @Override
    public void onClick(View v) {
        if (v == iv_clear) {
            et_search.setText("");
            showAll();
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTimeZoneDialog";
    }

    @Override
    protected void initData() {
        list = (ArrayList<SobotTimezone>) getIntent().getSerializableExtra("zoneList");
        if (list == null) {
            list = new ArrayList<>();
        }
        if (getIntent().getSerializableExtra("cusFieldConfig") != null) {
            cusFieldConfig = (SobotCusFieldConfig) getIntent().getSerializableExtra("cusFieldConfig");
        }
        if (cusFieldConfig == null) {
            finish();
        }
        if (cusFieldConfig.getTimezone() != null) {
            selectStauts = cusFieldConfig.getTimezone();
        }

        List<SobotTimezone> temList = new ArrayList();
        temList.addAll(list);
        adapter = new SobotTimeZoneAdapter(this, temList, selectStauts, new SobotTimeZoneAdapter.SobotTimezoneListener() {
            @Override
            public void selectStatus(SobotTimezone model) {
                selectStauts = model;
                Intent intent = new Intent();
                intent.putExtra("CATEGORYSMALL", "CATEGORYSMALL");
                intent.putExtra("fieldType", ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_ZONE);
                intent.putExtra("selectStauts", selectStauts);
                intent.putExtra("category_fieldId", cusFieldConfig.getFieldId() + "");
                setResult(cusFieldConfig.getFieldType(), intent);
                finish();
            }
        }
        );
        rv_list.setAdapter(adapter);
        if (temList.size() > 0) {
            showList();
        } else {
            showEmpt();
        }
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_time_zone;
    }

    @Override
    protected void initView() {
        super.initView();
        //根布局
        if (coustom_pop_layout == null) {
            coustom_pop_layout = findViewById(R.id.sobot_container);
        }
        rv_list = findViewById(R.id.rv_list);
        sobot_tv_title = findViewById(R.id.sobot_tv_title);
        rv_list.setLayoutManager(new LinearLayoutManager(this));

        ll_search = findViewById(R.id.ll_search);
        et_search = findViewById(R.id.et_search);
        iv_clear = findViewById(R.id.sobot_iv_clear);
        sobot_iv_search = findViewById(R.id.sobot_iv_search);
        tv_nodata = findViewById(R.id.tv_nodata);
        iv_clear.setOnClickListener(this);
        et_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showSoftKeyboard();
                } else {
                    hideKeyboard();
                }
            }
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int inputCount = s.length();
                if (inputCount > 0) {
                    iv_clear.setVisibility(View.VISIBLE);
                    sobot_iv_search.setVisibility(View.GONE);
                } else {
                    iv_clear.setVisibility(View.GONE);
                    sobot_iv_search.setVisibility(View.VISIBLE);
                }
                //搜索
                setIv_search();
            }
        });
        et_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Drawable bgDrawable = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.sobot_bg_line_4, null);
                    ll_search.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, ThemeUtils.getThemeColor(getContext())));
                } else {
                    ll_search.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.sobot_search_bg, null));
                }
            }
        });
        sobot_tv_title.setText(R.string.sobot_time_zone);
        // 刘海屏/挖孔避让已由基类 SobotDialogBaseActivity 统一处理（对根容器 sobot_container 整体避让），
        // 此处不再对内容区单独避让，避免与根容器避让叠加造成双重内缩
        // 悬浮窗（windowIsFloating）下系统 adjustResize 不可靠：竖屏把弹窗整体顶出屏幕（搜索框不可见），
        // 横屏把窗口压成"屏高-键盘高"的窄条导致内容全被裁掉（只剩蒙层）。
        // 改 ADJUST_NOTHING 关闭系统 resize/pan，窗口保持全高，键盘避让由 startKeyboardAvoidance 手动处理，横竖屏行为一致。
        if (getWindow() != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().setAttributes(lp);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        dialogPanel = findViewById(R.id.sobot_dialog_content);
        startKeyboardAvoidance();
    }

    /**
     * 手动键盘避让（与 SobotTicketDetailActivity 同款方案）：监听键盘弹起/收起。
     * 弹起时给根容器加 bottom padding 把面板抬到键盘上方，同时把面板高度压到剩余可用高度
     * （面板是 SobotMHLinearLayout，onMeasure 会再钳制到 0.7 屏高上限，不会超屏）；
     * 收起时清掉 padding、面板高度恢复全高（同样被钳到 0.7 屏高）。
     * 横屏可用高度可能小于"标题+搜索框"高度，此时列表区域被压没、标题/搜索框仍可见，键盘收起后自动恢复。
     */
    private void startKeyboardAvoidance() {
        final View decorView = getWindow() != null ? getWindow().getDecorView() : null;
        if (decorView == null) {
            return;
        }
        // 先移除旧监听，避免重复注册
        stopKeyboardAvoidance();
        keyboardListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (coustom_pop_layout == null || dialogPanel == null) {
                    return;
                }
                Rect r = new Rect();
                decorView.getWindowVisibleDisplayFrame(r);
                int rootHeight = decorView.getRootView().getHeight();
                int keyboardHeight = rootHeight - r.bottom;
                if (keyboardHeight < 0) {
                    keyboardHeight = 0;
                }
                // 阈值过滤导航栏/状态栏（一般 < 屏高 15%），只有键盘弹起才抬升
                if (keyboardHeight > rootHeight * 0.15) {
                    coustom_pop_layout.setPadding(0, 0, 0, keyboardHeight);
                } else {
                    keyboardHeight = 0;
                    coustom_pop_layout.setPadding(0, 0, 0, 0);
                }
                // 面板高度 = 窗口高 - 键盘高；无键盘时等于全高，由 SobotMHLinearLayout 钳到 0.7 屏高
                ViewGroup.LayoutParams lp = dialogPanel.getLayoutParams();
                int targetHeight = Math.max(rootHeight - keyboardHeight, 0);
                if (lp.height != targetHeight) {
                    lp.height = targetHeight;
                    dialogPanel.setLayoutParams(lp);
                }
            }
        };
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
    }

    /**
     * 移除键盘高度监听（onDestroy 资源释放，防泄漏）
     */
    private void stopKeyboardAvoidance() {
        if (keyboardListener != null && getWindow() != null && getWindow().getDecorView() != null) {
            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
            keyboardListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        stopKeyboardAvoidance();
        super.onDestroy();
    }

    private void setIv_search() {
        final String searchText = et_search.getText().toString();
        if (StringUtils.isEmpty(searchText)) {
            showAll();
        } else {
            List<SobotTimezone> temList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                if (StringUtils.isNoEmpty(list.get(i).getTimezoneValue()) && list.get(i).getTimezoneValue().toLowerCase().contains(searchText.toLowerCase())) {
                    temList.add(list.get(i));
                }
            }
            adapter.setList(temList, searchText);
            if (temList.size() > 0) {
                showList();
            } else {
                showEmpt();
            }
        }
    }

    private void showEmpt() {
        tv_nodata.setVisibility(View.VISIBLE);
        rv_list.setVisibility(View.GONE);
    }

    private void showList() {
        tv_nodata.setVisibility(View.GONE);
        rv_list.setVisibility(View.VISIBLE);
    }

    private void showAll() {
        //显示全部数据
        List<SobotTimezone> temList = new ArrayList();
        temList.addAll(list);
        adapter.setList(temList, "");
        if (temList.size() > 0) {
            showList();
        } else {
            showEmpt();
        }
    }
}
