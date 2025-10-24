package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotSemanticsKeyWordTransfer;
import com.sobot.chat.api.model.ZhiChiGroupBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

import java.util.List;

public class RobotSemanticsKeyWordMessageHolder extends MsgHolderBase {

    private TextView tv_title;
    private LinearLayout sobot_keyword_grouplist;

    public RobotSemanticsKeyWordMessageHolder(Context context, View convertView) {
        super(context, convertView);
        tv_title = (TextView) convertView.findViewById(R.id.sobot_keyword_tips_msg);
        sobot_keyword_grouplist = (LinearLayout) convertView.findViewById(R.id.sobot_keyword_grouplist);
    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        if (message != null) {
            SobotSemanticsKeyWordTransfer sobotKeyWordTransfer = message.getSemanticsKeyWordTransfer();
            if (sobotKeyWordTransfer != null) {
                if (StringUtils.isNoEmpty(sobotKeyWordTransfer.getTransferTips())) {
                    tv_title.setVisibility(View.VISIBLE);
                    HtmlTools.getInstance(context).setRichText(tv_title, sobotKeyWordTransfer.getTransferTips(), isRight ? getLinkTextColor() : getLinkTextColor());
                } else {
                    tv_title.setVisibility(View.GONE);
                }

                List<ZhiChiGroupBase> groupList = sobotKeyWordTransfer.getGroupList();
                if (groupList != null && groupList.size() > 0) {
                    sobot_keyword_grouplist.removeAllViews();
                    sobot_keyword_grouplist.setVisibility(View.VISIBLE);
                    for (int i = 0; i < groupList.size(); i++) {
                        KeyWorkTempModel model = new KeyWorkTempModel();
                        model.setTempGroupId(groupList.get(i).getGroupId());
                        model.setSemanticsKeyWordId(sobotKeyWordTransfer.getSemanticsKeyWordId());
                        model.setSemanticsKeyWordName(sobotKeyWordTransfer.getSemanticsKeyWordName());
                        model.setSemanticsKeyWordQuestionId(sobotKeyWordTransfer.getSemanticsKeyWordQuestionId());
                        model.setSemanticsKeyWordQuestion(sobotKeyWordTransfer.getSemanticsKeyWordQuestion());
                        model.setAnwerMsgId(message.getMsgId());
                        model.setRuleld(message.getRuleId());
                        TextView tv = ChatUtils.initAnswerItemTextView(context, false);
                        tv.setText(groupList.get(i).getGroupName());
                        tv.setTag(model);
                        tv.setOnClickListener(mKeyWorkCheckGroupClickListener);
                        sobot_keyword_grouplist.addView(tv);
                    }
                } else {
                    sobot_keyword_grouplist.setVisibility(View.GONE);
                }
            }
        }
        refreshReadStatus();
        resetMaxWidth();
    }

    private View.OnClickListener mKeyWorkCheckGroupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction(ZhiChiConstants.SOBOT_BROCAST_SEMANTICS_KEYWORD_CLICK);
            KeyWorkTempModel sobotKeyWordTransfer = (KeyWorkTempModel) v.getTag();
            intent.putExtra("tempGroupId", sobotKeyWordTransfer.getTempGroupId());
            intent.putExtra("semanticsKeyWordId", sobotKeyWordTransfer.getSemanticsKeyWordId());
            intent.putExtra("semanticsKeyWordName", sobotKeyWordTransfer.getSemanticsKeyWordName());
            intent.putExtra("semanticsKeyWordQuestionId", sobotKeyWordTransfer.getSemanticsKeyWordQuestionId());
            intent.putExtra("semanticsKeyWordQuestion", sobotKeyWordTransfer.getSemanticsKeyWordQuestion());
            intent.putExtra("anwerMsgId", sobotKeyWordTransfer.getAnwerMsgId());
            intent.putExtra("ruleld", sobotKeyWordTransfer.getRuleld());
            CommonUtils.sendLocalBroadcast(mContext, intent);
        }
    };

    class KeyWorkTempModel {
        private String tempGroupId;
        private String anwerMsgId;
        private String ruleld;

        /**
         * 语义id
         */
        private String semanticsKeyWordId;
        /**
         * 语义名称
         */
        private String semanticsKeyWordName;
        /**
         * 问法id
         */
        private String semanticsKeyWordQuestionId;
        /**
         * 问法
         */
        private String semanticsKeyWordQuestion;

        public String getAnwerMsgId() {
            return anwerMsgId;
        }

        public void setAnwerMsgId(String anwerMsgId) {
            this.anwerMsgId = anwerMsgId;
        }

        public String getRuleld() {
            return ruleld;
        }

        public void setRuleld(String ruleld) {
            this.ruleld = ruleld;
        }

        public String getTempGroupId() {
            return tempGroupId;
        }

        public void setTempGroupId(String tempGroupId) {
            this.tempGroupId = tempGroupId;
        }

        public String getSemanticsKeyWordId() {
            return semanticsKeyWordId;
        }

        public void setSemanticsKeyWordId(String semanticsKeyWordId) {
            this.semanticsKeyWordId = semanticsKeyWordId;
        }

        public String getSemanticsKeyWordName() {
            return semanticsKeyWordName;
        }

        public void setSemanticsKeyWordName(String semanticsKeyWordName) {
            this.semanticsKeyWordName = semanticsKeyWordName;
        }

        public String getSemanticsKeyWordQuestionId() {
            return semanticsKeyWordQuestionId;
        }

        public void setSemanticsKeyWordQuestionId(String semanticsKeyWordQuestionId) {
            this.semanticsKeyWordQuestionId = semanticsKeyWordQuestionId;
        }

        public String getSemanticsKeyWordQuestion() {
            return semanticsKeyWordQuestion;
        }

        public void setSemanticsKeyWordQuestion(String semanticsKeyWordQuestion) {
            this.semanticsKeyWordQuestion = semanticsKeyWordQuestion;
        }
    }
}