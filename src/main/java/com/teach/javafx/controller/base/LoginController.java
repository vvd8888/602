package com.teach.javafx.controller.base;

import javafx.scene.control.TextInputDialog;
import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        onLoginButtonClick("22","123456");
    }



//    @FXML
//    protected void onTeacherLoginButtonClick() {
//        System.out.println("=== 测试教师登录 ===");
//
//        // 测试1: 尝试教师账号
//        System.out.println("测试1: 教师账号 200799013517/123456");
//        LoginRequest req1 = new LoginRequest("200799013517", "123456");
//        String result1 = HttpRequestUtil.login(req1);
//        System.out.println("结果: " + (result1 == null ? "成功" : "失败: " + result1));
//
//        // 测试2: 尝试学生账号
//        System.out.println("\n测试2: 学生账号 2022030001/123456");
//        LoginRequest req2 = new LoginRequest("2022030001", "123456");
//        String result2 = HttpRequestUtil.login(req2);
//        System.out.println("结果: " + (result2 == null ? "成功" : "失败: " + result2));
//
//        // 测试3: 尝试管理员账号
//        System.out.println("\n测试3: 管理员账号 admin/123456");
//        LoginRequest req3 = new LoginRequest("admin", "123456");
//        String result3 = HttpRequestUtil.login(req3);
//        System.out.println("结果: " + (result3 == null ? "成功" : "失败: " + result3));
//
//        // 根据结果决定
//        if (result2 == null) {
//            // 学生账号能登录，用学生账号进入教师界面
//            System.out.println("使用学生账号进入教师界面");
//            try {
//                loadTeacherView();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else if (result3 == null) {
//            // 管理员账号能登录
//            System.out.println("教师功能未实现，进入管理员界面");
//            onAdminLoginButtonClick();
//        } else {
//            // 使用Alert代替MessageDialog
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("登录失败");
//            alert.setHeaderText(null);
//            alert.setContentText("所有测试登录都失败");
//            alert.showAndWait();
//        }
//    }

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
            // 获取当前Stage并打开课程管理界面
            Stage stage = (Stage) courseBtn.getScene().getWindow();
            openCourseManagement(stage);
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

    /**
     * 从API获取课程列表
     */
    private ObservableList<Map<String, Object>> loadCoursesFromAPI() {
        ObservableList<Map<String, Object>> courseData = FXCollections.observableArrayList();

        try {
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/course/getCourseList", req);

            if (res != null && res.getCode() == 0) {
                List<Map<String, Object>> apiData = (List<Map<String, Object>>) res.getData();
                courseData.addAll(apiData);
                System.out.println("从API加载了 " + apiData.size() + " 门课程");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("加载失败");
                alert.setHeaderText(null);
                alert.setContentText("无法从服务器加载课程数据");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("加载失败");
            alert.setHeaderText(null);
            alert.setContentText("加载课程数据时发生错误: " + e.getMessage());
            alert.showAndWait();
        }

        return courseData;
    }

    /**
     * 保存课程到API
     */
    private boolean saveCourseToAPI(Map<String, Object> courseData) {
        try {
            DataRequest req = new DataRequest();
            // 添加所有字段到请求
            for (Map.Entry<String, Object> entry : courseData.entrySet()) {
                req.add(entry.getKey(), entry.getValue());
            }

            DataResponse res = HttpRequestUtil.request("/api/course/courseSave", req);
            if (res != null && res.getCode() == 0) {
                System.out.println("课程保存成功");
                return true;
            } else {
                System.out.println("课程保存失败: " + (res != null ? res.getMsg() : "无响应"));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从API删除课程
     */
    private boolean deleteCourseFromAPI(String courseId) {
        try {
            DataRequest req = new DataRequest();
            req.add("courseId", courseId);

            DataResponse res = HttpRequestUtil.request("/api/course/courseDelete", req);
            if (res != null && res.getCode() == 0) {
                System.out.println("课程删除成功");
                return true;
            } else {
                System.out.println("课程删除失败: " + (res != null ? res.getMsg() : "无响应"));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }/**
     * 课程管理界面
     */
    private void openCourseManagement(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 20;");

        // 标题区域
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10);

        Label title = new Label("课程管理 - 教师模式");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button backButton = new Button("返回");
        backButton.setOnAction(e -> {
            // 返回教师主界面
            createDefaultTeacherView();
        });

        titleBox.getChildren().addAll(backButton, title);
        BorderPane.setMargin(titleBox, new Insets(0, 0, 20, 0));
        root.setTop(titleBox);

        // 搜索和操作区域
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

        TextField searchField = new TextField();
        searchField.setPromptText("搜索课程名称、编号、教师...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("搜索");
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button addButton = new Button("+ 添加课程");
        addButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchBox.getChildren().addAll(searchField, searchButton, spacer, addButton);

        // 课程表格
        TableView<Map<String, Object>> courseTable = new TableView<>();
        // 设置表格可编辑
        courseTable.setEditable(true);

        // 从API加载课程数据
        ObservableList<Map<String, Object>> courseData = loadCoursesFromAPI();

        // 创建列
        TableColumn<Map<String, Object>, String> numCol = new TableColumn<>("课程编号");
        numCol.setCellValueFactory(new MapValueFactory("num"));
        numCol.setPrefWidth(100);

        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("课程名称");
        nameCol.setCellValueFactory(new MapValueFactory("name"));
        nameCol.setPrefWidth(150);

        TableColumn<Map<String, Object>, String> creditCol = new TableColumn<>("学分");
        creditCol.setCellValueFactory(new MapValueFactory("credit"));
        creditCol.setPrefWidth(80);

        // 前序课列
        TableColumn<Map<String, Object>, String> preCourseCol = new TableColumn<>("前序课");
        preCourseCol.setCellValueFactory(new MapValueFactory("preCourse"));
        preCourseCol.setPrefWidth(100);
        // 设置为可编辑
        preCourseCol.setCellFactory(TextFieldTableCell.forTableColumn());
        preCourseCol.setOnEditCommit(event -> {
            Map<String, Object> row = event.getRowValue();
            String newPreCourse = event.getNewValue();
            row.put("preCourse", newPreCourse);

            // 保存到API
            boolean success = saveCourseToAPI(row);
            if (success) {
                // 重新加载数据
                courseData.setAll(loadCoursesFromAPI());
                courseTable.setItems(courseData);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("保存成功");
                alert.setHeaderText(null);
                alert.setContentText("前序课已更新为: " + newPreCourse);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("保存失败");
                alert.setHeaderText(null);
                alert.setContentText("更新前序课时发生错误，请重试");
                alert.showAndWait();
            }
        });

        // 新增字段列
        TableColumn<Map<String, Object>, String> teacherCol = new TableColumn<>("授课教师");
        teacherCol.setCellValueFactory(new MapValueFactory("teacher"));
        teacherCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> timeCol = new TableColumn<>("上课时间");
        timeCol.setCellValueFactory(new MapValueFactory("time"));
        timeCol.setPrefWidth(120);

        TableColumn<Map<String, Object>, String> classroomCol = new TableColumn<>("上课地点");
        classroomCol.setCellValueFactory(new MapValueFactory("classroom"));
        classroomCol.setPrefWidth(120);

        courseTable.getColumns().addAll(numCol, nameCol, creditCol, preCourseCol,
                teacherCol, timeCol, classroomCol);

        courseTable.setItems(courseData);

        // 创建VBox容器
        VBox centerContainer = new VBox(10);
        centerContainer.getChildren().addAll(searchBox, courseTable);
        VBox.setVgrow(courseTable, Priority.ALWAYS);

        root.setCenter(centerContainer);

        // 底部按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 10;");

        Button editButton = new Button("编辑课程");
        editButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");

        Button editPreCourseButton = new Button("编辑前序课");
        editPreCourseButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button deleteButton = new Button("删除课程");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        Button refreshButton = new Button("刷新列表");
        refreshButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        // 搜索按钮事件
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            if (!keyword.isEmpty()) {
                ObservableList<Map<String, Object>> filteredData = FXCollections.observableArrayList();
                for (Map<String, Object> course : courseData) {
                    boolean match = false;

                    // 检查各个字段是否包含关键词
                    if (course.get("num") != null && course.get("num").toString().toLowerCase().contains(keyword)) {
                        match = true;
                    } else if (course.get("name") != null && course.get("name").toString().toLowerCase().contains(keyword)) {
                        match = true;
                    } else if (course.get("teacher") != null && course.get("teacher").toString().toLowerCase().contains(keyword)) {
                        match = true;
                    } else if (course.get("classroom") != null && course.get("classroom").toString().toLowerCase().contains(keyword)) {
                        match = true;
                    }

                    if (match) {
                        filteredData.add(course);
                    }
                }
                courseTable.setItems(filteredData);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("搜索结果");
                alert.setHeaderText(null);
                alert.setContentText("找到 " + filteredData.size() + " 门相关课程");
                alert.showAndWait();
            } else {
                courseTable.setItems(courseData);
            }
        });

        // 添加课程按钮事件
        addButton.setOnAction(e -> {
            // 创建添加课程对话框
            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("添加新课程");
            dialog.setHeaderText("请输入课程信息");

            // 设置按钮类型
            ButtonType addButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // 创建表单
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 10, 10, 10));

            TextField numField = new TextField();
            numField.setPromptText("例如：CS101");
            TextField nameField = new TextField();
            nameField.setPromptText("例如：计算机基础");
            TextField creditField = new TextField();
            creditField.setPromptText("例如：3");
            TextField preCourseField = new TextField();
            preCourseField.setPromptText("例如：无");
            TextField teacherField = new TextField();
            teacherField.setPromptText("例如：张老师");
            TextField timeField = new TextField();
            timeField.setPromptText("例如：周一 1-2节");
            TextField classroomField = new TextField();
            classroomField.setPromptText("例如：教学楼A101");

            grid.add(new Label("课程编号*:"), 0, 0);
            grid.add(numField, 1, 0);
            grid.add(new Label("课程名称*:"), 0, 1);
            grid.add(nameField, 1, 1);
            grid.add(new Label("学分*:"), 0, 2);
            grid.add(creditField, 1, 2);
            grid.add(new Label("前序课:"), 0, 3);
            grid.add(preCourseField, 1, 3);
            grid.add(new Label("授课教师:"), 0, 4);
            grid.add(teacherField, 1, 4);
            grid.add(new Label("上课时间:"), 0, 5);
            grid.add(timeField, 1, 5);
            grid.add(new Label("上课地点:"), 0, 6);
            grid.add(classroomField, 1, 6);

            dialog.getDialogPane().setContent(grid);

            // 将结果转换为Map
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    // 验证必填字段
                    if (numField.getText().trim().isEmpty() ||
                            nameField.getText().trim().isEmpty() ||
                            creditField.getText().trim().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("输入不完整");
                        alert.setHeaderText(null);
                        alert.setContentText("请填写带*号的必填字段");
                        alert.showAndWait();
                        return null;
                    }

                    try {
                        Map<String, Object> newCourse = new java.util.HashMap<>();
                        newCourse.put("num", numField.getText().trim());
                        newCourse.put("name", nameField.getText().trim());
                        newCourse.put("credit", creditField.getText().trim());
                        newCourse.put("teacher", teacherField.getText().trim());
                        newCourse.put("time", timeField.getText().trim());
                        newCourse.put("classroom", classroomField.getText().trim());

                        if (!preCourseField.getText().trim().isEmpty()) {
                            newCourse.put("preCourse", preCourseField.getText().trim());
                        }

                        return newCourse;
                    } catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("输入错误");
                        alert.setHeaderText(null);
                        alert.setContentText("请检查输入格式");
                        alert.showAndWait();
                        return null;
                    }
                }
                return null;
            });

            // 显示对话框并处理结果
            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(newCourse -> {
                // 保存到API
                boolean success = saveCourseToAPI(newCourse);
                if (success) {
                    // 重新加载数据
                    courseData.setAll(loadCoursesFromAPI());
                    courseTable.setItems(courseData);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("添加成功");
                    alert.setHeaderText(null);
                    alert.setContentText("课程 '" + newCourse.get("name") + "' 已成功添加");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("添加失败");
                    alert.setHeaderText(null);
                    alert.setContentText("添加课程时发生错误，请重试");
                    alert.showAndWait();
                }
            });
        });

        // 编辑课程按钮事件
        editButton.setOnAction(e -> {
            Map<String, Object> selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                // 创建编辑对话框
                Dialog<Map<String, Object>> dialog = new Dialog<>();
                dialog.setTitle("编辑课程");
                dialog.setHeaderText("修改课程信息");

                ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 10, 10, 10));

                // 使用只读的课程编号
                TextField numField = new TextField(selectedCourse.get("num").toString());
                numField.setEditable(false);
                numField.setStyle("-fx-background-color: #f5f5f5;");

                TextField nameField = new TextField(selectedCourse.get("name").toString());
                TextField creditField = new TextField(selectedCourse.get("credit").toString());
                TextField preCourseField = new TextField(
                        selectedCourse.get("preCourse") != null ? selectedCourse.get("preCourse").toString() : "");
                TextField teacherField = new TextField(
                        selectedCourse.get("teacher") != null ? selectedCourse.get("teacher").toString() : "");
                TextField timeField = new TextField(
                        selectedCourse.get("time") != null ? selectedCourse.get("time").toString() : "");
                TextField classroomField = new TextField(
                        selectedCourse.get("classroom") != null ? selectedCourse.get("classroom").toString() : "");

                grid.add(new Label("课程编号:"), 0, 0);
                grid.add(numField, 1, 0);
                grid.add(new Label("课程名称*:"), 0, 1);
                grid.add(nameField, 1, 1);
                grid.add(new Label("学分*:"), 0, 2);
                grid.add(creditField, 1, 2);
                grid.add(new Label("前序课:"), 0, 3);
                grid.add(preCourseField, 1, 3);
                grid.add(new Label("授课教师:"), 0, 4);
                grid.add(teacherField, 1, 4);
                grid.add(new Label("上课时间:"), 0, 5);
                grid.add(timeField, 1, 5);
                grid.add(new Label("上课地点:"), 0, 6);
                grid.add(classroomField, 1, 6);

                dialog.getDialogPane().setContent(grid);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        // 验证必填字段
                        if (nameField.getText().trim().isEmpty() ||
                                creditField.getText().trim().isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("输入不完整");
                            alert.setHeaderText(null);
                            alert.setContentText("请填写带*号的必填字段");
                            alert.showAndWait();
                            return null;
                        }

                        try {
                            // 创建更新后的课程数据
                            Map<String, Object> updatedCourse = new java.util.HashMap<>(selectedCourse);
                            updatedCourse.put("name", nameField.getText().trim());
                            updatedCourse.put("credit", creditField.getText().trim());
                            updatedCourse.put("teacher", teacherField.getText().trim());
                            updatedCourse.put("time", timeField.getText().trim());
                            updatedCourse.put("classroom", classroomField.getText().trim());

                            if (!preCourseField.getText().trim().isEmpty()) {
                                updatedCourse.put("preCourse", preCourseField.getText().trim());
                            } else {
                                updatedCourse.remove("preCourse");
                            }

                            return updatedCourse;
                        } catch (Exception ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("输入错误");
                            alert.setHeaderText(null);
                            alert.setContentText("请检查输入格式");
                            alert.showAndWait();
                            return null;
                        }
                    }
                    return null;
                });

                // 显示对话框并处理结果
                Optional<Map<String, Object>> result = dialog.showAndWait();
                result.ifPresent(updatedCourse -> {
                    // 保存到API
                    boolean success = saveCourseToAPI(updatedCourse);
                    if (success) {
                        // 重新加载数据
                        courseData.setAll(loadCoursesFromAPI());
                        courseTable.setItems(courseData);

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("保存成功");
                        alert.setHeaderText(null);
                        alert.setContentText("课程 '" + updatedCourse.get("name") + "' 已更新");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("保存失败");
                        alert.setHeaderText(null);
                        alert.setContentText("更新课程时发生错误，请重试");
                        alert.showAndWait();
                    }
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("提示");
                alert.setHeaderText(null);
                alert.setContentText("请先在表格中选择要编辑的课程");
                alert.showAndWait();
            }
        });

        // 编辑前序课按钮事件 - 独立的事件处理
        editPreCourseButton.setOnAction(e -> {
            Map<String, Object> selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                editPreCourse(selectedCourse, courseData, courseTable);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("提示");
                alert.setHeaderText(null);
                alert.setContentText("请先在表格中选择要编辑前序课的课程");
                alert.showAndWait();
            }
        });

        // 删除课程按钮事件
        deleteButton.setOnAction(e -> {
            Map<String, Object> selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("确认删除");
                confirm.setHeaderText("删除课程");
                confirm.setContentText("确定要永久删除课程: " + selectedCourse.get("name") +
                        " (" + selectedCourse.get("num") + ") 吗？");

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        String courseId = selectedCourse.get("courseId") != null ?
                                selectedCourse.get("courseId").toString() : null;

                        if (courseId != null) {
                            boolean success = deleteCourseFromAPI(courseId);
                            if (success) {
                                // 重新加载数据
                                courseData.setAll(loadCoursesFromAPI());
                                courseTable.setItems(courseData);

                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("删除成功");
                                alert.setHeaderText(null);
                                alert.setContentText("课程已删除");
                                alert.showAndWait();
                            } else {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("删除失败");
                                alert.setHeaderText(null);
                                alert.setContentText("删除课程时发生错误，请重试");
                                alert.showAndWait();
                            }
                        }
                    }
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("提示");
                alert.setHeaderText(null);
                alert.setContentText("请先在表格中选择要删除的课程");
                alert.showAndWait();
            }
        });

        // 刷新按钮事件
        refreshButton.setOnAction(e -> {
            // 重新从API加载数据
            courseData.setAll(loadCoursesFromAPI());
            courseTable.setItems(courseData);
            searchField.clear();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("刷新完成");
            alert.setHeaderText(null);
            alert.setContentText("已重新加载课程数据，共 " + courseData.size() + " 门课程");
            alert.showAndWait();
        });

        buttonBox.getChildren().addAll(editButton, editPreCourseButton, deleteButton, refreshButton);
        root.setBottom(buttonBox);

        // 创建场景并设置到Stage
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("课程管理 - 教师模式");
    }
    /**
     * 教师模式：编辑前序课
     */
    private void editPreCourse(Map<String, Object> selectedCourse,
                               ObservableList<Map<String, Object>> courseData,
                               TableView<Map<String, Object>> courseTable) {
        // 创建对话框编辑前序课
        TextInputDialog dialog = new TextInputDialog(
                selectedCourse.get("preCourse") != null ? selectedCourse.get("preCourse").toString() : ""
        );
        dialog.setTitle("编辑前序课");
        dialog.setHeaderText("请输入前序课的名称或编号");
        dialog.setContentText("前序课:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPreCourse -> {
            // 更新选中的课程
            Map<String, Object> updatedCourse = new java.util.HashMap<>(selectedCourse);
            updatedCourse.put("preCourse", newPreCourse);

            // 保存到API
            boolean success = saveCourseToAPI(updatedCourse);
            if (success) {
                // 重新加载数据
                courseData.setAll(loadCoursesFromAPI());
                courseTable.setItems(courseData);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("保存成功");
                alert.setHeaderText(null);
                alert.setContentText("前序课已更新为: " + newPreCourse);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("保存失败");
                alert.setHeaderText(null);
                alert.setContentText("更新前序课时发生错误，请重试");
                alert.showAndWait();
            }
        });
    }
}