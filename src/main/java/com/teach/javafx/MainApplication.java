package com.teach.javafx;

import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * 应用的主程序 MainApplication
 */
public class MainApplication extends Application {
    private static Stage mainStage;
    private static double stageWidth = -1;
    private static double stageHeight = -1;
    private static boolean canClose = true;

    // 用户会话管理
    public static class UserSession {
        private static String username = "";
        private static String role = "";      // "teacher", "student", "admin" 或数字："2", "1", "0"
        private static String userId = "";
        private static String token = "";

        public static void clear() {
            username = "";
            role = "";
            userId = "";
            token = "";
        }

        public static String getUsername() { return username; }
        public static void setUsername(String username) { UserSession.username = username; }

        public static String getRole() { return role; }
        public static void setRole(String role) { UserSession.role = role; }

        public static String getUserId() { return userId; }
        public static void setUserId(String userId) { UserSession.userId = userId; }

        public static String getToken() { return token; }
        public static void setToken(String token) { UserSession.token = token; }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("登录");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            if(canClose) {
                HttpRequestUtil.close();
            } else {
                event.consume();
            }
        });
        mainStage = stage;
    }

    public static void resetStage(String name, Scene scene) {
        if(stageWidth > 0) {
            mainStage.setWidth(stageWidth);
            mainStage.setHeight(stageHeight);
            mainStage.setX(0);
            mainStage.setY(0);
        }
        mainStage.setTitle(name);
        mainStage.setScene(scene);
        mainStage.setMaximized(true);
        mainStage.show();
    }

    public static void loginStage(String name, Scene scene) {
        stageWidth = mainStage.getWidth();
        stageHeight = mainStage.getHeight();
        mainStage.setTitle(name);
        mainStage.setScene(scene);
        double x = (stageWidth-320)/2;
        double y = (stageHeight-240)/2;
        mainStage.setX(x);
        mainStage.setY(y);
        mainStage.setWidth(320);
        mainStage.setHeight(240);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setCanClose(boolean canClose) {
        MainApplication.canClose = canClose;
    }

    // ========== 新增：用户信息获取方法 ==========

    /**
     * 获取当前用户角色
     * @return 用户角色字符串（"teacher", "student", "admin" 或 "2", "1", "0"）
     */
    public static String getCurrentUserRole() {
        return UserSession.getRole();
    }

    /**
     * 获取当前用户名
     * @return 用户名
     */
    public static String getCurrentUserName() {
        return UserSession.getUsername();
    }

    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static String getCurrentUserId() {
        return UserSession.getUserId();
    }

    /**
     * 获取当前用户令牌
     * @return 令牌
     */
    public static String getCurrentUserToken() {
        return UserSession.getToken();
    }

    /**
     * 设置用户会话信息
     */
    public static void setUserSession(String username, String role, String userId, String token) {
        UserSession.setUsername(username);
        UserSession.setRole(role);
        UserSession.setUserId(userId);
        UserSession.setToken(token);

        System.out.println("✅ 用户会话已设置: " + username + ", 角色: " + role);
    }

    /**
     * 清除用户会话
     */
    public static void clearUserSession() {
        UserSession.clear();
        System.out.println("✅ 用户会话已清除");
    }

    /**
     * 检查用户是否已登录
     */
    public static boolean isLoggedIn() {
        return UserSession.getUsername() != null && !UserSession.getUsername().isEmpty();
    }

    /**
     * 检查用户是否为老师
     */
    public static boolean isTeacher() {
        String role = UserSession.getRole();
        return "teacher".equals(role) || "2".equals(role);
    }

    /**
     * 检查用户是否为学生
     */
    public static boolean isStudent() {
        String role = UserSession.getRole();
        return "student".equals(role) || "1".equals(role);
    }

    /**
     * 检查用户是否为管理员
     */
    public static boolean isAdmin() {
        String role = UserSession.getRole();
        return "admin".equals(role) || "0".equals(role);
    }
}