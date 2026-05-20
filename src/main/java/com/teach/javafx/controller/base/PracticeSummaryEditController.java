package com.teach.javafx.controller.base;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PracticeSummaryEditController {

    @FXML private Label dialogTitle;
    @FXML private ComboBox<OptionItem> summaryTypeCombo;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Button draftBtn;
    @FXML private Button submitBtn;

    private Integer projectId;
    private ToolController ownerController;

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
        dialogTitle.setText("提交总结");

        summaryTypeCombo.getItems().addAll(
                new OptionItem(null, "PERSONAL", "个人总结"),
                new OptionItem(null, "TEAM", "团队总结（仅队长）"));
        summaryTypeCombo.getSelectionModel().select(0);
    }

    public void setOwnerController(ToolController owner) {
        this.ownerController = owner;
    }

    @FXML
    protected void onSummaryTypeChanged() {}

    @FXML
    protected void onSaveDraftButtonClick() { doSave("DRAFT"); }

    @FXML
    protected void onSubmitButtonClick() { doSave("SUBMIT"); }

    private void doSave(String action) {
        OptionItem typeItem = summaryTypeCombo.getSelectionModel().getSelectedItem();
        if (typeItem == null) {
            MessageDialog.showDialog("请选择总结类型");
            return;
        }
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            MessageDialog.showDialog("标题不能为空");
            return;
        }

        DataRequest req = new DataRequest();
        req.add("projectId", projectId);
        req.add("action", action);
        req.add("summaryType", typeItem.getValue());
        req.add("title", title);
        req.add("content", contentArea.getText());

        DataResponse res = HttpRequestUtil.request("/api/practice/submitSummary", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("SUBMIT".equals(action) ? "总结已提交" : "草稿已保存");
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
        Stage stage = (Stage) dialogTitle.getScene().getWindow();
        stage.close();
    }
}
