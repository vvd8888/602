package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;

public class StudentSelectController {

    @FXML
    private TableView<Map<String, Object>> studentTable;

    @FXML
    private TableColumn<Map<String, Object>, String> numCol;

    @FXML
    private TableColumn<Map<String, Object>, String> nameCol;

    @FXML
    private TableColumn<Map<String, Object>, String> genderCol;

    @FXML
    private TableColumn<Map<String, Object>, String> classNameCol;

    @FXML
    private TableColumn<Map<String, Object>, String> phoneCol;

    @FXML
    private TableColumn<Map<String, Object>, String> emailCol;

    @FXML
    private TableColumn<Map<String, Object>, String> operationCol;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button viewMyInfoButton;

    private ObservableList<Map<String, Object>> studentData = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> allStudentData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println("StudentSelectController 初始化开始");

        // 配置表格列
        configureTableColumns();

        // 加载学生数据
        loadStudents();

        // 设置按钮事件
        searchButton.setOnAction(e -> searchStudents());
        viewMyInfoButton.setOnAction(e -> viewMyInfo());

        System.out.println("StudentSelectController 初始化完成");
    }

    private void configureTableColumns() {
        System.out.println("配置表格列...");

        // 使用数据库实际字段名
        numCol.setCellValueFactory(new MapValueFactory("num"));
        nameCol.setCellValueFactory(new MapValueFactory("name"));
        genderCol.setCellValueFactory(new MapValueFactory("gender"));
        // 修改这里：className -> dept
        classNameCol.setCellValueFactory(new MapValueFactory("dept"));
        phoneCol.setCellValueFactory(new MapValueFactory("phone"));
        emailCol.setCellValueFactory(new MapValueFactory("email"));

        // 操作列 - 查看详情按钮
        operationCol.setCellFactory(param -> new TableCell<Map<String, Object>, String>() {
            private final Button viewButton = new Button("查看详情");

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                viewButton.setOnAction(event -> {
                    Map<String, Object> student = getTableView().getItems().get(getIndex());
                    viewStudentDetails(student);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }

    private void loadStudents() {
        try {
            System.out.println("=== 开始加载学生数据 ===");

            // 尝试多个可能的API端点
            String[] apiEndpoints = {
                    "/api/student/getCompleteStudentList",  // 新的API
                    "/api/student/getStudentList",          // 原来的API
                    "/api/student/findAll",                 // 可能的通用API
                    "/api/student/list",                    // 可能的通用API
                    "/api/data/students"                    // 可能的通用API
            };

            for (String endpoint : apiEndpoints) {
                System.out.println("尝试API: " + endpoint);

                DataRequest req = new DataRequest();
                DataResponse res = HttpRequestUtil.request(endpoint, req);

                if (res != null && res.getCode() == 0) {
                    List<Map<String, Object>> apiData = (List<Map<String, Object>>) res.getData();
                    System.out.println("从 " + endpoint + " 返回数据条数: " + (apiData != null ? apiData.size() : 0));

                    if (apiData != null && !apiData.isEmpty()) {
                        // 打印第一条数据的字段，帮助调试
                        Map<String, Object> first = apiData.get(0);
                        System.out.println("=== API返回字段示例 ===");
                        for (String key : first.keySet()) {
                            System.out.println(key + ": " + first.get(key));
                        }
                        System.out.println("=====================");

                        allStudentData.setAll(apiData);
                        studentData.setAll(apiData);
                        studentTable.setItems(studentData);

                        System.out.println("成功从数据库加载 " + apiData.size() + " 条真实学生数据");

                        // 显示成功提示
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("数据库连接成功");
                        alert.setHeaderText(null);
                        alert.setContentText("已从数据库加载 " + apiData.size() + " 条学生记录");
                        alert.showAndWait();

                        return; // 成功，退出方法
                    }
                } else {
                    System.out.println(endpoint + " 调用失败: " + (res != null ? res.getMsg() : "无响应"));
                }
            }

            // 如果上面的API都失败，尝试从person表获取
            System.out.println("所有API都失败，尝试从person表获取");
            loadAllPersonsAndFilterStudents();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("API调用异常: " + e.getMessage());
            loadSimulatedRealData();
        }
    }
    private void loadSimulatedRealData() {
        System.out.println("=== 生成模拟的真实学生数据 ===");

        List<Map<String, Object>> simulatedData = new java.util.ArrayList<>();

        // 创建20个模拟的学生记录
        for (int i = 1; i <= 20; i++) {
            java.util.Map<String, Object> student = new java.util.HashMap<>();

            // 学号
            String studentNum = String.format("202203%04d", i);
            student.put("num", studentNum);

            // 姓名
            String[] surnames = {"张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴"};
            String[] givenNames = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
                    "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞"};
            String name = surnames[(i-1) % surnames.length] + givenNames[(i-1) % givenNames.length];
            student.put("name", name);

            // 性别
            student.put("gender", (i % 2 == 0) ? "男" : "女");

            // 学院
            String[] depts = {"计算机学院", "软件学院", "信息工程学院", "网络空间安全学院", "人工智能学院"};
            student.put("dept", depts[(i-1) % depts.length]);

            // 专业
            String[] majors = {"计算机科学与技术", "软件工程", "网络工程", "信息安全", "数据科学", "人工智能"};
            student.put("major", majors[(i-1) % majors.length]);

            // 班级
            String className = "2022级" + ((i-1) % 6 + 1) + "班";
            student.put("className", className);

            // 电话
            String phone = "138" + String.format("%08d", 10000000 + i);
            student.put("phone", phone);

            // 邮箱
            student.put("email", studentNum + "@university.edu.cn");

            // 类型（1表示学生）
            student.put("type", "1");

            simulatedData.add(student);
        }

        allStudentData.setAll(simulatedData);
        studentData.setAll(simulatedData);
        studentTable.setItems(studentData);

        System.out.println("生成了 " + simulatedData.size() + " 条模拟的学生数据");

        // 显示提示
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("使用模拟数据");
        alert.setHeaderText(null);
        alert.setContentText("未能连接到数据库，已使用模拟数据。\n共生成 " + simulatedData.size() + " 条学生记录。");
        alert.showAndWait();
    }
    private void loadAllPersonsAndFilterStudents() {
        try {
            System.out.println("=== 尝试从person表获取所有人员 ===");

            // 创建请求
            DataRequest req = new DataRequest();

            // 尝试调用获取人员列表的API
            DataResponse res = HttpRequestUtil.request("/api/person/getPersonList", req);

            if (res != null && res.getCode() == 0) {
                List<Map<String, Object>> allPersons = (List<Map<String, Object>>) res.getData();

                if (allPersons != null && !allPersons.isEmpty()) {
                    System.out.println("从person表获取了 " + allPersons.size() + " 条人员记录");

                    // 筛选出学生 (假设type=1表示学生)
                    List<Map<String, Object>> students = new java.util.ArrayList<>();

                    for (Map<String, Object> person : allPersons) {
                        Object typeObj = person.get("type");
                        if (typeObj != null && "1".equals(typeObj.toString())) {
                            // 这是学生，添加到列表
                            students.add(person);
                        }
                    }

                    if (!students.isEmpty()) {
                        allStudentData.setAll(students);
                        studentData.setAll(students);
                        studentTable.setItems(studentData);

                        System.out.println("筛选出 " + students.size() + " 名学生");

                        // 显示提示
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("数据加载成功");
                        alert.setHeaderText(null);
                        alert.setContentText("从数据库加载了 " + students.size() + " 名学生");
                        alert.showAndWait();

                        return;
                    } else {
                        System.out.println("没有找到学生数据（type=1的记录）");
                    }
                } else {
                    System.out.println("person表为空或没有数据");
                }
            } else {
                System.out.println("获取person列表失败: " + (res != null ? res.getMsg() : "无响应"));
            }

            // 如果上面失败了，使用模拟数据
            System.out.println("从person表获取失败，使用模拟数据");
            loadSimulatedRealData();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取person列表异常: " + e.getMessage());
            loadSimulatedRealData();
        }
    }

    private void loadTestData() {
        List<Map<String, Object>> testData = new java.util.ArrayList<>();

        // 使用数据库中的实际数据格式
        for (int i = 1; i <= 10; i++) {
            java.util.Map<String, Object> student = new java.util.HashMap<>();
            student.put("num", "20220300" + (i < 10 ? "0" + i : i));
            student.put("name", "测试学生" + i);
            student.put("gender", i % 2 == 0 ? "男" : "女");
            student.put("dept", "计算机科学与技术");  // 注意字段名是dept
            student.put("phone", "138001380" + (i < 10 ? "0" + i : i));
            student.put("email", "student" + i + "@test.com");
            testData.add(student);
        }

        allStudentData.setAll(testData);
        studentData.setAll(testData);
        studentTable.setItems(studentData);

        System.out.println("加载了测试数据: " + testData.size() + " 条");
    }

    private void searchStudents() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            studentTable.setItems(allStudentData);
            return;
        }

        ObservableList<Map<String, Object>> filteredData = FXCollections.observableArrayList();
        for (Map<String, Object> student : allStudentData) {
            boolean match = false;

            // 检查学号
            if (student.get("num") != null &&
                    student.get("num").toString().toLowerCase().contains(keyword)) {
                match = true;
            }
            // 检查姓名
            else if (student.get("name") != null &&
                    student.get("name").toString().toLowerCase().contains(keyword)) {
                match = true;
            }
            // 检查班级（现在应该是dept）
            else if (student.get("dept") != null &&
                    student.get("dept").toString().toLowerCase().contains(keyword)) {
                match = true;
            }

            if (match) {
                filteredData.add(student);
            }
        }

        studentTable.setItems(filteredData);

        if (filteredData.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "搜索结果",
                    "没有找到匹配的学生");
        }
    }

    private void viewStudentDetails(Map<String, Object> student) {
        String studentNum = (String) student.get("num");
        String studentName = (String) student.get("name");

        // 保存选中的学生信息
        AppStore.setSelectedStudentNum(studentNum);
        AppStore.setSelectedStudentName(studentName);

        // 记录日志（可选）
        System.out.println("选择查看学生: " + studentNum + " - " + studentName);

        // 进入主框架
        loadMainFrame();
    }

    private void viewMyInfo() {
        // 获取当前登录学生（假设登录用户名是学号）
        String currentUser = AppStore.getUsername();

        if (currentUser != null && !currentUser.isEmpty()) {
            // 在当前数据中查找这个学号
            for (Map<String, Object> student : allStudentData) {
                if (currentUser.equals(student.get("num"))) {
                    // 找到当前登录学生
                    String studentName = (String) student.get("name");
                    AppStore.setSelectedStudentNum(currentUser);
                    AppStore.setSelectedStudentName(studentName != null ? studentName : "我的信息");

                    System.out.println("查看我的信息: " + currentUser);
                    loadMainFrame();
                    return;
                }
            }

            // 如果没找到，使用默认值
            AppStore.setSelectedStudentNum(currentUser);
            AppStore.setSelectedStudentName("我的信息");
            loadMainFrame();
        } else {
            showAlert(Alert.AlertType.WARNING, "提示",
                    "无法确定当前用户，请先登录");
        }
    }

    private void loadMainFrame() {
        try {
            Stage stage = (Stage) studentTable.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(
                    MainApplication.class.getResource("base/main-frame.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

            // 更新标题
            String studentName = AppStore.getSelectedStudentName();
            String title = "教学管理系统 - 学生";
            if (studentName != null && !studentName.isEmpty()) {
                title += " (" + studentName + ")";
            }

            stage.setScene(scene);
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "加载失败",
                    "无法加载主框架: " + e.getMessage());
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