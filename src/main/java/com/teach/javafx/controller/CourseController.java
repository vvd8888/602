package com.teach.javafx.controller;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.cell.MapValueFactory;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

/**
 * CourseController 登录交互控制类 对应 course-panel.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class CourseController {
    @FXML
    private TableView<Map<String, Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> numColumn;
    @FXML
    private TableColumn<Map,String> nameColumn;
    @FXML
    private TableColumn<Map,String> creditColumn;
    @FXML
    private TableColumn<Map,String> preCourseColumn;

    // 新增字段列
    @FXML
    private TableColumn<Map, String> teacherColumn;
    @FXML
    private TableColumn<Map, String> timeColumn;
    @FXML
    private TableColumn<Map, String> classroomColumn;

    @FXML
    private TableColumn<Map,FlowPane> operateColumn;

    private List<Map<String,Object>> courseList = new ArrayList<>();  // 学生信息列表数据
    private final ObservableList<Map<String,Object>> observableList= FXCollections.observableArrayList();  // TableView渲染列表

    @FXML
    private void onQueryButtonClick(){
        DataResponse res;
        DataRequest req = new DataRequest();
        res = HttpRequestUtil.request("/api/course/getCourseList",req); //从后台获取所有学生信息列表集合
        if(res != null && res.getCode()== 0) {
            courseList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    private void setTableViewData() {
        observableList.clear();
        Map<String, Object> map;
        FlowPane flowPane;
        Button saveButton, editPreCourseButton, deleteButton;

        for (int j = 0; j < courseList.size(); j++) {
            map = courseList.get(j);
            flowPane = new FlowPane();
            flowPane.setHgap(10);
            flowPane.setAlignment(Pos.CENTER);

            saveButton = new Button("保存");
            saveButton.setId("save" + j);
            saveButton.setOnAction(e -> saveItem(((Button) e.getSource()).getId()));

            editPreCourseButton = new Button("编辑前序课");
            editPreCourseButton.setId("editPre" + j);
            editPreCourseButton.setOnAction(e -> editPreCourse(((Button) e.getSource()).getId()));

            deleteButton = new Button("删除");
            deleteButton.setId("delete" + j);
            deleteButton.setOnAction(e -> deleteItem(((Button) e.getSource()).getId()));

            flowPane.getChildren().addAll(saveButton, editPreCourseButton, deleteButton);
            map.put("operate", flowPane);
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }

    public void saveItem(String name){
        if(name == null)
            return;
        int j = Integer.parseInt(name.substring(4));
        Map<String,Object> data = courseList.get(j);

        // 构建保存请求 - 使用DataRequest的add方法
        DataRequest req = new DataRequest();
        req.add("courseId", data.get("courseId"));
        req.add("num", data.get("num"));
        req.add("name", data.get("name"));
        req.add("credit", data.get("credit"));
        req.add("preCourseId", data.get("preCourseId"));
        req.add("coursePath", data.get("coursePath"));
        req.add("teacher", data.get("teacher"));
        req.add("time", data.get("time"));
        req.add("classroom", data.get("classroom"));

        DataResponse res = HttpRequestUtil.request("/api/course/courseSave", req);
        if(res != null && res.getCode() == 0) {
            System.out.println("保存成功: " + data);
        } else {
            System.out.println("保存失败: " + (res != null ? res.getMsg() : "无响应"));
        }
    }

    public void deleteItem(String name){
        if(name == null)
            return;
        int j = Integer.parseInt(name.substring(6));
        Map<String,Object> data = courseList.get(j);

        // 构建删除请求 - 使用DataRequest的add方法
        DataRequest req = new DataRequest();
        req.add("courseId", data.get("courseId"));

        DataResponse res = HttpRequestUtil.request("/api/course/courseDelete", req);
        if(res != null && res.getCode() == 0) {
            System.out.println("删除成功: " + data);
            onQueryButtonClick(); // 刷新列表
        } else {
            System.out.println("删除失败: " + (res != null ? res.getMsg() : "无响应"));
        }
    }

    // 编辑前序课的方法
    private void editPreCourse(String name) {
        if (name == null) return;
        int j = Integer.parseInt(name.substring(7));
        Map<String, Object> data = courseList.get(j);

        // 创建对话框编辑前序课
        TextInputDialog dialog = new TextInputDialog(data.get("preCourse") != null ? data.get("preCourse").toString() : "");
        dialog.setTitle("编辑前序课");
        dialog.setHeaderText("请输入前序课的名称或编号");
        dialog.setContentText("前序课:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPreCourse -> {
            data.put("preCourse", newPreCourse);
            observableList.set(j, data);
        });
    }

    @FXML
    public void initialize() {
        // 设置原有列
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        numColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        numColumn.setOnEditCommit(event -> {
            Map<String,Object> map = event.getRowValue();
            map.put("num", event.getNewValue());
        });

        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("name", event.getNewValue());
        });

        creditColumn.setCellValueFactory(new MapValueFactory<>("credit"));
        creditColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        creditColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("credit", event.getNewValue());
        });

        preCourseColumn.setCellValueFactory(new MapValueFactory<>("preCourse"));
        preCourseColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        preCourseColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("preCourse", event.getNewValue());
        });

        // 设置新增列
        teacherColumn.setCellValueFactory(new MapValueFactory<>("teacher"));
        teacherColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        teacherColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("teacher", event.getNewValue());
        });

        timeColumn.setCellValueFactory(new MapValueFactory<>("time"));
        timeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        timeColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("time", event.getNewValue());
        });

        classroomColumn.setCellValueFactory(new MapValueFactory<>("classroom"));
        classroomColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        classroomColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("classroom", event.getNewValue());
        });

        operateColumn.setCellValueFactory(new MapValueFactory<>("operate"));
        dataTableView.setEditable(true);
        onQueryButtonClick();
    }
}
//差一点点