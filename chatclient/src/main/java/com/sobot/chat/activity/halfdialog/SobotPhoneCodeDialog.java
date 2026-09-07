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
import com.sobot.chat.adapter.SobotPhoneCodeAdapter;
import com.sobot.chat.api.model.SobotPhoneCode;
import com.sobot.chat.utils.SobotPhoneCodeUtil;
import com.sobot.chat.utils.SobotSoftKeyboardUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义字段 --手机号--区号
 */
public class SobotPhoneCodeDialog extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private List<SobotPhoneCode> list;
    private SobotPhoneCodeAdapter adapter;
    private SobotPhoneCode selectStauts;
    private RecyclerView rv_list;
    //搜索框
    private LinearLayout ll_search;
    private EditText et_search;//搜索
    private ImageView iv_clear, sobot_iv_search;
    private TextView tv_nodata, sobot_tv_title;
    //键盘避让：白色内容面板（SobotMHLinearLayout）及其布局监听
    private View dialogPanel;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;

    @Override
    public void onClick(View v) {
        if (v == iv_clear) {
            et_search.setText("");
            //显示全部数据
            adapter.setList(list, "");
            showList();
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotPhoneCodeDialog";
    }

    @Override
    protected void initData() {
        list = new ArrayList<>();
        List<SobotPhoneCode> templist = SobotPhoneCodeUtil.initCurrenty(this);
        if (templist == null) {
            templist = new ArrayList<>();
        }
        String pinyin = "";
        for (int i = 0; i < templist.size(); i++) {
            String frist = templist.get(i).getPinyin().substring(0, 1);
            if (!frist.equals(pinyin)) {
                pinyin = frist;
                SobotPhoneCode code = new SobotPhoneCode();
                code.setPinyin(frist);
                list.add(code);
            }
            list.add(templist.get(i));
        }
        adapter = new SobotPhoneCodeAdapter(this, list, selectStauts, new SobotPhoneCodeAdapter.SobotItemListener() {
            @Override
            public void selectItem(SobotPhoneCode model) {
                selectStauts = model;
                if (selectStauts != null) {
                    Intent intent = new Intent();
                    intent.putExtra("selectCode", selectStauts.getPhone_code());
                    setResult(4001, intent);
                }
                finish();
            }
        }
        );
        rv_list.setAdapter(adapter);
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
        sobot_tv_title = findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_phone_code);
        rv_list = findViewById(R.id.rv_list);
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
                    SobotSoftKeyboardUtils.showSoftKeyboard(getSobotBaseActivity());
                } else {
                    SobotSoftKeyboardUtils.hideKeyboard(getSobotBaseActivity());
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
        // 悬浮窗（windowIsFloating）下系统 adjustResize 不可靠：竖屏把弹窗整体顶出屏幕（搜索框不可见），
        // 横屏把窗口压成"屏高-键盘高"的窄条导致内容全被裁掉（只剩蒙层）。
        // 改 ADJUST_NOTHING 关闭系统 resize/pan，窗口保持全高，键盘避让由 startKeyboardAvoidance 手动处理，横竖屏行为一致
        // （与 SobotZoneActivity / SobotTicketDetailActivity 同款方案）。
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
     * 手动键盘避让：监听键盘弹起/收起。
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
            showList();
            adapter.setList(list, searchText);
        } else {
            List<SobotPhoneCode> temList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                if (StringUtils.isNoEmpty(list.get(i).getPhone_code()) && list.get(i).getPhone_code().contains(searchText)) {
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
}
