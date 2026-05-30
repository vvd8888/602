package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import java.util.*;

public class TeacherOpenCourseController {

    // 统计标签
    @FXML
    private Label totalCoursesLabel;
    @FXML
    private Label openCoursesLabel;
    @FXML
    private Label closedCoursesLabel;

    // 搜索和筛选
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterCombo;

    // 课程表格
    @FXML
    private TableView<Map<String, Object>> coursesTable;
    @FXML
    private TableColumn<Map<String, Object>, String> numColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> nameColumn;
    @FXML
    private TableColumn<Map<String, Object>, Number> creditColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> teacherColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> timeColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> classroomColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> statusColumn;
    @FXML
    private TableColumn<Map<String, Object>, Number> studentCountColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> operateColumn;

    private ObservableList<Map<String, Object>> myCoursesList = FXCollections.observableArrayList();
    private List<Map<String, Object>> allCourses = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("✅ TeacherOpenCourseController 初始化");

        setupSearchComponents();
        setupMyCoursesTable();
        loadData();

        // 检查当前用户角色，如果是管理员则隐藏打分按钮
        checkRoleAndHideGradeButton();
    }

    private void setupSearchComponents() {
        statusFilterCombo.getItems().addAll("全部", "开放", "关闭", "待审核");
        statusFilterCombo.setValue("全部");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCourses();
        });

        statusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterCourses();
        });
    }

    private void setupMyCoursesTable() {
        coursesTable.setItems(myCoursesList);

        numColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            String value = rowData.get("num") != null ? rowData.get("num").toString() : "";
            return new SimpleStringProperty(value);
        });

        nameColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            String value = rowData.get("name") != null ? rowData.get("name").toString() : "";
            return new SimpleStringProperty(value);
        });

        creditColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            Object credit = rowData.get("credit");
            int intValue = 0;
            if (credit instanceof Number) {
                intValue = ((Number) credit).intValue();
            } else if (credit instanceof String) {
                try {
                    intValue = Integer.parseInt((String) credit);
                } catch (NumberFormatException e) {
                    intValue = 0;
                }
            }
            return new SimpleIntegerProperty(intValue);
        });

        teacherColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            String value = rowData.get("teacher") != null ? rowData.get("teacher").toString() : "";
            return new SimpleStringProperty(value);
        });

        timeColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            String value = rowData.get("time") != null ? rowData.get("time").toString() : "";
            return new SimpleStringProperty(value);
        });

        classroomColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            String value = rowData.get("classroom") != null ? rowData.get("classroom").toString() : "";
            return new SimpleStringProperty(value);
        });

        statusColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            String value = rowData.get("status") != null ? rowData.get("status").toString() : "";
            return new SimpleStringProperty(value);
        });

        studentCountColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            Object count = rowData.get("studentCount");
            int intValue = 0;
            if (count instanceof Number) {
                intValue = ((Number) count).intValue();
            } else if (count instanceof String) {
                try {
                    intValue = Integer.parseInt((String) count);
                } catch (NumberFormatException e) {
                    intValue = 0;
                }
            }
            return new SimpleIntegerProperty(intValue);
        });

        statusColumn.setCellFactory(new Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>() {
            @Override
            public TableCell<Map<String, Object>, String> call(TableColumn<Map<String, Object>, String> param) {
                return new TableCell<Map<String, Object>, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            switch (item) {
                                case "PENDING":
                                    setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                                    setText("待审核");
                                    break;
                                case "APPROVED":
                                case "OPEN":
                                    setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                                    setText("开放");
                                    break;
                                case "REJECTED":
                                case "CLOSED":
                                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                                    setText("关闭");
                                    break;
                                default:
                                    setText(item);
                                    setStyle("");
                            }
                        }
                    }
                };
            }
        });

        operateColumn.setCellFactory(new Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>() {
            @Override
            public TableCell<Map<String, Object>, String> call(final TableColumn<Map<String, Object>, String> param) {
                return new TableCell<Map<String, Object>, String>() {
                    private final HBox hbox = new HBox(5);
                    private final Button editButton = new Button("编辑");
                    private final Button deleteButton = new Button("删除");
                    private final Button toggleButton = new Button("");
                    private final Button gradeButton = new Button("打分");

                    {
                        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
                        toggleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
                        gradeButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 12px;");

                        editButton.setOnAction(event -> {
                            Map<String, Object> data = getTableView().getItems().get(getIndex());
                            handleEdit(data);
                        });

                        deleteButton.setOnAction(event -> {
                            Map<String, Object> data = getTableView().getItems().get(getIndex());
                            handleDelete(data);
                        });

                        toggleButton.setOnAction(event -> {
                            Map<String, Object> data = getTableView().getItems().get(getIndex());
                            handleToggleStatus(data);
                        });

                        gradeButton.setOnAction(event -> {
                            Map<String, Object> data = getTableView().getItems().get(getIndex());
                            handleGradeStudents(data);
                        });

                        hbox.setAlignment(Pos.CENTER);
                        hbox.getChildren().addAll(editButton, deleteButton, toggleButton, gradeButton);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                            String status = (String) rowData.get("status");

                            if ("OPEN".equals(status) || "APPROVED".equals(status)) {
                                toggleButton.setText("关闭");
                                toggleButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
                            } else {
                                toggleButton.setText("开放");
                                toggleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
                            }

                            // 如果是管理员，隐藏打分按钮
                            String role = com.teach.javafx.AppStore.getJwt() != null
                                ? com.teach.javafx.AppStore.getJwt().getRole() : "";
                            if ("ROLE_ADMIN".equals(role)) {
                                hbox.getChildren().remove(gradeButton);
                            } else {
                                // 教师角色：根据是否有学生选课来控制打分按钮
                                Object studentCountObj = rowData.get("studentCount");
                                int studentCount = 0;
                                if (studentCountObj instanceof Number) {
                                    studentCount = ((Number) studentCountObj).intValue();
                                }

                                if (studentCount > 0) {
                                    gradeButton.setDisable(false);
                                    gradeButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 12px;");
                                } else {
                                    gradeButton.setDisable(true);
                                    gradeButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666; -fx-font-size: 12px;");
                                }
                            }

                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }

    private void loadData() {
        loadTeacherCourses();
        updateStatistics();
    }

    private void loadTeacherCourses() {
        try {
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/course/getCourseList", req);

            if (res != null && res.getCode() == 0) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) res.getData();
                allCourses.clear();
                allCourses.addAll(data);

                // 加载每门课程的学生数量
                updateCourseStudentCounts();

                myCoursesList.clear();
                myCoursesList.addAll(allCourses);

                updateStatistics();
                System.out.println("✅ 成功加载 " + myCoursesList.size() + " 门课程");
            } else {
                showErrorAlert("加载课程失败", res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("加载课程失败", "网络错误：" + e.getMessage());
        }
    }

    @FXML
    private void onSearchClick() {
        filterCourses();
    }

    private void filterCourses() {
        String keyword = searchField.getText().toLowerCase();
        String statusFilter = statusFilterCombo.getValue();

        ObservableList<Map<String, Object>> filteredList = FXCollections.observableArrayList();

        for (Map<String, Object> course : allCourses) {
            boolean matchKeyword = false;
            boolean matchStatus = true;

            if (keyword == null || keyword.isEmpty()) {
                matchKeyword = true;
            } else {
                String num = course.get("num") != null ? course.get("num").toString().toLowerCase() : "";
                String name = course.get("name") != null ? course.get("name").toString().toLowerCase() : "";
                String teacher = course.get("teacher") != null ? course.get("teacher").toString().toLowerCase() : "";
                String classroom = course.get("classroom") != null ? course.get("classroom").toString().toLowerCase() : "";

                if (num.contains(keyword) || name.contains(keyword) || teacher.contains(keyword) || classroom.contains(keyword)) {
                    matchKeyword = true;
                }
            }

            if (statusFilter != null && !"全部".equals(statusFilter)) {
                String status = course.get("status") != null ? course.get("status").toString() : "";
                String chineseStatus = "";

                switch (status) {
                    case "PENDING": chineseStatus = "待审核"; break;
                    case "APPROVED":
                    case "OPEN": chineseStatus = "开放"; break;
                    case "REJECTED":
                    case "CLOSED": chineseStatus = "关闭"; break;
                    default: chineseStatus = status;
                }

                matchStatus = statusFilter.equals(chineseStatus);
            }

            if (matchKeyword && matchStatus) {
                filteredList.add(course);
            }
        }

        myCoursesList.clear();
        myCoursesList.addAll(filteredList);
        updateStatistics();

        System.out.println("🔍 筛选结果: " + myCoursesList.size() + " 门课程");
    }

    @FXML
    private void onAddNewCourseClick() {
        openCourseDialog(null);
    }

    @FXML
    private void onRefreshClick() {
        loadTeacherCourses();
    }

    private void handleEdit(Map<String, Object> course) {
        openCourseDialog(course);
    }

    private void handleDelete(Map<String, Object> course) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("确定要删除课程: " + course.get("name") + "?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                DataRequest req = new DataRequest();
                req.add("courseId", course.get("courseId"));
                DataResponse res = HttpRequestUtil.request("/api/course/courseDelete", req);
                if (res != null && res.getCode() == 0) {
                    loadTeacherCourses();
                } else {
                    showErrorAlert("删除失败", res);
                }
            }
        });
    }

    private void handleToggleStatus(Map<String, Object> course) {
        System.out.println("🚀 开始处理课程状态切换");
        System.out.println("课程信息: " + course);

        String currentStatus = (String) course.get("status");
        System.out.println("当前状态: " + currentStatus);

        String newStatus = "OPEN".equals(currentStatus) || "APPROVED".equals(currentStatus) ? "CLOSED" : "OPEN";
        System.out.println("新状态: " + newStatus);

        Object courseId = course.get("courseId");
        System.out.println("课程ID: " + courseId);

        if (courseId == null) {
            System.out.println("❌ 错误: courseId 为 null");
            showErrorAlert("操作失败", "无法获取课程ID");
            return;
        }

        DataRequest req = new DataRequest();
        req.add("courseId", courseId);
        req.add("status", newStatus);

        System.out.println("📤 发送请求到: /api/course/updateCourseStatus");
        System.out.println("请求参数: courseId=" + courseId + ", status=" + newStatus);

        try {
            DataResponse res = HttpRequestUtil.request("/api/course/updateCourseStatus", req);

            if (res != null) {
                System.out.println("📥 收到响应");
                System.out.println("响应代码: " + res.getCode());
                System.out.println("响应消息: " + res.getMsg());

                if (res.getCode() == 0) {
                    System.out.println("✅ 状态更新成功，重新加载课程列表");
                    showSuccessAlert("操作成功", "课程状态已更新");
                    loadTeacherCourses();
                } else {
                    System.out.println("❌ 状态更新失败: " + res.getMsg());
                    showErrorAlert("更新状态失败", res);

                    // 如果接口不存在，尝试其他可能的接口
                    if (res.getCode() == 404 || res.getCode() == 500) {
                        System.out.println("⚠️ 尝试备用接口...");
                        tryAlternativeApi(courseId, newStatus);
                    }
                }
            } else {
                System.out.println("❌ 响应为 null");
                showErrorAlert("网络错误", "服务器无响应");
            }
        } catch (Exception e) {
            System.out.println("❌ 请求异常: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("请求异常", e.getMessage());
        }
    }

    /**
     * 尝试备用接口
     */
    private void tryAlternativeApi(Object courseId, String newStatus) {
        System.out.println("尝试备用接口...");

        // 可能的后端接口列表
        String[] possibleApis = {
                "/api/course/courseUpdateStatus",
                "/api/course/updateStatus",
                "/api/course/toggleStatus",
                "/api/course/changeStatus"
        };

        for (String api : possibleApis) {
            System.out.println("尝试接口: " + api);

            DataRequest req = new DataRequest();
            req.add("courseId", courseId);
            req.add("status", newStatus);

            try {
                DataResponse res = HttpRequestUtil.request(api, req);
                if (res != null && res.getCode() == 0) {
                    System.out.println("✅ 备用接口成功: " + api);
                    showSuccessAlert("操作成功", "课程状态已更新");
                    loadTeacherCourses();
                    return;
                }
            } catch (Exception e) {
                // 继续尝试下一个接口
            }
        }

        System.out.println("❌ 所有接口都失败");
        showErrorAlert("接口不存在", "请检查后端是否实现了更新课程状态的接口");
    }

    private void openCourseDialog(Map<String, Object> courseData) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(courseData == null ? "开设新课程" : "编辑课程");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField numField = new TextField();
        numField.setPromptText("课程编号");
        TextField nameField = new TextField();
        nameField.setPromptText("课程名称");
        TextField creditField = new TextField();
        creditField.setPromptText("学分");
        TextField teacherField = new TextField();
        teacherField.setPromptText("授课教师");
        TextField timeField = new TextField();
        timeField.setPromptText("上课时间");
        TextField classroomField = new TextField();
        classroomField.setPromptText("上课地点");

        if (courseData != null) {
            numField.setText((String) courseData.get("num"));
            nameField.setText((String) courseData.get("name"));
            creditField.setText(courseData.get("credit").toString());
            teacherField.setText((String) courseData.get("teacher"));
            timeField.setText((String) courseData.get("time"));
            classroomField.setText((String) courseData.get("classroom"));
        }

        grid.add(new Label("课程编号:"), 0, 0);
        grid.add(numField, 1, 0);
        grid.add(new Label("课程名称:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("学分:"), 0, 2);
        grid.add(creditField, 1, 2);
        grid.add(new Label("授课教师:"), 0, 3);
        grid.add(teacherField, 1, 3);
        grid.add(new Label("上课时间:"), 0, 4);
        grid.add(timeField, 1, 4);
        grid.add(new Label("上课地点:"), 0, 5);
        grid.add(classroomField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Map<String, Object> result = new HashMap<>();
                result.put("num", numField.getText());
                result.put("name", nameField.getText());
                result.put("credit", creditField.getText());
                result.put("teacher", teacherField.getText());
                result.put("time", timeField.getText());
                result.put("classroom", classroomField.getText());

                if (courseData != null) {
                    result.put("courseId", courseData.get("courseId"));
                }

                return result;
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();

        result.ifPresent(formData -> {
            saveCourse(formData);
        });
    }

    private void saveCourse(Map<String, Object> courseData) {
        DataRequest req = new DataRequest();

        for (Map.Entry<String, Object> entry : courseData.entrySet()) {
            req.add(entry.getKey(), entry.getValue());
        }

        String apiUrl = courseData.containsKey("courseId") ? "/api/course/courseSave" : "/api/course/teacher/openCourse";
        DataResponse res = HttpRequestUtil.request(apiUrl, req);

        if (res != null && res.getCode() == 0) {
            showSuccessAlert("保存成功", "课程信息已保存！");
            loadTeacherCourses();
        } else {
            showErrorAlert("保存失败", res);
        }
    }

    private void updateStatistics() {
        int total = myCoursesList.size();
        int open = 0;
        int closed = 0;

        for (Map<String, Object> course : myCoursesList) {
            String status = (String) course.get("status");
            if ("OPEN".equals(status) || "APPROVED".equals(status)) {
                open++;
            } else if ("CLOSED".equals(status) || "REJECTED".equals(status)) {
                closed++;
            }
        }

        if (totalCoursesLabel != null) {
            totalCoursesLabel.setText("总课程数: " + total);
        }
        if (openCoursesLabel != null) {
            openCoursesLabel.setText("开放课程: " + open);
        }
        if (closedCoursesLabel != null) {
            closedCoursesLabel.setText("关闭课程: " + closed);
        }

        // 打印调试信息
        System.out.println(" 统计信息 - 总课程: " + total + ", 开放: " + open + ", 关闭: " + closed);
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showErrorAlert(String title, DataResponse res) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(res != null ? res.getMsg() : "未知错误");
        alert.show();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    /**
     * 处理学生打分
     */
    private void handleGradeStudents(Map<String, Object> course) {
        System.out.println("📝 打开课程打分对话框: " + course.get("name"));

        // 获取该课程的选课学生列表
        List<Map<String, Object>> studentSelections = getCourseStudents(course);

        if (studentSelections.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("该课程暂无选课学生");
            alert.show();
            return;
        }

        // 创建打分对话框
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("课程打分 - " + course.get("name"));
        dialog.setHeaderText("课程: " + course.get("name") + " (编号: " + course.get("num") + ")");

        ButtonType saveButtonType = new ButtonType("保存成绩", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 创建表格显示学生列表
        TableView<Map<String, Object>> studentTable = new TableView<>();
        studentTable.setPrefHeight(400);

        // 学号列
        TableColumn<Map<String, Object>, String> studentNumCol = new TableColumn<>("学号");
        studentNumCol.setCellValueFactory(cellData -> {
            Object val = cellData.getValue().get("courseNum");
            return new SimpleStringProperty(val != null ? val.toString() : "");
        });
        studentNumCol.setPrefWidth(100);

        // 姓名列
        TableColumn<Map<String, Object>, String> studentNameCol = new TableColumn<>("学生姓名");
        studentNameCol.setCellValueFactory(cellData -> {
            Object val = cellData.getValue().get("studentName");
            return new SimpleStringProperty(val != null ? val.toString() : "");
        });
        studentNameCol.setPrefWidth(150);

        // 成绩列 - 使用自定义Cell Factory实现可编辑
        TableColumn<Map<String, Object>, String> scoreCol = new TableColumn<>("成绩");
        scoreCol.setCellValueFactory(cellData -> {
            Object mark = cellData.getValue().get("mark");
            return new SimpleStringProperty(mark != null ? mark.toString() : "");
        });
        
        // 使用自定义Cell实现可编辑功能
        scoreCol.setCellFactory(column -> new TableCell<Map<String, Object>, String>() {
            private final TextField textField = new TextField();
            
            {
                textField.setOnAction(e -> {
                    Map<String, Object> student = getTableView().getItems().get(getIndex());
                    String newValue = textField.getText().trim();
                    
                    System.out.println("📝 编辑成绩 - 学生: " + student.get("studentName") + ", 新值: " + newValue);
                    
                    try {
                        int score = Integer.parseInt(newValue);
                        if (score < 0 || score > 100) {
                            showErrorAlert("成绩错误", "成绩必须在 0-100 之间");
                            textField.setText(student.get("mark") != null ? student.get("mark").toString() : "");
                            return;
                        }
                        
                        // 更新数据模型
                        student.put("mark", score);
                        System.out.println("✅ 成绩已更新: " + student.get("studentName") + " = " + score);
                        
                        // 刷新表格显示
                        ((TableView<Map<String, Object>>) getTableView()).refresh();
                    } catch (NumberFormatException e2) {
                        showErrorAlert("成绩错误", "请输入有效的数字");
                        textField.setText(student.get("mark") != null ? student.get("mark").toString() : "");
                    }
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, Object> student = getTableView().getItems().get(getIndex());
                    textField.setText(item != null ? item : "");
                    setGraphic(textField);
                }
            }
        });
        scoreCol.setEditable(true);
        scoreCol.setPrefWidth(100);

        // 状态列
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("选课状态");
        statusCol.setCellValueFactory(cellData -> {
            Object val = cellData.getValue().get("selectionStatus");
            String status = val != null ? val.toString() : "";
            switch (status) {
                case "PENDING": return new SimpleStringProperty("待审核");
                case "APPROVED": return new SimpleStringProperty("已通过");
                case "REJECTED": return new SimpleStringProperty("已拒绝");
                default: return new SimpleStringProperty(status);
            }
        });
        statusCol.setPrefWidth(100);

        studentTable.getColumns().addAll(studentNumCol, studentNameCol, scoreCol, statusCol);
        studentTable.setItems(FXCollections.observableArrayList(studentSelections));
        studentTable.setEditable(true);

        dialog.getDialogPane().setContent(studentTable);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return true;
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(saved -> {
            if (saved) {
                saveStudentScores(course, studentSelections);
            }
        });
    }

    /**
     * 获取课程的选课学生列表
     */
    private List<Map<String, Object>> getCourseStudents(Map<String, Object> course) {
        List<Map<String, Object>> students = new ArrayList<>();

        try {
            Object courseIdObj = course.get("courseId");
            if (courseIdObj == null) {
                System.out.println("⚠️ 课程ID为空");
                return students;
            }

            // 统一转换为整数字符串格式
            String courseIdStr;
            if (courseIdObj instanceof Number) {
                courseIdStr = String.valueOf(((Number) courseIdObj).intValue());
            } else {
                courseIdStr = courseIdObj.toString();
            }

            System.out.println("🔍 查询课程ID: " + courseIdStr);

            // 使用getAllSelections接口获取所有学生的选课记录
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/student/getAllSelections", req);

            if (res != null && res.getCode() == 0) {
                Object dataObj = res.getData();
                if (dataObj instanceof List) {
                    List<Map<String, Object>> selections = (List<Map<String, Object>>) dataObj;

                    System.out.println(" 总选课记录数: " + selections.size());

                    for (Map<String, Object> selection : selections) {
                        Object selectionCourseIdObj = selection.get("courseId");
                        if (selectionCourseIdObj == null) {
                            continue;
                        }

                        // 统一转换为整数字符串格式进行比较
                        String selectionCourseId;
                        if (selectionCourseIdObj instanceof Number) {
                            selectionCourseId = String.valueOf(((Number) selectionCourseIdObj).intValue());
                        } else {
                            selectionCourseId = selectionCourseIdObj.toString();
                        }

                        // 匹配课程ID
                        if (courseIdStr.equals(selectionCourseId)) {
                            students.add(selection);
                            System.out.println("✅ 找到学生: " + selection.get("studentName") + " - " + selection.get("courseName"));
                        }
                    }

                    System.out.println("✅ 共找到 " + students.size() + " 个选课学生");
                }
            } else {
                System.out.println("️ 获取选课记录失败: " + (res != null ? res.getMsg() : "未知错误"));
            }
        } catch (Exception e) {
            System.out.println("❌ 获取选课学生列表失败: " + e.getMessage());
            e.printStackTrace();
        }

        return students;
    }

    /**
     * 保存学生成绩
     */
    private void saveStudentScores(Map<String, Object> course, List<Map<String, Object>> students) {
        System.out.println(" 开始保存学生成绩...");
        System.out.println(" 学生总数: " + students.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (Map<String, Object> student : students) {
            Object markObj = student.get("mark");
            Object selectionIdObj = student.get("selectionId");
            Object studentName = student.get("studentName");
            
            System.out.println(" 处理学生: " + studentName + ", mark=" + markObj + ", selectionId=" + selectionIdObj);
            
            if (markObj != null && !markObj.toString().isEmpty() && selectionIdObj != null) {
                try {
                    // 尝试使用更新选课记录的接口
                    DataRequest req = new DataRequest();
                    req.add("selectionId", selectionIdObj);
                    req.add("mark", markObj);
                    
                    System.out.println(" 发送请求: /api/student/updateSelectionMark, selectionId=" + selectionIdObj + ", mark=" + markObj);
                    
                    // 尝试调用后端的成绩更新接口
                    DataResponse res = HttpRequestUtil.request("/api/student/updateSelectionMark", req);
                    
                    if (res != null && res.getCode() == 0) {
                        successCount++;
                        System.out.println("✅ 成绩保存成功: " + studentName + ", mark=" + markObj);
                    } else {
                        failCount++;
                        System.out.println("❌ 成绩保存失败: " + studentName + ", msg=" + (res != null ? res.getMsg() : "未知错误"));
                    }
                } catch (Exception e) {
                    failCount++;
                    System.out.println("❌ 成绩保存异常: " + studentName + ", " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ 跳过学生: " + studentName + " (mark=" + markObj + ", selectionId=" + selectionIdObj + ")");
            }
        }
        
        // 显示保存结果
        String message = String.format("成绩保存完成！\n成功: %d 个\n失败: %d 个", successCount, failCount);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("保存结果");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
        
        System.out.println(message);
    }

    /**
     * 更新每门课程的学生数量（有学生选则为1，否则为0）
     */
    private void updateCourseStudentCounts() {
        try {
            System.out.println("📊 开始更新课程学生数量...");
            System.out.println(" 当前课程总数: " + allCourses.size());

            // 使用新接口获取所有选课记录
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/student/getAllSelections", req);

            System.out.println("📊 选课接口响应 - code: " + (res != null ? res.getCode() : "null"));

            List<Map<String, Object>> selections = new ArrayList<>();

            if (res != null && res.getCode() == 0) {
                Object dataObj = res.getData();
                System.out.println(" 数据类型: " + (dataObj != null ? dataObj.getClass().getName() : "null"));

                if (dataObj instanceof List) {
                    selections = (List<Map<String, Object>>) dataObj;
                    System.out.println("📊 选课记录总数: " + selections.size());

                    if (!selections.isEmpty()) {
                        System.out.println("📊 第一条选课记录: " + selections.get(0));
                    }
                } else {
                    System.out.println("⚠️ data不是List类型");
                }
            } else {
                System.out.println("⚠️ 获取选课记录失败: " + (res != null ? res.getMsg() : "未知错误"));
            }

            // 统计每门课程的学生数量（统计所有选课记录，包括待审核的）
            Map<String, Integer> courseStudentCountMap = new HashMap<>();
            for (Map<String, Object> selection : selections) {
                // 统计所有选课记录（包括PENDING、APPROVED等所有状态）
                Object courseIdObj = selection.get("courseId");
                if (courseIdObj != null) {
                    String courseIdStr;
                    if (courseIdObj instanceof Number) {
                        courseIdStr = String.valueOf(((Number) courseIdObj).intValue());
                    } else {
                        courseIdStr = courseIdObj.toString();
                    }

                    // 统计该课程的学生数量
                    courseStudentCountMap.put(courseIdStr,
                        courseStudentCountMap.getOrDefault(courseIdStr, 0) + 1);

                    System.out.println("📊 添加选课 - 课程ID: " + courseIdStr + ", 学生: " + selection.get("studentName") + ", 状态: " + selection.get("selectionStatus"));
                }
            }

            System.out.println("📊 课程学生数量统计结果: " + courseStudentCountMap);

            // 更新课程数据中的学生数量
            int updatedCount = 0;
            for (Map<String, Object> course : allCourses) {
                Object courseIdObj = course.get("courseId");
                if (courseIdObj != null) {
                    String courseIdStr;
                    if (courseIdObj instanceof Number) {
                        courseIdStr = String.valueOf(((Number) courseIdObj).intValue());
                    } else {
                        courseIdStr = courseIdObj.toString();
                    }

                    // 从统计结果中获取学生数量，如果没有则为0
                    int studentCount = courseStudentCountMap.getOrDefault(courseIdStr, 0);
                    course.put("studentCount", studentCount);

                    if (studentCount > 0) {
                        updatedCount++;
                    }

                    System.out.println("✅ 课程 " + course.get("name") + " (ID:" + courseIdStr + ") 学生数: " + studentCount);
                } else {
                    System.out.println("️ 课程 " + course.get("name") + " 的courseId为null");
                }
            }

            System.out.println("✅ 成功更新课程学生数量，共 " + updatedCount + " 门课程有学生");
        } catch (Exception e) {
            System.out.println(" 更新课程学生数量时出错: " + e.getMessage());
            e.printStackTrace();
            // 出错时全部设为0
            for (Map<String, Object> course : allCourses) {
                course.put("studentCount", 0);
            }
        }
    }

    /**
     * 检查角色并隐藏打分按钮（管理员不能打分）
     */
    private void checkRoleAndHideGradeButton() {
        String role = com.teach.javafx.AppStore.getJwt() != null
            ? com.teach.javafx.AppStore.getJwt().getRole() : "";

        if ("ROLE_ADMIN".equals(role)) {
            System.out.println("🔒 管理员模式：打分功能已禁用");
        } else if ("ROLE_TEACHER".equals(role)) {
            System.out.println("👨‍🏫 教师模式：打分功能可用");
        }
    }
}