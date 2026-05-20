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
import java.util.Map;

public class QuestionnaireListController extends ToolController {

    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, String> deadlineColumn;
    @FXML private TableColumn<Map, String> questionCountColumn;
    @FXML private TableColumn<Map, String> statusColumn;

    @FXML private ToggleButton allFilterBtn;
    @FXML private ToggleButton todoFilterBtn;
    @FXML private ToggleButton doneFilterBtn;

    @FXML private Label detailTitleLabel;
    @FXML private Label detailCreatorLabel;
    @FXML private Label detailDeadlineLabel;
    @FXML private Label detailAnonymousLabel;
    @FXML private TextArea detailDescArea;
    @FXML private Label detailInfoLabel;
    @FXML private Button fillBtn;
    @FXML private Button viewBtn;

    private ArrayList<Map> questionnaireList = new ArrayList<>();
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private ToggleGroup filterGroup;
    private boolean refreshing = false;
    private Map<String, Object> selectedQuestionnaire = null;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        deadlineColumn.setCellValueFactory(new MapValueFactory<>("deadline"));
        questionCountColumn.setCellValueFactory(new MapValueFactory<>("questionCount"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("statusDisplay"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        tsm.getSelectedIndices().addListener(this::onTableRowSelect);

        filterGroup = new ToggleGroup();
        allFilterBtn.setToggleGroup(filterGroup);
        todoFilterBtn.setToggleGroup(filterGroup);
        doneFilterBtn.setToggleGroup(filterGroup);

        clearDetail();
        loadList();
    }

    private String getFilter() {
        ToggleButton selected = (ToggleButton) filterGroup.getSelectedToggle();
        if (selected == todoFilterBtn) return "TODO";
        if (selected == doneFilterBtn) return "DONE";
        return "ALL";
    }

    @FXML
    protected void onFilterChanged() { loadList(); }

    @FXML
    protected void onRefreshButtonClick() { loadList(); }

    private void loadList() {
        refreshing = true;
        DataRequest req = new DataRequest();
        req.add("filter", getFilter());
        DataResponse res = HttpRequestUtil.request("/api/questionnaire/getQuestionnaireList", req);
        if (res != null && res.getCode() == 0) {
            questionnaireList = (ArrayList<Map>) res.getData();
            if (questionnaireList == null) questionnaireList = new ArrayList<>();
            for (Map m : questionnaireList) {
                boolean hasSubmitted = CommonMethod.getBoolean(m, "hasSubmitted");
                boolean isExpired = CommonMethod.getBoolean(m, "isExpired");
                if (hasSubmitted)
                    m.put("statusDisplay", "已提交");
                else if (isExpired)
                    m.put("statusDisplay", "已截止");
                else
                    m.put("statusDisplay", "待填写");
            }
            observableList.clear();
            observableList.addAll(questionnaireList);
            dataTableView.setItems(observableList);
        }
        refreshing = false;
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        if (refreshing) return;
        Map row = dataTableView.getSelectionModel().getSelectedItem();
        if (row == null) {
            clearDetail();
            return;
        }
        selectedQuestionnaire = row;
        detailTitleLabel.setText(CommonMethod.getString(row, "title"));
        detailCreatorLabel.setText("发布者：" + CommonMethod.getString(row, "creatorName"));
        detailDeadlineLabel.setText("截止：" + CommonMethod.getString(row, "deadline"));
        detailAnonymousLabel.setText(CommonMethod.getBoolean(row, "anonymous") ? "匿名问卷" : "实名问卷");

        // 加载详情（含说明）
        Integer qId = CommonMethod.getInteger(row, "questionnaireId");
        DataRequest req = new DataRequest();
        req.add("questionnaireId", qId);
        DataResponse res = HttpRequestUtil.request("/api/questionnaire/getQuestionnaireDetail", req);
        boolean canModify = false;
        if (res != null && res.getCode() == 0) {
            Map data = (Map) res.getData();
            detailDescArea.setText(CommonMethod.getString(data, "description"));
            canModify = CommonMethod.getBoolean(data, "allowModify");
            String allowModify = canModify ? "可修改" : "不可修改";
            String allowLate = CommonMethod.getBoolean(data, "allowLate") ? "可补交" : "不可补交";
            detailInfoLabel.setText(allowModify + " | " + allowLate
                    + " | 共 " + CommonMethod.getString(row, "questionCount") + " 题");
        }

        boolean hasSubmitted = CommonMethod.getBoolean(row, "hasSubmitted");
        fillBtn.setVisible(!hasSubmitted || canModify);
        viewBtn.setVisible(hasSubmitted);
    }

    @FXML
    protected void onFillButtonClick() {
        if (selectedQuestionnaire == null) return;
        Integer qId = CommonMethod.getInteger(selectedQuestionnaire, "questionnaireId");
        boolean hasSubmitted = CommonMethod.getBoolean(selectedQuestionnaire, "hasSubmitted");
        openFillDialog(qId, hasSubmitted ? CommonMethod.getInteger(selectedQuestionnaire, "responseId") : null);
    }

    @FXML
    protected void onViewButtonClick() {
        if (selectedQuestionnaire == null) return;
        Integer qId = CommonMethod.getInteger(selectedQuestionnaire, "questionnaireId");
        openFillDialog(qId, null);
    }

    private void openFillDialog(Integer questionnaireId, Integer responseId) {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("com/teach/javafx/view/questionnaire-fill-panel.fxml");
            if (url == null) {
                url = com.teach.javafx.MainApplication.class.getResource("com/teach/javafx/view/questionnaire-fill-panel.fxml");
            }
            if (url == null) {
                url = com.teach.javafx.MainApplication.class.getResource("/com/teach/javafx/view/questionnaire-fill-panel.fxml");
            }
            if (url == null) {
                MessageDialog.showDialog("找不到填写页面 FXML 文件");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());
            QuestionnaireFillController ctrl = loader.getController();
            ctrl.setQuestionnaireId(questionnaireId);
            ctrl.setOwnerController(this);

            Stage stage = new Stage();
            stage.setTitle("填写问卷");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadList();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.showDialog("加载问卷失败：" + e.getMessage());
        }
    }

    private void clearDetail() {
        selectedQuestionnaire = null;
        detailTitleLabel.setText("选择左侧问卷查看详情");
        detailCreatorLabel.setText("");
        detailDeadlineLabel.setText("");
        detailAnonymousLabel.setText("");
        detailDescArea.setText("");
        detailInfoLabel.setText("");
        fillBtn.setVisible(false);
        viewBtn.setVisible(false);
    }

    @Override
    public void doRefresh() { loadList(); }
}
