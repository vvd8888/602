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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class QuestionnaireManageController extends ToolController {

    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, String> statusColumn;
    @FXML private TableColumn<Map, String> deadlineColumn;
    @FXML private TableColumn<Map, String> answerCountColumn;

    @FXML private ToggleButton allStatusBtn;
    @FXML private ToggleButton draftStatusBtn;
    @FXML private ToggleButton publishedStatusBtn;
    @FXML private ToggleButton withdrawnStatusBtn;

    @FXML private Label editTitleLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descArea;
    @FXML private CheckBox anonymousCheckBox;
    @FXML private CheckBox allowLateCheckBox;
    @FXML private CheckBox allowModifyCheckBox;
    @FXML private DatePicker deadlineField;
    @FXML private VBox questionsContainer;

    @FXML private Button draftBtn;
    @FXML private Button publishBtn;
    @FXML private Button withdrawBtn;
    @FXML private Button statsBtn;

    private ArrayList<Map> questionnaireList = new ArrayList<>();
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private ToggleGroup statusGroup;
    private Integer editingId = null;
    private String editingStatus = null;
    private List<Map<String, Object>> editQuestions = new ArrayList<>();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("status"));
        deadlineColumn.setCellValueFactory(new MapValueFactory<>("deadline"));
        answerCountColumn.setCellValueFactory(new MapValueFactory<>("answerCount"));

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        tsm.getSelectedIndices().addListener(this::onTableRowSelect);

        statusGroup = new ToggleGroup();
        allStatusBtn.setToggleGroup(statusGroup);
        draftStatusBtn.setToggleGroup(statusGroup);
        publishedStatusBtn.setToggleGroup(statusGroup);
        withdrawnStatusBtn.setToggleGroup(statusGroup);

        clearEditForm();
        loadList();
    }

    private String getStatusFilter() {
        ToggleButton selected = (ToggleButton) statusGroup.getSelectedToggle();
        if (selected == draftStatusBtn) return "DRAFT";
        if (selected == publishedStatusBtn) return "PUBLISHED";
        if (selected == withdrawnStatusBtn) return "WITHDRAWN";
        return "";
    }

    @FXML protected void onStatusFilterChanged() { loadList(); }
    @FXML protected void onRefreshButtonClick() { loadList(); }

    private void loadList() {
        DataRequest req = new DataRequest();
        req.add("status", getStatusFilter());
        DataResponse res = HttpRequestUtil.request("/api/questionnaire/getMyQuestionnaireList", req);
        if (res != null && res.getCode() == 0) {
            questionnaireList = (ArrayList<Map>) res.getData();
            if (questionnaireList == null) questionnaireList = new ArrayList<>();
            for (Map m : questionnaireList) {
                String st = CommonMethod.getString(m, "status");
                switch (st) {
                    case "DRAFT" -> m.put("status", "草稿");
                    case "PUBLISHED" -> m.put("status", "已发布");
                    case "WITHDRAWN" -> m.put("status", "已撤回");
                }
            }
            observableList.clear();
            observableList.addAll(questionnaireList);
            dataTableView.setItems(observableList);
        }
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        Map row = dataTableView.getSelectionModel().getSelectedItem();
        if (row == null) return;
        Integer qId = CommonMethod.getInteger(row, "questionnaireId");
        loadForEdit(qId);
    }

    private void loadForEdit(Integer qId) {
        DataRequest req = new DataRequest();
        req.add("status", "");
        DataResponse res = HttpRequestUtil.request("/api/questionnaire/getMyQuestionnaireList", req);
        if (res == null || res.getCode() != 0) return;
        List<Map> all = (List<Map>) res.getData();
        Map target = null;
        for (Map m : all) {
            if (qId.equals(CommonMethod.getInteger(m, "questionnaireId"))) {
                target = m;
                break;
            }
        }
        if (target == null) return;

        editingId = qId;
        editingStatus = CommonMethod.getString(target, "status");
        titleField.setText(CommonMethod.getString(target, "title"));
        String dl = CommonMethod.getString(target, "deadline");
        if (dl != null && !dl.isEmpty()) {
            try { deadlineField.setValue(LocalDate.parse(dl.substring(0, 10))); } catch (Exception ignored) {}
        }

        // 加载详情
        DataRequest detailReq = new DataRequest();
        detailReq.add("questionnaireId", qId);
        DataResponse detailRes = HttpRequestUtil.request("/api/questionnaire/getQuestionnaireDetail", detailReq);
        if (detailRes != null && detailRes.getCode() == 0) {
            Map data = (Map) detailRes.getData();
            descArea.setText(CommonMethod.getString(data, "description"));
            anonymousCheckBox.setSelected(CommonMethod.getBoolean(data, "anonymous"));
            allowLateCheckBox.setSelected(CommonMethod.getBoolean(data, "allowLate"));
            allowModifyCheckBox.setSelected(CommonMethod.getBoolean(data, "allowModify"));

            List<Map> questionList = (List<Map>) data.get("questions");
            editQuestions = new ArrayList<>();
            if (questionList != null) {
                for (Map q : questionList) {
                    Map<String, Object> eq = new HashMap<>(q);
                    editQuestions.add(eq);
                }
            }
            rebuildQuestionCards();
        }

        boolean isDraft = "DRAFT".equals(editingStatus);
        boolean isPublished = "PUBLISHED".equals(editingStatus);
        setEditMode(isDraft);
        draftBtn.setVisible(isDraft);
        publishBtn.setVisible(isDraft);
        withdrawBtn.setVisible(isPublished);
        statsBtn.setVisible(isPublished);
        editTitleLabel.setText(isDraft ? "编辑问卷（草稿）" : "查看问卷");
    }

    private void setEditMode(boolean edit) {
        titleField.setDisable(!edit);
        descArea.setDisable(!edit);
        anonymousCheckBox.setDisable(!edit);
        allowLateCheckBox.setDisable(!edit);
        allowModifyCheckBox.setDisable(!edit);
        deadlineField.setDisable(!edit);
        questionsContainer.setDisable(!edit);
    }

    @FXML
    protected void onNewButtonClick() {
        editingId = null;
        editingStatus = null;
        clearEditForm();
        setEditMode(true);
        draftBtn.setVisible(true);
        publishBtn.setVisible(true);
        withdrawBtn.setVisible(false);
        statsBtn.setVisible(false);
        editTitleLabel.setText("新建问卷");
    }

    // ===== 题目编辑 =====

    private int nextSortOrder() {
        return editQuestions.size() + 1;
    }

    @FXML protected void onAddSingleQuestion() { addQuestion("SINGLE"); }
    @FXML protected void onAddMultiQuestion() { addQuestion("MULTI"); }
    @FXML protected void onAddTextQuestion() { addQuestion("TEXT"); }
    @FXML protected void onAddScaleQuestion() { addQuestion("SCALE"); }

    private void addQuestion(String type) {
        Map<String, Object> q = new HashMap<>();
        q.put("questionId", null);
        q.put("questionType", type);
        q.put("title", "");
        q.put("required", true);
        q.put("sortOrder", nextSortOrder());
        if ("SCALE".equals(type)) { q.put("scaleMin", 0); q.put("scaleMax", 10); }
        if ("SINGLE".equals(type) || "MULTI".equals(type)) {
            List<Map<String, Object>> opts = new ArrayList<>();
            Map<String, Object> o1 = new HashMap<>();
            o1.put("optionId", null); o1.put("content", ""); o1.put("sortOrder", 1);
            opts.add(o1);
            Map<String, Object> o2 = new HashMap<>();
            o2.put("optionId", null); o2.put("content", ""); o2.put("sortOrder", 2);
            opts.add(o2);
            q.put("options", opts);
        }
        editQuestions.add(q);
        rebuildQuestionCards();
    }

    private void rebuildQuestionCards() {
        questionsContainer.getChildren().clear();
        for (int i = 0; i < editQuestions.size(); i++) {
            questionsContainer.getChildren().add(buildQuestionCard(i, editQuestions.get(i)));
        }
    }

    private VBox buildQuestionCard(int index, Map<String, Object> q) {
        String type = CommonMethod.getString(q, "questionType");
        String title = CommonMethod.getString(q, "title");

        VBox card = new VBox(6);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-radius: 4;");

        HBox header = new HBox(8);
        Label typeLabel = new Label("[" + type + "]");
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #3b82f6; -fx-font-weight: bold;");
        TextField titleField = new TextField(title);
        titleField.setPromptText("题目标题");
        titleField.setPrefWidth(300);
        titleField.textProperty().addListener((obs, old, val) -> q.put("title", val));
        CheckBox requiredCb = new CheckBox("必填");
        requiredCb.setSelected(CommonMethod.getBoolean(q, "required"));
        requiredCb.setOnAction(e -> q.put("required", requiredCb.isSelected()));
        Button deleteBtn = new Button("× 删除");
        deleteBtn.setOnAction(e -> {
            int ret = MessageDialog.choiceDialog("确认删除该题目？");
            if (ret == MessageDialog.CHOICE_YES) {
                editQuestions.remove(index);
                rebuildQuestionCards();
            }
        });
        Button upBtn = new Button("↑");
        Button downBtn = new Button("↓");
        upBtn.setOnAction(e -> { if (index > 0) { Collections.swap(editQuestions, index, index - 1); rebuildQuestionCards(); } });
        downBtn.setOnAction(e -> { if (index < editQuestions.size() - 1) { Collections.swap(editQuestions, index, index + 1); rebuildQuestionCards(); } });

        header.getChildren().addAll(typeLabel, titleField, requiredCb, upBtn, downBtn, deleteBtn);

        VBox extraBox = new VBox(4);
        extraBox.setPadding(new Insets(4, 0, 0, 16));

        if ("SCALE".equals(type)) {
            HBox scaleBox = new HBox(8);
            TextField minField = new TextField(String.valueOf(CommonMethod.getInteger0(q, "scaleMin")));
            minField.setPrefWidth(60);
            minField.textProperty().addListener((obs, old, val) -> { try { q.put("scaleMin", Integer.parseInt(val)); } catch (Exception ignored) {} });
            TextField maxField = new TextField(String.valueOf(CommonMethod.getInteger0(q, "scaleMax")));
            maxField.setPrefWidth(60);
            maxField.textProperty().addListener((obs, old, val) -> { try { q.put("scaleMax", Integer.parseInt(val)); } catch (Exception ignored) {} });
            scaleBox.getChildren().addAll(new Label("最小值："), minField, new Label("最大值："), maxField);
            extraBox.getChildren().add(scaleBox);
        }

        if ("SINGLE".equals(type) || "MULTI".equals(type)) {
            List<Map<String, Object>> opts = (List<Map<String, Object>>) q.get("options");
            if (opts == null) { opts = new ArrayList<>(); q.put("options", opts); }
            final List<Map<String, Object>> finalOpts = opts;

            VBox optsBox = new VBox(3);
            for (int j = 0; j < finalOpts.size(); j++) {
                final Map<String, Object> opt = finalOpts.get(j);
                HBox optRow = new HBox(4);
                TextField optField = new TextField(CommonMethod.getString(opt, "content"));
                optField.setPrefWidth(250);
                optField.setPromptText("选项内容");
                optField.textProperty().addListener((obs, old, val) -> opt.put("content", val));
                Button optDelBtn = new Button("×");
                final int oj = j;
                optDelBtn.setOnAction(e -> {
                    finalOpts.remove(oj);
                    rebuildQuestionCards();
                });
                optRow.getChildren().addAll(new Label("选项" + (j + 1) + ":"), optField, optDelBtn);
                optsBox.getChildren().add(optRow);
            }
            Button addOptBtn = new Button("+ 添加选项");
            addOptBtn.setOnAction(e -> {
                Map<String, Object> newOpt = new HashMap<>();
                newOpt.put("optionId", null); newOpt.put("content", ""); newOpt.put("sortOrder", finalOpts.size() + 1);
                finalOpts.add(newOpt);
                rebuildQuestionCards();
            });
            optsBox.getChildren().add(addOptBtn);
            extraBox.getChildren().add(optsBox);
        }

        card.getChildren().addAll(header, extraBox);
        return card;
    }

    // ===== 保存 / 发布 / 撤回 =====

    private void doSave(String action) {
        if (titleField.getText().trim().isEmpty()) {
            MessageDialog.showDialog("标题不能为空");
            return;
        }
        DataRequest req = new DataRequest();
        req.add("questionnaireId", editingId);
        req.add("title", titleField.getText().trim());
        req.add("description", descArea.getText());
        req.add("anonymous", anonymousCheckBox.isSelected());
        req.add("allowLate", allowLateCheckBox.isSelected());
        req.add("allowModify", allowModifyCheckBox.isSelected());
        req.add("deadline", deadlineField.getValue() != null ? deadlineField.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE) + " 23:59" : "");
        req.add("action", action);

        // 整理题目排序
        for (int i = 0; i < editQuestions.size(); i++) {
            editQuestions.get(i).put("sortOrder", i + 1);
            List<Map<String, Object>> opts = (List<Map<String, Object>>) editQuestions.get(i).get("options");
            if (opts != null) {
                for (int j = 0; j < opts.size(); j++)
                    opts.get(j).put("sortOrder", j + 1);
            }
        }
        req.add("questions", editQuestions);

        DataResponse res = HttpRequestUtil.request("/api/questionnaire/saveQuestionnaire", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("草稿".equals(action) ? "草稿已保存" : "问卷已发布");
            clearEditForm();
            loadList();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML protected void onSaveDraftButtonClick() { doSave("DRAFT"); }
    @FXML protected void onPublishButtonClick() { doSave("PUBLISH"); }

    @FXML
    protected void onWithdrawButtonClick() {
        if (editingId == null) { MessageDialog.showDialog("请先选择一条已发布的问卷"); return; }
        int ret = MessageDialog.choiceDialog("确认撤回该问卷？撤回后将删除所有回答记录");
        if (ret != MessageDialog.CHOICE_YES) return;
        DataRequest req = new DataRequest();
        req.add("questionnaireId", editingId);
        DataResponse res = HttpRequestUtil.request("/api/questionnaire/withdrawQuestionnaire", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("已撤回");
            clearEditForm();
            loadList();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onViewStatsButtonClick() {
        if (editingId == null) { MessageDialog.showDialog("请先选择一条已发布的问卷"); return; }
        DataRequest req = new DataRequest();
        req.add("questionnaireId", editingId);
        DataResponse res = HttpRequestUtil.request("/api/questionnaire/getStatistics", req);
        if (res == null || res.getCode() != 0) return;

        Map data = (Map) res.getData();
        StringBuilder sb = new StringBuilder();
        sb.append("===== 问卷统计 =====\n\n");
        sb.append("提交人数：").append(data.get("submitCount")).append(" / ").append(data.get("totalTarget")).append("\n");
        sb.append("提交率：").append(CommonMethod.getString(data, "submitRate")).append("\n\n");

        List<Map> qStats = (List<Map>) data.get("questions");
        if (qStats != null) {
            for (Map qs : qStats) {
                sb.append("【").append(CommonMethod.getString(qs, "questionType")).append("】");
                sb.append(CommonMethod.getString(qs, "title")).append("\n");
                String type = CommonMethod.getString(qs, "questionType");
                Object statsObj = qs.get("stats");

                if ("TEXT".equals(type)) {
                    Map stats = (Map) statsObj;
                    sb.append("  文本回答数：").append(stats.get("answerCount")).append("\n");
                    List<String> answers = (List<String>) stats.get("answers");
                    if (answers != null) {
                        for (int i = 0; i < Math.min(answers.size(), 5); i++)
                            sb.append("  - \"").append(answers.get(i)).append("\"\n");
                        if (answers.size() > 5) sb.append("  ... 共 ").append(answers.size()).append(" 条\n");
                    }
                } else if ("SCALE".equals(type)) {
                    Map stats = (Map) statsObj;
                    sb.append("  平均：").append(stats.get("avg")).append(" 最低：").append(stats.get("min"))
                            .append(" 最高：").append(stats.get("max")).append("\n");
                } else {
                    List<Map> stats = (List<Map>) statsObj;
                    for (Map st : stats) {
                        sb.append("  ").append(CommonMethod.getString(st, "content"))
                                .append("：").append(st.get("count")).append(" (").append(CommonMethod.getString(st, "rate")).append(")\n");
                    }
                }
                sb.append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("问卷统计");
        alert.setHeaderText("统计详情");
        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefSize(500, 450);
        alert.getDialogPane().setContent(ta);
        alert.showAndWait();
    }

    private void clearEditForm() {
        editingId = null;
        editingStatus = null;
        titleField.clear();
        descArea.clear();
        anonymousCheckBox.setSelected(false);
        allowLateCheckBox.setSelected(false);
        allowModifyCheckBox.setSelected(false);
        deadlineField.setValue(null);
        editQuestions.clear();
        questionsContainer.getChildren().clear();
        editTitleLabel.setText("编辑问卷");
        setEditMode(false);
        draftBtn.setVisible(false);
        publishBtn.setVisible(false);
        withdrawBtn.setVisible(false);
        statsBtn.setVisible(false);
    }

    @Override
    public void doRefresh() { loadList(); }
    @Override
    public void doNew() { onNewButtonClick(); }
}
