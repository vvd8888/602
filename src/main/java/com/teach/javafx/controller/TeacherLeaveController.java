package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 教师端 - 请假审批控制器
 * 对应 teacher-leave-panel.fxml
 */
public class TeacherLeaveController extends ToolController {
    // ====================== TableView 表格 ======================
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map, String> studentNumColumn;
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    @FXML
    private TableColumn<Map, String> teacherNameColumn;
    @FXML
    private TableColumn<Map, String> leaveDateColumn;
    @FXML
    private TableColumn<Map, String> reasonColumn;
    @FXML
    private TableColumn<Map, String> stateNameColumn;
    @FXML
    private TableColumn<Map, String> teacherCommentColumn;
    @FXML
    private TableColumn<Map, String> adminCommentColumn;

    // ====================== 表单输入框 ======================
    @FXML
    private TextField studentNumField;
    @FXML
    private TextField studentNameField;
    // ✅ 替换为普通文本框
    @FXML
    private TextField teacherNameField;
    @FXML
    private TextField leaveDateField;
    @FXML
    private TextField reasonField;
    @FXML
    private TextField teacherCommentField;
    @FXML
    private TextField adminCommentField;

    // ====================== 查询区域 ======================
    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<OptionItem> stateComboBox;

    // ====================== 按钮 ======================
    @FXML
    private Button passButton;
    @FXML
    private Button notPassButton;

    // ====================== 内部变量 ======================
    private Integer studentLeaveId = null;
    private ArrayList<Map> studentLeaveList = new ArrayList();
    private List<OptionItem> stateList;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    // ====================== 初始化 ======================
    @FXML
    public void initialize() {
        // 表格列绑定
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        teacherNameColumn.setCellValueFactory(new MapValueFactory<>("teacherName"));
        leaveDateColumn.setCellValueFactory(new MapValueFactory<>("leaveDate"));
        reasonColumn.setCellValueFactory(new MapValueFactory<>("reason"));
        stateNameColumn.setCellValueFactory(new MapValueFactory<>("stateName"));
        teacherCommentColumn.setCellValueFactory(new MapValueFactory<>("teacherComment"));
        adminCommentColumn.setCellValueFactory(new MapValueFactory<>("adminComment"));

        // 表格选中监听
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);

        // 状态下拉框
        stateList = HttpRequestUtil.getDictionaryOptionItemList("SHZTM");
        stateList.addFirst(new OptionItem(-1, "-1", "请选择..."));
        stateComboBox.getItems().addAll(stateList);

        // 权限设置
        if (adminCommentField != null) {
            adminCommentField.setDisable(true);
        }
        if (teacherCommentField != null) {
            teacherCommentField.setDisable(false);
        }

        // 加载数据
        onQueryButtonClick();
    }

    // ====================== 清空表单 ======================
    public void clearPanel() {
        studentLeaveId = null;
        studentNumField.setText("");
        studentNameField.setText("");
        teacherNameField.setText("");  // ✅ 清空老师姓名
        leaveDateField.setText("");
        reasonField.setText("");
        teacherCommentField.setText("");
        adminCommentField.setText("");
    }

    // ====================== 选中表格显示详情 ======================
    protected void changeStudentInfo() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        studentLeaveId = CommonMethod.getInteger(form, "studentLeaveId");
        studentNumField.setText(CommonMethod.getString(form, "studentNum"));
        studentNameField.setText(CommonMethod.getString(form, "studentName"));
        // ✅ 直接显示指导老师姓名，和学号姓名逻辑一致
        teacherNameField.setText(CommonMethod.getString(form, "teacherName"));

        leaveDateField.setText(CommonMethod.getString(form, "leaveDate"));
        reasonField.setText(CommonMethod.getString(form, "reason"));
        teacherCommentField.setText(CommonMethod.getString(form, "teacherComment"));
        adminCommentField.setText(CommonMethod.getString(form, "adminComment"));
    }

    // 表格选择事件
    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeStudentInfo();
    }

    // ====================== 查询按钮 ======================
    @FXML
    protected void onQueryButtonClick() {
        String search = searchTextField.getText();
        DataRequest req = new DataRequest();
        OptionItem op = stateComboBox.getSelectionModel().getSelectedItem();
        if (op != null) {
            req.add("state", Integer.parseInt(op.getValue()));
        }
        req.add("search", search);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/getStudentLeaveList", req);
        if (res != null && res.getCode() == 0) {
            studentLeaveList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    // 刷新表格
    private void setTableViewData() {
        observableList.clear();
        observableList.addAll(studentLeaveList);
        dataTableView.setItems(observableList);
    }

    // ====================== 审核功能 ======================
    @FXML
    protected void onPassButtonClick() {
        doCheck(2); // 2=审核通过
    }

    @FXML
    protected void onNotPassButtonClick() {
        doCheck(3); // 3=审核不通过
    }

    // 执行审核
    protected void doCheck(Integer state) {
        if (studentLeaveId == null) {
            com.teach.javafx.controller.base.MessageDialog.showDialog("请选择一条请假记录！");
            return;
        }
        DataRequest req = new DataRequest();
        req.add("studentLeaveId", studentLeaveId);
        req.add("teacherComment", teacherCommentField.getText());
        req.add("state", state);
        DataResponse res = HttpRequestUtil.request("/api/studentLeave/studentLeaveCheck", req);
        if (res.getCode() == 0) {
            com.teach.javafx.controller.base.MessageDialog.showDialog("审核成功！");
            onQueryButtonClick();
        } else {
            com.teach.javafx.controller.base.MessageDialog.showDialog(res.getMsg());
        }
    }
}