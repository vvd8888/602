package com.teach.javafx.controller.base;

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
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PracticeManageController extends ToolController {

    // 立项审核表格
    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, String> projectTypeColumn;
    @FXML private TableColumn<Map, String> leaderColumn;
    @FXML private TableColumn<Map, String> statusColumn;
    @FXML private TableColumn<Map, String> submitTimeColumn;

    @FXML private ComboBox<OptionItem> typeComboBox;
    @FXML private ComboBox<OptionItem> statusComboBox;

    // 审核详情
    @FXML private VBox reviewDetailBox;
    @FXML private Label reviewTitleLabel;
    @FXML private Label reviewTypeLabel;
    @FXML private Label reviewLeaderLabel;
    @FXML private Label reviewMentorLabel;
    @FXML private Label reviewMembersLabel;
    @FXML private TextArea reviewDescArea;
    @FXML private TextArea reviewCommentArea;
    @FXML private TextArea reviewHistoryArea;
    @FXML private Button approveBtn;
    @FXML private Button rejectBtn;
    @FXML private Button progressBtn;
    @FXML private Button completeBtn;

    // 选题管理
    @FXML private TableView<Map> topicTableView;
    @FXML private TableColumn<Map, String> topicTitleColumn;
    @FXML private TableColumn<Map, String> topicStatusColumn;
    @FXML private TableColumn<Map, String> topicTimeColumn;
    @FXML private ToggleButton allTopicBtn;
    @FXML private ToggleButton draftTopicBtn;
    @FXML private ToggleButton publishedTopicBtn;
    @FXML private ToggleButton withdrawnTopicBtn;

    @FXML private VBox topicEditBox;
    @FXML private Label topicEditTitleLabel;
    @FXML private TextField topicTitleField;
    @FXML private TextArea topicDescArea;
    @FXML private TextArea topicReqArea;
    @FXML private Button topicDraftBtn;
    @FXML private Button topicPublishBtn;
    @FXML private Button topicWithdrawBtn;

    private ArrayList<Map> projectList = new ArrayList<>();
    private ObservableList<Map> obsProjectList = FXCollections.observableArrayList();
    private ArrayList<Map> topicList = new ArrayList<>();
    private ObservableList<Map> obsTopicList = FXCollections.observableArrayList();
    private ToggleGroup topicGroup;
    private boolean refreshing = false;

    private Integer selectedProjectId = null;
    private Integer selectedTopicId = null;

    @FXML
    public void initialize() {
        // 立项审核表格
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        projectTypeColumn.setCellValueFactory(new MapValueFactory<>("projectType"));
        leaderColumn.setCellValueFactory(new MapValueFactory<>("leaderName"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("statusDisplay"));
        submitTimeColumn.setCellValueFactory(new MapValueFactory<>("submitTime"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        tsm.getSelectedIndices().addListener(this::onProjectRowSelect);

        typeComboBox.getItems().addAll(
                new OptionItem(null, "", "全部类型"),
                new OptionItem(null, "NORMAL", "普通立项"),
                new OptionItem(null, "KEY", "重点立项"));
        typeComboBox.getSelectionModel().select(0);

        statusComboBox.getItems().addAll(
                new OptionItem(null, "", "全部状态"),
                new OptionItem(null, "SUBMITTED", "待审核"),
                new OptionItem(null, "APPROVED", "已通过"),
                new OptionItem(null, "IN_PROGRESS", "进行中"),
                new OptionItem(null, "COMPLETED", "已完成"),
                new OptionItem(null, "SUMMARY_SUBMITTED", "待审核总结"),
                new OptionItem(null, "FINISHED", "已结束"));
        statusComboBox.getSelectionModel().select(0);

        // 选题管理
        topicTitleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        topicStatusColumn.setCellValueFactory(new MapValueFactory<>("status"));
        topicTimeColumn.setCellValueFactory(new MapValueFactory<>("createTime"));

        TableView.TableViewSelectionModel<Map> topicTsm = topicTableView.getSelectionModel();
        topicTsm.getSelectedIndices().addListener(this::onTopicRowSelect);

        topicGroup = new ToggleGroup();
        allTopicBtn.setToggleGroup(topicGroup);
        draftTopicBtn.setToggleGroup(topicGroup);
        publishedTopicBtn.setToggleGroup(topicGroup);
        withdrawnTopicBtn.setToggleGroup(topicGroup);

        clearReviewDetail();
        clearTopicEdit();
        loadProjectList();
        loadTopicList();
    }

    // ===== 立项审核 =====

    @FXML
    protected void onFilterChanged() {
        loadProjectList();
        clearReviewDetail();
    }

    @FXML
    protected void onRefreshButtonClick() {
        loadProjectList();
        loadTopicList();
    }

    private void loadProjectList() {
        refreshing = true;
        DataRequest req = new DataRequest();
        OptionItem typeItem = typeComboBox.getSelectionModel().getSelectedItem();
        OptionItem statusItem = statusComboBox.getSelectionModel().getSelectedItem();
        req.add("projectType", typeItem != null ? typeItem.getValue() : "");
        req.add("status", statusItem != null ? statusItem.getValue() : "");

        DataResponse res = HttpRequestUtil.request("/api/practice/getReviewProjectList", req);
        if (res != null && res.getCode() == 0) {
            projectList = (ArrayList<Map>) res.getData();
            if (projectList == null) projectList = new ArrayList<>();
            for (Map m : projectList) {
                String pt = CommonMethod.getString(m, "projectType");
                m.put("projectType", "KEY".equals(pt) ? "重点" : "普通");
            }
            obsProjectList.clear();
            obsProjectList.addAll(projectList);
            dataTableView.setItems(obsProjectList);
        }
        refreshing = false;
    }

    public void onProjectRowSelect(ListChangeListener.Change<? extends Integer> change) {
        if (refreshing) return;
        Map row = dataTableView.getSelectionModel().getSelectedItem();
        if (row == null) {
            clearReviewDetail();
            return;
        }
        selectedProjectId = CommonMethod.getInteger(row, "projectId");
        loadProjectDetail(selectedProjectId);
    }

    private void loadProjectDetail(Integer projectId) {
        DataRequest req = new DataRequest();
        req.add("projectId", projectId);
        DataResponse res = HttpRequestUtil.request("/api/practice/getProjectDetail", req);
        if (res == null || res.getCode() != 0) return;

        Map<String, Object> data = (Map<String, Object>) res.getData();
        reviewTitleLabel.setText(CommonMethod.getString(data, "title"));
        reviewTypeLabel.setText("类型：" + ("KEY".equals(CommonMethod.getString(data, "projectType")) ? "重点立项" : "普通立项"));
        reviewLeaderLabel.setText("队长：" + CommonMethod.getString(data, "leaderName"));
        String mentor = CommonMethod.getString(data, "mentor");
        reviewMentorLabel.setText("指导老师：" + (mentor != null && !mentor.isEmpty() ? mentor : "无"));
        reviewMembersLabel.setText("成员：" + CommonMethod.getString(data, "memberNames"));
        reviewDescArea.setText(CommonMethod.getString(data, "description"));
        reviewCommentArea.clear();

        // 审核记录
        List<Map> reviews = (List<Map>) data.get("reviews");
        StringBuilder sb = new StringBuilder();
        if (reviews != null && !reviews.isEmpty()) {
            for (Map r : reviews) {
                sb.append("[").append(CommonMethod.getString(r, "reviewType"))
                        .append("] ").append(CommonMethod.getString(r, "reviewerName"))
                        .append(" - ").append(CommonMethod.getString(r, "reviewResult"))
                        .append(" (").append(CommonMethod.getString(r, "reviewTime")).append(")\n");
                String comment = CommonMethod.getString(r, "comment");
                if (comment != null && !comment.isEmpty())
                    sb.append("  ").append(comment).append("\n");
            }
        } else {
            sb.append("暂无审核记录");
        }
        reviewHistoryArea.setText(sb.toString());

        String status = CommonMethod.getString(data, "status");
        boolean isSubmitted = "SUBMITTED".equals(status);
        boolean isApproved = "APPROVED".equals(status);
        boolean isInProgress = "IN_PROGRESS".equals(status);
        boolean isSummarySubmitted = "SUMMARY_SUBMITTED".equals(status);

        approveBtn.setVisible(isSubmitted);
        rejectBtn.setVisible(isSubmitted);
        progressBtn.setVisible(isApproved);
        completeBtn.setVisible(isInProgress);

        reviewDetailBox.setVisible(true);
    }

    @FXML
    protected void onApproveButtonClick() {
        if (selectedProjectId == null) return;
        doReview("APPROVED");
    }

    @FXML
    protected void onRejectButtonClick() {
        if (selectedProjectId == null) return;
        if (reviewCommentArea.getText().trim().isEmpty()) {
            MessageDialog.showDialog("驳回时请输入审核意见");
            return;
        }
        doReview("REJECTED");
    }

    private void doReview(String result) {
        DataRequest req = new DataRequest();
        req.add("projectId", selectedProjectId);
        req.add("reviewResult", result);
        req.add("comment", reviewCommentArea.getText().trim());
        DataResponse res = HttpRequestUtil.request("/api/practice/reviewProject", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("APPROVED".equals(result) ? "已通过" : "已驳回");
            loadProjectList();
            clearReviewDetail();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onSetProgressButtonClick() {
        if (selectedProjectId == null) return;
        DataRequest req = new DataRequest();
        req.add("projectId", selectedProjectId);
        req.add("status", "IN_PROGRESS");
        DataResponse res = HttpRequestUtil.request("/api/practice/updateProjectStatus", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("已设为进行中");
            loadProjectList();
            loadProjectDetail(selectedProjectId);
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onSetCompleteButtonClick() {
        if (selectedProjectId == null) return;
        DataRequest req = new DataRequest();
        req.add("projectId", selectedProjectId);
        req.add("status", "COMPLETED");
        DataResponse res = HttpRequestUtil.request("/api/practice/updateProjectStatus", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("已设为已完成");
            loadProjectList();
            loadProjectDetail(selectedProjectId);
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    private void clearReviewDetail() {
        selectedProjectId = null;
        reviewTitleLabel.setText("选择左侧项目查看详情");
        reviewTypeLabel.setText("");
        reviewLeaderLabel.setText("");
        reviewMentorLabel.setText("");
        reviewMembersLabel.setText("");
        reviewDescArea.setText("");
        reviewCommentArea.setText("");
        reviewHistoryArea.setText("");
        approveBtn.setVisible(false);
        rejectBtn.setVisible(false);
        progressBtn.setVisible(false);
        completeBtn.setVisible(false);
    }

    // ===== 重点选题管理 =====

    @FXML
    protected void onTopicFilterChanged() { loadTopicList(); }

    private String getTopicFilter() {
        ToggleButton selected = (ToggleButton) topicGroup.getSelectedToggle();
        if (selected == draftTopicBtn) return "DRAFT";
        if (selected == publishedTopicBtn) return "PUBLISHED";
        if (selected == withdrawnTopicBtn) return "WITHDRAWN";
        return "";
    }

    private void loadTopicList() {
        DataRequest req = new DataRequest();
        req.add("status", getTopicFilter());
        DataResponse res = HttpRequestUtil.request("/api/practice/getKeyTopicList", req);
        if (res != null && res.getCode() == 0) {
            topicList = (ArrayList<Map>) res.getData();
            if (topicList == null) topicList = new ArrayList<>();
            obsTopicList.clear();
            obsTopicList.addAll(topicList);
            topicTableView.setItems(obsTopicList);
        }
    }

    public void onTopicRowSelect(ListChangeListener.Change<? extends Integer> change) {
        Map row = topicTableView.getSelectionModel().getSelectedItem();
        if (row == null) {
            clearTopicEdit();
            return;
        }
        selectedTopicId = CommonMethod.getInteger(row, "keyTopicId");
        String status = CommonMethod.getString(row, "status");
        topicEditBox.setVisible(true);
        topicEditBox.setManaged(true);
        topicTitleField.setText(CommonMethod.getString(row, "title"));
        topicDescArea.setText(CommonMethod.getString(row, "description"));
        topicReqArea.setText(CommonMethod.getString(row, "requirements"));

        boolean isDraft = "DRAFT".equals(status);
        boolean isPublished = "PUBLISHED".equals(status);
        topicDraftBtn.setVisible(isDraft);
        topicPublishBtn.setVisible(isDraft);
        topicWithdrawBtn.setVisible(isPublished);
        topicTitleField.setDisable(!isDraft);
        topicDescArea.setDisable(!isDraft);
        topicReqArea.setDisable(!isDraft);
        topicEditTitleLabel.setText(isDraft ? "编辑选题（草稿）" : "查看选题");
    }

    @FXML
    protected void onNewTopicClick() {
        selectedTopicId = null;
        clearTopicEdit();
        topicEditBox.setVisible(true);
        topicEditBox.setManaged(true);
        topicTitleField.setDisable(false);
        topicDescArea.setDisable(false);
        topicReqArea.setDisable(false);
        topicDraftBtn.setVisible(true);
        topicPublishBtn.setVisible(true);
        topicWithdrawBtn.setVisible(false);
        topicEditTitleLabel.setText("新建选题");
    }

    @FXML
    protected void onSaveTopicDraftButtonClick() { doSaveTopic("DRAFT"); }

    @FXML
    protected void onPublishTopicButtonClick() { doSaveTopic("PUBLISH"); }

    private void doSaveTopic(String action) {
        if (topicTitleField.getText().trim().isEmpty()) {
            MessageDialog.showDialog("标题不能为空");
            return;
        }
        DataRequest req = new DataRequest();
        req.add("keyTopicId", selectedTopicId);
        req.add("action", action);
        req.add("title", topicTitleField.getText().trim());
        req.add("description", topicDescArea.getText());
        req.add("requirements", topicReqArea.getText());

        DataResponse res = HttpRequestUtil.request("/api/practice/saveKeyTopic", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("PUBLISH".equals(action) ? "选题已发布" : "草稿已保存");
            clearTopicEdit();
            loadTopicList();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onWithdrawTopicButtonClick() {
        if (selectedTopicId == null) return;
        int ret = MessageDialog.choiceDialog("确认撤回该选题？");
        if (ret != MessageDialog.CHOICE_YES) return;

        DataRequest req = new DataRequest();
        req.add("keyTopicId", selectedTopicId);
        DataResponse res = HttpRequestUtil.request("/api/practice/withdrawKeyTopic", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("已撤回");
            clearTopicEdit();
            loadTopicList();
        }
    }

    private void clearTopicEdit() {
        selectedTopicId = null;
        topicTitleField.clear();
        topicDescArea.clear();
        topicReqArea.clear();
        topicTitleField.setDisable(true);
        topicDescArea.setDisable(true);
        topicReqArea.setDisable(true);
        topicDraftBtn.setVisible(false);
        topicPublishBtn.setVisible(false);
        topicWithdrawBtn.setVisible(false);
        topicEditBox.setVisible(false);
        topicEditBox.setManaged(false);
        topicEditTitleLabel.setText("编辑选题");
    }

    @Override
    public void doRefresh() {
        loadProjectList();
        loadTopicList();
    }
}
