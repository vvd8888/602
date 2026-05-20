package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.CommonMethod;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class QuestionnaireFillController {

    @FXML private Label titleLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label anonymousLabel;
    @FXML private Label descLabel;
    @FXML private VBox questionsContainer;
    @FXML private Button submitBtn;

    private Integer questionnaireId;
    private Integer existingResponseId;
    private List<Map> questions;
    private Map<Integer, List<Object>> answerControls = new HashMap<>();
    private QuestionnaireListController ownerController;

    public void setQuestionnaireId(Integer questionnaireId) {
        this.questionnaireId = questionnaireId;
        loadQuestionnaire();
    }

    public void setOwnerController(QuestionnaireListController ctrl) {
        this.ownerController = ctrl;
    }

    @FXML
    public void initialize() {
    }

    private void loadQuestionnaire() {
        DataRequest req = new DataRequest();
        req.add("questionnaireId", questionnaireId);
        DataResponse res = HttpRequestUtil.request("/api/questionnaire/getQuestionnaireDetail", req);
        if (res == null || res.getCode() != 0) {
            MessageDialog.showDialog("加载问卷失败");
            closeDialog();
            return;
        }

        Map data = (Map) res.getData();
        titleLabel.setText(CommonMethod.getString(data, "title"));
        deadlineLabel.setText("截止时间：" + CommonMethod.getString(data, "deadline"));
        anonymousLabel.setText(CommonMethod.getBoolean(data, "anonymous") ? "（匿名问卷）" : "");
        descLabel.setText(CommonMethod.getString(data, "description"));

        questions = (List<Map>) data.get("questions");
        if (questions == null) questions = new ArrayList<>();

        // 尝试加载已有回答
        DataRequest respReq = new DataRequest();
        respReq.add("questionnaireId", questionnaireId);
        DataResponse respRes = HttpRequestUtil.request("/api/questionnaire/getMyResponse", respReq);
        Map<String, String> existingAnswers = new HashMap<>();
        if (respRes != null && respRes.getCode() == 0) {
            Map respData = (Map) respRes.getData();
            existingResponseId = CommonMethod.getInteger(respData, "responseId");
            List<Map> ansList = (List<Map>) respData.get("answers");
            if (ansList != null) {
                for (Map a : ansList)
                    existingAnswers.put(String.valueOf(CommonMethod.getInteger(a, "questionId")),
                            CommonMethod.getString(a, "answerValue"));
            }
        }

        buildQuestionForms(existingAnswers);
    }

    private void buildQuestionForms(Map<String, String> existingAnswers) {
        questionsContainer.getChildren().clear();
        answerControls.clear();

        for (Map q : questions) {
            Integer questionId = CommonMethod.getInteger(q, "questionId");
            String type = CommonMethod.getString(q, "questionType");
            String title = CommonMethod.getString(q, "title");
            boolean required = CommonMethod.getBoolean(q, "required");

            VBox card = new VBox(8);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");

            Label qLabel = new Label(title + (required ? " *" : ""));
            qLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            card.getChildren().add(qLabel);

            String existing = existingAnswers.get(String.valueOf(questionId));
            List<Object> controls = new ArrayList<>();
            answerControls.put(questionId, controls);

            switch (type) {
                case "SINGLE": {
                    ToggleGroup tg = new ToggleGroup();
                    List<Map> opts = (List<Map>) q.get("options");
                    if (opts != null) {
                        for (Map opt : opts) {
                            RadioButton rb = new RadioButton(CommonMethod.getString(opt, "content"));
                            rb.setToggleGroup(tg);
                            rb.setUserData(String.valueOf(CommonMethod.getInteger(opt, "optionId")));
                            if (existing != null && existing.equals(String.valueOf(CommonMethod.getInteger(opt, "optionId"))))
                                rb.setSelected(true);
                            controls.add(rb);
                            card.getChildren().add(rb);
                        }
                    }
                    // 保存 toggleGroup 引用
                    controls.add(tg);
                    break;
                }
                case "MULTI": {
                    List<Map> opts = (List<Map>) q.get("options");
                    if (opts != null) {
                        Set<String> selectedSet = new HashSet<>();
                        if (existing != null && !existing.isEmpty()) {
                            for (String s : existing.split(","))
                                selectedSet.add(s.trim());
                        }
                        for (Map opt : opts) {
                            CheckBox cb = new CheckBox(CommonMethod.getString(opt, "content"));
                            cb.setUserData(String.valueOf(CommonMethod.getInteger(opt, "optionId")));
                            if (selectedSet.contains(String.valueOf(CommonMethod.getInteger(opt, "optionId"))))
                                cb.setSelected(true);
                            controls.add(cb);
                            card.getChildren().add(cb);
                        }
                    }
                    break;
                }
                case "TEXT": {
                    TextArea ta = new TextArea();
                    ta.setPrefRowCount(3);
                    ta.setWrapText(true);
                    if (existing != null) ta.setText(existing);
                    controls.add(ta);
                    card.getChildren().add(ta);
                    break;
                }
                case "SCALE": {
                    int min = CommonMethod.getInteger0(q, "scaleMin");
                    int max = CommonMethod.getInteger0(q, "scaleMax");
                    Label rangeLabel = new Label(min + " ～ " + max);
                    rangeLabel.setStyle("-fx-font-size: 11px;");
                    card.getChildren().add(rangeLabel);

                    HBox hbox = new HBox(5);
                    TextField tf = new TextField();
                    tf.setPrefWidth(80);
                    tf.setPromptText(min + "-" + max);
                    if (existing != null) tf.setText(existing);
                    controls.add(tf);

                    Slider slider = new Slider(min, max, existing != null ? Double.parseDouble(existing) : (min + max) / 2.0);
                    slider.setShowTickLabels(true);
                    slider.setShowTickMarks(true);
                    slider.setMajorTickUnit(Math.max(1, (max - min) / 5.0));
                    slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                        tf.setText(String.valueOf(Math.round(newVal.doubleValue())));
                    });
                    tf.textProperty().addListener((obs, oldVal, newVal) -> {
                        try {
                            int v = Integer.parseInt(newVal);
                            if (v >= min && v <= max)
                                slider.setValue(v);
                        } catch (NumberFormatException ignored) {}
                    });
                    controls.add(slider);
                    hbox.getChildren().addAll(new Label("数值："), tf, slider);
                    card.getChildren().add(hbox);
                    break;
                }
            }
            questionsContainer.getChildren().add(card);
        }
    }

    @FXML
    protected void onSubmitButtonClick() {
        DataRequest req = new DataRequest();
        req.add("questionnaireId", questionnaireId);
        if (existingResponseId != null)
            req.add("responseId", existingResponseId);

        List<Map<String, Object>> answers = new ArrayList<>();
        for (Map q : questions) {
            Integer qId = CommonMethod.getInteger(q, "questionId");
            String type = CommonMethod.getString(q, "questionType");
            boolean required = CommonMethod.getBoolean(q, "required");
            List<Object> controls = answerControls.get(qId);
            if (controls == null || controls.isEmpty()) continue;

            String value = "";
            switch (type) {
                case "SINGLE": {
                    ToggleGroup tg = null;
                    for (Object c : controls) {
                        if (c instanceof ToggleGroup) tg = (ToggleGroup) c;
                    }
                    if (tg != null && tg.getSelectedToggle() != null)
                        value = String.valueOf(tg.getSelectedToggle().getUserData());
                    break;
                }
                case "MULTI": {
                    List<String> selected = new ArrayList<>();
                    for (Object c : controls) {
                        if (c instanceof CheckBox cb && cb.isSelected())
                            selected.add(String.valueOf(cb.getUserData()));
                    }
                    value = String.join(",", selected);
                    break;
                }
                case "TEXT": {
                    for (Object c : controls) {
                        if (c instanceof TextArea ta)
                            value = ta.getText();
                    }
                    break;
                }
                case "SCALE": {
                    for (Object c : controls) {
                        if (c instanceof TextField tf)
                            value = tf.getText();
                    }
                    break;
                }
            }

            if (required && (value == null || value.isEmpty())) {
                MessageDialog.showDialog("请完成必填题目：" + CommonMethod.getString(q, "title"));
                return;
            }

            Map<String, Object> am = new HashMap<>();
            am.put("questionId", qId);
            am.put("answerValue", value);
            answers.add(am);
        }

        req.add("answers", answers);
        DataResponse res = HttpRequestUtil.request("/api/questionnaire/submitResponse", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("提交成功");
            closeDialog();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onCancelButtonClick() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) submitBtn.getScene().getWindow();
        stage.close();
    }
}
