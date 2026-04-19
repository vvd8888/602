package com.teach.javafx;

import com.teach.javafx.controller.base.MainFrameController;
import com.teach.javafx.request.JwtResponse;

/**
 * 前端应用全程数据类
 * JwtResponse jwt 客户登录信息
 */
public class AppStore {
    private static JwtResponse jwt;
    private static MainFrameController mainFrameController;

    // 添加新的字段
    private static String username;                // 当前登录用户名
    private static String selectedStudentNum;      // 选中的学生学号
    private static String selectedStudentName;     // 选中的学生姓名

    private AppStore(){
    }

    public static JwtResponse getJwt() {
        return jwt;
    }

    public static void setJwt(JwtResponse jwt) {
        AppStore.jwt = jwt;
    }

    public static MainFrameController getMainFrameController() {
        return mainFrameController;
    }

    public static void setMainFrameController(MainFrameController mainFrameController) {
        AppStore.mainFrameController = mainFrameController;
    }

    // 添加新的getter和setter方法

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        AppStore.username = username;
    }

    public static String getSelectedStudentNum() {
        return selectedStudentNum;
    }

    public static void setSelectedStudentNum(String selectedStudentNum) {
        AppStore.selectedStudentNum = selectedStudentNum;
    }

    public static String getSelectedStudentName() {
        return selectedStudentName;
    }

    public static void setSelectedStudentName(String selectedStudentName) {
        AppStore.selectedStudentName = selectedStudentName;
    }

    /**
     * 清除用户相关数据（用于退出登录）
     */
    public static void clearUserData() {
        username = null;
        selectedStudentNum = null;
        selectedStudentName = null;
    }
}