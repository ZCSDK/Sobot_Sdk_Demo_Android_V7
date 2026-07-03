package com.sobot.chat.widget;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.widget.switchkeyboardlib.model.SobotPlusEntity;
import com.sobot.chat.widget.switchkeyboardlib.panel.FunctionGridAdapter;
import com.sobot.chat.widget.switchkeyboardlib.panel.FunctionMenuPageView;

import java.util.List;

/**
 * 横屏下底部表情/加号面板的浮卡 Popup。
 * <ul>
 *     <li>表情面板：把 fl_emoji 从 ll_menu 中搬入 popup 内的 emoji 容器，关闭归还；
 *     共用原 EmojiAdapter，点击/删除等业务无须复制</li>
 *     <li>加号功能面板：popup 内独立 RecyclerView + GridLayoutManager（固定 3 列），
 *     数据从 FunctionMenuPageView.getShowList() 取，点击回调走原 OnFunctionItemClickListener</li>
 * </ul>
 */
public class SobotChatBottomPanelPopup {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_EMOJI = 1;
    public static final int TYPE_FUNCTION = 2;

    /**
     * 浮卡宽度占屏幕宽度的比例
     */
    private static final float WIDTH_RATIO = 0.45f;
    /**
     * 浮卡高度占屏幕高度的比例
     */
    private static final float HEIGHT_RATIO = 0.79f;
    /**
     * 加号功能 grid 列数（固定 3 列）
     */
    private static final int FUNCTION_SPAN_COUNT = 3;

    private final Activity mActivity;
    private PopupWindow mPopupWindow;
    private FrameLayout mEmojiContainer;
    private RecyclerView mFunctionRv;
    private View mCardView;//内层卡片 view，用于对外回调时作为可见 root

    private View mCurrentEmojiPanel;
    private ViewGroup mEmojiOriginalParent;
    private int mEmojiOriginalIndex = -1;
    private ViewGroup.LayoutParams mEmojiOriginalParams;
    private View mStretchedInner;
    private int mStretchedInnerOriginalHeight;

    private int mCurrentType = TYPE_NONE;
    private OnPanelDismissListener mDismissListener;

    public interface OnPanelDismissListener {
        /**
         * @param dismissedType 关闭时显示的面板类型（TYPE_EMOJI / TYPE_FUNCTION）
         */
        void onPanelDismiss(int dismissedType);
    }

    public SobotChatBottomPanelPopup(Activity activity) {
        this.mActivity = activity;
    }

    public void setOnPanelDismissListener(OnPanelDismissListener listener) {
        this.mDismissListener = listener;
    }

    public int getCurrentType() {
        return mCurrentType;
    }

    /**
     * 返回 popup 内层卡片 view（含背景/圆角），用于对外回调时作为可见 view 参数。
     * popup 未创建时返回 null。
     */
    public View getCardView() {
        return mCardView;
    }

    public boolean isShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    /**
     * 显示表情面板：把 emojiPanel（fl_emoji）从原 parent 搬到 popup 内 emoji 容器。
     */
    public void showEmoji(View emojiPanel, View verticalAnchor, View anchorRightEdgeView) {
        if (emojiPanel == null || mActivity == null || mActivity.isFinishing()) {
            return;
        }
        ensurePopup();
        switchType(TYPE_EMOJI);
        if (mCurrentEmojiPanel != emojiPanel) {
            detachEmojiPanel();
            attachEmojiPanel(emojiPanel);
        }
        showAt(verticalAnchor, anchorRightEdgeView);
    }

    /**
     * 显示加号功能面板：popup 内独立 RV 用 GridLayoutManager 显示。
     */
    public void showFunction(List<SobotPlusEntity> items,
                             FunctionMenuPageView.OnFunctionItemClickListener listener,
                             View verticalAnchor, View anchorRightEdgeView) {
        if (items == null || mActivity == null || mActivity.isFinishing()) {
            return;
        }
        ensurePopup();
        switchType(TYPE_FUNCTION);
        if (mFunctionRv.getLayoutManager() == null) {
            mFunctionRv.setLayoutManager(new GridLayoutManager(mActivity, FUNCTION_SPAN_COUNT));
        }
        mFunctionRv.setAdapter(new FunctionGridAdapter(mActivity, items, listener));
        showAt(verticalAnchor, anchorRightEdgeView);
    }

    private void switchType(int newType) {
        if (mCurrentType == newType) {
            return;
        }
        //切换到非 emoji 类型前，把 emoji 容器内的面板归还到原 parent
        if (mCurrentType == TYPE_EMOJI && newType != TYPE_EMOJI) {
            detachEmojiPanel();
        }
        mCurrentType = newType;
        if (mEmojiContainer != null) {
            mEmojiContainer.setVisibility(newType == TYPE_EMOJI ? View.VISIBLE : View.GONE);
        }
        if (mFunctionRv != null) {
            mFunctionRv.setVisibility(newType == TYPE_FUNCTION ? View.VISIBLE : View.GONE);
        }
    }

    private void showAt(View verticalAnchor, View anchorRightEdgeView) {
        if (verticalAnchor == null || anchorRightEdgeView == null) {
            return;
        }

        int screenWidth = ScreenUtils.getScreenWidth(mActivity);
        int screenHeight = ScreenUtils.getScreenHeight(mActivity);
        //真正的卡片宽度（按屏幕比例）
        int cardWidth = (int) (screenWidth * WIDTH_RATIO);

        int[] anchorLoc = new int[2];
        verticalAnchor.getLocationInWindow(anchorLoc);
        int anchorTop = anchorLoc[1];

        int[] rightLoc = new int[2];
        anchorRightEdgeView.getLocationInWindow(rightLoc);
        int rightEdge = rightLoc[0] + anchorRightEdgeView.getWidth();

        int offset = mActivity.getResources()
                .getDimensionPixelSize(R.dimen.sobot_chat_bottom_panel_popup_offset);
        //外层透明 padding，给 elevation 阴影预留扩散空间；定位时需要补偿
        int shadowInset = mActivity.getResources()
                .getDimensionPixelSize(R.dimen.sobot_chat_bottom_panel_popup_shadow_inset);

        //卡片高度：emoji 用 77% 屏高（rv_emoji 已被拉伸到 MATCH_PARENT），function 按内容 wrap_content
        int cardHeight;
        int ratioMaxHeight = (int) (screenHeight * HEIGHT_RATIO);
        if (mCurrentType == TYPE_FUNCTION) {
            cardHeight = measureFunctionCardHeight(cardWidth, shadowInset);
            //function 内容自适应，但不超过 77% 屏高（多余内容由 RV 内部滚动）
            if (cardHeight > ratioMaxHeight) {
                cardHeight = ratioMaxHeight;
            }
        } else {
            cardHeight = ratioMaxHeight;
        }
        //再受锚点上方可用空间限制
        int maxCardHeight = anchorTop - offset;
        if (maxCardHeight > 0 && cardHeight > maxCardHeight) {
            cardHeight = maxCardHeight;
        }
        //PopupWindow 总尺寸 = 卡片 + 四周阴影 inset
        int popupWidth = cardWidth + shadowInset * 2;
        int popupHeight = cardHeight + shadowInset * 2;
        mPopupWindow.setWidth(popupWidth);
        mPopupWindow.setHeight(popupHeight);

        //卡片右边贴齐 anchorRightEdgeView 右边；卡片底部距锚点 offset
        int x = rightEdge + shadowInset - popupWidth;
        int y = anchorTop - offset - cardHeight - shadowInset;
        if (y < 0) {
            y = 0;
        }
        if (x < 0) {
            x = 0;
        }

        try {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.update(x, y, popupWidth, popupHeight);
            } else {
                mPopupWindow.showAtLocation(anchorRightEdgeView, Gravity.NO_GRAVITY, x, y);
            }
        } catch (Exception e) {
            //Activity 已 finish 或 token 失效时，show/update 会抛 BadTokenException
            LogUtils.e("SobotChatBottomPanelPopup showAt failed: " + e.getMessage());
        }
    }

    /**
     * 测量加号 grid 自然高度并换算成卡片高度（包含内层卡片 padding）。
     * 加号项数少时不撑满 77%，按内容自适应。
     */
    private int measureFunctionCardHeight(int cardWidth, int shadowInset) {
        View content = mPopupWindow.getContentView();
        if (content == null) {
            return 0;
        }
        int totalWidth = cardWidth + shadowInset * 2;
        int wSpec = View.MeasureSpec.makeMeasureSpec(totalWidth, View.MeasureSpec.EXACTLY);
        int hSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        content.measure(wSpec, hSpec);
        //content = 外层透明 padding(shadowInset) + 内层卡片；扣掉上下 shadowInset 得卡片高
        return content.getMeasuredHeight() - shadowInset * 2;
    }

    private void ensurePopup() {
        if (mPopupWindow != null) {
            return;
        }
        View content = LayoutInflater.from(mActivity)
                .inflate(R.layout.sobot_layout_chat_bottom_panel_popup, null, false);
        mEmojiContainer = content.findViewById(R.id.fl_bottom_panel_popup_root);
        mFunctionRv = content.findViewById(R.id.rv_function_popup);
        mCardView = content.findViewById(R.id.fl_bottom_panel_popup_card);

        mPopupWindow = new PopupWindow(content,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(false);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPopupWindow.setClippingEnabled(false);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                int dismissedType = mCurrentType;
                detachEmojiPanel();
                if (mFunctionRv != null) {
                    mFunctionRv.setAdapter(null);
                }
                mCurrentType = TYPE_NONE;
                if (mDismissListener != null) {
                    mDismissListener.onPanelDismiss(dismissedType);
                }
            }
        });
    }

    private void attachEmojiPanel(View panel) {
        ViewGroup parent = (ViewGroup) panel.getParent();
        if (parent != null) {
            mEmojiOriginalParent = parent;
            mEmojiOriginalIndex = parent.indexOfChild(panel);
            mEmojiOriginalParams = panel.getLayoutParams();
            parent.removeView(panel);
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mEmojiContainer.addView(panel, lp);
        panel.setVisibility(View.VISIBLE);
        mCurrentEmojiPanel = panel;
        stretchInnerScrollable(panel);
    }

    private void stretchInnerScrollable(View panel) {
        //直接用 R.id.rv_emoji，避免 getIdentifier 在 aar 集成 / ProGuard 资源混淆下查不到
        View inner = panel.findViewById(R.id.rv_emoji);
        if (inner == null || inner.getLayoutParams() == null) {
            return;
        }
        mStretchedInner = inner;
        mStretchedInnerOriginalHeight = inner.getLayoutParams().height;
        inner.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        inner.requestLayout();
    }

    private void detachEmojiPanel() {
        if (mCurrentEmojiPanel == null) {
            return;
        }
        if (mStretchedInner != null && mStretchedInner.getLayoutParams() != null) {
            mStretchedInner.getLayoutParams().height = mStretchedInnerOriginalHeight;
            mStretchedInner.requestLayout();
            mStretchedInner = null;
        }
        if (mCurrentEmojiPanel.getParent() == mEmojiContainer) {
            mEmojiContainer.removeView(mCurrentEmojiPanel);
        }
        if (mEmojiOriginalParent != null) {
            try {
                int index = mEmojiOriginalIndex;
                if (index < 0 || index > mEmojiOriginalParent.getChildCount()) {
                    index = mEmojiOriginalParent.getChildCount();
                }
                if (mEmojiOriginalParams != null) {
                    mEmojiOriginalParent.addView(mCurrentEmojiPanel, index, mEmojiOriginalParams);
                } else {
                    mEmojiOriginalParent.addView(mCurrentEmojiPanel, index);
                }
                mCurrentEmojiPanel.setVisibility(View.GONE);
            } catch (Exception e) {
                //再次 addView 时 parent 状态可能已不一致；记录日志便于排查
                LogUtils.e("SobotChatBottomPanelPopup detachEmojiPanel restore failed: " + e.getMessage());
            }
        }
        mCurrentEmojiPanel = null;
        mEmojiOriginalParent = null;
        mEmojiOriginalIndex = -1;
        mEmojiOriginalParams = null;
    }
}
