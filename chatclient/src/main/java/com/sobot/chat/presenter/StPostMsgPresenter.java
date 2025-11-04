package com.sobot.chat.presenter;

import android.content.Context;
import android.content.Intent;

import com.sobot.chat.activity.halfdialog.SobotMuItiPostMsgActivty;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.SobotLeaveMsgConfig;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

/**
 * 留言逻辑处理
 *
 * @author Created by jinxl on 2019/3/5.
 */
public class StPostMsgPresenter {
    public static final String INTENT_KEY_UID = "intent_key_uid";
    public static final String INTENT_KEY_CONFIG = "intent_key_config";
    public static final String INTENT_KEY_GROUPID = "intent_key_groupid";
    public static final String INTENT_KEY_CUSTOMERID = "intent_key_customerid";
    public static final String INTENT_KEY_COMPANYID = "intent_key_companyid";
    public static final String INTENT_KEY_TEMPID = "intent_key_tempid";
    public static final String INTENT_KEY_FROM = "intent_key_from";
    public static final String INTENT_KEY_TICKET_LIST = "intent_key_ticket_list";
    public static final String INTENT_KEY_TEMP_LIST = "intent_key_temp_list";
    //进入留言列表
    public static final int TICKET_TO_LIST=0;
    //进入留言新建
    public static final int TICKET_TO_NEW=1;

    private StPostMsgPresenter() {
    }

    ZhiChiApi mApi;
    private Object mCancelTag;
    private Context mContext;
    private ObtainTemplateListDelegate mDelegate;

    //防止获取留言模板列表接口重复调用
    private boolean mIsRunning;

    private boolean mIsActive = true;

    private StPostMsgPresenter(Object cancelTag, Context context) {
        this.mCancelTag = cancelTag;
        this.mContext = context;
        this.mIsActive = true;
        mApi = SobotMsgManager.getInstance(mContext).getZhiChiApi();
    }


    public static StPostMsgPresenter newInstance(Object tag, Context context) {
        return new StPostMsgPresenter(tag, context);
    }

    public interface ObtainTemplateListDelegate {
        void onSuccess(Intent intent);

    }

    /**
     * 获取留言模板的配置,然后启动多轮工单节点留言弹窗
     *
     * @param uid
     * @param templateId
     */
    public void obtainTmpConfigToMuItiPostMsg(final String uid, final String templateId, final String tipMsgId) {
        mApi.getMsgTemplateConfig(mCancelTag, uid, templateId, new StringResultCallBack<SobotLeaveMsgConfig>() {
            @Override
            public void onSuccess(SobotLeaveMsgConfig data) {
                Intent intent = new Intent(mContext, SobotMuItiPostMsgActivty.class);
                intent.putExtra(INTENT_KEY_UID, uid);
                intent.putExtra("templateId", templateId);
                intent.putExtra("tipMsgId", tipMsgId);
                intent.putExtra(INTENT_KEY_CONFIG, data);
                mContext.startActivity(intent);
            }

            @Override
            public void onFailure(Exception e, String des) {
                processReqFailure(e, des);
            }
        });
    }

    /**
     * 处理接口失败的情况
     *
     * @param e
     * @param des
     */
    private void processReqFailure(Exception e, String des) {
        mIsRunning = false;
        if (!mIsActive) {
            return;
        }
        ToastUtil.showToast(mContext, des);
    }

    /**
     * 注销接口
     */
    public void destory() {
        mIsActive = false;
        HttpUtils.getInstance().cancelTag(mCancelTag);
    }
}
