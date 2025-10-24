package com.sobot.chat.widget.switchkeyboardlib.model;

import java.io.Serializable;

public class SobotPlusEntity implements Serializable {
    public int iconResId;
    public String iconUrl;
    public String name;
    public String action;
    public String extModelLink;

    public int index = 0;

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
     * @param iconResId 菜单图标
     * @param name      菜单名称
     * @param action    菜单动作 当点击按钮时会将对应action返回给callback
     *                  以此作为依据，判断用户点击了哪个按钮
     */
    public SobotPlusEntity(int iconResId, String name, String action, int index) {
        this.iconResId = iconResId;
        this.name = name;
        this.action = action;
        this.index = index;
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

    public SobotPlusEntity(String iconUrl, String name, String action, int index, String link) {
        this.iconUrl = iconUrl;
        this.name = name;
        this.action = action;
        this.index = index;
        this.extModelLink = link;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
