package com.sobot.chat.widget.kpswitch.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.sobot.chat.R;
import com.sobot.chat.SobotUIConfig;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SobotVisitorSchemeExtModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.kpswitch.view.emoticon.EmoticonsFuncView;
import com.sobot.chat.widget.kpswitch.view.emoticon.EmoticonsIndicatorView;
import com.sobot.chat.widget.kpswitch.view.plus.SobotPlusPageView;
import com.sobot.chat.widget.kpswitch.widget.adpater.PageSetAdapter;
import com.sobot.chat.widget.kpswitch.widget.adpater.PlusAdapter;
import com.sobot.chat.widget.kpswitch.widget.data.PageSetEntity;
import com.sobot.chat.widget.kpswitch.widget.data.PlusPageEntity;
import com.sobot.chat.widget.kpswitch.widget.data.PlusPageSetEntity;
import com.sobot.chat.widget.kpswitch.widget.interfaces.PageViewInstantiateListener;
import com.sobot.chat.widget.kpswitch.widget.interfaces.PlusDisplayListener;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 聊天面板   更多菜单
 *
 * @author Created by jinxl on 2018/7/31.
 */
public class ChattingPanelUploadView extends BaseChattingPanelView implements EmoticonsFuncView.OnEmoticonsPageViewListener {

    private static final String ACTION_SATISFACTION = "sobot_action_satisfaction";
    private static final String ACTION_LEAVEMSG = "sobot_action_leavemsg";
    private static final String ACTION_PIC = "sobot_action_pic";
    private static final String ACTION_VIDEO = "sobot_action_video";
    private static final String ACTION_CAMERA = "sobot_action_camera";
    private static final String ACTION_CHOOSE_FILE = "sobot_action_choose_file";
    private static final String ACTION_OPEN_WEB = "sobot_action_open_web";


    private List<SobotPlusEntity> robotList = new ArrayList<>();
    private List<SobotPlusEntity> operatorList = new ArrayList<>();

    //当前接待模式
    private int mCurrentClientMode = -1;
    private EmoticonsFuncView mEmoticonsFuncView;
    private EmoticonsIndicatorView mEmoticonsIndicatorView;
    private PageSetAdapter pageSetAdapter;
    private SobotPlusClickListener mListener;
    private SobotPlusCountListener countListener;

    public ChattingPanelUploadView(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        return View.inflate(context, R.layout.sobot_upload_layout, null);
    }

    @Override
    public void initData() {

        Information information = (Information) SharedPreferencesUtil.getObject(context, "sobot_last_current_info");
        ZhiChiInitModeBase initModeBase = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context, ZhiChiConstant.sobot_last_current_initModel);

        int leaveMsg = SharedPreferencesUtil.getIntData(context, ZhiChiConstant.sobot_msg_flag, ZhiChiConstant.sobot_msg_flag_open);
        //是否留言转离线消息,留言转离线消息模式下,人工模式加号中菜单不显示留言
        boolean msgToTicket = SharedPreferencesUtil.getBooleanData(context,
                ZhiChiConstant.sobot_leave_msg_flag, false);
        mEmoticonsFuncView = (EmoticonsFuncView) getRootView().findViewById(R.id.view_epv);
        mEmoticonsIndicatorView = ((EmoticonsIndicatorView) getRootView().findViewById(R.id.view_eiv));
        mEmoticonsFuncView.setOnIndicatorListener(this);

        //图片
        SobotPlusEntity picEntity = new SobotPlusEntity(R.drawable.sobot_take_picture_normal, context.getResources().getString(R.string.sobot_upload), ACTION_PIC);
        //视频
        SobotPlusEntity videoEntity = new SobotPlusEntity(R.drawable.sobot_take_video_normal, context.getResources().getString(R.string.sobot_upload_video), ACTION_VIDEO);
        //拍照
        SobotPlusEntity cameraEntity = new SobotPlusEntity(R.drawable.sobot_camera_picture_normal, context.getResources().getString(R.string.sobot_attach_take_pic), ACTION_CAMERA);
        //文件
        SobotPlusEntity fileEntity = new SobotPlusEntity(R.drawable.sobot_choose_file_normal, context.getResources().getString(R.string.sobot_choose_file), ACTION_CHOOSE_FILE);
        //留言
        SobotPlusEntity leavemsgEntity = new SobotPlusEntity(R.drawable.sobot_bottombar_conversation, context.getResources().getString(R.string.sobot_str_bottom_message), ACTION_LEAVEMSG);
        //评价
        SobotPlusEntity satisfactionEntity = new SobotPlusEntity(R.drawable.sobot_picture_satisfaction_normal, context.getResources().getString(R.string.sobot_str_bottom_satisfaction), ACTION_SATISFACTION);

        robotList.clear();
        operatorList.clear();

        if (information != null) {
            boolean serviceOpen = (initModeBase != null && initModeBase.getVisitorScheme() != null);
            List<SobotVisitorSchemeExtModel> appAppExtModelList = null;
            List<SobotVisitorSchemeExtModel> appExtModelManList = null;
            if (serviceOpen) {
                appAppExtModelList = initModeBase.getVisitorScheme().getAppExtModelList();
                appExtModelManList = initModeBase.getVisitorScheme().getAppExtModelManList();
            }

            if (serviceOpen && null != appAppExtModelList) {
                for (int i = 0; i < appAppExtModelList.size(); i++) {
                    SobotVisitorSchemeExtModel extModel = appAppExtModelList.get(i);
                    //1.留言 2 服务评价 3文件 4表情  5截图  6自定义跳转链接 7 图片 8 视频 9 拍摄
                    if (extModel.getExtModelType() == 7) {
                        picEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_PIC, i);
                        robotList.add(picEntity);
                    } else if (extModel.getExtModelType() == 8) {
                        videoEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_VIDEO, i);
                        robotList.add(videoEntity);
                    } else if (extModel.getExtModelType() == 9) {
                        cameraEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_CAMERA, i);
                        robotList.add(cameraEntity);
                    } else if (extModel.getExtModelType() == 3) {
                        fileEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_CHOOSE_FILE, i);
                        robotList.add(fileEntity);
                    } else if (extModel.getExtModelType() == 1) {
                        leavemsgEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_LEAVEMSG, i);
                        robotList.add(leavemsgEntity);
                    } else if (extModel.getExtModelType() == 2) {
                        satisfactionEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_SATISFACTION, i);
                        robotList.add(satisfactionEntity);
                    }else{
                        SobotPlusEntity webEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_OPEN_WEB, i,extModel.getExtModelLink());
                        robotList.add(webEntity);
                    }
                }
                if (serviceOpen && null != appExtModelManList) {
                    for (int i = 0; i < appExtModelManList.size(); i++) {
                        SobotVisitorSchemeExtModel extModel = appExtModelManList.get(i);
                        //1.留言 2 服务评价 3文件 4表情  5截图  6自定义跳转链接 7 图片 8 视频 9 拍摄
                        if (extModel.getExtModelType() == 7) {
                            picEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_PIC, i);
                            operatorList.add(picEntity);
                        } else if (extModel.getExtModelType() == 8) {
                            videoEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_VIDEO, i);
                            operatorList.add(videoEntity);
                        } else if (extModel.getExtModelType() == 9) {
                            cameraEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_CAMERA, i);
                            operatorList.add(cameraEntity);
                        } else if (extModel.getExtModelType() == 3) {
                            fileEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_CHOOSE_FILE, i);
                            operatorList.add(fileEntity);
                        } else if (extModel.getExtModelType() == 1) {
                            leavemsgEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_LEAVEMSG, i);
                            operatorList.add(leavemsgEntity);
                        } else if (extModel.getExtModelType() == 2) {
                            satisfactionEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_SATISFACTION, i);
                            operatorList.add(satisfactionEntity);
                        }else{
                            SobotPlusEntity webEntity = new SobotPlusEntity(extModel.getExtModelPhoto(), extModel.getExtModelName(), ACTION_OPEN_WEB, i,extModel.getExtModelLink());
                            operatorList.add(webEntity);
                        }
                    }
                }
            }

            //相册
            if (information.isHideMenuPicture()) {
                robotList.remove(picEntity);
                operatorList.remove(picEntity);
            }
            //视频
            if (information.isHideMenuVedio()) {
                robotList.remove(videoEntity);
                operatorList.remove(videoEntity);
            }
            //拍照
            if (information.isHideMenuCamera()) {
                robotList.remove(cameraEntity);
                operatorList.remove(cameraEntity);
            }
            //文件
            if (information.isHideMenuFile()) {
                robotList.remove(fileEntity);
                operatorList.remove(fileEntity);
            }
            //留言
            if (information.isHideMenuLeave()) {
                robotList.remove(leavemsgEntity);
                operatorList.remove(leavemsgEntity);
            }
            if (information.isHideMenuManualLeave()) {
                operatorList.remove(leavemsgEntity);
            }
            //评价
            if (information.isHideMenuSatisfaction()) {
                robotList.remove(satisfactionEntity);
                operatorList.remove(satisfactionEntity);
            }
            //排序
            if (robotList != null) {
                Collections.sort(robotList, new Comparator<SobotPlusEntity>() {
                    @Override
                    public int compare(SobotPlusEntity sobotPlusEntity, SobotPlusEntity t1) {
                        return sobotPlusEntity.index - t1.index;
                    }
                });
            }
            if (operatorList != null) {
                Collections.sort(operatorList, new Comparator<SobotPlusEntity>() {
                    @Override
                    public int compare(SobotPlusEntity sobotPlusEntity, SobotPlusEntity t1) {
                        return sobotPlusEntity.index - t1.index;
                    }
                });
            }
        }

    }

    public static class SobotPlusEntity {
        public int iconResId;
        public String iconUrl;
        public String name;
        public String action;
        public String extModelLink;

        private int index = 0;

        /**
         * 自定义菜单实体类
         *
         * @param iconResId 菜单图标
         * @param name      菜单名称
         * @param action    菜单动作 当点击按钮时会将对应action返回给callback
         *                  以此作为依据，判断用户点击了哪个按钮
         */
        public SobotPlusEntity(int iconResId, String name, String action) {
            this.iconResId = iconResId;
            this.name = name;
            this.action = action;
        }

        /**
         * 自定义菜单实体类
         *
         * @param iconUrl 菜单图标 url
         * @param name    菜单名称
         * @param action  菜单动作 当点击按钮时会将对应action返回给callback
         *                以此作为依据，判断用户点击了哪个按钮
         */
        public SobotPlusEntity(String iconUrl, String name, String action, int index) {
            this.iconUrl = iconUrl;
            this.name = name;
            this.action = action;
            this.index = index;
        }
        public SobotPlusEntity(String iconUrl, String name, String action, int index,String link) {
            this.iconUrl = iconUrl;
            this.name = name;
            this.action = action;
            this.index = index;
            this.extModelLink = link;
        }
    }

    private void setAdapter(List<SobotPlusEntity> datas) {
        if (pageSetAdapter == null) {
            pageSetAdapter = new PageSetAdapter();
        } else {
            pageSetAdapter.getPageSetEntityList().clear();
        }

        PlusPageSetEntity pageSetEntity
                = new PlusPageSetEntity.Builder()
                .setLine(context.getResources().getInteger(R.integer.sobot_plus_menu_line))
                .setRow(context.getResources().getInteger(R.integer.sobot_plus_menu_row))
                .setDataList(datas)
                .setIPageViewInstantiateItem(new PageViewInstantiateListener<PlusPageEntity>() {
                    @Override
                    public View instantiateItem(ViewGroup container, int position, PlusPageEntity pageEntity) {
                        if (pageEntity.getRootView() == null) {
                            //下面这个view  就是一个gridview
                            SobotPlusPageView pageView = new SobotPlusPageView(container.getContext());
                            pageView.setNumColumns(pageEntity.getRow());
                            pageEntity.setRootView(pageView);
                            try {
                                PlusAdapter adapter = new PlusAdapter(container.getContext(), pageEntity, mListener);
                                adapter.setItemHeightMaxRatio(1.8);
                                adapter.setOnDisPlayListener(getPlusItemDisplayListener(mListener));
                                pageView.getGridView().setAdapter(adapter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return pageEntity.getRootView();
                    }
                })
                .build();
        pageSetAdapter.add(pageSetEntity);
        mEmoticonsFuncView.setAdapter(pageSetAdapter);
    }

    /**
     * 这个是adapter里面的bindview回调
     * 作用就是绑定数据用的
     *
     * @param plusClickListener 点击表情的回调
     * @return
     */
    public PlusDisplayListener<Object> getPlusItemDisplayListener(final SobotPlusClickListener plusClickListener) {
        return new PlusDisplayListener<Object>() {
            @Override
            public void onBindView(int position, ViewGroup parent, PlusAdapter.ViewHolder viewHolder, Object object) {
                final SobotPlusEntity plusEntity = (SobotPlusEntity) object;
                if (plusEntity == null) {
                    return;
                }
                // 显示菜单
                //viewHolder.ly_root.setBackgroundResource(getResDrawableId("sobot_bg_emoticon"));
                viewHolder.mMenu.setText(plusEntity.name);
                if (plusEntity.iconResId != 0) {
                    SobotBitmapUtil.display(context, plusEntity.iconResId, viewHolder.mMenuIcon);
                } else {
                    SobotBitmapUtil.display(context, plusEntity.iconUrl, viewHolder.mMenuIcon);
                }
                viewHolder.mMenu.setTag(plusEntity.action);
                viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            View sobot_plus_menu = v.findViewById(R.id.sobot_plus_menu);
                            String action = (String) sobot_plus_menu.getTag();
                            if (ACTION_SATISFACTION.equals(action)) {
                                //评价客服或机器人
                                mListener.btnSatisfaction();
                            } else if (ACTION_LEAVEMSG.equals(action)) {
                                //留言
                                mListener.startToPostMsgActivty(false);
                            } else if (ACTION_PIC.equals(action)) {
                                //图库
                                mListener.btnPicture();
                            } else if (ACTION_VIDEO.equals(action)) {
                                //视频
                                mListener.btnVedio();
                            } else if (ACTION_CAMERA.equals(action)) {
                                //拍照
                                mListener.btnCameraPicture();
                            } else if (ACTION_CHOOSE_FILE.equals(action)) {
                                //选择文件
                                mListener.chooseFile();
                            } else if (ACTION_OPEN_WEB.equals(action)) {
                                //打开网页
                                mListener.openWeb(plusEntity.extModelLink);
                            } else {
                                if (SobotUIConfig.pulsMenu.sSobotPlusMenuListener != null) {
                                    SobotUIConfig.pulsMenu.sSobotPlusMenuListener.onClick(v, action);
                                }
                            }
                        }
                    }
                });
            }
        };
    }

    @Override
    public void emoticonSetChanged(PageSetEntity pageSetEntity) {

    }

    @Override
    public void playTo(int position, PageSetEntity pageSetEntity) {
        mEmoticonsIndicatorView.playTo(position, pageSetEntity);
    }

    @Override
    public void playBy(int oldPosition, int newPosition, PageSetEntity pageSetEntity) {
        mEmoticonsIndicatorView.playBy(oldPosition, newPosition, pageSetEntity);
    }

    public interface SobotPlusClickListener extends SobotBasePanelListener {
        void btnPicture();

        void btnVedio();

        void btnCameraPicture();

        void btnSatisfaction();

        void startToPostMsgActivty(boolean flag);

        void chooseFile();
        void openWeb(String url);
    }

    public interface SobotPlusCountListener extends SobotBasePanelCountListener {
        //设置机器人聊天模式下加号面板菜单功能的数量
        void setRobotOperatorCount(int robotCount);

        //设置人工聊天模式下加号面板菜单功能的数量
        void setOperatorCount(int operatorCount);
    }

    @Override
    public void setListener(SobotBasePanelListener listener) {
        if (listener != null && listener instanceof SobotPlusClickListener) {
            mListener = (SobotPlusClickListener) listener;
        }
    }

    @Override
    public void setCountListener(SobotBasePanelCountListener listener) {
        if (listener != null && listener instanceof SobotPlusCountListener) {
            countListener = (SobotPlusCountListener) listener;
        }
    }

    @Override
    public String getRootViewTag() {
        return "ChattingPanelUploadView";
    }

    @Override
    public void onViewStart(Bundle bundle) {
        int tmpClientMode = bundle.getInt("current_client_model");
        if (mCurrentClientMode == -1 || mCurrentClientMode != tmpClientMode) {
            //在初次调用或者接待模式改变时修改view
            List<SobotPlusEntity> tmpList = new ArrayList<>();
            if (bundle.getInt("current_client_model") == ZhiChiConstant.client_model_robot) {
                tmpList.addAll(robotList);
            } else {
                tmpList.addAll(operatorList);
                if (SobotUIConfig.pulsMenu.operatorMenus != null) {
                    tmpList.addAll(SobotUIConfig.pulsMenu.operatorMenus);
                }
            }
            if (SobotUIConfig.pulsMenu.menus != null) {
                tmpList.addAll(SobotUIConfig.pulsMenu.menus);
            }
            setAdapter(tmpList);
            if (countListener != null) {
                countListener.setRobotOperatorCount(tmpList.size());
                countListener.setOperatorCount(tmpList.size());
            }
        }

        mCurrentClientMode = tmpClientMode;
    }
}