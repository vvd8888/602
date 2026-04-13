package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.*;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
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

    // ========== 数据变量 ==========
    private Integer personId = null;
    private ArrayList<Map> teacherList = new ArrayList<>();
    private List<OptionItem> genderList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(teacherList);
        dataTableView.setItems(observableList);
    }

    @FXML
    public void initialize() {
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

        setTableViewData();
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