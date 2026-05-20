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

public class PracticeKeyListController extends ToolController {

    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> col1;
    @FXML private TableColumn<Map, String> col2;
    @FXML private TableColumn<Map, String> col3;
    @FXML private TableColumn<Map, String> col4;

    @FXML private ToggleButton allFilterBtn;
    @FXML private ToggleButton draftFilterBtn;
    @FXML private ToggleButton reviewFilterBtn;
    @FXML private ToggleButton progressFilterBtn;
    @FXML private ToggleButton doneFilterBtn;

    @FXML private RadioButton topicViewBtn;
    @FXML private RadioButton projectViewBtn;
    @FXML private Label sectionLabel;

    @FXML private Label detailTitleLabel;
    @FXML private Label detailSubtitleLabel;
    @FXML private TextArea detailDescArea;
    @FXML private TextArea detailReqArea;
    @FXML private Label detailMembersLabel;
    @FXML private Label detailReviewLabel;

    @FXML private Button applyBtn;
    @FXML private Button editBtn;
    @FXML private Button submitBtn;
    @FXML private Button deleteBtn;
    @FXML private Button summaryBtn;

    private ArrayList<Map> topicList = new ArrayList<>();
    private ArrayList<Map> projectList = new ArrayList<>();
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private ToggleGroup filterGroup;
    private ToggleGroup viewGroup;
    private boolean showingTopics = true;
    private boolean refreshing = false;
    private Map<String, Object> selectedRow = null;

    @FXML
    public void initialize() {
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        tsm.getSelectedIndices().addListener(this::onTableRowSelect);

        filterGroup = new ToggleGroup();
        allFilterBtn.setToggleGroup(filterGroup);
        draftFilterBtn.setToggleGroup(filterGroup);
        reviewFilterBtn.setToggleGroup(filterGroup);
        progressFilterBtn.setToggleGroup(filterGroup);
        doneFilterBtn.setToggleGroup(filterGroup);

        viewGroup = new ToggleGroup();
        topicViewBtn.setToggleGroup(viewGroup);
        projectViewBtn.setToggleGroup(viewGroup);

        clearDetail();
        loadTopics();
    }

    @FXML
    protected void onSwitchView() {
        if (projectViewBtn.isSelected()) {
            showingTopics = false;
            sectionLabel.setText("我的重点立项项目");
            col1.setText("项目名称");
            col2.setText("团队");
            col3.setText("指导老师");
            col4.setText("状态");
            loadMyProjects();
        } else {
            showingTopics = true;
            sectionLabel.setText("可申请的重点选题");
            col1.setText("标题");
            col2.setText("发布者");
            col3.setText("发布时间");
            col4.setText("状态");
            loadTopics();
        }
    }

    @FXML
    protected void onFilterChanged() { loadMyProjects(); }

    @FXML
    protected void onRefreshButtonClick() {
        if (showingTopics) loadTopics();
        else loadMyProjects();
    }

    private void loadTopics() {
        refreshing = true;
        DataResponse res = HttpRequestUtil.request("/api/practice/getAvailableKeyTopics", new DataRequest());
        if (res != null && res.getCode() == 0) {
            topicList = (ArrayList<Map>) res.getData();
            if (topicList == null) topicList = new ArrayList<>();
            observableList.clear();
            observableList.addAll(topicList);
            dataTableView.setItems(observableList);
        }
        clearDetail();
        refreshing = false;
    }

    private void loadMyProjects() {
        refreshing = true;
        DataRequest req = new DataRequest();
        req.add("projectType", "KEY");
        DataResponse res = HttpRequestUtil.request("/api/practice/getMyProjectList", req);
        if (res != null && res.getCode() == 0) {
            projectList = (ArrayList<Map>) res.getData();
            if (projectList == null) projectList = new ArrayList<>();
            applyFilter();
            observableList.clear();
            observableList.addAll(projectList);
            dataTableView.setItems(observableList);
        }
        clearDetail();
        refreshing = false;
    }

    private void applyFilter() {
        String filter = getFilter();
        if ("ALL".equals(filter)) return;
        List<Map> filtered = new ArrayList<>();
        for (Map m : projectList) {
            String status = CommonMethod.getString(m, "status");
            boolean include = switch (filter) {
                case "DRAFT_REJECTED" -> "DRAFT".equals(status) || "REJECTED".equals(status);
                case "SUBMITTED" -> "SUBMITTED".equals(status);
                case "ACTIVE" -> "APPROVED".equals(status) || "IN_PROGRESS".equals(status) || "COMPLETED".equals(status);
                case "FINISHED" -> "FINISHED".equals(status);
                default -> true;
            };
            if (include) filtered.add(m);
        }
        projectList = new ArrayList<>(filtered);
    }

    private String getFilter() {
        ToggleButton selected = (ToggleButton) filterGroup.getSelectedToggle();
        if (selected == draftFilterBtn) return "DRAFT_REJECTED";
        if (selected == reviewFilterBtn) return "SUBMITTED";
        if (selected == progressFilterBtn) return "ACTIVE";
        if (selected == doneFilterBtn) return "FINISHED";
        return "ALL";
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        if (refreshing) return;
        Map row = dataTableView.getSelectionModel().getSelectedItem();
        if (row == null) {
            clearDetail();
            return;
        }
        selectedRow = row;

        if (showingTopics) {
            showTopicDetail(row);
        } else {
            showProjectDetail(row);
        }
    }

    private void showTopicDetail(Map row) {
        Integer keyTopicId = CommonMethod.getInteger(row, "keyTopicId");
        detailTitleLabel.setText(CommonMethod.getString(row, "title"));
        detailSubtitleLabel.setText("发布者：" + CommonMethod.getString(row, "creatorName")
                + " | " + CommonMethod.getString(row, "createTime"));
        detailDescArea.setText(CommonMethod.getString(row, "description"));
        detailReqArea.setText(CommonMethod.getString(row, "requirements"));
        detailMembersLabel.setText("");
        detailReviewLabel.setText("");

        applyBtn.setVisible(true);
        editBtn.setVisible(false);
        submitBtn.setVisible(false);
        deleteBtn.setVisible(false);
        summaryBtn.setVisible(false);
        applyBtn.setUserData(keyTopicId);
    }

    private void showProjectDetail(Map row) {
        Integer projectId = CommonMethod.getInteger(row, "projectId");
        DataRequest req = new DataRequest();
        req.add("projectId", projectId);
        DataResponse res = HttpRequestUtil.request("/api/practice/getProjectDetail", req);
        if (res == null || res.getCode() != 0) return;
        Map<String, Object> data = (Map<String, Object>) res.getData();

        detailTitleLabel.setText(CommonMethod.getString(data, "title"));
        String teamName = CommonMethod.getString(data, "teamName");
        String mentor = CommonMethod.getString(data, "mentor");
        detailSubtitleLabel.setText("团队：" + (teamName != null && !teamName.isEmpty() ? teamName : "未命名")
                + " | 指导老师：" + (mentor != null && !mentor.isEmpty() ? mentor : "无")
                + " | " + CommonMethod.getString(data, "statusDisplay"));
        detailDescArea.setText(CommonMethod.getString(data, "description"));
        detailReqArea.setText("");
        detailMembersLabel.setText("成员：" + CommonMethod.getString(data, "memberNames"));

        List<Map> reviews = (List<Map>) data.get("reviews");
        StringBuilder reviewText = new StringBuilder();
        if (reviews != null && !reviews.isEmpty()) {
            for (Map r : reviews) {
                String comment = CommonMethod.getString(r, "comment");
                if (comment != null && !comment.isEmpty()) {
                    reviewText.append(CommonMethod.getString(r, "reviewResult"))
                            .append("：").append(comment).append("\n");
                }
            }
        }
        detailReviewLabel.setText(reviewText.toString());

        String status = CommonMethod.getString(data, "status");
        boolean isDraft = "DRAFT".equals(status);
        boolean isRejected = "REJECTED".equals(status);
        boolean isCompleted = "COMPLETED".equals(status) || "SUMMARY_SUBMITTED".equals(status);

        applyBtn.setVisible(false);
        editBtn.setVisible(isDraft || isRejected);
        submitBtn.setVisible(isDraft || isRejected);
        deleteBtn.setVisible(isDraft);
        summaryBtn.setVisible(isCompleted);
    }

    @FXML
    protected void onApplyButtonClick() {
        if (applyBtn.getUserData() == null) return;
        Integer keyTopicId = (Integer) applyBtn.getUserData();
        openEditDialog(null, "KEY", keyTopicId);
    }

    @FXML
    protected void onEditButtonClick() {
        if (selectedRow == null || showingTopics) return;
        openEditDialog(CommonMethod.getInteger(selectedRow, "projectId"), "KEY", null);
    }

    @FXML
    protected void onSubmitButtonClick() {
        if (selectedRow == null || showingTopics) return;
        int ret = MessageDialog.choiceDialog("确认提交审核？");
        if (ret != MessageDialog.CHOICE_YES) return;

        Integer projectId = CommonMethod.getInteger(selectedRow, "projectId");
        DataRequest detailReq = new DataRequest();
        detailReq.add("projectId", projectId);
        DataResponse detailRes = HttpRequestUtil.request("/api/practice/getProjectDetail", detailReq);
        if (detailRes == null || detailRes.getCode() != 0) return;
        Map<String, Object> data = (Map<String, Object>) detailRes.getData();

        DataRequest saveReq = new DataRequest();
        saveReq.add("projectId", projectId);
        saveReq.add("action", "SUBMIT");
        saveReq.add("projectType", "KEY");
        saveReq.add("title", CommonMethod.getString(data, "title"));
        saveReq.add("description", CommonMethod.getString(data, "description"));
        saveReq.add("mentor", CommonMethod.getString(data, "mentor"));
        saveReq.add("startDate", CommonMethod.getString(data, "startDate"));
        saveReq.add("endDate", CommonMethod.getString(data, "endDate"));

        DataResponse saveRes = HttpRequestUtil.request("/api/practice/saveProject", saveReq);
        if (saveRes != null && saveRes.getCode() == 0) {
            MessageDialog.showDialog("已提交审核");
            loadMyProjects();
            clearDetail();
        } else if (saveRes != null) {
            MessageDialog.showDialog(saveRes.getMsg());
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        if (selectedRow == null || showingTopics) return;
        int ret = MessageDialog.choiceDialog("确认删除该草稿项目？");
        if (ret != MessageDialog.CHOICE_YES) return;

        DataRequest req = new DataRequest();
        req.add("projectId", CommonMethod.getInteger(selectedRow, "projectId"));
        DataResponse res = HttpRequestUtil.request("/api/practice/deleteProject", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("已删除");
            clearDetail();
            loadMyProjects();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onSummaryButtonClick() {
        if (selectedRow == null || showingTopics) return;
        openSummaryDialog(CommonMethod.getInteger(selectedRow, "projectId"));
    }

    private void openEditDialog(Integer projectId, String projectType, Integer keyTopicId) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("com/teach/javafx/view/practice-project-edit.fxml");
            if (url == null)
                url = com.teach.javafx.MainApplication.class.getResource("com/teach/javafx/view/practice-project-edit.fxml");
            if (url == null)
                url = com.teach.javafx.MainApplication.class.getResource("/com/teach/javafx/view/practice-project-edit.fxml");
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

            if (showingTopics) loadTopics();
            else loadMyProjects();
            clearDetail();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.showDialog("加载编辑页面失败：" + e.getMessage());
        }
    }

    private void openSummaryDialog(Integer projectId) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("com/teach/javafx/view/practice-summary-edit.fxml");
            if (url == null)
                url = com.teach.javafx.MainApplication.class.getResource("com/teach/javafx/view/practice-summary-edit.fxml");
            if (url == null)
                url = com.teach.javafx.MainApplication.class.getResource("/com/teach/javafx/view/practice-summary-edit.fxml");
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

            loadMyProjects();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.showDialog("加载总结页面失败：" + e.getMessage());
        }
    }

    private void clearDetail() {
        selectedRow = null;
        detailTitleLabel.setText("选择左侧选题或项目查看详情");
        detailSubtitleLabel.setText("");
        detailDescArea.setText("");
        detailReqArea.setText("");
        detailMembersLabel.setText("");
        detailReviewLabel.setText("");
        applyBtn.setVisible(false);
        editBtn.setVisible(false);
        submitBtn.setVisible(false);
        deleteBtn.setVisible(false);
        summaryBtn.setVisible(false);
    }

    @Override
    public void doRefresh() {
        if (showingTopics) loadTopics();
        else loadMyProjects();
    }
}
