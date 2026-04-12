package com.teach.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;

// 移除 @Component 注解
// import org.springframework.stereotype.Component;

// @Component  // 移除这行
public class TeacherController {
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // 初始化方法
        if (welcomeLabel != null) {
            welcomeLabel.setText("教师管理系统");
        }
    }
}