package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private VBox vbox;

    @FXML
    public void initialize() {
        vbox.setStyle("-fx-background-image: url('shanda1.jpg'); -fx-background-repeat: no-repeat; -fx-background-size: cover;");
    }

    @FXML
    protected void onAdminLoginButtonClick() {
        onLoginButtonClick("admin", "123456");
    }

    @FXML
    protected void onStudentLoginButtonClick() {
        onLoginButtonClick("2022030001", "123456");
    }

    @FXML
    protected void onTeacherLoginButtonClick() {
        onLoginButtonClick("22", "123456");
    }

    protected void onLoginButtonClick(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String msg = HttpRequestUtil.login(loginRequest);

        if (msg != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("登录失败");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
            return;
        }

        // 保存当前登录用户名
        AppStore.setUsername(username);
        System.out.println("用户登录: " + username);

        // 恢复原来的逻辑：学生登录先选择同学，其他直接进入
        if (username.startsWith("2022")) {
            // 学生登录：先进入同学选择界面
            loadStudentSelectView();
        } else {
            // 教师和管理员：直接进入主框架
            loadMainFrame();
        }
    }

    /**
     * 加载同学选择界面
     */
    private void loadStudentSelectView() {
        try {
            // 使用绝对路径
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/com/teach/javafx/base/student-select.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            MainApplication.resetStage("选择同学", scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.WARNING, "提示",
                    "同学选择界面加载失败，将直接进入主界面");
            loadMainFrame();
        }
    }

    /**
     * 加载主框架
     */
    private void loadMainFrame() {
        try {
            // 使用绝对路径
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/com/teach/javafx/base/main-frame.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

            // 设置窗口标题
            String username = AppStore.getUsername();
            String title = "教学管理系统";
            if (username != null) {
                if (username.equals("admin")) {
                    title += " - 管理员";
                } else if (username.equals("22")) {
                    title += " - 教师";
                } else if (username.startsWith("2022")) {
                    title += " - 学生";
                }
            }

            MainApplication.resetStage(title, scene);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}