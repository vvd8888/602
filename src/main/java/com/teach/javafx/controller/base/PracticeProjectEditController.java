package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.CommonMethod;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PracticeProjectEditController {

    @FXML private Label dialogTitle;
    @FXML private TextField titleField;
    @FXML private TextField teamNameField;
    @FXML private TextField mentorField;
    @FXML private DatePicker startDateField;
    @FXML private DatePicker endDateField;
    @FXML private TextArea descArea;
    @FXML private TextField memberSearchField;
    @FXML private VBox membersContainer;
    @FXML private Button draftBtn;
    @FXML private Button submitEditBtn;

    private Integer editingProjectId;
    private String projectType;
    private Integer keyTopicId;
    private ToolController ownerController;

    // 临时队员列表
    private List<Map<String, Object>> tempMembers = new ArrayList<>();

    public void setProject(Integer projectId, String projectType, Integer keyTopicId) {
        this.editingProjectId = projectId;
        this.projectType = projectType;
        this.keyTopicId = keyTopicId;

        if (projectId != null && projectId > 0) {
            dialogTitle.setText("编辑项目");
            loadExistingProject(projectId);
        } else {
            dialogTitle.setText("新建项目");
            if ("KEY".equals(projectType) && keyTopicId != null) {
                // 预填选题信息
                loadKeyTopic(keyTopicId);
            }
        }
    }

    public void setOwnerController(ToolController owner) {
        this.ownerController = owner;
    }

    private void loadExistingProject(Integer projectId) {
        DataRequest req = new DataRequest();
        req.add("projectId", projectId);
        DataResponse res = HttpRequestUtil.request("/api/practice/getProjectDetail", req);
        if (res == null || res.getCode() != 0) return;
        Map<String, Object> data = (Map<String, Object>) res.getData();

        titleField.setText(CommonMethod.getString(data, "title"));
        teamNameField.setText(CommonMethod.getString(data, "teamName"));
        mentorField.setText(CommonMethod.getString(data, "mentor"));
        String sd = CommonMethod.getString(data, "startDate");
        String ed = CommonMethod.getString(data, "endDate");
        if (sd != null && !sd.isEmpty()) { try { startDateField.setValue(LocalDate.parse(sd.substring(0, 10))); } catch (Exception ignored) {} }
        if (ed != null && !ed.isEmpty()) { try { endDateField.setValue(LocalDate.parse(ed.substring(0, 10))); } catch (Exception ignored) {} }
        descArea.setText(CommonMethod.getString(data, "description"));

        this.projectType = CommonMethod.getString(data, "projectType");
        this.keyTopicId = CommonMethod.getInteger(data, "keyTopicId");

        List<Map> members = (List<Map>) data.get("members");
        if (members != null) {
            for (Map m : members) {
                Map<String, Object> mm = new HashMap<>();
                mm.put("personId", CommonMethod.getInteger(m, "personId"));
                mm.put("personName", CommonMethod.getString(m, "personName"));
                mm.put("className", CommonMethod.getString(m, "className"));
                if (!"LEADER".equals(CommonMethod.getString(m, "role"))) {
                    tempMembers.add(mm);
                }
            }
        }
        rebuildMembersUI();
    }

    private void loadKeyTopic(Integer keyTopicId) {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/practice/getAvailableKeyTopics", req);
        if (res == null || res.getCode() != 0) return;
        List<Map> topics = (List<Map>) res.getData();
        if (topics == null) return;
        for (Map t : topics) {
            if (keyTopicId.equals(CommonMethod.getInteger(t, "keyTopicId"))) {
                titleField.setText(CommonMethod.getString(t, "title"));
                descArea.setText(CommonMethod.getString(t, "requirements"));
                break;
            }
        }
    }

    @FXML
    protected void onSearchMember() {
        String keyword = memberSearchField.getText().trim();
        if (keyword.isEmpty()) {
            MessageDialog.showDialog("请输入学号或姓名");
            return;
        }

        // 搜索学生
        DataRequest req = new DataRequest();
        req.add("numName", keyword);
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentList", req);
        if (res == null || res.getCode() != 0 || res.getData() == null) {
            MessageDialog.showDialog("未找到该学生");
            return;
        }

        List<Map> students = (List<Map>) res.getData();
        if (students.isEmpty()) {
            MessageDialog.showDialog("未找到该学生");
            return;
        }

        Map student = students.get(0);
        Integer personId = CommonMethod.getInteger(student, "personId");
        String name = CommonMethod.getString(student, "name");
        String className = CommonMethod.getString(student, "className");

        if (personId == null) {
            MessageDialog.showDialog("未找到该学生");
            return;
        }

        // 检查是否是队长本人
        Integer myPersonId = AppStore.getJwt().getId();
        if (personId.equals(myPersonId)) {
            MessageDialog.showDialog("队长已在团队中，无需重复添加");
            return;
        }

        // 检查是否已在列表中
        for (Map m : tempMembers) {
            if (personId.equals(m.get("personId"))) {
                MessageDialog.showDialog("该学生已在队员列表中");
                return;
            }
        }

        // 检查队员人数上限
        if (tempMembers.size() >= 10) {
            MessageDialog.showDialog("队员人数已达上限（最多10人）");
            return;
        }

        Map<String, Object> member = new HashMap<>();
        member.put("personId", personId);
        member.put("personName", name);
        member.put("className", className);
        tempMembers.add(member);

        memberSearchField.clear();
        rebuildMembersUI();
    }

    @FXML
    protected void onAddMemberButtonClick() {
        onSearchMember();
    }

    private void rebuildMembersUI() {
        membersContainer.getChildren().clear();
        for (int i = 0; i < tempMembers.size(); i++) {
            Map<String, Object> m = tempMembers.get(i);
            HBox row = new HBox(8);
            row.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 2 4;");

            Label info = new Label(
                    CommonMethod.getString(m, "personName") + " (" +
                            CommonMethod.getString(m, "className") + ")");
            info.setStyle("-fx-font-size: 12px;");

            Button removeBtn = new Button("移除");
            removeBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #ef4444; -fx-text-fill: white;");
            final int idx = i;
            removeBtn.setOnAction(e -> {
                tempMembers.remove(idx);
                rebuildMembersUI();
            });

            row.getChildren().addAll(info, removeBtn);
            membersContainer.getChildren().add(row);
        }
    }

    @FXML
    protected void onSaveDraftButtonClick() { doSave("DRAFT"); }

    @FXML
    protected void onSubmitButtonClick() { doSave("SUBMIT"); }

    private void doSave(String action) {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            MessageDialog.showDialog("项目名称不能为空");
            return;
        }

        LocalDate startDate = startDateField.getValue();
        LocalDate endDate = endDateField.getValue();
        LocalDate today = LocalDate.now();

        if (startDate == null) {
            MessageDialog.showDialog("请选择开始日期");
            return;
        }
        if (startDate.isBefore(today)) {
            MessageDialog.showDialog("开始日期不能早于今天");
            return;
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            MessageDialog.showDialog("结束日期不能早于开始日期");
            return;
        }

        DataRequest req = new DataRequest();
        req.add("projectId", editingProjectId);
        req.add("action", action);
        req.add("title", title);
        req.add("description", descArea.getText());
        req.add("projectType", projectType);
        req.add("keyTopicId", keyTopicId);
        req.add("teamName", teamNameField.getText().trim());
        req.add("mentor", mentorField.getText().trim());
        req.add("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        req.add("endDate", endDate != null ? endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
        req.add("members", tempMembers);

        DataResponse res = HttpRequestUtil.request("/api/practice/saveProject", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("SUBMIT".equals(action) ? "已提交审核" : "草稿已保存");
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
