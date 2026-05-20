package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PracticeNormalListController extends ToolController {

    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, String> teamNameColumn;
    @FXML private TableColumn<Map, String> mentorColumn;
    @FXML private TableColumn<Map, String> memberCountColumn;
    @FXML private TableColumn<Map, String> statusColumn;

    @FXML private ToggleButton allFilterBtn;
    @FXML private ToggleButton draftFilterBtn;
    @FXML private ToggleButton reviewFilterBtn;
    @FXML private ToggleButton progressFilterBtn;
    @FXML private ToggleButton summaryFilterBtn;
    @FXML private ToggleButton doneFilterBtn;

    @FXML private Label detailTitleLabel;
    @FXML private Label detailTeamLabel;
    @FXML private Label detailMentorLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailMembersLabel;
    @FXML private TextArea detailDescArea;
    @FXML private TextArea detailReviewArea;

    @FXML private Button editBtn;
    @FXML private Button submitBtn;
    @FXML private Button deleteBtn;
    @FXML private Button summaryBtn;

    private ArrayList<Map> projectList = new ArrayList<>();
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private ToggleGroup filterGroup;
    private boolean refreshing = false;
    private Map<String, Object> selectedProject = null;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        teamNameColumn.setCellValueFactory(new MapValueFactory<>("teamName"));
        mentorColumn.setCellValueFactory(new MapValueFactory<>("mentor"));
        memberCountColumn.setCellValueFactory(new MapValueFactory<>("memberCount"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("statusDisplay"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        tsm.getSelectedIndices().addListener(this::onTableRowSelect);

        filterGroup = new ToggleGroup();
        allFilterBtn.setToggleGroup(filterGroup);
        draftFilterBtn.setToggleGroup(filterGroup);
        reviewFilterBtn.setToggleGroup(filterGroup);
        progressFilterBtn.setToggleGroup(filterGroup);
        summaryFilterBtn.setToggleGroup(filterGroup);
        doneFilterBtn.setToggleGroup(filterGroup);

        clearDetail();
        loadList();
    }

    private String getFilter() {
        ToggleButton selected = (ToggleButton) filterGroup.getSelectedToggle();
        if (selected == draftFilterBtn) return "DRAFT_REJECTED";
        if (selected == reviewFilterBtn) return "SUBMITTED";
        if (selected == progressFilterBtn) return "ACTIVE";
        if (selected == summaryFilterBtn) return "SUMMARY_SUBMITTED";
        if (selected == doneFilterBtn) return "FINISHED";
        return "ALL";
    }

    @FXML
    protected void onFilterChanged() { loadList(); }

    @FXML
    protected void onRefreshButtonClick() { loadList(); }

    private void loadList() {
        refreshing = true;
        DataRequest req = new DataRequest();
        req.add("projectType", "NORMAL");
        DataResponse res = HttpRequestUtil.request("/api/practice/getMyProjectList", req);
        if (res != null && res.getCode() == 0) {
            projectList = (ArrayList<Map>) res.getData();
            if (projectList == null) projectList = new ArrayList<>();
            applyFilter();
            observableList.clear();
            observableList.addAll(projectList);
            dataTableView.setItems(observableList);
        }
        refreshing = false;
    }

    private void applyFilter() {
        String filter = getFilter();
        List<Map> filtered = new ArrayList<>();
        for (Map m : projectList) {
            String status = CommonMethod.getString(m, "status");
            boolean include = switch (filter) {
                case "DRAFT_REJECTED" -> "DRAFT".equals(status) || "REJECTED".equals(status);
                case "SUBMITTED" -> "SUBMITTED".equals(status);
                case "ACTIVE" -> "APPROVED".equals(status) || "IN_PROGRESS".equals(status) || "COMPLETED".equals(status);
                case "SUMMARY_SUBMITTED" -> "SUMMARY_SUBMITTED".equals(status);
                case "FINISHED" -> "FINISHED".equals(status);
                default -> true;
            };
            if (include) filtered.add(m);
        }
        projectList = new ArrayList<>(filtered);
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        if (refreshing) return;
        Map row = dataTableView.getSelectionModel().getSelectedItem();
        if (row == null) {
            clearDetail();
            return;
        }
        selectedProject = row;
        loadDetail(CommonMethod.getInteger(row, "projectId"));
    }

    private void loadDetail(Integer projectId) {
        DataRequest req = new DataRequest();
        req.add("projectId", projectId);
        DataResponse res = HttpRequestUtil.request("/api/practice/getProjectDetail", req);
        if (res == null || res.getCode() != 0) return;

        Map<String, Object> data = (Map<String, Object>) res.getData();
        detailTitleLabel.setText(CommonMethod.getString(data, "title"));
        String teamName = CommonMethod.getString(data, "teamName");
        detailTeamLabel.setText("团队：" + (teamName != null && !teamName.isEmpty() ? teamName : "未命名"));
        String mentor = CommonMethod.getString(data, "mentor");
        detailMentorLabel.setText("指导老师：" + (mentor != null && !mentor.isEmpty() ? mentor : "无"));
        detailStatusLabel.setText("状态：" + CommonMethod.getString(data, "statusDisplay"));
        detailDescArea.setText(CommonMethod.getString(data, "description"));
        detailMembersLabel.setText("成员：" + CommonMethod.getString(data, "memberNames"));

        // 审核记录
        List<Map> reviews = (List<Map>) data.get("reviews");
        StringBuilder reviewText = new StringBuilder();
        if (reviews != null && !reviews.isEmpty()) {
            for (Map r : reviews) {
                reviewText.append("[").append(CommonMethod.getString(r, "reviewType"))
                        .append("] ").append(CommonMethod.getString(r, "reviewerName"))
                        .append(" - ").append(CommonMethod.getString(r, "reviewResult"))
                        .append(" ").append(CommonMethod.getString(r, "reviewTime")).append("\n");
                String comment = CommonMethod.getString(r, "comment");
                if (comment != null && !comment.isEmpty())
                    reviewText.append("  意见：").append(comment).append("\n");
            }
        } else {
            reviewText.append("暂无审核记录");
        }
        detailReviewArea.setText(reviewText.toString());

        String status = CommonMethod.getString(data, "status");
        boolean isDraft = "DRAFT".equals(status);
        boolean isRejected = "REJECTED".equals(status);
        boolean isSubmitted = "SUBMITTED".equals(status);
        boolean isCompleted = "COMPLETED".equals(status) || "SUMMARY_SUBMITTED".equals(status);

        editBtn.setVisible(isDraft || isRejected);
        submitBtn.setVisible(isDraft || isRejected);
        deleteBtn.setVisible(isDraft);
        summaryBtn.setVisible(isCompleted);
    }

    @FXML
    protected void onNewButtonClick() {
        openEditDialog(null, "NORMAL", null);
    }

    @FXML
    protected void onEditButtonClick() {
        if (selectedProject == null) return;
        openEditDialog(CommonMethod.getInteger(selectedProject, "projectId"), "NORMAL", null);
    }

    @FXML
    protected void onSubmitButtonClick() {
        if (selectedProject == null) return;
        int ret = MessageDialog.choiceDialog("确认提交审核？提交后不可编辑");
        if (ret != MessageDialog.CHOICE_YES) return;
        doSubmit(CommonMethod.getInteger(selectedProject, "projectId"));
    }

    private void doSubmit(Integer projectId) {
        DataRequest req = new DataRequest();
        req.add("projectId", projectId);
        req.add("action", "SUBMIT");
        // 需要获取当前项目完整数据来提交——实际上后端 saveProject 支持只传 projectId + action
        // 但 saveProject 会覆盖其他字段，所以这里用单独的方法
        // 我们直接用空title提交——后端会校验...
        // 实际上我们直接用submitProject——但saveProject有status检查
        // 简单方案：load detail, then call save with all data and action=SUBMIT
        DataResponse detailRes = HttpRequestUtil.request("/api/practice/getProjectDetail", req);
        if (detailRes == null || detailRes.getCode() != 0) return;
        Map<String, Object> data = (Map<String, Object>) detailRes.getData();

        DataRequest saveReq = new DataRequest();
        saveReq.add("projectId", projectId);
        saveReq.add("action", "SUBMIT");
        saveReq.add("projectType", "NORMAL");
        saveReq.add("title", CommonMethod.getString(data, "title"));
        saveReq.add("description", CommonMethod.getString(data, "description"));
        saveReq.add("mentor", CommonMethod.getString(data, "mentor"));
        saveReq.add("startDate", CommonMethod.getString(data, "startDate"));
        saveReq.add("endDate", CommonMethod.getString(data, "endDate"));

        DataResponse saveRes = HttpRequestUtil.request("/api/practice/saveProject", saveReq);
        if (saveRes != null && saveRes.getCode() == 0) {
            MessageDialog.showDialog("已提交审核");
            loadList();
            clearDetail();
        } else if (saveRes != null) {
            MessageDialog.showDialog(saveRes.getMsg());
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        if (selectedProject == null) return;
        int ret = MessageDialog.choiceDialog("确认删除该草稿项目？");
        if (ret != MessageDialog.CHOICE_YES) return;

        DataRequest req = new DataRequest();
        req.add("projectId", CommonMethod.getInteger(selectedProject, "projectId"));
        DataResponse res = HttpRequestUtil.request("/api/practice/deleteProject", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("已删除");
            clearDetail();
            loadList();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onSummaryButtonClick() {
        if (selectedProject == null) return;
        openSummaryDialog(CommonMethod.getInteger(selectedProject, "projectId"));
    }

    private void openEditDialog(Integer projectId, String projectType, Integer keyTopicId) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("com/teach/javafx/view/practice-project-edit.fxml");
            if (url == null) {
                url = com.teach.javafx.MainApplication.class.getResource("com/teach/javafx/view/practice-project-edit.fxml");
            }
            if (url == null) {
                url = com.teach.javafx.MainApplication.class.getResource("/com/teach/javafx/view/practice-project-edit.fxml");
            }
            if (url == null) {
                MessageDialog.showDialog("找不到项目编辑页面 FXML 文件");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());
            PracticeProjectEditController ctrl = loader.getController();
            ctrl.setProject(projectId, projectType, keyTopicId);
            ctrl.setOwnerController(this);

            Stage stage = new Stage();
            stage.setTitle(projectId == null ? "新建项目" : "编辑项目");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadList();
            clearDetail();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.showDialog("加载编辑页面失败：" + e.getMessage());
        }
    }

    private void openSummaryDialog(Integer projectId) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("com/teach/javafx/view/practice-summary-edit.fxml");
            if (url == null) {
                url = com.teach.javafx.MainApplication.class.getResource("com/teach/javafx/view/practice-summary-edit.fxml");
            }
            if (url == null) {
                url = com.teach.javafx.MainApplication.class.getResource("/com/teach/javafx/view/practice-summary-edit.fxml");
            }
            if (url == null) {
                MessageDialog.showDialog("找不到总结编辑页面 FXML 文件");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());
            PracticeSummaryEditController ctrl = loader.getController();
            ctrl.setProjectId(projectId);
            ctrl.setOwnerController(this);

            Stage stage = new Stage();
            stage.setTitle("提交总结");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadList();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.showDialog("加载总结页面失败：" + e.getMessage());
        }
    }

    private void clearDetail() {
        selectedProject = null;
        detailTitleLabel.setText("选择左侧项目查看详情");
        detailTeamLabel.setText("");
        detailMentorLabel.setText("");
        detailStatusLabel.setText("");
        detailDescArea.setText("");
        detailMembersLabel.setText("");
        detailReviewArea.setText("");
        editBtn.setVisible(false);
        submitBtn.setVisible(false);
        deleteBtn.setVisible(false);
        summaryBtn.setVisible(false);
    }

    @Override
    public void doRefresh() { loadList(); }
}
