package com.teach.javafx.controller.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private VBox vbox;

    // JSON处理器
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // 数据文件路径
    private static final String COURSE_DATA_FILE = "courses.json";

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
     * 保存课程数据到文件
     */
    private void saveCoursesToFile(ObservableList<Course> courseData) {
        try {
            List<Course> list = new ArrayList<>(courseData);
            String json = gson.toJson(list);

            // 写入文件
            Path path = Paths.get(COURSE_DATA_FILE);
            Files.write(path, json.getBytes(StandardCharsets.UTF_8));

            System.out.println("课程数据已保存到文件: " + COURSE_DATA_FILE);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("保存失败");
            alert.setHeaderText(null);
            alert.setContentText("保存课程数据失败: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * 从文件加载课程数据
     */
    private ObservableList<Course> loadCoursesFromFile() {
        ObservableList<Course> courseData = FXCollections.observableArrayList();

        try {
            Path path = Paths.get(COURSE_DATA_FILE);
            if (Files.exists(path)) {
                String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                Type listType = new TypeToken<List<Course>>(){}.getType();
                List<Course> list = gson.fromJson(json, listType);

                if (list != null) {
                    courseData.addAll(list);
                    System.out.println("从文件加载了 " + list.size() + " 门课程");
                }
            } else {
                // 如果文件不存在，添加默认数据
                System.out.println("课程数据文件不存在，使用默认数据");
                addDefaultCourses(courseData);
                saveCoursesToFile(courseData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("加载课程数据失败，使用默认数据");
            addDefaultCourses(courseData);
        }

        return courseData;
    }

    /**
     * 添加默认课程数据
     */
    private void addDefaultCourses(ObservableList<Course> courseData) {
        courseData.addAll(
                new Course("CS101", "计算机基础", "张老师", "周一 1-2节", "教学楼A101", 3),
                new Course("MA201", "高等数学", "李老师", "周二 3-4节", "教学楼B201", 4),
                new Course("EN301", "大学英语", "王老师", "周三 5-6节", "教学楼C301", 2),
                new Course("PHY401", "大学物理", "赵老师", "周四 7-8节", "教学楼D401", 3),
                new Course("CHE501", "大学化学", "孙老师", "周五 9-10节", "教学楼E501", 2)
        );
    }

    /**
     * 课程管理界面
     */
    private void openCourseManagement(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 20;");

        // 标题区域
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10);

        Label title = new Label("课程管理");
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
        searchField.setPromptText("搜索课程名称或教师...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("搜索");
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button addButton = new Button("+ 添加课程");
        addButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchBox.getChildren().addAll(searchField, searchButton, spacer, addButton);
        root.setCenter(searchBox);

        // 课程表格
        TableView<Course> courseTable = new TableView<>();

        // 创建列
        TableColumn<Course, String> idCol = new TableColumn<>("课程编号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        idCol.setPrefWidth(100);

        TableColumn<Course, String> nameCol = new TableColumn<>("课程名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(150);

        TableColumn<Course, String> teacherCol = new TableColumn<>("授课教师");
        teacherCol.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        teacherCol.setPrefWidth(120);

        TableColumn<Course, String> timeCol = new TableColumn<>("上课时间");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setPrefWidth(120);

        TableColumn<Course, String> classroomCol = new TableColumn<>("上课地点");
        classroomCol.setCellValueFactory(new PropertyValueFactory<>("classroom"));
        classroomCol.setPrefWidth(120);

        TableColumn<Course, Integer> creditCol = new TableColumn<>("学分");
        creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        creditCol.setPrefWidth(80);

        courseTable.getColumns().addAll(idCol, nameCol, teacherCol, timeCol, classroomCol, creditCol);

        // 从文件加载课程数据
        ObservableList<Course> courseData = loadCoursesFromFile();

        courseTable.setItems(courseData);

        // 创建VBox容器来放置搜索框和表格
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

        Button deleteButton = new Button("删除课程");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        Button refreshButton = new Button("刷新列表");
        refreshButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");

        // 搜索按钮事件
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            if (!keyword.isEmpty()) {
                ObservableList<Course> filteredData = FXCollections.observableArrayList();
                for (Course course : courseData) {
                    if (course.getCourseName().toLowerCase().contains(keyword) ||
                            course.getTeacher().toLowerCase().contains(keyword) ||
                            course.getCourseId().toLowerCase().contains(keyword) ||
                            course.getClassroom().toLowerCase().contains(keyword)) {
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
            Dialog<Course> dialog = new Dialog<>();
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

            TextField courseIdField = new TextField();
            courseIdField.setPromptText("例如：CS101");
            TextField courseNameField = new TextField();
            courseNameField.setPromptText("例如：计算机基础");
            TextField teacherField = new TextField();
            teacherField.setPromptText("例如：张老师");
            TextField timeField = new TextField();
            timeField.setPromptText("例如：周一 1-2节");
            TextField classroomField = new TextField();
            classroomField.setPromptText("例如：教学楼A101");
            TextField creditField = new TextField();
            creditField.setPromptText("例如：3");

            grid.add(new Label("课程编号:"), 0, 0);
            grid.add(courseIdField, 1, 0);
            grid.add(new Label("课程名称:"), 0, 1);
            grid.add(courseNameField, 1, 1);
            grid.add(new Label("授课教师:"), 0, 2);
            grid.add(teacherField, 1, 2);
            grid.add(new Label("上课时间:"), 0, 3);
            grid.add(timeField, 1, 3);
            grid.add(new Label("上课地点:"), 0, 4);
            grid.add(classroomField, 1, 4);
            grid.add(new Label("学分:"), 0, 5);
            grid.add(creditField, 1, 5);

            dialog.getDialogPane().setContent(grid);

            // 将结果转换为Course对象
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        // 验证必填字段
                        if (courseIdField.getText().trim().isEmpty() ||
                                courseNameField.getText().trim().isEmpty() ||
                                teacherField.getText().trim().isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("输入不完整");
                            alert.setHeaderText(null);
                            alert.setContentText("请填写课程编号、课程名称和授课教师");
                            alert.showAndWait();
                            return null;
                        }

                        int credit = 0;
                        if (!creditField.getText().trim().isEmpty()) {
                            credit = Integer.parseInt(creditField.getText());
                        }

                        return new Course(
                                courseIdField.getText().trim(),
                                courseNameField.getText().trim(),
                                teacherField.getText().trim(),
                                timeField.getText().trim(),
                                classroomField.getText().trim(),
                                credit
                        );
                    } catch (NumberFormatException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("输入错误");
                        alert.setHeaderText(null);
                        alert.setContentText("学分必须是数字");
                        alert.showAndWait();
                        return null;
                    }
                }
                return null;
            });

            // 显示对话框并处理结果
            dialog.showAndWait().ifPresent(newCourse -> {
                if (newCourse != null) {
                    // 检查是否已存在相同课程编号
                    boolean exists = courseData.stream()
                            .anyMatch(c -> c.getCourseId().equals(newCourse.getCourseId()));

                    if (exists) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("课程已存在");
                        alert.setHeaderText(null);
                        alert.setContentText("课程编号 " + newCourse.getCourseId() + " 已存在，请使用其他编号");
                        alert.showAndWait();
                    } else {
                        courseData.add(newCourse);
                        // 保存到文件
                        saveCoursesToFile(courseData);
                        courseTable.setItems(courseData);

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("添加成功");
                        alert.setHeaderText(null);
                        alert.setContentText("课程 '" + newCourse.getCourseName() + "' 已成功添加并保存");
                        alert.showAndWait();
                    }
                }
            });
        });

        // 编辑课程按钮事件
        editButton.setOnAction(e -> {
            Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                // 创建编辑对话框
                Dialog<Course> dialog = new Dialog<>();
                dialog.setTitle("编辑课程");
                dialog.setHeaderText("修改课程信息");

                ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 10, 10, 10));

                // 使用只读的课程编号
                TextField courseIdField = new TextField(selectedCourse.getCourseId());
                courseIdField.setEditable(false);
                courseIdField.setStyle("-fx-background-color: #f5f5f5;");

                TextField courseNameField = new TextField(selectedCourse.getCourseName());
                TextField teacherField = new TextField(selectedCourse.getTeacher());
                TextField timeField = new TextField(selectedCourse.getTime());
                TextField classroomField = new TextField(selectedCourse.getClassroom());
                TextField creditField = new TextField(String.valueOf(selectedCourse.getCredit()));

                grid.add(new Label("课程编号:"), 0, 0);
                grid.add(courseIdField, 1, 0);
                grid.add(new Label("课程名称:"), 0, 1);
                grid.add(courseNameField, 1, 1);
                grid.add(new Label("授课教师:"), 0, 2);
                grid.add(teacherField, 1, 2);
                grid.add(new Label("上课时间:"), 0, 3);
                grid.add(timeField, 1, 3);
                grid.add(new Label("上课地点:"), 0, 4);
                grid.add(classroomField, 1, 4);
                grid.add(new Label("学分:"), 0, 5);
                grid.add(creditField, 1, 5);

                dialog.getDialogPane().setContent(grid);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        try {
                            // 验证必填字段
                            if (courseNameField.getText().trim().isEmpty() ||
                                    teacherField.getText().trim().isEmpty()) {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("输入不完整");
                                alert.setHeaderText(null);
                                alert.setContentText("请填写课程名称和授课教师");
                                alert.showAndWait();
                                return null;
                            }

                            int credit = 0;
                            if (!creditField.getText().trim().isEmpty()) {
                                credit = Integer.parseInt(creditField.getText());
                            }

                            Course updatedCourse = new Course(
                                    selectedCourse.getCourseId(), // 保持原课程编号
                                    courseNameField.getText().trim(),
                                    teacherField.getText().trim(),
                                    timeField.getText().trim(),
                                    classroomField.getText().trim(),
                                    credit
                            );

                            return updatedCourse;
                        } catch (NumberFormatException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("输入错误");
                            alert.setHeaderText(null);
                            alert.setContentText("学分必须是数字");
                            alert.showAndWait();
                            return null;
                        }
                    }
                    return null;
                });

                dialog.showAndWait().ifPresent(updatedCourse -> {
                    if (updatedCourse != null) {
                        // 更新原课程
                        int index = courseData.indexOf(selectedCourse);
                        if (index != -1) {
                            courseData.set(index, updatedCourse);
                            // 保存到文件
                            saveCoursesToFile(courseData);
                            courseTable.setItems(courseData);

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("保存成功");
                            alert.setHeaderText(null);
                            alert.setContentText("课程 '" + updatedCourse.getCourseName() + "' 已更新并保存");
                            alert.showAndWait();
                        }
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

        // 删除课程按钮事件
        deleteButton.setOnAction(e -> {
            Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("确认删除");
                confirm.setHeaderText("删除课程");
                confirm.setContentText("确定要永久删除课程: " + selectedCourse.getCourseName() + " (" + selectedCourse.getCourseId() + ") 吗？");

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        courseData.remove(selectedCourse);
                        // 保存到文件
                        saveCoursesToFile(courseData);
                        courseTable.setItems(courseData);

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("删除成功");
                        alert.setHeaderText(null);
                        alert.setContentText("课程已删除并保存");
                        alert.showAndWait();
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
            // 重新从文件加载数据
            ObservableList<Course> refreshedData = loadCoursesFromFile();
            courseData.setAll(refreshedData);
            courseTable.setItems(courseData);
            searchField.clear();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("刷新完成");
            alert.setHeaderText(null);
            alert.setContentText("已从文件重新加载课程数据，共 " + courseData.size() + " 门课程");
            alert.showAndWait();
        });

        buttonBox.getChildren().addAll(editButton, deleteButton, refreshButton);
        root.setBottom(buttonBox);

        // 创建场景并设置到Stage
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("课程管理");
    }

    /**
     * 课程数据模型类
     */
    public static class Course {
        private String courseId;
        private String courseName;
        private String teacher;
        private String time;
        private String classroom;
        private int credit;

        public Course(String courseId, String courseName, String teacher, String time, String classroom, int credit) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.teacher = teacher;
            this.time = time;
            this.classroom = classroom;
            this.credit = credit;
        }

        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public String getTeacher() { return teacher; }
        public void setTeacher(String teacher) { this.teacher = teacher; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public String getClassroom() { return classroom; }
        public void setClassroom(String classroom) { this.classroom = classroom; }

        public int getCredit() { return credit; }
        public void setCredit(int credit) { this.credit = credit; }
    }
}