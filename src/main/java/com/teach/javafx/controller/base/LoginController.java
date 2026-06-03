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
    private PasswordField passwordField;
    @FXML
    private VBox vbox;
    @FXML
    private Button loginBtn;

    @FXML
    public void initialize() {
        vbox.setStyle("-fx-background-image: url('shanda1.jpg'); -fx-background-repeat: no-repeat; -fx-background-size: cover;");
        
        // 设置默认值为管理员账号
        usernameField.setText("admin");
        passwordField.setText("123456");
        
        System.out.println("=== 登录界面初始化完成 ===");
        System.out.println("服务器地址: " + com.teach.javafx.request.HttpRequestUtil.serverUrl);
        System.out.println("默认账号: admin (管理员)");
    }

    @FXML
    protected void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("输入错误");
            alert.setHeaderText(null);
            alert.setContentText("请输入用户名和密码！");
            alert.showAndWait();
            return;
        }
        
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
        System.out.println("✅ 用户登录成功: " + username);

        // 所有角色统一进入主框架
        loadMainFrame();
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
            
            // 加载CSS样式表 - 确保样式生效
            String cssPath = "/com/teach/javafx/css/modern-theme.css";
            java.net.URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("✅ CSS样式表已加载: " + cssUrl);
            } else {
                System.out.println("❌ CSS样式表未找到: " + cssPath);
            }

            // 设置窗口标题
            String username = AppStore.getUsername();
            String title = "教学管理系统";
            if (username != null) {
                if (username.equals("admin")) {
                    title += " - 管理员";
                } else if (username.equals("3")) {
                    title += " - 教师";
                } else if (username.startsWith("2022")) {
                    title += " - 学生";
                }
            }

            MainApplication.resetStage(title, scene);
            System.out.println("✅ 主框架已加载，用户: " + username);
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
