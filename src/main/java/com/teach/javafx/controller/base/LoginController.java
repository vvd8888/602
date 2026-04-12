package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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
        onLoginButtonClick("admin","123456");
    }

    @FXML
    protected void onStudentLoginButtonClick() {
        onLoginButtonClick("2022030001","123456");
    }

    @FXML
    protected void onTeacherLoginButtonClick() {
        System.out.println("=== 测试教师登录 ===");

        // 测试1: 尝试教师账号
        System.out.println("测试1: 教师账号 200799013517/123456");
        LoginRequest req1 = new LoginRequest("200799013517", "123456");
        String result1 = HttpRequestUtil.login(req1);
        System.out.println("结果: " + (result1 == null ? "成功" : "失败: " + result1));

        // 测试2: 尝试学生账号
        System.out.println("\n测试2: 学生账号 2022030001/123456");
        LoginRequest req2 = new LoginRequest("2022030001", "123456");
        String result2 = HttpRequestUtil.login(req2);
        System.out.println("结果: " + (result2 == null ? "成功" : "失败: " + result2));

        // 测试3: 尝试管理员账号
        System.out.println("\n测试3: 管理员账号 admin/123456");
        LoginRequest req3 = new LoginRequest("admin", "123456");
        String result3 = HttpRequestUtil.login(req3);
        System.out.println("结果: " + (result3 == null ? "成功" : "失败: " + result3));

        // 根据结果决定
        if (result2 == null) {
            // 学生账号能登录，用学生账号进入教师界面
            System.out.println("使用学生账号进入教师界面");
            try {
                loadTeacherView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (result3 == null) {
            // 管理员账号能登录
            System.out.println("教师功能未实现，进入管理员界面");
            onAdminLoginButtonClick();
        } else {
            // 使用Alert代替MessageDialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("登录失败");
            alert.setHeaderText(null);
            alert.setContentText("所有测试登录都失败");
            alert.showAndWait();
        }
    }

    protected void onLoginButtonClick(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username,password);
        String msg = HttpRequestUtil.login(loginRequest);
        if(msg != null) {
            // 使用Alert代替MessageDialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("登录失败");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
            return;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/main-frame.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), -1, -1);
            AppStore.setMainFrameController((MainFrameController) fxmlLoader.getController());
            MainApplication.resetStage("教学管理系统", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载教师界面
     */
    private void loadTeacherView() throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    MainApplication.class.getResource("base/teacher-main.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            MainApplication.resetStage("教学管理系统 - 教师", scene);
        } catch (Exception e) {
            // 如果教师界面不存在，创建默认界面
            System.out.println("教师界面不存在，创建默认教师界面");
            createDefaultTeacherView();
        }
    }

    /**
     * 创建默认教师界面
     */
    private void createDefaultTeacherView() {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // 标题
        Label title = new Label("教师管理系统");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // 欢迎语
        Label welcome = new Label("欢迎使用教师管理系统");
        welcome.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        // 功能区域
        VBox functionBox = new VBox(10);
        functionBox.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 10;");
        functionBox.setAlignment(Pos.CENTER);

        Label functionLabel = new Label("系统功能");
        functionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 功能按钮
        Button courseBtn = new Button("课程管理");
        courseBtn.setPrefWidth(200);
        courseBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("课程管理功能");
            alert.showAndWait();
        });

        Button studentBtn = new Button("学生管理");
        studentBtn.setPrefWidth(200);
        studentBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("学生管理功能");
            alert.showAndWait();
        });

        Button scoreBtn = new Button("成绩录入");
        scoreBtn.setPrefWidth(200);
        scoreBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("成绩录入功能");
            alert.showAndWait();
        });

        functionBox.getChildren().addAll(functionLabel, courseBtn, studentBtn, scoreBtn);

        // 底部按钮
        Button logoutButton = new Button("退出登录");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(
                        MainApplication.class.getResource("base/login-view.fxml")
                );
                Scene scene = new Scene(fxmlLoader.load(), 400, 300);
                stage.setScene(scene);
                stage.setTitle("登录");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(title, welcome, functionBox, logoutButton);

        Scene scene = new Scene(root, 900, 600);
        MainApplication.resetStage("教学管理系统 - 教师", scene);
    }
}