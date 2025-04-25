package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.horizontalgridpage.HorizontalGridPage;
import com.sobot.chat.widget.horizontalgridpage.PageBuilder;
import com.sobot.chat.widget.horizontalgridpage.PageCallBack;
import com.sobot.chat.widget.horizontalgridpage.PageGridAdapter;
import com.sobot.chat.widget.image.SobotProgressImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder1 extends MsgHolderBase {

    private TextView tv_title;
    private PageGridAdapter adapter;
    private HorizontalGridPage pageView;
    private Context mContext;
    private PageBuilder pageBuilder;

    public ZhiChiMessageBase message;

    public RobotTemplateMessageHolder1(Context context, View convertView) {
        super(context, convertView);
        tv_title = (TextView) convertView.findViewById(R.id.sobot_template1_msg);
        pageView = (HorizontalGridPage) convertView.findViewById(R.id.pageView);
        this.mContext = context;
    }

    //初始化翻页控件布局 多少行 多少列
    public void initView(int row, int column) {
        //只初始化一次，不然会重复创建
        if (pageBuilder != null) {
            return;
        }
        pageBuilder = new PageBuilder.Builder()
                .setGrid(row, column)//设置网格
                .setPageMargin(0)//页面边距
                .setIndicatorMargins(4, 4, 4, 4)//设置指示器间隔
                .setIndicatorSize(6)//设置指示器大小
                .setIndicatorRes(android.R.drawable.presence_invisible,
                        android.R.drawable.presence_online)//设置指示器图片资源
                .setIndicatorGravity(Gravity.CENTER)//设置指示器位置
                .setSwipePercent(40)//设置翻页滑动距离百分比（1-100）
                .setShowIndicator(true)//设置显示指示器
                .setSpace(0)//设置间距
                .setItemHeight(ScreenUtils.dip2px(mContext, 130))
                .build();

        adapter = new PageGridAdapter<>(new PageCallBack() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sobot_chat_msg_item_template1_item_l, parent, false);
                return new Template1ViewHolder(view, parent.getContext());
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                //注意：因为data经过转换，所以此处不能使用data.get(position)而要使用adapter.getData().get(position)
                Map<String, String> interfaceRet = (Map<String, String>) adapter.getData().get(position);
                Template1ViewHolder viewHolder = (Template1ViewHolder) holder;
                if (!TextUtils.isEmpty(interfaceRet.get("thumbnail"))) {
                    viewHolder.sobotThumbnail.setVisibility(View.VISIBLE);
                    viewHolder.sobotSummary.setMaxLines(1);
                    viewHolder.sobotSummary.setEllipsize(TextUtils.TruncateAt.END);
                    viewHolder.sobotThumbnail.setImageUrl(interfaceRet.get("thumbnail"));
                } else {
                    viewHolder.sobotThumbnail.setVisibility(View.GONE);
                }

                viewHolder.sobotTitle.setText(interfaceRet.get("title"));
                viewHolder.sobotSummary.setText(interfaceRet.get("summary"));
                viewHolder.sobotLable.setText(interfaceRet.get("label"));
                viewHolder.sobotOtherLable.setText(interfaceRet.get("tag"));

                if (!TextUtils.isEmpty(interfaceRet.get("label"))) {
                    viewHolder.sobotLable.setVisibility(View.VISIBLE);
                    viewHolder.sobotLable.setTextColor(ThemeUtils.getThemeColor(mContext));
                } else {
                    viewHolder.sobotLable.setVisibility(View.GONE);
                }
//                // 测量 TextView 的宽度
//                viewHolder.sobotLable.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//                int labelWidth = viewHolder.sobotLable.getMeasuredWidth();
//
//                viewHolder.sobotOtherLable.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//                int otherFlagWidth = viewHolder.sobotOtherLable.getMeasuredWidth();
//
//                // 获取容器的宽度
//                viewHolder.sobot_ll_lable.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//                int containerWidth = viewHolder.sobot_ll_lable.getMeasuredWidth();
//
//                // 如果两个 TextView 的总宽度超过容器宽度，则将方向改为垂直
//                if (labelWidth + otherFlagWidth > containerWidth) {
//                    viewHolder.sobot_ll_lable.setOrientation(LinearLayout.VERTICAL);
//                } else {
//                    viewHolder.sobot_ll_lable.setOrientation(LinearLayout.HORIZONTAL);
//                }
            }

            @Override
            public void onItemClickListener(View view, int position) {
                String lastCid = SharedPreferencesUtil.getStringData(mContext, "lastCid", "");
                //当前cid相同相同才能重复点;ClickFlag 是否允许多次点击 0:只点击一次 1:允许重复点击
                //ClickFlag=0 时  ClickCount=0可点击，大于0 不可点击
                if (adapter.getZhiChiMessageBaseData().getSugguestionsFontColor() == 0) {
                    if (!TextUtils.isEmpty(adapter.getZhiChiMessageBaseData().getCid()) && lastCid.equals(adapter.getZhiChiMessageBaseData().getCid())) {
                        if (adapter.getZhiChiMessageBaseData().getAnswer().getMultiDiaRespInfo().getClickFlag() == 0 && adapter.getZhiChiMessageBaseData().getClickCount() > 0) {
                            return;
                        }
                        adapter.getZhiChiMessageBaseData().addClickCount();
                    } else {
                        return;
                    }
                } else {
                    return;
                }
                SobotMultiDiaRespInfo mMultiDiaRespInfo = adapter.getZhiChiMessageBaseData().getAnswer().getMultiDiaRespInfo();
                Map<String, String> mInterfaceRet = (Map<String, String>) adapter.getData().get(position);
                if (mContext == null || mMultiDiaRespInfo == null || mInterfaceRet == null) {
                    return;
                }

                if (mMultiDiaRespInfo.getEndFlag() && !TextUtils.isEmpty(mInterfaceRet.get("anchor"))) {
                    if (SobotOption.newHyperlinkListener != null) {
                        //如果返回true,拦截;false 不拦截
                        boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, mInterfaceRet.get("anchor"));
                        if (isIntercept) {
                            return;
                        }
                    }
                    Intent intent = new Intent(mContext, WebViewActivity.class);
                    intent.putExtra("url", mInterfaceRet.get("anchor"));
                    mContext.startActivity(intent);
                } else {
                    ChatUtils.sendMultiRoundQuestions(mContext, mMultiDiaRespInfo, mInterfaceRet, msgCallBack);
                }
            }

            @Override
            public void onItemLongClickListener(View view, int position) {

            }
        });
        pageView.setLayoutParams(new LinearLayout.LayoutParams(msgCardWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        pageView.init(pageBuilder, message.getCurrentPageNum());
        adapter.init(pageBuilder);
        pageView.setAdapter(adapter, message);
    }


    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        this.message = message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            String msgStr = ChatUtils.getMultiMsgTitle(multiDiaRespInfo);
            if (!TextUtils.isEmpty(msgStr)) {
                HtmlTools.getInstance(context).setRichText(tv_title, msgStr.replaceAll("\n", "<br/>"), getLinkTextColor());
                tv_title.setVisibility(View.VISIBLE);
            } else {
                tv_title.setVisibility(View.INVISIBLE);
            }
            final List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
            if ("000000".equals(multiDiaRespInfo.getRetCode()) && interfaceRetList != null && interfaceRetList.size() > 0) {
                resetMaxWidth();
                pageView.setVisibility(View.VISIBLE);
                if (interfaceRetList.size() >= 3) {
                    initView(3, 1);
                } else {
                    initView(interfaceRetList.size(), (int) Math.ceil(interfaceRetList.size() / 3.0f));
                }
                adapter.setData((ArrayList) interfaceRetList);
                adapter.setZhiChiMessageBaseData(message);
            } else {
                pageView.setVisibility(View.GONE);
            }
        }
        refreshItem();//左侧消息刷新顶和踩布局
        checkShowTransferBtn();//检查转人工逻辑
        //关联问题显示逻辑
        if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
            resetAnswersList();
        } else {
            hideAnswers();
        }
        pageView.selectCurrentItem();
        refreshReadStatus();
    }


    /**
     * 自定义ViewHolder来更新item，这里这是演示更新选中项的背景
     */
    class Template1ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout sobotLayout,sobot_ll_lable;
        SobotProgressImageView sobotThumbnail;
        TextView sobotTitle;
        TextView sobotSummary;
        TextView sobotLable;
        TextView sobotOtherLable;


        public Template1ViewHolder(View convertView, Context context) {
            super(convertView);
            sobotLayout = (LinearLayout) convertView.findViewById(R.id.sobot_template1_item_);
            sobot_ll_lable = (LinearLayout) convertView.findViewById(R.id.sobot_ll_lable);
            sobotThumbnail = (SobotProgressImageView) convertView.findViewById(R.id.sobot_template1_item_thumbnail);
            sobotTitle = (TextView) convertView.findViewById(R.id.sobot_template1_item_title);
            sobotSummary = (TextView) convertView.findViewById(R.id.sobot_template1_item_summary);
            sobotLable = (TextView) convertView.findViewById(R.id.sobot_template1_item_lable);
            sobotOtherLable = (TextView) convertView.findViewById(R.id.sobot_template1_item_other_flag);
        }
    }

}