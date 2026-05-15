package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.*;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TeacherController 教师管理控制类 对应 teacher-panel.fxml
 * 负责教师信息的增删改查
 */
public class TeacherController extends ToolController {

    // ========== 表格列 ==========
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> numColumn;      // 工号
    @FXML
    private TableColumn<Map, String> nameColumn;     // 姓名
    @FXML
    private TableColumn<Map, String> deptColumn;     // 院系
    @FXML
    private TableColumn<Map, String> titleColumn;    // 职称
    @FXML
    private TableColumn<Map, String> degreeColumn;   // 学历
    @FXML
    private TableColumn<Map, String> genderColumn;   // 性别
    @FXML
    private TableColumn<Map, String> phoneColumn;    // 电话
    @FXML
    private TableColumn<Map, String> emailColumn;    // 邮箱
    @FXML
    private TableColumn<Map, String> addressColumn;  // 地址

    // ========== 表单输入域 ==========
    @FXML
    private TextField numField;      // 工号
    @FXML
    private TextField nameField;     // 姓名
    @FXML
    private TextField deptField;     // 院系
    @FXML
    private TextField titleField;    // 职称
    @FXML
    private TextField degreeField;   // 学历
    @FXML
    private TextField cardField;     // 证件号码
    @FXML
    private TextField phoneField;    // 电话
    @FXML
    private TextField emailField;    // 邮箱
    @FXML
    private TextField addressField;  // 地址

    // ========== 查询和下拉框 ==========
    @FXML
    private TextField numNameTextField;  // 查询输入框
    @FXML
    private ComboBox<OptionItem> genderComboBox;  // 性别下拉框

    // ========== 新增：开设课程按钮 ==========
    @FXML
    private Button openCourseButton;  // 开设课程按钮

    // ========== 数据变量 ==========
    private Integer personId = null;
    private ArrayList<Map> teacherList = new ArrayList<>();
    private List<OptionItem> genderList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    // ========== 用户信息 ==========
    private String currentUserRole = "";
    private String currentUserName = "";

    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(teacherList);
        dataTableView.setItems(observableList);
    }

    @FXML
    public void initialize() {
        System.out.println("TeacherController 初始化...");

        // 加载用户信息
        loadUserInfo();

        // 初始化性别下拉框
        genderList = HttpRequestUtil.getDictionaryOptionItemList("XBM");
        if (genderList != null) {
            genderComboBox.getItems().addAll(genderList);
        }

        // 从后台获取教师列表
        DataRequest req = new DataRequest();
        req.add("numName", "");
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherList", req);
        if (res != null && res.getCode() == 0) {
            teacherList = (ArrayList<Map>) res.getData();
        }

        // 设置表格列映射
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        deptColumn.setCellValueFactory(new MapValueFactory<>("dept"));
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        degreeColumn.setCellValueFactory(new MapValueFactory<>("degree"));
        genderColumn.setCellValueFactory(new MapValueFactory<>("gender"));
        phoneColumn.setCellValueFactory(new MapValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new MapValueFactory<>("email"));
        addressColumn.setCellValueFactory(new MapValueFactory<>("address"));

        // 表格选中行监听
        dataTableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                changeTeacherInfo();
            }
        });

        // 根据用户角色控制按钮显示
        controlButtonByUserRole();

        setTableViewData();
    }

    /**
     * 加载用户信息
     */
    private void loadUserInfo() {
        try {
            // 尝试从全局应用获取当前用户信息
            currentUserRole = MainApplication.getCurrentUserRole();
            currentUserName = MainApplication.getCurrentUserName();

            System.out.println("当前用户: " + currentUserName + ", 角色: " + currentUserRole);

        } catch (Exception e) {
            System.out.println("获取用户信息失败: " + e.getMessage());
            // 默认为老师角色用于测试
            currentUserRole = "teacher";
            currentUserName = "测试老师";
        }
    }

    /**
     * 根据用户角色控制按钮显示
     */
    private void controlButtonByUserRole() {
        // 只有老师角色（"teacher" 或 "2"）才能看到"开设课程"按钮
        if (openCourseButton != null) {
            if ("teacher".equals(currentUserRole) || "2".equals(currentUserRole)) {
                openCourseButton.setVisible(true);
                openCourseButton.setManaged(true);
            } else {
                openCourseButton.setVisible(false);
                openCourseButton.setManaged(false);
            }
        }
    }

    public void clearPanel() {
        personId = null;
        numField.setText("");
        nameField.setText("");
        deptField.setText("");
        titleField.setText("");
        degreeField.setText("");
        cardField.setText("");
        genderComboBox.getSelectionModel().select(-1);
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
    }

    protected void changeTeacherInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherInfo", req);
        if (res.getCode() != 0) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        numField.setText(CommonMethod.getString(form, "num"));
        nameField.setText(CommonMethod.getString(form, "name"));
        deptField.setText(CommonMethod.getString(form, "dept"));
        titleField.setText(CommonMethod.getString(form, "title"));
        degreeField.setText(CommonMethod.getString(form, "degree"));
        cardField.setText(CommonMethod.getString(form, "card"));
        int genderIndex = CommonMethod.getOptionItemIndexByValue(genderList, CommonMethod.getString(form, "gender"));
        if (genderIndex >= 0) {
            genderComboBox.getSelectionModel().select(genderIndex);
        }
        phoneField.setText(CommonMethod.getString(form, "phone"));
        emailField.setText(CommonMethod.getString(form, "email"));
        addressField.setText(CommonMethod.getString(form, "address"));
    }

    @FXML
    protected void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherList", req);
        if (res != null && res.getCode() == 0) {
            teacherList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    @FXML
    protected void onAddButtonClick() {
        clearPanel();
    }

    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/teacher/teacherDelete", req);
        if (res != null) {
            if (res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
                onQueryButtonClick();
            } else {
                MessageDialog.showDialog(res.getMsg());
            }
        }
    }

    @FXML
    protected void onSaveButtonClick() {
        if (numField.getText().isEmpty()) {
            MessageDialog.showDialog("工号为空，不能保存");
            return;
        }
        Map<String, Object> form = new HashMap<>();
        form.put("num", numField.getText());
        form.put("name", nameField.getText());
        form.put("dept", deptField.getText());
        form.put("title", titleField.getText());
        form.put("degree", degreeField.getText());
        form.put("card", cardField.getText());
        if (genderComboBox.getSelectionModel().getSelectedItem() != null) {
            form.put("gender", genderComboBox.getSelectionModel().getSelectedItem().getValue());
        }
        form.put("phone", phoneField.getText());
        form.put("email", emailField.getText());
        form.put("address", addressField.getText());

        DataRequest req = new DataRequest();
        req.add("personId", personId);
        req.add("form", form);
        DataResponse res = HttpRequestUtil.request("/api/teacher/teacherEditSave", req);
        if (res.getCode() == 0) {
            personId = CommonMethod.getIntegerFromObject(res.getData());
            MessageDialog.showDialog("保存成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    /**
     * 开设课程按钮点击事件
     */
    @FXML
    protected void openCourseWindow() {
        try {
            System.out.println("🚀 正在打开老师开设课程界面...");

            // 检查用户角色
            if (!"teacher".equals(currentUserRole) && !"2".equals(currentUserRole)) {
                return;
            }

            // 检查 FXML 文件是否存在
            String fxmlPath = "/teacher-open-course.fxml";
            System.out.println("尝试加载 FXML: " + fxmlPath);

            // 加载老师开设课程界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 创建新窗口
            Stage stage = new Stage();
            stage.setTitle("老师开设课程 - " + currentUserName);
            stage.setScene(new Scene(root, 1200, 800));

            // 设置为模态窗口
            stage.initModality(Modality.APPLICATION_MODAL);

            // 显示窗口
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== ToolController 实现 ==========

    @Override
    public void doNew() {
        clearPanel();
    }

    @Override
    public void doSave() {
        onSaveButtonClick();
    }

    @Override
    public void doDelete() {
        onDeleteButtonClick();
    }

    @Override
    public void doExport() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        byte[] bytes = HttpRequestUtil.requestByteData("/api/teacher/getTeacherListExcl", req);
        if (bytes != null) {
            try {
                FileChooser fileDialog = new FileChooser();
                fileDialog.setTitle("请选择保存的文件");
                fileDialog.setInitialDirectory(new File("C:/"));
                fileDialog.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
                File file = fileDialog.showSaveDialog(null);
                if (file != null) {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(bytes);
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}