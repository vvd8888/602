package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import java.util.*;

public class StudentSelectCourseController {

    // 统计标签
    @FXML
    private Label availableCoursesLabel;
    @FXML
    private Label selectedCoursesLabel;
    @FXML
    private Label totalCreditsLabel;
    @FXML
    private Label semesterLabel;

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
    private TableColumn<Map<String, Object>, Number> capacityColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> operationColumn;

    private ObservableList<Map<String, Object>> availableCoursesList = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> selectedCoursesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("🎒 StudentSelectCourseController 初始化 - 学生选课界面");

        setupCoursesTable();
        loadData();
    }

    private void setupCoursesTable() {
        coursesTable.setItems(availableCoursesList);

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

        capacityColumn.setCellValueFactory(cellData -> {
            Map<String, Object> rowData = cellData.getValue();
            Object capacity = rowData.get("capacity");
            int intValue = 50; // 默认容量
            if (capacity instanceof Number) {
                intValue = ((Number) capacity).intValue();
            } else if (capacity instanceof String) {
                try {
                    intValue = Integer.parseInt((String) capacity);
                } catch (NumberFormatException e) {
                    intValue = 50;
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
                                case "FULL":
                                    setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                                    setText("已满");
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

        operationColumn.setCellFactory(new Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>() {
            @Override
            public TableCell<Map<String, Object>, String> call(final TableColumn<Map<String, Object>, String> param) {
                return new TableCell<Map<String, Object>, String>() {
                    private final HBox hbox = new HBox(5);
                    private final Button selectButton = new Button("选课");
                    private final Button detailButton = new Button("详情");

                    {
                        selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
                        detailButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");

                        selectButton.setOnAction(event -> {
                            Map<String, Object> data = getTableView().getItems().get(getIndex());
                            handleSelectCourse(data);
                        });

                        detailButton.setOnAction(event -> {
                            Map<String, Object> data = getTableView().getItems().get(getIndex());
                            showCourseDetail(data);
                        });

                        hbox.setAlignment(Pos.CENTER);
                        hbox.getChildren().addAll(selectButton, detailButton);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                            String status = (String) rowData.get("status");
                            Integer studentCount = rowData.get("studentCount") != null ?
                                    Integer.parseInt(rowData.get("studentCount").toString()) : 0;
                            Integer capacity = rowData.get("capacity") != null ?
                                    Integer.parseInt(rowData.get("capacity").toString()) : 50;

                            // 检查是否已选此课程
                            Integer courseId = rowData.get("courseId") != null ?
                                    Integer.parseInt(rowData.get("courseId").toString()) : null;
                            boolean alreadySelected = isCourseAlreadySelected(courseId);

                            if (alreadySelected) {
                                selectButton.setText("已选");
                                selectButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-size: 12px;");
                                selectButton.setDisable(true);
                            } else if ("OPEN".equals(status) || "APPROVED".equals(status)) {
                                if (studentCount >= capacity) {
                                    selectButton.setText("已满");
                                    selectButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 12px;");
                                    selectButton.setDisable(true);
                                } else {
                                    selectButton.setText("选课");
                                    selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
                                    selectButton.setDisable(false);
                                }
                            } else {
                                selectButton.setText("不可选");
                                selectButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
                                selectButton.setDisable(true);
                            }

                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }

    private boolean isCourseAlreadySelected(Integer courseId) {
        if (courseId == null) return false;

        for (Map<String, Object> course : selectedCoursesList) {
            Integer selectedCourseId = course.get("courseId") != null ?
                    Integer.parseInt(course.get("courseId").toString()) : null;
            if (selectedCourseId != null && selectedCourseId.equals(courseId)) {
                return true;
            }
        }
        return false;
    }

    private void loadData() {
        loadAvailableCourses();
        loadSelectedCourses();
        updateStatistics();
    }

    private void loadAvailableCourses() {
        try {
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/course/student/availableCourses", req);

            if (res != null && res.getCode() == 0) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) res.getData();
                availableCoursesList.clear();
                availableCoursesList.addAll(data);
                System.out.println("✅ 加载了 " + data.size() + " 门可选课程");
            } else {
                showErrorAlert("加载可选课程失败", res);
                addTestAvailableCourses();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("加载可选课程失败", "网络错误：" + e.getMessage());
            addTestAvailableCourses();
        }
    }

    private void loadSelectedCourses() {
        try {
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/course/student/mySelections", req);

            if (res != null && res.getCode() == 0) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) res.getData();
                selectedCoursesList.clear();
                selectedCoursesList.addAll(data);
                System.out.println("✅ 加载了 " + data.size() + " 门已选课程");
            } else {
                showErrorAlert("加载已选课程失败", res);
                addTestSelectedCourses();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("加载已选课程失败", "网络错误：" + e.getMessage());
            addTestSelectedCourses();
        }
    }

    private void addTestAvailableCourses() {
        if (availableCoursesList.isEmpty()) {
            System.out.println("📊 添加测试可选课程数据");

            for (int i = 1; i <= 5; i++) {
                Map<String, Object> course = new HashMap<>();
                course.put("courseId", 100 + i);
                course.put("num", "CS" + (100 + i));
                course.put("name", "课程" + i);
                course.put("credit", 2 + i);
                course.put("teacher", "老师" + i);
                course.put("time", "周" + i + " 1-2节");
                course.put("classroom", "A" + (100 + i));
                course.put("status", "OPEN");
                course.put("studentCount", 20 + i);
                course.put("capacity", 50);
                availableCoursesList.add(course);
            }
        }
    }

    private void addTestSelectedCourses() {
        if (selectedCoursesList.isEmpty()) {
            System.out.println("📊 添加测试已选课程数据");

            for (int i = 1; i <= 2; i++) {
                Map<String, Object> course = new HashMap<>();
                course.put("courseId", 200 + i);
                course.put("num", "CS" + (200 + i));
                course.put("name", "已选课程" + i);
                course.put("credit", 3);
                course.put("teacher", "老师" + (i + 5));
                course.put("time", "周" + i + " 3-4节");
                course.put("classroom", "B" + (200 + i));
                course.put("status", "APPROVED");
                course.put("selectionStatus", "APPROVED");
                selectedCoursesList.add(course);
            }
        }
    }

    @FXML
    private void onRefreshClick() {
        loadData();
    }

    private void handleSelectCourse(Map<String, Object> course) {
        Integer courseId = course.get("courseId") != null ?
                Integer.parseInt(course.get("courseId").toString()) : null;
        String courseName = (String) course.get("name");

        if (courseId == null) {
            showErrorAlert("选课失败", "课程ID无效");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认选课");
        confirmAlert.setHeaderText("确定要选择这门课程吗？");
        confirmAlert.setContentText("课程名称: " + courseName + "\n学分: " + course.get("credit"));
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                applyForCourse(courseId);
            }
        });
    }

    private void applyForCourse(Integer courseId) {
        try {
            DataRequest req = new DataRequest();
            req.add("courseId", courseId);

            System.out.println("📤 发送选课申请: courseId=" + courseId);
            DataResponse res = HttpRequestUtil.request("/api/course/student/apply", req);

            if (res != null && res.getCode() == 0) {
                System.out.println("✅ 选课申请成功");
                showSuccessAlert("选课成功", "选课申请已提交，等待审核");
                loadData(); // 重新加载数据
            } else {
                showErrorAlert("选课失败", res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("选课失败", "网络错误：" + e.getMessage());
        }
    }

    private void showCourseDetail(Map<String, Object> course) {
        Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
        detailAlert.setTitle("课程详情");
        detailAlert.setHeaderText((String) course.get("name"));

        String content = String.format(
                "课程编号: %s\n" +
                        "课程名称: %s\n" +
                        "学分: %s\n" +
                        "授课教师: %s\n" +
                        "上课时间: %s\n" +
                        "上课地点: %s\n" +
                        "状态: %s\n" +
                        "已选人数: %s/%s\n" +
                        "课程描述: %s",
                course.get("num"),
                course.get("name"),
                course.get("credit"),
                course.get("teacher"),
                course.get("time"),
                course.get("classroom"),
                getStatusChinese((String) course.get("status")),
                course.get("studentCount"),
                course.get("capacity"),
                "这是一门优秀的课程，建议选择。"
        );

        detailAlert.setContentText(content);
        detailAlert.show();
    }

    private String getStatusChinese(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "PENDING": return "待审核";
            case "OPEN":
            case "APPROVED": return "开放";
            case "CLOSED":
            case "REJECTED": return "关闭";
            case "FULL": return "已满";
            default: return status;
        }
    }

    private void updateStatistics() {
        int available = availableCoursesList.size();
        int selected = selectedCoursesList.size();
        int totalCredits = 0;

        for (Map<String, Object> course : selectedCoursesList) {
            Object creditObj = course.get("credit");
            if (creditObj instanceof Number) {
                totalCredits += ((Number) creditObj).intValue();
            } else if (creditObj instanceof String) {
                try {
                    totalCredits += Integer.parseInt((String) creditObj);
                } catch (NumberFormatException e) {
                }
            }
        }

        if (availableCoursesLabel != null) {
            availableCoursesLabel.setText(String.valueOf(available));
        }
        if (selectedCoursesLabel != null) {
            selectedCoursesLabel.setText(String.valueOf(selected));
        }
        if (totalCreditsLabel != null) {
            totalCreditsLabel.setText(String.valueOf(totalCredits));
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