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
    @FXML
    private Label totalStudentsLabel;

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

                    {
                        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
                        toggleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");

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

                        hbox.setAlignment(Pos.CENTER);
                        hbox.getChildren().addAll(editButton, deleteButton, toggleButton);
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
                myCoursesList.clear();
                myCoursesList.addAll(data);
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
        int totalStudents = 0;

        for (Map<String, Object> course : myCoursesList) {
            String status = (String) course.get("status");
            if ("OPEN".equals(status) || "APPROVED".equals(status)) {
                open++;
            } else if ("CLOSED".equals(status) || "REJECTED".equals(status)) {
                closed++;
            }

            Object studentCountObj = course.get("studentCount");
            if (studentCountObj instanceof Number) {
                totalStudents += ((Number) studentCountObj).intValue();
            } else if (studentCountObj instanceof String) {
                try {
                    totalStudents += Integer.parseInt((String) studentCountObj);
                } catch (NumberFormatException e) {
                }
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
        if (totalStudentsLabel != null) {
            totalStudentsLabel.setText("总学生数: " + totalStudents);
        }
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
}