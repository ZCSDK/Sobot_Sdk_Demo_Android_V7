package com.sobot.chat.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.RespInfoListBean;
import com.sobot.chat.api.model.SobotRobotGuess;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.WebViewSecurityUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动补全的editText
 */
public class ContainsEmojiEditText extends AppCompatEditText implements View.OnFocusChangeListener {
    private OnFocusChangeListener externalFocusListener;
    private static final String SOBOT_AUTO_COMPLETE_REQUEST_CANCEL_TAG = "SOBOT_AUTO_COMPLETE_REQUEST_CANCEL_TAG";
    private static final int MAX_AUTO_COMPLETE_NUM = 5;
    Handler handler = new Handler();
    SobotCustomPopWindow mPopWindow;

    View mContentView;
    SobotAutoCompelteAdapter mAdapter;
    MyWatcher myWatcher;
    MyEmojiWatcher myEmojiWatcher;
    String mUid;
    String mRobotFlag;
    boolean mIsAutoComplete;
    SobotAutoCompleteListener autoCompleteListener;

    private View activityRootView;
    private Rect previousVisibleRect = new Rect();
    private boolean isKeyboardVisible = false;

    public ContainsEmojiEditText(Context context) {
        super(context);
        initEditText();
    }

    public ContainsEmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEditText();
    }

    public ContainsEmojiEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEditText();
    }

    // 初始化edittext 控件
    private void initEditText() {
        setOnFocusChangeListener(this);
        myEmojiWatcher = new MyEmojiWatcher();
        addTextChangedListener(myEmojiWatcher);
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
            // 触发 setImeOptions 的 override，确保首次 IME 绑定前 NO_EXTRACT_UI | NO_FULLSCREEN 已生效
            setImeOptions(getImeOptions());
        }
        boolean supportFlag = SharedPreferencesUtil.getBooleanData(getContext(), ZhiChiConstant.SOBOT_CONFIG_SUPPORT, false);
        if (!supportFlag) {
            return;
        }
        try {
            activityRootView = ((Activity) getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
            if (activityRootView != null) {
                activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        activityRootView.getWindowVisibleDisplayFrame(r);
                        int heightDiff = activityRootView.getRootView().getHeight() - r.bottom;
                        if (heightDiff > 100) { // if more than 100 pixels, it's probably a keyboard...
                            if (!isKeyboardVisible) {
                                isKeyboardVisible = true;

                            }
                        } else {
                            if (isKeyboardVisible) {
                                isKeyboardVisible = false;
                                dismissPop();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {

        }

        myWatcher = new MyWatcher();
        addTextChangedListener(myWatcher);
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {//横屏
            setOnEditorActionListener(new OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {//完成
                        doAfterTextChanged(v.getText().toString());
                        return true;
                    }
                    if (actionId == KeyEvent.ACTION_DOWN) {
                        doAfterTextChanged(v.getText().toString());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 横屏下强制保留 NO_EXTRACT_UI | NO_FULLSCREEN，防止外部调用 setImeOptions 时
     * 覆盖标志后 IME 重启切回抽取/全屏模式遮挡输入框。
     */
    @Override
    public void setImeOptions(int imeOptions) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
            imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI
                    | EditorInfo.IME_FLAG_NO_FULLSCREEN;
        }
        super.setImeOptions(imeOptions);
    }

    public void doAfterTextChanged(String s) {
        if (!mIsAutoComplete) {
            return;
        }
        if (TextUtils.isEmpty(s)) {
            HttpUtils.getInstance().cancelTag(SOBOT_AUTO_COMPLETE_REQUEST_CANCEL_TAG);
            dismissPop();
        } else {
            HttpUtils.getInstance().cancelTag(SOBOT_AUTO_COMPLETE_REQUEST_CANCEL_TAG);
            ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(getContext()).getZhiChiApi();
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getContext(),
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null && initMode.isAiAgent()) {
                //Ai
                zhiChiApi.AiAnswerSuggest(getContext(), mUid, mRobotFlag, s, initMode.getCid(), initMode.getAiAgentCid(), new StringResultCallBack<ArrayList<RespInfoListBean>>() {
                    @Override
                    public void onSuccess(ArrayList<RespInfoListBean> list) {
                        if (getText() != null && StringUtils.isEmpty(getText().toString().trim())) {
                            //输入框内容为空 就返回并且隐藏弹窗
                            dismissPop();
                            return;
                        }
                        //只处理当前查询到的返回值
                        showPop((View) ContainsEmojiEditText.this.getParent(), list);
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        LogUtils.d("des" + des);
                    }
                });
            } else {
                zhiChiApi.robotGuess(SOBOT_AUTO_COMPLETE_REQUEST_CANCEL_TAG, mUid, mRobotFlag, s, new StringResultCallBack<SobotRobotGuess>() {
                    @Override
                    public void onSuccess(SobotRobotGuess result) {
                        try {
                            if (getText() != null && StringUtils.isEmpty(getText().toString().trim())) {
                                //输入框内容为空 就返回并且隐藏弹窗
                                dismissPop();
                                return;
                            }
                            String originQuestion = result.getOriginQuestion();
                            String currntContent = getText().toString();
                            if (currntContent.equals(originQuestion)) {
                                //只处理当前查询到的返回值
                                List<RespInfoListBean> respInfoList = result.getRespInfoList();
                                showPop((View) ContainsEmojiEditText.this.getParent(), respInfoList);
                            }
                        } catch (Exception e) {
                            LogUtils.e("uncaught", e);
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String des) {

                    }
                });
            }
        }
    }

    public void setRequestParams(String uid, String robotFlag) {
        this.mUid = uid;
        this.mRobotFlag = robotFlag;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            dismissPop();
        }
        // 调用外部监听器
        if (externalFocusListener != null) {
            externalFocusListener.onFocusChange(v, hasFocus);
        }
    }

    private class MyWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            LogUtils.e("beforeTextChanged: " + s.toString());
            doAfterTextChanged(s.toString());
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            doBeforeTextChanged();
            //LogUtils.e( "beforeTextChanged: "+s.toString());
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // LogUtils.e( "onTextChanged: "+s.toString());
        }
    }

    /**
     * 表情监听
     */
    private class MyEmojiWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }


    public boolean isShowing() {
        if (mPopWindow != null) {
            PopupWindow popupWindow = mPopWindow.getPopupWindow();
            if (popupWindow != null) {
                return popupWindow.isShowing();
            }
        }
        return false;
    }

    private void showPop(final View anchorView, final List<RespInfoListBean> list) {
        if (getWindowVisibility() == View.GONE) {
            return;
        }

        if (list == null || list.isEmpty()) {
            dismissPop();
            return;
        }
        dismissPop();
        View contentView = getContentView();
        //处理popWindow 显示内容
        final GridView listView = handleListView(contentView, list);

        // 找到圆角输入框容器(ll_chat_keyboard_panle)作为对齐目标，
        // 使弹窗左右与输入框对齐，同时天然避开横屏刘海屏（容器已由 Activity 根布局做了 cutout padding）。
        // 找不到时回退到 anchorView，行为与改动前一致。
        View alignTarget = anchorView;
        ViewParent parent = anchorView.getParent();
        while (parent instanceof View) {
            View v = (View) parent;
            if (v.getId() == R.id.ll_chat_keyboard_panle) {
                alignTarget = v;
                break;
            }
            parent = v.getParent();
        }
        int alignWidth = alignTarget.getWidth();
        if (alignWidth <= 0) {
            alignWidth = anchorView.getWidth();
        }
        int[] alignLoc = new int[2];
        alignTarget.getLocationOnScreen(alignLoc);

        // 直接读 GridView LayoutParams 算 popup 高度，避免依赖 measure pass —— measure 在第一次 show 时
        // GridView 还未完成 layout、setLayoutParams 未生效，会拿到偏差值。
        // popup 高度 = GridView height(rows*行高) + 上下 margin（LinearLayout 自身无 padding）。
        LinearLayout.LayoutParams gvLp = (LinearLayout.LayoutParams) listView.getLayoutParams();
        int popupHeight = gvLp.height + gvLp.topMargin + gvLp.bottomMargin;
        int anchorGapPx = getResources().getDimensionPixelSize(R.dimen.sobot_autocomplete_anchor_gap);
        int x = alignLoc[0];
        int y = alignLoc[1] - popupHeight - anchorGapPx;

        // 关键：每次都用 explicit 宽高新建 PopupWindow，不复用 —— 复用 + WRAP_CONTENT 自测会导致
        // "首次过高、复用过矮"的行数不一致 bug；用 explicit 高度构造，PopupWindow 从头到尾不需要自测 contentView。
        if (mPopWindow != null) {
            try {
                mPopWindow.dissmiss();
            } catch (Exception e) {
                LogUtils.e("uncaught", e);
            }
        }
        mPopWindow = new SobotCustomPopWindow.PopupWindowBuilder(getContext())
                .setView(contentView)
                .setFocusable(false)
                .setOutsideTouchable(false)
                .size(alignWidth, popupHeight)
                .create();
        mPopWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);
        // 强制 update 一次，兜底 SobotCustomPopWindow 内部 build 末尾的 update() noop 路径
        // （在 show 之前 update() 直接 return，导致 explicit height 没真正写入 WindowManager.LayoutParams）。
        PopupWindow rawPopup = mPopWindow.getPopupWindow();
        if (rawPopup != null && rawPopup.isShowing()) {
            rawPopup.update(x, y, alignWidth, popupHeight);
        }
    }

    private GridView handleListView(View contentView, final List<RespInfoListBean> list) {
        final GridView listView = contentView.findViewById(R.id.sobot_lv_menu);
        notifyAdapter(listView, list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                dismissPop();
                if (autoCompleteListener != null) {
                    SobotAutoCompelteAdapter adapter = (SobotAutoCompelteAdapter) listView.getAdapter();
                    List<RespInfoListBean> datas = adapter.getDatas();
                    if (datas != null && position < datas.size()) {
                        RespInfoListBean respInfoListBean = datas.get(position);
                        autoCompleteListener.onRobotGuessComplete(respInfoListBean.getQuestion());
                    }
                }
//                ToastUtil.showToast(getContext(), "" + position);
            }
        });
        return listView;

    }

    private void notifyAdapter(GridView listView, final List<RespInfoListBean> list) {
        if (list == null || listView == null) {
            return;
        }
        // 与 mContentView 一致，每次新建 adapter；旧 adapter 已绑定到旧 listView，无需复用
        mAdapter = new SobotAutoCompelteAdapter(getContext(), new ArrayList<>(list));
        listView.setAdapter(mAdapter);
        listView.setSelection(0);

        measureListViewHeight(listView, list.size());
    }

    /**
     * 按 GridView 实际列数计算高度：行数 = ceil(展示 item 数 / 列数)。
     * 列数由 @integer/sobot_list_span_count 在资源限定符下决定（手机竖屏/Pad=1，手机横屏=2）。
     * 单行高度走 dimen @dimen/sobot_autocomplete_row_height（手机横屏 32dp，其他 36dp）。
     */
    private void measureListViewHeight(GridView listView, int count) {
        int displayCount = Math.min(count, MAX_AUTO_COMPLETE_NUM);
        // 不能用 listView.getNumColumns()——GridView 首次 measure 之前返回 0，
        // 会让 columns 退化成 1，rows 被算成 item 数。直接读 integer 资源。
        int columns = Math.max(1, getResources().getInteger(R.integer.sobot_list_span_count));
        int rows = (int) Math.ceil(displayCount * 1.0 / columns);
        int rowHeightPx = getResources().getDimensionPixelSize(R.dimen.sobot_autocomplete_row_height);
        int listHeight = rowHeightPx * rows;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listView.getLayoutParams();
        params.height = listHeight;
        listView.setLayoutParams(params);
    }

    public void dismissPop() {
        if (mPopWindow != null) {
            try {
                mPopWindow.dissmiss();
            } catch (Exception e) {
                LogUtils.e("uncaught", e);
            }
        }
    }

    private View getContentView() {
        // 每次重 inflate，不复用 mContentView —— 复用会让 PopupWindow 内部 layout 缓存在多次 show/dismiss
        // 后状态不一致（同样 1 行触发但高度不同），是 "首次高、复用矮" 的根因。
        mContentView = LayoutInflater.from(getContext()).inflate(R.layout.sobot_layout_auto_complete, null);
        // inflate(layout, null) 不会给 root 生成 LayoutParams（getLayoutParams() == null）；PopupWindow.createBackgroundView
        // 据此走 MATCH_PARENT 分支，把 LinearLayout 撑满 popup window，造成"内容上排、底部留白"。
        // 显式设 WRAP_CONTENT 强制走 WRAP_CONTENT 分支，让 contentView 真实跟随 LinearLayout 测量结果。
        mContentView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return mContentView;
    }

    private static class SobotAutoCompelteAdapter extends SobotBaseAdapter<RespInfoListBean> {

        private SobotAutoCompelteAdapter(Context context, List<RespInfoListBean> list) {
            super(context, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.sobot_item_auto_complete_menu, null);
                holder = new ViewHolder(context, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            RespInfoListBean child = list.get(position);
            if (child != null && !TextUtils.isEmpty(child.getHighlight())) {
                holder.sobot_child_menu.setText(WebViewSecurityUtil.safeFromHtml(child.getHighlight()));
            } else {
                holder.sobot_child_menu.setText("");
            }
            return convertView;
        }

        private static class ViewHolder {
            private TextView sobot_child_menu;

            private ViewHolder(Context context, View view) {
                sobot_child_menu = (TextView) view.findViewById(R.id.sobot_child_menu);
            }
        }
    }

    public void setAutoCompleteEnable(boolean flag) {
        mIsAutoComplete = flag;
        if (!mIsAutoComplete) {
            HttpUtils.getInstance().cancelTag(SOBOT_AUTO_COMPLETE_REQUEST_CANCEL_TAG);
            dismissPop();
        } else {
            initEditText();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeTextChangedListener(myWatcher);
        HttpUtils.getInstance().cancelTag(SOBOT_AUTO_COMPLETE_REQUEST_CANCEL_TAG);
        dismissPop();
        autoCompleteListener = null;
        mContentView = null;
        super.onDetachedFromWindow();
    }

    public void setSobotAutoCompleteListener(SobotAutoCompleteListener listener) {
        autoCompleteListener = listener;
    }

    public interface SobotAutoCompleteListener {
        void onRobotGuessComplete(String question);
    }

    // 提供专门的方法设置外部监听器
    public void setExternalOnFocusChangeListener(OnFocusChangeListener l) {
        this.externalFocusListener = l;
    }

    // 禁止外部直接使用setOnFocusChangeListener覆盖内部监听器
    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        if (l == this) {
            super.setOnFocusChangeListener(l);
        } else {
            setExternalOnFocusChangeListener(l);
        }
    }
}