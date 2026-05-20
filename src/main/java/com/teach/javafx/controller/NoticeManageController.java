package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * NoticeManageController 管理员/教师端通知管理
 */
public class NoticeManageController extends ToolController {

    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, String> noticeTypeColumn;
    @FXML private TableColumn<Map, String> statusColumn;
    @FXML private TableColumn<Map, String> publishTimeColumn;
    @FXML private TableColumn<Map, String> readInfoColumn;

    @FXML private ToggleButton allStatusBtn;
    @FXML private ToggleButton draftStatusBtn;
    @FXML private ToggleButton publishedStatusBtn;
    @FXML private ToggleButton withdrawnStatusBtn;

    @FXML private Label editTitleLabel;
    @FXML private TextField titleField;
    @FXML private ComboBox<OptionItem> typeComboBox;
    @FXML private CheckBox allStudentCheckBox;
    @FXML private ComboBox<OptionItem> classComboBox;
    @FXML private TextField personIdField;
    @FXML private Label targetSummaryLabel;
    @FXML private TextArea contentArea;

    @FXML private Button draftBtn;
    @FXML private Button publishBtn;
    @FXML private Button withdrawBtn;
    @FXML private Button readStatusBtn;

    private ArrayList<Map> noticeList = new ArrayList<>();
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private ToggleGroup statusGroup;

    private Integer editingNoticeId = null;
    private String editingStatus = null;

    // 暂存的目标列表
    private List<Map<String, String>> currentTargets = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        noticeTypeColumn.setCellValueFactory(new MapValueFactory<>("noticeType"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("status"));
        publishTimeColumn.setCellValueFactory(new MapValueFactory<>("publishTime"));
        readInfoColumn.setCellValueFactory(new MapValueFactory<>("readInfo"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        tsm.getSelectedIndices().addListener(this::onTableRowSelect);

        // 状态筛选按钮组
        statusGroup = new ToggleGroup();
        allStatusBtn.setToggleGroup(statusGroup);
        draftStatusBtn.setToggleGroup(statusGroup);
        publishedStatusBtn.setToggleGroup(statusGroup);
        withdrawnStatusBtn.setToggleGroup(statusGroup);

        // 类型下拉
        typeComboBox.getItems().addAll(
                new OptionItem(null, "SYSTEM", "系统通知"),
                new OptionItem(null, "COURSE", "课程通知"),
                new OptionItem(null, "ACTIVITY", "活动通知"));
        typeComboBox.getSelectionModel().select(0);

        // 加载班级列表
        loadClassNames();

        // 初始状态
        allStudentCheckBox.setSelected(true);
        updateTargetSummary();
        clearEditForm();
        loadNotices();
    }

    private void loadClassNames() {
        DataRequest req = new DataRequest();
        req.add("numName", "");
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentList", req);
        if (res != null && res.getCode() == 0) {
            List<Map> students = (List<Map>) res.getData();
            classNames = students.stream()
                    .map(s -> CommonMethod.getString(s, "className"))
                    .filter(c -> c != null && !c.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            classComboBox.getItems().clear();
            for (String cn : classNames) {
                classComboBox.getItems().add(new OptionItem(null, cn, cn));
            }
        }
    }

    // ===== 列表 =====

    private String getStatusFilter() {
        ToggleButton selected = (ToggleButton) statusGroup.getSelectedToggle();
        if (selected == draftStatusBtn) return "DRAFT";
        if (selected == publishedStatusBtn) return "PUBLISHED";
        if (selected == withdrawnStatusBtn) return "WITHDRAWN";
        return "";
    }

    @FXML
    protected void onStatusFilterChanged() { loadNotices(); }

    @FXML
    protected void onRefreshButtonClick() { loadNotices(); }

    private void loadNotices() {
        DataRequest req = new DataRequest();
        req.add("status", getStatusFilter());
        DataResponse res = HttpRequestUtil.request("/api/notice/getMyNoticeList", req);
        if (res != null && res.getCode() == 0) {
            noticeList = (ArrayList<Map>) res.getData();
            if (noticeList == null) noticeList = new ArrayList<>();
            for (Map m : noticeList) {
                m.put("readInfo", m.get("readCount") + " / " + m.get("targetCount"));
                String st = CommonMethod.getString(m, "status");
                switch (st) {
                    case "DRAFT" -> m.put("status", "草稿");
                    case "PUBLISHED" -> m.put("status", "已发布");
                    case "WITHDRAWN" -> m.put("status", "已撤回");
                }
            }
            observableList.clear();
            observableList.addAll(noticeList);
            dataTableView.setItems(observableList);
        }
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        Map<String, Object> row = dataTableView.getSelectionModel().getSelectedItem();
        if (row == null) return;
        Integer noticeId = CommonMethod.getInteger(row, "noticeId");
        loadNoticeForEdit(noticeId);
    }

    private void loadNoticeForEdit(Integer noticeId) {
        DataRequest req = new DataRequest();
        req.add("status", "");
        DataResponse res = HttpRequestUtil.request("/api/notice/getMyNoticeList", req);
        if (res == null || res.getCode() != 0) return;
        List<Map> all = (List<Map>) res.getData();
        Map<String, Object> target = null;
        for (Map m : all) {
            if (noticeId.equals(CommonMethod.getInteger(m, "noticeId"))) {
                target = m;
                break;
            }
        }
        if (target == null) return;

        editingNoticeId = noticeId;
        editingStatus = CommonMethod.getString(target, "status");
        titleField.setText(CommonMethod.getString(target, "title"));
        contentArea.setText(""); // 详情需要单独请求
        String noticeType = CommonMethod.getString(target, "noticeType");
        typeComboBox.getSelectionModel().select(
                CommonMethod.getOptionItemIndexByValue(typeComboBox.getItems(), noticeType));

        // 加载完整详情（含 content）
        DataRequest detailReq = new DataRequest();
        detailReq.add("noticeId", noticeId);
        DataResponse detailRes = HttpRequestUtil.request("/api/notice/getNoticeDetail", detailReq);
        if (detailRes != null && detailRes.getCode() == 0) {
            Map<String, Object> detail = (Map<String, Object>) detailRes.getData();
            contentArea.setText(CommonMethod.getString(detail, "content"));
        }

        boolean isDraft = "DRAFT".equals(editingStatus);
        boolean isPublished = "PUBLISHED".equals(editingStatus);
        titleField.setDisable(!isDraft);
        typeComboBox.setDisable(!isDraft);
        contentArea.setDisable(!isDraft);
        allStudentCheckBox.setDisable(!isDraft);
        classComboBox.setDisable(!isDraft);
        personIdField.setDisable(!isDraft);
        draftBtn.setVisible(isDraft);
        publishBtn.setVisible(isDraft);
        withdrawBtn.setVisible(isPublished);
        readStatusBtn.setVisible(isPublished);
        editTitleLabel.setText(isDraft ? "编辑通知（草稿）" : "查看通知");
    }

    // ===== 编辑操作 =====

    @FXML
    protected void onNewButtonClick() {
        editingNoticeId = null;
        editingStatus = null;
        clearEditForm();
        titleField.setDisable(false);
        typeComboBox.setDisable(false);
        contentArea.setDisable(false);
        allStudentCheckBox.setDisable(false);
        classComboBox.setDisable(false);
        personIdField.setDisable(false);
        draftBtn.setVisible(true);
        publishBtn.setVisible(true);
        withdrawBtn.setVisible(false);
        readStatusBtn.setVisible(false);
        editTitleLabel.setText("新建通知");
    }

    @FXML
    protected void onAddClassTarget() {
        OptionItem selected = classComboBox.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null) {
            MessageDialog.showDialog("请先选择班级");
            return;
        }
        String cn = selected.getValue();
        boolean exists = currentTargets.stream()
                .anyMatch(t -> "CLASS".equals(t.get("targetType")) && cn.equals(t.get("targetValue")));
        if (!exists) {
            Map<String, String> t = new HashMap<>();
            t.put("targetType", "CLASS");
            t.put("targetValue", cn);
            currentTargets.add(t);
        }
        updateTargetSummary();
    }

    @FXML
    protected void onAddPersonTarget() {
        String pid = personIdField.getText().trim();
        if (pid.isEmpty()) {
            MessageDialog.showDialog("请输入人员ID");
            return;
        }
        boolean exists = currentTargets.stream()
                .anyMatch(t -> "PERSON".equals(t.get("targetType")) && pid.equals(t.get("targetValue")));
        if (!exists) {
            Map<String, String> t = new HashMap<>();
            t.put("targetType", "PERSON");
            t.put("targetValue", pid);
            currentTargets.add(t);
        }
        personIdField.clear();
        updateTargetSummary();
    }

    private void updateTargetSummary() {
        if (allStudentCheckBox.isSelected()) {
            targetSummaryLabel.setText("当前目标：全体学生" +
                    (currentTargets.isEmpty() ? "" : " + " + currentTargets.size() + "个额外目标"));
        } else if (currentTargets.isEmpty()) {
            targetSummaryLabel.setText("⚠ 请至少添加一个目标或勾选全体学生");
        } else {
            StringBuilder sb = new StringBuilder("目标：");
            for (Map<String, String> t : currentTargets) {
                if ("CLASS".equals(t.get("targetType")))
                    sb.append(" [班级:").append(t.get("targetValue")).append("]");
                else
                    sb.append(" [人员ID:").append(t.get("targetValue")).append("]");
            }
            targetSummaryLabel.setText(sb.toString());
        }
    }

    private List<Map<String, String>> buildTargets() {
        List<Map<String, String>> targets = new ArrayList<>(currentTargets);
        if (allStudentCheckBox.isSelected()) {
            Map<String, String> all = new HashMap<>();
            all.put("targetType", "ALL");
            all.put("targetValue", "");
            targets.add(all);
        }
        return targets;
    }

    private void doSave(String action) {
        if (titleField.getText().trim().isEmpty()) {
            MessageDialog.showDialog("标题不能为空");
            return;
        }
        List<Map<String, String>> targets = buildTargets();
        if (targets.isEmpty()) {
            MessageDialog.showDialog("请选择至少一个发送目标");
            return;
        }

        DataRequest req = new DataRequest();
        req.add("noticeId", editingNoticeId);
        req.add("title", titleField.getText().trim());
        req.add("content", contentArea.getText());
        OptionItem typeItem = typeComboBox.getSelectionModel().getSelectedItem();
        req.add("noticeType", typeItem != null ? typeItem.getValue() : "SYSTEM");
        req.add("action", action);
        req.add("targets", targets);

        DataResponse res = HttpRequestUtil.request("/api/notice/saveNotice", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog(action.equals("DRAFT") ? "草稿已保存" : "通知已发布");
            clearEditForm();
            loadNotices();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onSaveDraftButtonClick() { doSave("DRAFT"); }

    @FXML
    protected void onPublishButtonClick() { doSave("PUBLISH"); }

    @FXML
    protected void onWithdrawButtonClick() {
        if (editingNoticeId == null) {
            MessageDialog.showDialog("请先选择一条已发布的通知");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认撤回该通知？撤回后学生将不可见");
        if (ret != MessageDialog.CHOICE_YES) return;

        DataRequest req = new DataRequest();
        req.add("noticeId", editingNoticeId);
        DataResponse res = HttpRequestUtil.request("/api/notice/withdrawNotice", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("已撤回");
            clearEditForm();
            loadNotices();
        }
    }

    @FXML
    protected void onViewReadStatusButtonClick() {
        if (editingNoticeId == null) {
            MessageDialog.showDialog("请先选择一条已发布的通知");
            return;
        }
        DataRequest req = new DataRequest();
        req.add("noticeId", editingNoticeId);
        DataResponse res = HttpRequestUtil.request("/api/notice/getReadStatus", req);
        if (res == null || res.getCode() != 0) return;

        Map<String, Object> data = (Map<String, Object>) res.getData();
        StringBuilder sb = new StringBuilder();
        sb.append("===== 已读情况 =====\n\n");
        sb.append("已读：").append(data.get("readCount")).append(" 人\n");
        sb.append("未读：").append(data.get("unreadCount")).append(" 人\n");
        sb.append("已读率：").append(CommonMethod.getString(data, "readRate")).append("\n\n");

        sb.append("--- 已读 ---\n");
        List<Map> readList = (List<Map>) data.get("readList");
        if (readList != null && !readList.isEmpty()) {
            for (Map r : readList) {
                sb.append("  ").append(CommonMethod.getString(r, "name"))
                        .append("（").append(CommonMethod.getString(r, "className")).append("）")
                        .append("  ").append(CommonMethod.getString(r, "readTime")).append("\n");
            }
        } else {
            sb.append("  暂无\n");
        }

        sb.append("\n--- 未读 ---\n");
        List<Map> unreadList = (List<Map>) data.get("unreadList");
        if (unreadList != null && !unreadList.isEmpty()) {
            for (Map r : unreadList) {
                sb.append("  ").append(CommonMethod.getString(r, "name"))
                        .append("（").append(CommonMethod.getString(r, "className")).append("）\n");
            }
        } else {
            sb.append("  暂无\n");
        }

        // 用原生 Alert 显示，可以滚动
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("已读详情");
        alert.setHeaderText("通知已读情况");
        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefSize(500, 400);
        alert.getDialogPane().setContent(ta);
        alert.showAndWait();
    }

    private void clearEditForm() {
        editingNoticeId = null;
        editingStatus = null;
        titleField.clear();
        contentArea.clear();
        typeComboBox.getSelectionModel().select(0);
        allStudentCheckBox.setSelected(true);
        currentTargets.clear();
        updateTargetSummary();
        personIdField.clear();
        editTitleLabel.setText("编辑通知");
        titleField.setDisable(true);
        typeComboBox.setDisable(true);
        contentArea.setDisable(true);
        allStudentCheckBox.setDisable(true);
        classComboBox.setDisable(true);
        personIdField.setDisable(true);
        draftBtn.setVisible(false);
        publishBtn.setVisible(false);
        withdrawBtn.setVisible(false);
        readStatusBtn.setVisible(false);
    }

    @Override
    public void doRefresh() { loadNotices(); }

    @Override
    public void doNew() { onNewButtonClick(); }
}
