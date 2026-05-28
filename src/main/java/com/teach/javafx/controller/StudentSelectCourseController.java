package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.*;

public class StudentSelectCourseController {

    // 课程模型类
    public static class Course {
        private SimpleIntegerProperty id = new SimpleIntegerProperty();
        private SimpleStringProperty name = new SimpleStringProperty();
        private SimpleStringProperty type = new SimpleStringProperty();
        private SimpleIntegerProperty credit = new SimpleIntegerProperty();
        private SimpleStringProperty teacherName = new SimpleStringProperty();
        private SimpleStringProperty teacherId = new SimpleStringProperty();
        private SimpleIntegerProperty maxStudents = new SimpleIntegerProperty();
        private SimpleIntegerProperty currentStudents = new SimpleIntegerProperty();
        private SimpleStringProperty time = new SimpleStringProperty();
        private SimpleStringProperty location = new SimpleStringProperty();
        private SimpleStringProperty description = new SimpleStringProperty();
        private boolean selected = false; // 是否已选

        // 构造函数
        public Course() {
        }

        public Course(int id, String name, String type, int credit, String teacherName,
                      int maxStudents, int currentStudents, String time) {
            this.id.set(id);
            this.name.set(name);
            this.type.set(type);
            this.credit.set(credit);
            this.teacherName.set(teacherName);
            this.maxStudents.set(maxStudents);
            this.currentStudents.set(currentStudents);
            this.time.set(time);
        }

        // Getters and Setters
        public int getId() {
            return id.get();
        }

        public void setId(int id) {
            this.id.set(id);
        }

        public SimpleIntegerProperty idProperty() {
            return id;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public String getType() {
            return type.get();
        }

        public void setType(String type) {
            this.type.set(type);
        }

        public SimpleStringProperty typeProperty() {
            return type;
        }

        public int getCredit() {
            return credit.get();
        }

        public void setCredit(int credit) {
            this.credit.set(credit);
        }

        public SimpleIntegerProperty creditProperty() {
            return credit;
        }

        public String getTeacherName() {
            return teacherName.get();
        }

        public void setTeacherName(String teacherName) {
            this.teacherName.set(teacherName);
        }

        public SimpleStringProperty teacherNameProperty() {
            return teacherName;
        }

        public String getTeacherId() {
            return teacherId.get();
        }

        public void setTeacherId(String teacherId) {
            this.teacherId.set(teacherId);
        }

        public SimpleStringProperty teacherIdProperty() {
            return teacherId;
        }

        public int getMaxStudents() {
            return maxStudents.get();
        }

        public void setMaxStudents(int maxStudents) {
            this.maxStudents.set(maxStudents);
        }

        public SimpleIntegerProperty maxStudentsProperty() {
            return maxStudents;
        }

        public int getCurrentStudents() {
            return currentStudents.get();
        }

        public void setCurrentStudents(int currentStudents) {
            this.currentStudents.set(currentStudents);
        }

        public SimpleIntegerProperty currentStudentsProperty() {
            return currentStudents;
        }

        public String getTime() {
            return time.get();
        }

        public void setTime(String time) {
            this.time.set(time);
        }

        public SimpleStringProperty timeProperty() {
            return time;
        }

        public String getLocation() {
            return location.get();
        }

        public void setLocation(String location) {
            this.location.set(location);
        }

        public SimpleStringProperty locationProperty() {
            return location;
        }

        public String getDescription() {
            return description.get();
        }

        public void setDescription(String description) {
            this.description.set(description);
        }

        public SimpleStringProperty descriptionProperty() {
            return description;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getStudentCount() {
            return getCurrentStudents() + "/" + getMaxStudents();
        }
    }

    // 已选课程模型类
    public static class SelectedCourse extends Course {
        private String selectTime;

        public SelectedCourse() {
            super();
        }

        public SelectedCourse(Course course) {
            super();
            setId(course.getId());
            setName(course.getName());
            setType(course.getType());
            setCredit(course.getCredit());
            setTeacherName(course.getTeacherName());
            setTeacherId(course.getTeacherId());
            setMaxStudents(course.getMaxStudents());
            setCurrentStudents(course.getCurrentStudents());
            setTime(course.getTime());
            setLocation(course.getLocation());
            setDescription(course.getDescription());
        }

        public String getSelectTime() {
            return selectTime;
        }

        public void setSelectTime(String selectTime) {
            this.selectTime = selectTime;
        }
    }

    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab availableTab;
    @FXML
    private Tab selectedTab;

    // 可选课程表格
    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, Integer> courseIdCol;
    @FXML
    private TableColumn<Course, String> courseNameCol;
    @FXML
    private TableColumn<Course, String> courseTypeCol;
    @FXML
    private TableColumn<Course, Integer> creditCol;
    @FXML
    private TableColumn<Course, String> teacherNameCol;
    @FXML
    private TableColumn<Course, String> studentCountCol;
    @FXML
    private TableColumn<Course, String> courseTimeCol;
    @FXML
    private TableColumn<Course, String> actionCol;

    // 已选课程表格
    @FXML
    private TableView<SelectedCourse> selectedCourseTable;
    @FXML
    private TableColumn<SelectedCourse, Integer> selectedCourseIdCol;
    @FXML
    private TableColumn<SelectedCourse, String> selectedCourseNameCol;
    @FXML
    private TableColumn<SelectedCourse, String> selectedCourseTypeCol;
    @FXML
    private TableColumn<SelectedCourse, Integer> selectedCreditCol;
    @FXML
    private TableColumn<SelectedCourse, String> selectedTeacherNameCol;
    @FXML
    private TableColumn<SelectedCourse, String> selectedCourseTimeCol;
    @FXML
    private TableColumn<SelectedCourse, String> selectedActionCol;

    // 统计标签
    @FXML
    private Label totalCourseCount;
    @FXML
    private Label selectedCourseCount;
    @FXML
    private Label remainingCourseCount;
    @FXML
    private Label totalCreditsLabel;

    // 搜索和筛选
    @FXML
    private TextField searchField;
    @FXML
    private ToggleButton toggleAll;
    @FXML
    private ToggleButton toggleAvailable;
    @FXML
    private ToggleButton toggleSelected;

    // 数据
    private ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private ObservableList<Course> availableCourses = FXCollections.observableArrayList();
    private ObservableList<SelectedCourse> selectedCourses = FXCollections.observableArrayList();

    // 搜索关键词
    private String searchKeyword = "";
    // 筛选状态：0-全部，1-可选，2-已选
    private int filterStatus = 0;

    @FXML
    public void initialize() {
        System.out.println("🎒 StudentSelectCourseController 初始化开始");

        // 初始化表格
        initializeCourseTable();
        initializeSelectedCourseTable();

        // 加载数据
        loadData();

        // 初始化统计信息
        updateStatistics();

        System.out.println("✅ StudentSelectCourseController 初始化完成");
    }

    /**
     * 初始化可选课程表格
     */
    private void initializeCourseTable() {
        // 设置列和数据绑定
        courseIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        courseNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        courseTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        teacherNameCol.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        studentCountCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentCount()));
        courseTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));

        // 操作列
        actionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Course, String> call(TableColumn<Course, String> param) {
                return new TableCell<>() {
                    private final Button selectButton = new Button("选课");

                    {
                        selectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        selectButton.setOnAction(event -> {
                            Course course = getTableView().getItems().get(getIndex());
                            handleSelectCourse(course);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Course course = getTableView().getItems().get(getIndex());

                            // 检查是否已选
                            boolean isSelected = false;
                            for (SelectedCourse sc : selectedCourses) {
                                if (sc.getId() == course.getId()) {
                                    isSelected = true;
                                    break;
                                }
                            }

                            // 检查是否已满
                            boolean isFull = course.getCurrentStudents() >= course.getMaxStudents();

                            if (isSelected) {
                                Button cancelButton = new Button("已选");
                                cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
                                cancelButton.setOnAction(event -> {
                                    SelectedCourse selectedCourse = null;
                                    for (SelectedCourse sc : selectedCourses) {
                                        if (sc.getId() == course.getId()) {
                                            selectedCourse = sc;
                                            break;
                                        }
                                    }
                                    if (selectedCourse != null) {
                                        handleDeselectCourse(selectedCourse);
                                    }
                                });
                                setGraphic(cancelButton);
                            } else if (isFull) {
                                Button fullButton = new Button("已满");
                                fullButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                                fullButton.setDisable(true);
                                setGraphic(fullButton);
                            } else {
                                setGraphic(selectButton);
                            }
                        }
                    }
                };
            }
        });

        courseTable.setItems(availableCourses);
    }

    /**
     * 初始化已选课程表格
     */
    private void initializeSelectedCourseTable() {
        // 设置列和数据绑定
        selectedCourseIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        selectedCourseNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        selectedCourseTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        selectedCreditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        selectedTeacherNameCol.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        selectedCourseTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));

        // 操作列
        selectedActionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<SelectedCourse, String> call(TableColumn<SelectedCourse, String> param) {
                return new TableCell<>() {
                    private final Button cancelButton = new Button("退选");

                    {
                        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                        cancelButton.setOnAction(event -> {
                            SelectedCourse course = getTableView().getItems().get(getIndex());
                            handleDeselectCourse(course);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(cancelButton);
                        }
                    }
                };
            }
        });

        selectedCourseTable.setItems(selectedCourses);
    }

    /**
     * 加载数据
     */
    private void loadData() {
        System.out.println("📥 从后端加载课程数据...");

        try {
            DataRequest req = new DataRequest();
            DataResponse res = HttpRequestUtil.request("/api/student/getCourses", req);

            if (res != null && res.getCode() == 0) {
                List<?> dataList = (List<?>) res.getData();

                if (dataList != null) {
                    allCourses.clear();

                    for (Object item : dataList) {
                        Map<String, Object> courseMap = (Map<String, Object>) item;

                        Course course = new Course();
                        // 兼容 courseId 和 id 两种字段名
                        Object idObj = courseMap.get("id");
                        if (idObj == null) {
                            idObj = courseMap.get("courseId");
                        }

                        // 处理ID可能是String或Number的情况
                        if (idObj instanceof Number) {
                            course.setId(((Number) idObj).intValue());
                        } else if (idObj instanceof String) {
                            // 如果courseId是字符串，尝试转换为hashCode或使用其他方式生成唯一ID
                            String courseIdStr = (String) idObj;
                            try {
                                course.setId(Integer.parseInt(courseIdStr));
                            } catch (NumberFormatException e) {
                                // 如果无法解析为整数，使用hashCode
                                course.setId(courseIdStr.hashCode());
                            }
                        }

                        // 兼容 name 和 courseName 两种字段名
                        Object nameObj = courseMap.get("name");
                        if (nameObj == null) {
                            nameObj = courseMap.get("courseName");
                        }
                        course.setName(nameObj != null ? (String) nameObj : "");

                        // 兼容 type 字段
                        Object typeObj = courseMap.get("type");
                        if (typeObj == null) {
                            typeObj = courseMap.get("courseType");
                        }
                        course.setType(typeObj != null ? (String) typeObj : "");

                        // 处理credit字段
                        Object creditObj = courseMap.get("credit");
                        if (creditObj instanceof Number) {
                            course.setCredit(((Number) creditObj).intValue());
                        } else if (creditObj instanceof String) {
                            try {
                                course.setCredit(Integer.parseInt((String) creditObj));
                            } catch (NumberFormatException e) {
                                course.setCredit(0);
                            }
                        } else {
                            course.setCredit(0);
                        }

                        // 兼容 teacherName 和 teacher 字段
                        Object teacherObj = courseMap.get("teacherName");
                        if (teacherObj == null) {
                            teacherObj = courseMap.get("teacher");
                        }
                        course.setTeacherName(teacherObj != null ? (String) teacherObj : "");

                        // 处理teacherId
                        Object teacherIdObj = courseMap.get("teacherId");
                        if (teacherIdObj == null) {
                            teacherIdObj = courseMap.get("teacher");
                        }
                        course.setTeacherId(teacherIdObj != null ? (String) teacherIdObj : "");

                        // 处理maxStudents，如果没有则设置默认值
                        Object maxStudentsObj = courseMap.get("maxStudents");
                        if (maxStudentsObj instanceof Number) {
                            course.setMaxStudents(((Number) maxStudentsObj).intValue());
                        } else {
                            course.setMaxStudents(50); // 默认最大人数
                        }

                        // 处理currentStudents，如果没有则设置默认值
                        Object currentStudentsObj = courseMap.get("currentStudents");
                        if (currentStudentsObj instanceof Number) {
                            course.setCurrentStudents(((Number) currentStudentsObj).intValue());
                        } else {
                            course.setCurrentStudents(0);
                        }

                        // 处理time字段
                        Object timeObj = courseMap.get("time");
                        course.setTime(timeObj != null ? (String) timeObj : "");

                        // 处理location字段，兼容 classroom
                        Object locationObj = courseMap.get("location");
                        if (locationObj == null) {
                            locationObj = courseMap.get("classroom");
                        }
                        course.setLocation(locationObj != null ? (String) locationObj : "");

                        // 处理description字段
                        Object descriptionObj = courseMap.get("description");
                        course.setDescription(descriptionObj != null ? (String) descriptionObj : "");

                        allCourses.add(course);
                    }

                    System.out.println("✅ 成功加载 " + allCourses.size() + " 门课程");
                }
            } else {
                System.err.println("❌ 获取课程列表失败: " + (res != null ? res.getMsg() : "网络错误"));
                loadMockData();
            }
        } catch (Exception e) {
            System.err.println("⚠️ 加载课程数据异常: " + e.getMessage());
            e.printStackTrace();
            loadMockData();
        }

        filterCourses();
        loadMySelections();
        updateStatistics();

        System.out.println("✅ 课程数据加载完成，可选课程数：" + allCourses.size() +
                "，已选课程数：" + selectedCourses.size());
    }

    /**
     * 加载模拟数据（备用）
     */
    private void loadMockData() {
        System.out.println("📦 使用模拟数据...");

        List<Course> courses = new ArrayList<>();
        courses.add(new Course(1001, "Java程序设计", "必修", 3, "张老师", 50, 0, "周一 1-2节"));
        courses.add(new Course(1002, "数据库原理", "必修", 4, "李老师", 60, 0, "周二 3-4节"));
        courses.add(new Course(1003, "Web开发", "选修", 2, "王老师", 40, 0, "周三 5-6节"));
        courses.add(new Course(1004, "数据结构", "必修", 4, "赵老师", 50, 0, "周四 1-2节"));
        courses.add(new Course(1005, "人工智能基础", "选修", 3, "刘老师", 30, 0, "周五 3-4节"));
        courses.add(new Course(1006, "软件工程", "必修", 3, "陈老师", 45, 0, "周一 7-8节"));
        courses.add(new Course(1007, "移动应用开发", "选修", 2, "杨老师", 35, 0, "周二 5-6节"));
        courses.add(new Course(1008, "计算机网络", "必修", 4, "周老师", 55, 0, "周三 1-2节"));
        courses.add(new Course(1009, "操作系统", "必修", 4, "吴老师", 50, 0, "周四 3-4节"));
        courses.add(new Course(1010, "Python编程", "选修", 3, "孙老师", 40, 0, "周五 5-6节"));

        allCourses.setAll(courses);
    }

    /**
     * 加载我的选课信息
     */
    private void loadMySelections() {
        try {
            DataRequest req = new DataRequest();
            req.add("studentId", AppStore.getJwt().getId());
            
            DataResponse res = HttpRequestUtil.request("/api/student/getMySelections", req);
            
            if (res != null && res.getCode() == 0) {
                Object dataObj = res.getData();
                
                // 后端直接返回 ArrayList，不是 Map
                if (dataObj instanceof List) {
                    List<?> selectionsList = (List<?>) dataObj;
                    
                    selectedCourses.clear();
                    
                    for (Object item : selectionsList) {
                        Map<String, Object> selectionMap = (Map<String, Object>) item;

                        SelectedCourse sc = new SelectedCourse();

                        // 兼容 courseId 和 id
                        Object idObj = selectionMap.get("id");
                        if (idObj == null) {
                            idObj = selectionMap.get("courseId");
                        }

                        if (idObj instanceof Number) {
                            sc.setId(((Number) idObj).intValue());
                        } else if (idObj instanceof String) {
                            String courseIdStr = (String) idObj;
                            try {
                                sc.setId(Integer.parseInt(courseIdStr));
                            } catch (NumberFormatException e) {
                                sc.setId(courseIdStr.hashCode());
                            }
                        }

                        // 兼容 courseName 和 name
                        Object nameObj = selectionMap.get("name");
                        if (nameObj == null) {
                            nameObj = selectionMap.get("courseName");
                        }
                        sc.setName(nameObj != null ? (String) nameObj : "");

                        // 处理type字段
                        Object typeObj = selectionMap.get("type");
                        if (typeObj == null) {
                            typeObj = selectionMap.get("courseType");
                        }
                        sc.setType(typeObj != null ? (String) typeObj : "");

                        // 处理credit字段 - 兼容 courseCredit
                        Object creditObj = selectionMap.get("credit");
                        if (creditObj == null) {
                            creditObj = selectionMap.get("courseCredit");
                        }
                        if (creditObj instanceof Number) {
                            sc.setCredit(((Number) creditObj).intValue());
                        } else if (creditObj instanceof String) {
                            try {
                                sc.setCredit(Integer.parseInt((String) creditObj));
                            } catch (NumberFormatException e) {
                                sc.setCredit(0);
                            }
                        } else {
                            sc.setCredit(0);
                        }

                        // 兼容 teacherName 和 teacher - 兼容 courseTeacher
                        Object teacherObj = selectionMap.get("teacherName");
                        if (teacherObj == null) {
                            teacherObj = selectionMap.get("teacher");
                        }
                        if (teacherObj == null) {
                            teacherObj = selectionMap.get("courseTeacher");
                        }
                        sc.setTeacherName(teacherObj != null ? (String) teacherObj : "");

                        // 处理time字段 - 兼容 courseTime
                        Object timeObj = selectionMap.get("time");
                        if (timeObj == null) {
                            timeObj = selectionMap.get("courseTime");
                        }
                        sc.setTime(timeObj != null ? (String) timeObj : "");

                        // 处理location字段，兼容 classroom - 兼容 courseClassroom
                        Object locationObj = selectionMap.get("location");
                        if (locationObj == null) {
                            locationObj = selectionMap.get("classroom");
                        }
                        if (locationObj == null) {
                            locationObj = selectionMap.get("courseClassroom");
                        }
                        sc.setLocation(locationObj != null ? (String) locationObj : "");

                        // 处理description字段
                        Object descriptionObj = selectionMap.get("description");
                        sc.setDescription(descriptionObj != null ? (String) descriptionObj : "");

                        selectedCourses.add(sc);
                        System.out.println("✅ 加载已选课程: " + sc.getName() + " (id=" + sc.getId() + ")");

                        // 标记为已选 - 同时更新 allCourses 和 availableCourses
                        for (Course c : allCourses) {
                            if (c.getId() == sc.getId()) {
                                c.setSelected(true);
                                System.out.println("  标记课程为已选: " + c.getName());
                                break;
                            }
                        }
                    }
                    
                    System.out.println("✅ 成功加载 " + selectedCourses.size() + " 门已选课程");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ 加载选课信息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 筛选课程
     */
    private void filterCourses() {
        availableCourses.clear();

        for (Course course : allCourses) {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            // 搜索筛选
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                matchesSearch = course.getName().contains(searchKeyword) ||
                        course.getTeacherName().contains(searchKeyword);
            }

            // 状态筛选
            if (filterStatus == 1) { // 只显示可选
                matchesStatus = !course.isSelected() &&
                        course.getCurrentStudents() < course.getMaxStudents();
            } else if (filterStatus == 2) { // 只显示已选
                matchesStatus = course.isSelected();
            }

            if (matchesSearch && matchesStatus) {
                availableCourses.add(course);
            }
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        int total = allCourses.size();
        int selected = selectedCourses.size();
        int available = 0;
        int totalCredits = 0;

        // 计算可选课程数
        for (Course course : allCourses) {
            if (!course.isSelected() && course.getCurrentStudents() < course.getMaxStudents()) {
                available++;
            }
        }

        // 计算总学分
        for (SelectedCourse course : selectedCourses) {
            totalCredits += course.getCredit();
        }

        totalCourseCount.setText(String.valueOf(total));
        selectedCourseCount.setText(String.valueOf(selected));
        remainingCourseCount.setText(String.valueOf(available));
        totalCreditsLabel.setText("总学分：" + totalCredits);
    }

    /**
     * 选课
     */
    @FXML
    private void handleSelectCourse(Course course) {
        System.out.println("选课: " + course.getName());

        // 检查是否已选
        for (SelectedCourse sc : selectedCourses) {
            if (sc.getId() == course.getId()) {
                showAlert(Alert.AlertType.WARNING, "提示", "您已经选择了这门课程！");
                return;
            }
        }

        // 检查是否已满
        if (course.getCurrentStudents() >= course.getMaxStudents()) {
            showAlert(Alert.AlertType.WARNING, "提示", "该课程已满，无法选择！");
            return;
        }

        // 检查学分限制（假设最多选20学分）
        int currentCredits = 0;
        for (SelectedCourse sc : selectedCourses) {
            currentCredits += sc.getCredit();
        }
        if (currentCredits + course.getCredit() > 20) {
            showAlert(Alert.AlertType.WARNING, "提示", "学分超过限制！最多可选20学分，当前已选" + currentCredits + "学分。");
            return;
        }

        // 创建已选课程对象
        SelectedCourse selectedCourse = new SelectedCourse(course);
        selectedCourse.setSelectTime(new Date().toString());

        // 添加到已选列表
        selectedCourses.add(selectedCourse);

        // 更新原课程的已选状态
        course.setSelected(true);

        // 更新课程人数
        course.setCurrentStudents(course.getCurrentStudents() + 1);

        // 刷新表格
        courseTable.refresh();
        selectedCourseTable.refresh();

        // 更新统计
        updateStatistics();

        // 显示成功消息
        showAlert(Alert.AlertType.INFORMATION, "成功",
                "选课成功！\n课程：" + course.getName() + "\n教师：" + course.getTeacherName() +
                        "\n\n请点击\"提交选课\"完成选课");
    }

    /**
     * 退选课程
     */
    @FXML
    private void handleDeselectCourse(SelectedCourse course) {
        System.out.println("退选: " + course.getName() + " (courseId=" + course.getId() + ")");

        // 确认退选
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认退选");
        confirmAlert.setHeaderText("确定要退选这门课程吗？");
        confirmAlert.setContentText("课程：" + course.getName() + "\n退选后需要点击\"提交选课\"才能生效");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 从已选列表中移除
            selectedCourses.remove(course);

            // 更新原课程的已选状态 - 关键修复
            for (Course c : allCourses) {
                if (c.getId() == course.getId()) {
                    c.setSelected(false);
                    c.setCurrentStudents(Math.max(0, c.getCurrentStudents() - 1));
                    System.out.println("✅ 已重置课程状态: " + c.getName() + ", selected=false");
                    break;
                }
            }
            
            // 如果该课程不在allCourses中（比如是从后端加载的已选课程），
            // 需要确保它能被重新选择
            boolean foundInAll = false;
            for (Course c : allCourses) {
                if (c.getId() == course.getId()) {
                    foundInAll = true;
                    break;
                }
            }
            
            if (!foundInAll) {
                System.out.println("⚠️ 退选的课程不在可选列表中: " + course.getName() + " (id=" + course.getId() + ")");
                // 将该课程重新添加到可选列表
                Course newCourse = new Course();
                newCourse.setId(course.getId());
                newCourse.setName(course.getName());
                newCourse.setType(course.getType());
                newCourse.setCredit(course.getCredit());
                newCourse.setTeacherName(course.getTeacherName());
                newCourse.setTeacherId(course.getTeacherId());
                newCourse.setMaxStudents(course.getMaxStudents());
                newCourse.setCurrentStudents(Math.max(0, course.getCurrentStudents() - 1));
                newCourse.setTime(course.getTime());
                newCourse.setLocation(course.getLocation());
                newCourse.setDescription(course.getDescription());
                newCourse.setSelected(false);
                allCourses.add(newCourse);
                System.out.println("✅ 已将退选课程重新添加到可选列表");
            }

            // 刷新表格
            courseTable.refresh();
            selectedCourseTable.refresh();
            filterCourses(); // 重新筛选，确保退选的课程出现在可选列表

            // 更新统计
            updateStatistics();

            // 显示成功消息
            showAlert(Alert.AlertType.INFORMATION, "成功", 
                "已从前台移除！\n课程：" + course.getName() + "\n请点击\"提交选课\"完成退选");
        }
    }

    /**
     * 退选全部
     */
    @FXML
    private void handleDeselectAll() {
        if (selectedCourses.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "提示", "您还没有选择任何课程！");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认退选");
        confirmAlert.setHeaderText("您确定要退选所有课程吗？");
        confirmAlert.setContentText("这将从前台移除 " + selectedCourses.size() + " 门课程\n请点击\"提交选课\"完成退选");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 清空已选列表（前端乐观更新）
            int count = selectedCourses.size();
            selectedCourses.clear();

            // 重置所有课程的已选状态
            for (Course c : allCourses) {
                c.setSelected(false);
            }

            // 刷新表格
            courseTable.refresh();
            selectedCourseTable.refresh();
            updateStatistics();

            showAlert(Alert.AlertType.INFORMATION, "成功",
                    "已从前台移除所有课程！共" + count + "门\n请点击\"提交选课\"完成退选");
        }
    }

    /**
     * 搜索
     */
    @FXML
    private void handleSearch() {
        searchKeyword = searchField.getText().trim();
        filterCourses();
    }

    /**
     * 重置
     */
    @FXML
    private void handleReset() {
        searchField.setText("");
        searchKeyword = "";
        toggleAll.setSelected(true);
        filterStatus = 0;
        filterCourses();
    }

    /**
     * 刷新列表
     */
    @FXML
    private void handleRefresh() {
        // 检查是否有未提交的选课变更
        if (!selectedCourses.isEmpty()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认刷新");
            confirmAlert.setHeaderText("您有未提交的选课变更");
            confirmAlert.setContentText("刷新将丢失未提交的选课数据，是否继续？\n\n建议先点击\"提交选课\"保存数据");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return; // 用户取消刷新
            }
        }
        
        loadData();
        showAlert(Alert.AlertType.INFORMATION, "提示", "数据已刷新！");
    }

    /**
     * 一键全选
     */
    @FXML
    private void handleSelectAll() {
        int added = 0;
        int skipped = 0;
        int currentCredits = 0;

        // 计算当前已选学分
        for (SelectedCourse sc : selectedCourses) {
            currentCredits += sc.getCredit();
        }

        for (Course course : availableCourses) {
            // 跳过已选和已满的课程
            if (course.isSelected() || course.getCurrentStudents() >= course.getMaxStudents()) {
                skipped++;
                continue;
            }

            // 检查学分限制
            if (currentCredits + course.getCredit() > 20) {
                skipped++;
                continue;
            }

            // 选课
            SelectedCourse selectedCourse = new SelectedCourse(course);
            selectedCourse.setSelectTime(new Date().toString());
            selectedCourses.add(selectedCourse);
            course.setSelected(true);
            course.setCurrentStudents(course.getCurrentStudents() + 1);
            currentCredits += course.getCredit();
            added++;

            // 保存到服务器
            saveSelectionToServer(course);
        }

        // 刷新表格
        courseTable.refresh();
        selectedCourseTable.refresh();
        updateStatistics();

        showAlert(Alert.AlertType.INFORMATION, "结果",
                "一键选课完成！\n成功选择：" + added + "门课程\n跳过：" + skipped + "门课程");
    }

    /**
     * 提交选课
     */
    @FXML
    private void handleSubmitSelection() {
        if (selectedCourses.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "提示", "您还没有选择任何课程！");
            return;
        }

        // 计算总学分
        int totalCredits = 0;
        StringBuilder courseList = new StringBuilder();
        for (SelectedCourse course : selectedCourses) {
            totalCredits += course.getCredit();
            courseList.append("\n- ").append(course.getName())
                    .append(" (").append(course.getCredit()).append("学分)");
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认提交");
        confirmAlert.setHeaderText("您确定要提交选课结果吗？");
        confirmAlert.setContentText("您选择了 " + selectedCourses.size() + " 门课程，共 " + totalCredits + " 学分" +
                courseList.toString());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 这里应该调用后端API提交选课结果
            boolean success = submitSelectionToServer();

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "成功",
                        "选课提交成功！\n已选择 " + selectedCourses.size() + " 门课程，共 " + totalCredits + " 学分");
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "提交失败，请稍后重试！");
            }
        }
    }

    /**
     * 查看选课结果
     */
    @FXML
    private void handleViewResult() {
        if (selectedCourses.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "选课结果", "您还没有选择任何课程！");
            return;
        }

        // 计算统计信息
        int totalCourses = selectedCourses.size();
        int totalCredits = 0;
        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Integer> teacherCount = new HashMap<>();

        for (SelectedCourse course : selectedCourses) {
            totalCredits += course.getCredit();

            // 按课程类型统计
            typeCount.put(course.getType(), typeCount.getOrDefault(course.getType(), 0) + 1);

            // 按教师统计
            teacherCount.put(course.getTeacherName(), teacherCount.getOrDefault(course.getTeacherName(), 0) + 1);
        }

        // 构建结果字符串
        StringBuilder result = new StringBuilder();
        result.append("=== 选课结果汇总 ===\n\n");
        result.append("📊 基本信息：\n");
        result.append("课程总数：").append(totalCourses).append(" 门\n");
        result.append("总学分：").append(totalCredits).append(" 分\n\n");

        result.append("📈 课程类型分布：\n");
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            result.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" 门\n");
        }
        result.append("\n");

        result.append("👨‍🏫 教师课程分布：\n");
        for (Map.Entry<String, Integer> entry : teacherCount.entrySet()) {
            result.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" 门\n");
        }
        result.append("\n");

        result.append("📋 课程列表：\n");
        for (SelectedCourse course : selectedCourses) {
            result.append("- ").append(course.getName())
                    .append(" (").append(course.getType()).append(")")
                    .append(" - ").append(course.getTeacherName())
                    .append(" (").append(course.getCredit()).append("学分)\n");
        }

        // 使用多行文本的Alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("选课结果");
        alert.setHeaderText("您的选课结果如下：");
        alert.setContentText(result.toString());
        alert.getDialogPane().setPrefSize(500, 400);
        alert.showAndWait();
    }

    /**
     * 显示提示框
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 处理筛选条件变化
     */
    @FXML
    private void handleFilterChange() {
        if (toggleAll.isSelected()) {
            filterStatus = 0;
        } else if (toggleAvailable.isSelected()) {
            filterStatus = 1;
        } else if (toggleSelected.isSelected()) {
            filterStatus = 2;
        }
        filterCourses();
    }

    /**
     * 保存选课到服务器（已废弃）
     * 改为在提交选课时统一处理
     */
    private void saveSelectionToServer(Course course) {
        // 此方法已废弃，选课改为前端乐观更新，提交时统一同步
        System.out.println("⚠️ saveSelectionToServer 已废弃，由 submitSelectionToServer 统一处理");
    }

    /**
     * 从服务器移除选课（后端API暂未实现）
     * 退选操作改为前端乐观更新，提交选课时统一同步到后端
     */
    private boolean removeSelectionFromServer(SelectedCourse course) {
        // 后端 /api/student/cancelCourse 接口尚未实现
        // 暂时返回true，让前端流程继续
        System.out.println("️ 退课API未实现，将由提交选课时统一处理: " + course.getName());
        return true;
    }

    /**
     * 提交选课到服务器
     */
    private boolean submitSelectionToServer() {
        try {
            DataRequest req = new DataRequest();

            // 提交当前已选的课程ID列表
            List<Integer> courseIds = new ArrayList<>();
            for (SelectedCourse course : selectedCourses) {
                courseIds.add(course.getId());
            }

            // 尝试多种参数名
            req.add("courseIds", courseIds);
            req.add("ids", courseIds);
            req.add("courseIdList", courseIds);
            req.add("studentId", AppStore.getJwt().getId());
            req.add("studentName", AppStore.getJwt().getUsername());

            System.out.println("📤 提交选课请求: 共" + selectedCourses.size() + "门课程, courseIds=" + courseIds);

            DataResponse res = HttpRequestUtil.request("/api/student/submitSelections", req);

            System.out.println("📥 提交选课响应: code=" + (res != null ? res.getCode() : "null") +
                    ", msg=" + (res != null ? res.getMsg() : "null"));

            if (res != null && res.getCode() == 0) {
                System.out.println("✅ 提交选课成功");
                return true;
            } else {
                String errorMsg = res != null ? res.getMsg() : "未知错误";
                System.out.println("⚠️ 提交选课失败: " + errorMsg);
                showAlert(Alert.AlertType.WARNING, "提交失败",
                        "提交选课时出错：" + errorMsg + "\n请检查网络连接或联系管理员");
                return false;
            }
        } catch (Exception e) {
            System.out.println("⚠️ 提交选课请求异常: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "错误", "提交选课时发生异常：" + e.getMessage());
            return false;
        }
    }
}

