package com.sobot.chat.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotTicketDetailActivity;
import com.sobot.chat.adapter.SobotTicketInfoAdapter;
import com.sobot.chat.api.model.SobotTicketStatus;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.presenter.StPostMsgPresenter;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 留言记录列表界面
 *
 * @author Created by jinxl on 2019/3/7.
 */
public class SobotTicketInfoFragment extends SobotChatBaseFragment {

    private final static int REQUEST_CODE = 0x001;

    private RecyclerView recyclerView;
    private TextView mEmptyView;
    private SobotTicketInfoAdapter mAdapter;

    private String mUid = "";
    private String mCustomerId = "";
    private String mCompanyId = "";

    private List<SobotUserTicketInfo> mList = new ArrayList<>();
    private List<SobotTicketStatus> statusList;

    public static SobotTicketInfoFragment newInstance(Bundle data) {
        Bundle arguments = new Bundle();
        arguments.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, data);
        SobotTicketInfoFragment fragment = new SobotTicketInfoFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments().getBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
            if (bundle != null) {
                mUid = bundle.getString(StPostMsgPresenter.INTENT_KEY_UID);
                mCustomerId = bundle.getString(StPostMsgPresenter.INTENT_KEY_CUSTOMERID);
                mCompanyId = bundle.getString(StPostMsgPresenter.INTENT_KEY_COMPANYID);
            }
        }
        statusList = ChatUtils.getStatusList();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.sobot_fragment_ticket_info, container, false);
        initView(mRootView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initData();
        super.onActivityCreated(savedInstanceState);
    }

    protected void initView(View rootView) {
        recyclerView = rootView.findViewById(R.id.sobot_listview);
        mEmptyView = rootView.findViewById(R.id.sobot_empty);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        // 设置RecyclerView的LayoutManager
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new SobotTicketInfoAdapter(getActivity(), mList, new SobotTicketInfoAdapter.SobotItemListener() {
            @Override
            public void onItemClick(SobotUserTicketInfo model) {
                Intent intent = SobotTicketDetailActivity.newIntent(getContext(), mCompanyId, mUid, model);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    public void initData() {
        if ("null".equals(mCustomerId)) {
            mCustomerId = "";
        }
        if (!isAdded() || TextUtils.isEmpty(mCompanyId) || TextUtils.isEmpty(mUid)) {
            return;
        }
        if (statusList == null || statusList.size() == 0) {
            String companyId = SharedPreferencesUtil.getStringData(getContext(),
                    ZhiChiConstant.SOBOT_CONFIG_COMPANYID, "");
            String languageCode = SharedPreferencesUtil.getStringData(getContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
            zhiChiApi.getTicketStatus(getContext(), companyId, languageCode, new StringResultCallBack<List<SobotTicketStatus>>() {
                @Override
                public void onSuccess(List<SobotTicketStatus> sobotTicketStatuses) {
                    ChatUtils.setStatusList(sobotTicketStatuses);
                    if(statusList == null){
                        statusList=new ArrayList<>();
                    }else{
                        statusList.clear();
                    }
                    statusList.addAll(sobotTicketStatuses);
                    if (mAdapter != null) {
                        mAdapter.setStatusList(statusList);
                    }
                    requestDate();
                }

                @Override
                public void onFailure(Exception e, String s) {
                    requestDate();
                }
            });
        } else {
            requestDate();
        }

    }

    private void requestDate() {
        zhiChiApi.getUserTicketInfoList(SobotTicketInfoFragment.this, mUid, mCompanyId, mCustomerId, new StringResultCallBack<List<SobotUserTicketInfo>>() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<SobotUserTicketInfo> datas) {
                if (datas != null && datas.size() > 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    mList.clear();
                    mList.addAll(datas);
                    mAdapter.setStatusList(ChatUtils.getStatusList());
                    mAdapter.notifyDataSetChanged();
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                mEmptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                LogUtils.i(des);
            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            initData();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
