package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ShuttleBusController 班车班次管理控制类
 * 对应 shuttle-bus.fxml
 */
public class ShuttleBusController {
    @FXML
    private TableView<Map> scheduleTableView;
    @FXML
    private TableColumn<Map, String> routeColumn;
    @FXML
    private TableColumn<Map, String> departureTimeColumn;
    @FXML
    private TableColumn<Map, String> arrivalTimeColumn;
    @FXML
    private TableColumn<Map, String> seatsColumn;
    @FXML
    private TableColumn<Map, String> statusColumn;

    @FXML
    private Label detailTitleLabel;
    @FXML
    private Label detailDepartureCampus;
    @FXML
    private Label detailArrivalCampus;
    @FXML
    private Label detailDepartureTime;
    @FXML
    private Label detailArrivalTime;
    @FXML
    private Label detailTotalSeats;
    @FXML
    private Label detailReservedSeats;
    @FXML
    private Label detailStatus;
    @FXML
    private TextArea detailRemark;

    private List<Map> scheduleList = new ArrayList<>();
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private Map selectedSchedule = null;
    private List<Map> campusList = new ArrayList<>();

    @FXML
    public void initialize() {
        // 初始化表格列
        routeColumn.setCellValueFactory(new MapValueFactory<>("route"));
        departureTimeColumn.setCellValueFactory(new MapValueFactory<>("departureTime"));
        arrivalTimeColumn.setCellValueFactory(new MapValueFactory<>("arrivalTime"));
        seatsColumn.setCellValueFactory(new MapValueFactory<>("seats"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("statusText"));

        // 监听表格行选择
        scheduleTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSchedule = newVal;
                showScheduleDetail(newVal);
            }
        });

        // 加载校区列表
        loadCampusList();
        // 加载班次列表
        loadScheduleList();
    }

    /**
     * 加载校区列表
     */
    private void loadCampusList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/schoolbus/getCampusList", req);
        if (res != null && res.getCode() == 0) {
            campusList = (List<Map>) res.getData();
            System.out.println("校区列表加载成功，共 " + campusList.size() + " 个校区");
        }
    }

    /**
     * 加载班次列表
     */
    private void loadScheduleList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/schoolbus/getAllSchedules", req);
        if (res != null && res.getCode() == 0) {
            scheduleList = (List<Map>) res.getData();
            updateTableView();
        } else {
            MessageDialog.showDialog("加载班次列表失败：" + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    /**
     * 更新表格数据
     */
    private void updateTableView() {
        observableList.clear();
        for (Map schedule : scheduleList) {
            // 构建显示字段
            String departureCampus = (String) schedule.get("departureCampus");
            String arrivalCampus = (String) schedule.get("arrivalCampus");
            schedule.put("route", getCampusName(departureCampus) + " → " + getCampusName(arrivalCampus));

            Integer totalSeats = toInteger(schedule.get("totalSeats"));
            Integer reservedSeats = toInteger(schedule.get("reservedSeats"));
            if (totalSeats != null && reservedSeats != null) {
                schedule.put("seats", reservedSeats + "/" + totalSeats);
            }

            observableList.add(schedule);
        }
        scheduleTableView.setItems(observableList);
    }

    /**
     * 根据校区编码获取校区名称
     */
    private String getCampusName(String campusCode) {
        if (campusCode == null) return "";
        for (Map campus : campusList) {
            if (campusCode.equals(campus.get("campusCode"))) {
                return (String) campus.get("campusName");
            }
        }
        return campusCode;
    }

    /**
     * 根据校区名称获取校区编码
     */
    private String getCampusCode(String campusName) {
        if (campusName == null) return "";
        for (Map campus : campusList) {
            if (campusName.equals(campus.get("campusName"))) {
                return (String) campus.get("campusCode");
            }
        }
        return campusName;
    }

    /**
     * 显示班次详情
     */
    private void showScheduleDetail(Map schedule) {
        detailTitleLabel.setText("班次 #" + schedule.get("id"));
        detailDepartureCampus.setText(getCampusName((String) schedule.get("departureCampus")));
        detailArrivalCampus.setText(getCampusName((String) schedule.get("arrivalCampus")));
        detailDepartureTime.setText((String) schedule.get("departureTime"));
        detailArrivalTime.setText((String) schedule.get("arrivalTime"));
        
        Integer totalSeats = toInteger(schedule.get("totalSeats"));
        Integer reservedSeats = toInteger(schedule.get("reservedSeats"));
        detailTotalSeats.setText(totalSeats != null ? totalSeats.toString() : "");
        detailReservedSeats.setText(reservedSeats != null ? reservedSeats.toString() : "");
        detailStatus.setText((String) schedule.get("statusText"));

        String remark = (String) schedule.get("remark");
        detailRemark.setText(remark != null ? remark : "");
    }

    /**
     * 刷新按钮
     */
    @FXML
    protected void onRefreshButtonClick() {
        loadScheduleList();
        clearDetail();
    }

    /**
     * 清空详情面板
     */
    private void clearDetail() {
        selectedSchedule = null;
        detailTitleLabel.setText("选择班次查看详情");
        detailDepartureCampus.setText("");
        detailArrivalCampus.setText("");
        detailDepartureTime.setText("");
        detailArrivalTime.setText("");
        detailTotalSeats.setText("");
        detailReservedSeats.setText("");
        detailStatus.setText("");
        detailRemark.setText("");
    }

    /**
     * 新增班次
     */
    @FXML
    protected void onNewScheduleClick() {
        showScheduleEditDialog(null);
    }

    /**
     * 编辑班次
     */
    @FXML
    protected void onEditScheduleClick() {
        if (selectedSchedule == null) {
            MessageDialog.showDialog("请先选择一个班次");
            return;
        }
        showScheduleEditDialog(selectedSchedule);
    }

    /**
     * 显示班次编辑对话框
     */
    private void showScheduleEditDialog(Map schedule) {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(schedule == null ? "新增班次" : "编辑班次");
        dialog.setHeaderText(schedule == null ? "请填写班次信息" : "修改班次信息");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        ComboBox<String> departureCampusCombo = new ComboBox<>();
        ComboBox<String> arrivalCampusCombo = new ComboBox<>();
        // 显示中文校区名称
        departureCampusCombo.getItems().addAll(campusList.stream()
                .map(c -> (String) c.get("campusName")).toArray(String[]::new));
        arrivalCampusCombo.getItems().addAll(campusList.stream()
                .map(c -> (String) c.get("campusName")).toArray(String[]::new));

        TextField departureTimeField = new TextField();
        departureTimeField.setPromptText("例如：07:30");
        TextField arrivalTimeField = new TextField();
        arrivalTimeField.setPromptText("例如：08:00");
        TextField totalSeatsField = new TextField();
        totalSeatsField.setPromptText("例如：45");
        TextArea remarkArea = new TextArea();
        remarkArea.setPrefRowCount(3);

        // 如果是编辑，填充数据（将编码转换为中文名称显示）
        if (schedule != null) {
            String departureCode = (String) schedule.get("departureCampus");
            String arrivalCode = (String) schedule.get("arrivalCampus");
            departureCampusCombo.setValue(getCampusName(departureCode));
            arrivalCampusCombo.setValue(getCampusName(arrivalCode));
            departureTimeField.setText((String) schedule.get("departureTime"));
            arrivalTimeField.setText((String) schedule.get("arrivalTime"));
            totalSeatsField.setText(schedule.get("totalSeats") != null ? schedule.get("totalSeats").toString() : "");
            remarkArea.setText((String) schedule.get("remark"));
        }

        grid.add(new Label("起点校区 *:"), 0, 0);
        grid.add(departureCampusCombo, 1, 0);
        grid.add(new Label("终点校区 *:"), 0, 1);
        grid.add(arrivalCampusCombo, 1, 1);
        grid.add(new Label("发车时间 *:"), 0, 2);
        grid.add(departureTimeField, 1, 2);
        grid.add(new Label("到达时间:"), 0, 3);
        grid.add(arrivalTimeField, 1, 3);
        grid.add(new Label("座位总数 *:"), 0, 4);
        grid.add(totalSeatsField, 1, 4);
        grid.add(new Label("备注:"), 0, 5);
        grid.add(remarkArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // 验证必填字段
                String departureCampus = departureCampusCombo.getValue();
                String arrivalCampus = arrivalCampusCombo.getValue();
                String departureTime = departureTimeField.getText();
                String totalSeatsText = totalSeatsField.getText();

                if (departureCampus == null || departureCampus.isEmpty()) {
                    MessageDialog.showDialog("请选择起点校区");
                    return null;
                }
                if (arrivalCampus == null || arrivalCampus.isEmpty()) {
                    MessageDialog.showDialog("请选择终点校区");
                    return null;
                }
                if (departureTime == null || departureTime.trim().isEmpty()) {
                    MessageDialog.showDialog("请输入发车时间");
                    return null;
                }
                if (totalSeatsText == null || totalSeatsText.trim().isEmpty()) {
                    MessageDialog.showDialog("请输入座位总数");
                    return null;
                }

                Integer totalSeats;
                try {
                    totalSeats = Integer.parseInt(totalSeatsText.trim());
                    if (totalSeats <= 0) {
                        MessageDialog.showDialog("座位总数必须大于0");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    MessageDialog.showDialog("座位总数格式错误，请输入数字");
                    return null;
                }

                Map<String, Object> result = new java.util.HashMap<>();
                if (schedule != null) {
                    result.put("id", schedule.get("id"));
                }
                // 将中文名称转换回校区编码
                result.put("departureCampus", getCampusCode(departureCampus));
                result.put("arrivalCampus", getCampusCode(arrivalCampus));
                result.put("departureTime", departureTime.trim());
                result.put("arrivalTime", arrivalTimeField.getText() != null ? arrivalTimeField.getText().trim() : "");
                result.put("totalSeats", totalSeats);
                result.put("remark", remarkArea.getText());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            saveSchedule(result);
        });
    }

    /**
     * 安全地将各种数值类型转换为Integer
     * 处理Double、Long、Integer等不同类型的数值
     */
    private Integer toInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 保存班次
     */
    private void saveSchedule(Map<String, Object> data) {
        System.out.println("=== 开始保存班次 ===");
        System.out.println("保存数据: " + data);
        
        // 构建请求对象（直接发送JSON，不使用DataRequest包装）
        Map<String, Object> reqData = new java.util.HashMap<>();
        reqData.put("departureCampus", data.get("departureCampus"));
        reqData.put("arrivalCampus", data.get("arrivalCampus"));
        reqData.put("departureTime", data.get("departureTime"));
        reqData.put("arrivalTime", data.get("arrivalTime"));
        reqData.put("totalSeats", data.get("totalSeats"));
        reqData.put("remark", data.get("remark"));

        if (data.containsKey("id")) {
            reqData.put("id", data.get("id"));
        }

        System.out.println("保存班次请求参数: " + reqData);
        // 使用 requestWithoutUsername 直接发送JSON对象
        DataResponse res = HttpRequestUtil.requestWithoutUsername("/api/schoolbus/saveSchedule", reqData);
        System.out.println("保存班次响应结果: code=" + (res != null ? res.getCode() : "null") + ", msg=" + (res != null ? res.getMsg() : "null"));
        
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog(data.containsKey("id") ? "编辑成功" : "新增成功");
            loadScheduleList();
        } else {
            String errorMsg = "操作失败：" + (res != null ? res.getMsg() : "网络错误");
            System.err.println(errorMsg);
            MessageDialog.showDialog(errorMsg);
        }
    }

    /**
     * 删除班次
     */
    @FXML
    protected void onDeleteScheduleClick() {
        if (selectedSchedule == null) {
            MessageDialog.showDialog("请先选择一个班次");
            return;
        }

        int choice = MessageDialog.choiceDialog("确定要删除该班次吗？\n\n" +
                "路线：" + selectedSchedule.get("route") + "\n" +
                "时间：" + selectedSchedule.get("departureTime"));

        if (choice == MessageDialog.CHOICE_YES) {
            // 确保 id 是整数类型
            Object idObj = selectedSchedule.get("id");
            Integer id = toInteger(idObj);
            
            if (id == null) {
                MessageDialog.showDialog("班次ID格式错误");
                return;
            }
            
            // 使用 DataRequest（后端期望 DataRequest 格式）
            DataRequest req = new DataRequest();
            req.add("id", id);

            System.out.println("删除班次请求参数: id=" + id);
            DataResponse res = HttpRequestUtil.request("/api/schoolbus/deleteSchedule", req);
            System.out.println("删除班次响应结果: code=" + (res != null ? res.getCode() : "null") + ", msg=" + (res != null ? res.getMsg() : "null"));
            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("删除成功");
                loadScheduleList();
                clearDetail();
            } else {
                MessageDialog.showDialog("删除失败：" + (res != null ? res.getMsg() : "网络错误"));
            }
        }
    }

    /**
     * 查看预约
     */
    @FXML
    protected void onViewReservationsClick() {
        if (selectedSchedule == null) {
            MessageDialog.showDialog("请先选择一个班次");
            return;
        }

        // 确保 scheduleId 是整数类型
        Object scheduleIdObj = selectedSchedule.get("id");
        Integer scheduleId = toInteger(scheduleIdObj);
        
        if (scheduleId == null) {
            MessageDialog.showDialog("班次ID格式错误");
            return;
        }
        
        // 使用 DataRequest（后端期望 DataRequest 格式）
        DataRequest req = new DataRequest();
        req.add("scheduleId", scheduleId);

        System.out.println("查看预约请求参数: scheduleId=" + scheduleId);
        DataResponse res = HttpRequestUtil.request("/api/schoolbus/getScheduleReservations", req);
        System.out.println("查看预约响应结果: code=" + (res != null ? res.getCode() : "null") + ", msg=" + (res != null ? res.getMsg() : "null"));
        
        if (res != null && res.getCode() == 0) {
            List<Map> reservations = (List<Map>) res.getData();
            if (reservations != null && !reservations.isEmpty()) {
                StringBuilder sb = new StringBuilder("预约列表（" + reservations.size() + "条）：\n\n");
                for (Map r : reservations) {
                    sb.append("ID: ").append(r.get("id")).append("\n");
                    // 后端返回的是 personName 和 personNum，不是 username
                    sb.append("姓名: ").append(r.get("personName")).append("\n");
                    sb.append("学号/工号: ").append(r.get("personNum")).append("\n");
                    sb.append("类型: ").append(r.get("userTypeText")).append("\n");
                    sb.append("状态: ").append(r.get("statusText")).append("\n");
                    sb.append("预约时间: ").append(r.get("reservationTime")).append("\n");
                    if (r.get("remark") != null && !r.get("remark").toString().isEmpty()) {
                        sb.append("备注: ").append(r.get("remark")).append("\n");
                    }
                    sb.append("---\n");
                }
                MessageDialog.showDialog(sb.toString());
            } else {
                MessageDialog.showDialog("该班次暂无预约记录");
            }
        } else {
            MessageDialog.showDialog("获取预约列表失败：" + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    /**
     * 刷新（实现doRefresh接口）
     */
    public void doRefresh() {
        loadScheduleList();
    }
}
