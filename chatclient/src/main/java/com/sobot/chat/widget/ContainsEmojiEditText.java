package com.sobot.chat.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
    private static final int MAX_AUTO_COMPLETE_NUM = 4;
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
//                        e.printStackTrace();
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
        final ListView listView = handleListView(contentView, list);
        if (mPopWindow == null) {
            mPopWindow = new SobotCustomPopWindow.PopupWindowBuilder(getContext())
                    .setView(contentView)
                    .setFocusable(false)
                    .setOutsideTouchable(false)
                    .setWidthMatchParent(true)
                    .create();
        }
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        mPopWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0], location[1] - ScreenUtils.dip2px(getContext(), 33) * (list.size() > MAX_AUTO_COMPLETE_NUM ? MAX_AUTO_COMPLETE_NUM : list.size()) - ScreenUtils.dip2px(getContext(), 16+20));
    }

    private ListView handleListView(View contentView, final List<RespInfoListBean> list) {
        final ListView listView = (ListView) contentView.findViewById(R.id.sobot_lv_menu);
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

    private void notifyAdapter(ListView listView, final List<RespInfoListBean> list) {
        if (list == null || listView == null) {
            return;
        }
        if (mAdapter == null) {
            List<RespInfoListBean> tmpList = new ArrayList<>();
            tmpList.clear();
            tmpList.addAll(list);
            mAdapter = new SobotAutoCompelteAdapter(getContext(), tmpList);
            listView.setAdapter(mAdapter);
        } else {
            List<RespInfoListBean> datas = mAdapter.getDatas();
            if (datas != null) {
                datas.clear();
                datas.addAll(list);
            }
            mAdapter.notifyDataSetChanged();
        }
        listView.setSelection(0);

        measureListViewHeight(listView, list.size());
    }

    private void measureListViewHeight(ListView listView, int count) {
        int listHeight;
        if (count > MAX_AUTO_COMPLETE_NUM) {
            listHeight = ScreenUtils.dip2px(getContext(), 33) * MAX_AUTO_COMPLETE_NUM;
        } else {
            listHeight = ScreenUtils.dip2px(getContext(), 33) * count;
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listView.getLayoutParams();
        params.height = listHeight;
        listView.setLayoutParams(params);
    }

    public void dismissPop() {
        if (mPopWindow != null) {
            try {
                mPopWindow.dissmiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private View getContentView() {
        if (mContentView == null) {
            synchronized (ContainsEmojiEditText.class) {
                if (mContentView == null) {
                    mContentView = LayoutInflater.from(getContext()).inflate(R.layout.sobot_layout_auto_complete, null);
                }
            }
        }
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
                holder.sobot_child_menu.setText(Html.fromHtml(child.getHighlight()));
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