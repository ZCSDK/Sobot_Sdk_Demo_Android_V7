package com.sobot.chat.widget.switchkeyboardlib.panel;

import android.content.Context;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.SobotUIConfig;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SobotVisitorSchemeExtModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.switchkeyboardlib.model.SobotPlusEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 点击加号后 可分页的功能菜单控件
 */
public class FunctionMenuPageView extends LinearLayout {
    private RecyclerView recyclerView;
    private FunctionIndicatorView indicatorView;
    private FunctionPageAdapter adapter;
    private int rows = 2; // 默认行数
    private int columns = 3; // 默认列数
    private int realRows = 3; // 第一页真正行数
    private OnFunctionItemClickListener itemClickListener;
    private PagerSnapHelper snapHelper;

    //机器人模式下的菜单按钮集合
    private List<SobotPlusEntity> robotList = new ArrayList<>();
    //人工模式下模式下的菜单按钮集合
    private List<SobotPlusEntity> operatorList = new ArrayList<>();
    //真正现实的菜单按钮集合
    private List<SobotPlusEntity> showList = new ArrayList<>();

    //当前客户端模式
    protected int current_client_model = ZhiChiConstant.client_model_robot;

    public FunctionMenuPageView(Context context) {
        super(context);
        init();
    }

    public FunctionMenuPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FunctionMenuPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initData();
        rows = getContext().getResources().getInteger(R.integer.sobot_plus_menu_line);
        columns = getContext().getResources().getInteger(R.integer.sobot_plus_menu_row);
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
            rows = getContext().getResources().getInteger(R.integer.sobot_plus_menu_line_h);
            columns = getContext().getResources().getInteger(R.integer.sobot_plus_menu_row_h);
        }
        realRows = rows;
        // 初始化 RecyclerView
        recyclerView = new RecyclerView(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        // 完全禁用回弹效果 - 使用 View.OVER_SCROLL_NEVER
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        // 优化滑动体验
        recyclerView.setItemViewCacheSize(2); // 缓存视图数量
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
        // 添加分页滑动效果
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        // 设置适配器
        adapter = new FunctionPageAdapter(getContext(), rows, columns);
        adapter.setItems(robotList);
        recyclerView.setAdapter(adapter);

        // 添加 RecyclerView 到布局
        LayoutParams recyclerParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                getPanleHeight(adapter.getList())
        );
        addView(recyclerView, recyclerParams);

        // 设置页面切换监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateIndicator();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 滚动过程中也可以更新指示器
                updateIndicator();
            }
        });

        // 初始化指示器
        indicatorView = new FunctionIndicatorView(getContext());
        LayoutParams indicatorParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                ScreenUtils.dip2px(getContext(), 30)
        );
        indicatorView.setLayoutParams(indicatorParams);
        indicatorView.setGravity(Gravity.CENTER);
        indicatorView.setVisibility(GONE);
        addView(indicatorView);
        updateIndicator(); // 初始化时更新指示器
    }

    /**
     * 根据内容调整 RecyclerView 高度
     */
    private void adjustRecyclerViewHeight() {
        if (adapter == null || adapter.getItemCount() == 0) {
            return;
        }
        try {
            // 更新 RecyclerView 高度
            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = getPanleHeight(adapter.getList());
            recyclerView.setLayoutParams(params);
        } catch (Exception e) {
            // 如果测量失败，使用默认的 WRAP_CONTENT
            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = LayoutParams.WRAP_CONTENT;
            recyclerView.setLayoutParams(params);
        }
    }

    /**
     * 计算加号面板显示高度
     * <p>
     * 高度组成：
     * 1. 图标高度：每个图标56dp
     * 2. 图标间距：相邻图标之间10dp，边缘20dp
     * 3. 文本高度：根据实际文本内容计算
     * 4. 文本与图标间距：4dp
     *
     * @param meunList 菜单列表
     * @return 计算出的面板高度
     */
    private int getPanleHeight(List<SobotPlusEntity> meunList) {
        try {
            if (meunList == null || meunList.isEmpty()) {
                return 0;
            }

            // 默认面板高度
            int validPanelHeight = (int) getContext().getResources().getDimension(R.dimen.sobot_chat_plus_two_line_height);

            // 菜单文字总高度
            int meunNameTotalHeiht = 0;
            // 第一行菜单文字最大高度
            int firstMeunNameMaxHeiht = 0;
            // 第二行菜单文字最大高度
            int secondMeunNameMaxHeiht = 0;

            // 创建文本画笔用于测量
            TextPaint paint = new TextPaint();
            paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.sobot_text_font_12));
            paint.setAntiAlias(true); // 抗锯齿

            // 计算每个菜单项的可用宽度
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            // 外边距：左右各20dp + 列间距（列数-1）* 10dp
            int totalHorizontalSpacing = ScreenUtils.dip2px(getContext(), 20 * 2 + 10 * (columns - 1));
            int meunItemWidth = (screenWidth - totalHorizontalSpacing) / columns;

            // 菜单名字文字最小高度（默认40dp）
            int minHeight = ScreenUtils.dip2px(getContext(), 40);

            // 根据菜单数量计算行数和对应文本高度
            if (meunList.size() > columns) {
                // 超过一行，需要显示两行
                // 计算第一行文本最大高度
                int firstRowCount = Math.min(columns, meunList.size());
                for (int i = 0; i < firstRowCount; i++) {
                    String text = StringUtils.checkStringIsNull(meunList.get(i).name);
                    // 使用StaticLayout测量文本在指定宽度下的实际高度
                    StaticLayout layout = new StaticLayout(text, paint, meunItemWidth,
                            Layout.Alignment.ALIGN_NORMAL, 1.0f, 12f, false);
                    int height = layout.getHeight();
                    firstMeunNameMaxHeiht = Math.max(height, firstMeunNameMaxHeiht);
                }

                // 计算第二行文本最大高度
                for (int i = columns; i < meunList.size(); i++) {
                    String text = StringUtils.checkStringIsNull(meunList.get(i).name);
                    StaticLayout layout = new StaticLayout(text, paint, meunItemWidth,
                            Layout.Alignment.ALIGN_NORMAL, 1.0f, 12f, false);
                    int height = layout.getHeight();
                    secondMeunNameMaxHeiht = Math.max(height, secondMeunNameMaxHeiht);
                }
            } else {
                // 只有一行
                realRows = 1;
                for (int i = 0; i < meunList.size(); i++) {
                    String text = StringUtils.checkStringIsNull(meunList.get(i).name);
                    // 测量单行文本高度
                    StaticLayout layout = new StaticLayout(text, paint, meunItemWidth,
                            Layout.Alignment.ALIGN_NORMAL, 1.0f, 12f, false);
                    int height = layout.getHeight();
                    firstMeunNameMaxHeiht = Math.max(height, firstMeunNameMaxHeiht);
                }
            }

            // 确保最小高度
            firstMeunNameMaxHeiht = Math.max(minHeight, firstMeunNameMaxHeiht);
            secondMeunNameMaxHeiht = Math.max(minHeight, secondMeunNameMaxHeiht);

            // 根据实际行数计算最终面板高度
            if (realRows > 1) {
                meunNameTotalHeiht = firstMeunNameMaxHeiht + secondMeunNameMaxHeiht;
                // 多行高度 = 行间距 + 图标高度 + 文本高度
                validPanelHeight = ScreenUtils.dip2px(getContext(), 20 * realRows + (56 + 4) * realRows) + meunNameTotalHeiht;
            } else {
                // 单行高度 = 间距 + 图标高度 + 文本高度
                validPanelHeight = ScreenUtils.dip2px(getContext(), 20 + 56 + 4) + firstMeunNameMaxHeiht;
            }

            return validPanelHeight;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 初始化数据
     */
    public void initData() {
        Information information = (Information) SharedPreferencesUtil.getObject(getContext(), "sobot_last_current_info");
        ZhiChiInitModeBase initModeBase = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getContext(), ZhiChiConstant.sobot_last_current_initModel);
        int leaveMsg = SharedPreferencesUtil.getIntData(getContext(), ZhiChiConstant.sobot_msg_flag, ZhiChiConstant.sobot_msg_flag_open);
        //是否留言转离线消息,留言转离线消息模式下,人工模式加号中菜单不显示留言
        boolean msgToTicket = SharedPreferencesUtil.getBooleanData(getContext(),
                ZhiChiConstant.sobot_leave_msg_flag, false);
        //图片
        SobotPlusEntity picEntity = new SobotPlusEntity(R.drawable.sobot_chat_more_picture, getContext().getResources().getString(R.string.sobot_upload), ZhiChiConstant.ACTION_PIC);
        //视频
        SobotPlusEntity videoEntity = new SobotPlusEntity(R.drawable.sobot_chat_more_video, getContext().getResources().getString(R.string.sobot_upload_video), ZhiChiConstant.ACTION_VIDEO);
        //拍照
        SobotPlusEntity cameraEntity = new SobotPlusEntity(R.drawable.sobot_chat_more_camera, getContext().getResources().getString(R.string.sobot_attach_take_pic), ZhiChiConstant.ACTION_CAMERA);
        //文件
        SobotPlusEntity fileEntity = new SobotPlusEntity(R.drawable.sobot_chat_more_choose_file, getContext().getResources().getString(R.string.sobot_choose_file), ZhiChiConstant.ACTION_CHOOSE_FILE);
        //留言
        SobotPlusEntity leavemsgEntity = new SobotPlusEntity(R.drawable.sobot_chat_more_livemsg, getContext().getResources().getString(R.string.sobot_str_bottom_message), ZhiChiConstant.ACTION_LEAVEMSG);
        //评价
        SobotPlusEntity satisfactionEntity = new SobotPlusEntity(R.drawable.sobot_chat_more_satisfaction, getContext().getResources().getString(R.string.sobot_str_bottom_satisfaction), ZhiChiConstant.ACTION_SATISFACTION);

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
            boolean isAppNightMode = ThemeUtils.isAppNightMode(getContext());//是否夜间模式
            if (serviceOpen && null != appAppExtModelList) {
                for (int i = 0; i < appAppExtModelList.size(); i++) {
                    SobotVisitorSchemeExtModel extModel = appAppExtModelList.get(i);
                    String robotIconUrl = isAppNightMode ? extModel.getExtModelDarkPhoto() : extModel.getExtModelPhoto();
                    //1.留言 2 服务评价 3文件 4表情  5截图  6自定义跳转链接 7 图片 8 视频 9 拍摄
                    if (extModel.getExtModelType() == 7) {
                        picEntity = new SobotPlusEntity(robotIconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_PIC, i);
                        robotList.add(picEntity);
                    } else if (extModel.getExtModelType() == 8) {
                        videoEntity = new SobotPlusEntity(robotIconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_VIDEO, i);
                        robotList.add(videoEntity);
                    } else if (extModel.getExtModelType() == 9) {
                        cameraEntity = new SobotPlusEntity(robotIconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_CAMERA, i);
                        robotList.add(cameraEntity);
                    } else if (extModel.getExtModelType() == 3) {
                        fileEntity = new SobotPlusEntity(robotIconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_CHOOSE_FILE, i);
                        robotList.add(fileEntity);
                    } else if (extModel.getExtModelType() == 1) {
                        leavemsgEntity = new SobotPlusEntity(robotIconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_LEAVEMSG, i);
                        if (leaveMsg == ZhiChiConstant.sobot_msg_flag_open) {
                            robotList.add(leavemsgEntity);
                        }
                    } else if (extModel.getExtModelType() == 2) {
                        satisfactionEntity = new SobotPlusEntity(robotIconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_SATISFACTION, i);
                        robotList.add(satisfactionEntity);
                    } else {
                        SobotPlusEntity webEntity = new SobotPlusEntity(robotIconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_OPEN_WEB, i, extModel.getExtModelLink());
                        robotList.add(webEntity);
                    }
                }
            }
            if (serviceOpen && null != appExtModelManList) {
                for (int i = 0; i < appExtModelManList.size(); i++) {
                    SobotVisitorSchemeExtModel extModel = appExtModelManList.get(i);
                    String iconUrl = isAppNightMode ? extModel.getExtModelDarkPhoto() : extModel.getExtModelPhoto();
                    //1.留言 2 服务评价 3文件 4表情  5截图  6自定义跳转链接 7 图片 8 视频 9 拍摄
                    if (extModel.getExtModelType() == 7) {
                        picEntity = new SobotPlusEntity(iconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_PIC, i);
                        operatorList.add(picEntity);
                    } else if (extModel.getExtModelType() == 8) {
                        videoEntity = new SobotPlusEntity(iconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_VIDEO, i);
                        operatorList.add(videoEntity);
                    } else if (extModel.getExtModelType() == 9) {
                        cameraEntity = new SobotPlusEntity(iconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_CAMERA, i);
                        operatorList.add(cameraEntity);
                    } else if (extModel.getExtModelType() == 3) {
                        fileEntity = new SobotPlusEntity(iconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_CHOOSE_FILE, i);
                        operatorList.add(fileEntity);
                    } else if (extModel.getExtModelType() == 1) {
                        leavemsgEntity = new SobotPlusEntity(iconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_LEAVEMSG, i);
                        if (leaveMsg == ZhiChiConstant.sobot_msg_flag_open && !msgToTicket) {
                            operatorList.add(leavemsgEntity);
                        }
                    } else if (extModel.getExtModelType() == 2) {
                        satisfactionEntity = new SobotPlusEntity(iconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_SATISFACTION, i);
                        operatorList.add(satisfactionEntity);
                    } else {
                        SobotPlusEntity webEntity = new SobotPlusEntity(iconUrl, extModel.getExtModelName(), ZhiChiConstant.ACTION_OPEN_WEB, i, extModel.getExtModelLink());
                        operatorList.add(webEntity);
                    }
                }
            }

            //排序
            if (robotList != null) {
                Collections.sort(robotList, new Comparator<SobotPlusEntity>() {
                    @Override
                    public int compare(SobotPlusEntity sobotPlusEntity, SobotPlusEntity t1) {
                        return sobotPlusEntity.index - t1.index;
                    }
                });
                if (SobotUIConfig.pulsMenu.robotMenus != null) {
                    //添加机器人模式下的自定义菜单
                    robotList.addAll(SobotUIConfig.pulsMenu.robotMenus);
                }
            }
            if (operatorList != null) {
                Collections.sort(operatorList, new Comparator<SobotPlusEntity>() {
                    @Override
                    public int compare(SobotPlusEntity sobotPlusEntity, SobotPlusEntity t1) {
                        return sobotPlusEntity.index - t1.index;
                    }
                });
                if (SobotUIConfig.pulsMenu.operatorMenus != null) {
                    //添加人工模式下的自定义菜单
                    operatorList.addAll(SobotUIConfig.pulsMenu.operatorMenus);
                }
            }
        }
    }

    /**
     * 设置网格行列数
     *
     * @param rows    行数
     * @param columns 列数
     */
    public void setGridSize(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        if (adapter != null) {
            adapter.setGridSize(rows, columns);
            adjustRecyclerViewHeight(); // 重新调整高度
        }
    }

    /**
     * 根据接待模式刷新功能菜单布局
     *
     * @param current_client_model 当前接待模式 人工或者机器人
     */
    public void updateFunctionViews(ZhiChiInitModeBase initMode, int current_client_model) {
        initData();
        this.current_client_model = current_client_model;
        if (showList == null) {
            showList = new ArrayList<>();
        }
        if (operatorList == null) {
            operatorList = new ArrayList<>();
        }
        if (robotList == null) {
            robotList = new ArrayList<>();
        }
        showList.clear();
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            //人工模式
            showList.addAll(operatorList);
        } else {
            showList.addAll(robotList);
        }
        if (initMode != null && initMode.getAssignmentMode() == 1) {
            List<SobotPlusEntity> tempShowList = new ArrayList<>();
            //异步接待 待分配池后隐藏留言按钮
            for (int i = 0; i < showList.size(); i++) {
                if (!ZhiChiConstant.ACTION_LEAVEMSG.equals(showList.get(i).action)) {
                    tempShowList.add(showList.get(i));
                }
            }
            showList.clear();
            showList.addAll(tempShowList);
        }
        if (adapter != null) {
            adapter.setItems(showList);
            updateIndicator();
            adjustRecyclerViewHeight(); // 数据更新后重新调整高度
        }
    }

    /**
     * 设置功能项点击监听器
     *
     * @param listener 点击监听器
     */
    public void setOnFunctionItemClickListener(OnFunctionItemClickListener listener) {
        this.itemClickListener = listener;
        if (adapter != null) {
            adapter.setOnItemClickListener(listener);
        }
    }

    /**
     * 更新指示器状态
     */
    private void updateIndicator() {
        if (adapter == null || indicatorView == null) return;

        int totalPages = adapter.getItemCount();
        int currentPage = getCurrentPage();

        if (totalPages > 1) {
            indicatorView.setIndicatorCount(totalPages);
            indicatorView.setCurrentIndex(currentPage);
            indicatorView.setVisibility(VISIBLE);
        } else {
            indicatorView.setVisibility(GONE);
        }
    }

    /**
     * 获取当前页面索引
     */
    private int getCurrentPage() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null) return 0;

        // 使用 SnapHelper 获取当前居中的视图
        View centerView = snapHelper.findSnapView(layoutManager);
        if (centerView != null) {
            return layoutManager.getPosition(centerView);
        }

        // 备用方案：获取第一个可见项
        return layoutManager.findFirstVisibleItemPosition();
    }

    /**
     * 滚动到指定页面
     *
     * @param pageIndex 页面索引，从0开始
     */
    public void scrollToPage(int pageIndex) {
        if (recyclerView != null && adapter != null && recyclerView.getLayoutManager() != null && adapter != null) {
            if (pageIndex >= 0 && pageIndex < adapter.getItemCount()) {
                recyclerView.smoothScrollToPosition(pageIndex);
                // 或者使用下面的方法实现瞬间滚动而不是平滑滚动
                // ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(pageIndex, 0);
            }
        }
    }

    public List<SobotPlusEntity> getRobotList() {
        return robotList;
    }


    public List<SobotPlusEntity> getOperatorList() {
        return operatorList;
    }


    /**
     * 功能项点击监听接口
     */
    public interface OnFunctionItemClickListener {
        void onFunctionItemClick(SobotPlusEntity entity, int position);
    }
}
