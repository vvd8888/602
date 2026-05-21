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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ShuttleReservationController 班车预约控制类
 * 对应 shuttle-reserve.fxml
 */
public class ShuttleReservationController {
    @FXML
    private TableView<Map> scheduleTableView;
    @FXML
    private TableColumn<Map, String> routeColumn;
    @FXML
    private TableColumn<Map, String> departureTimeColumn;
    @FXML
    private TableColumn<Map, String> arrivalTimeColumn;
    @FXML
    private TableColumn<Map, String> availableSeatsColumn;
    @FXML
    private TableColumn<Map, String> statusColumn;

    @FXML
    private ComboBox<String> departureCampusCombo;
    @FXML
    private ComboBox<String> arrivalCampusCombo;

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
    private Label detailAvailableSeats;
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
        availableSeatsColumn.setCellValueFactory(new MapValueFactory<>("availableSeats"));
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
        // 加载可预约班次
        loadActiveSchedules();
    }

    /**
     * 加载校区列表
     */
    private void loadCampusList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/schoolbus/getCampusList", req);
        if (res != null && res.getCode() == 0) {
            campusList = (List<Map>) res.getData();
            // 填充下拉框（显示中文名称）
            for (Map campus : campusList) {
                String name = (String) campus.get("campusName");
                departureCampusCombo.getItems().add(name);
                arrivalCampusCombo.getItems().add(name);
            }
            departureCampusCombo.getItems().add(0, "");
            arrivalCampusCombo.getItems().add(0, "");
            departureCampusCombo.getSelectionModel().select(0);
            arrivalCampusCombo.getSelectionModel().select(0);
        }
    }

    /**
     * 加载正常运营的班次
     */
    private void loadActiveSchedules() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/schoolbus/getActiveSchedules", req);
        if (res != null && res.getCode() == 0) {
            scheduleList = (List<Map>) res.getData();
            updateTableView();
        } else {
            MessageDialog.showDialog("加载班次列表失败：" + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    /**
     * 按路线查询班次
     */
    @FXML
    protected void onQueryButtonClick() {
        String departure = departureCampusCombo.getValue();
        String arrival = arrivalCampusCombo.getValue();

        DataRequest req = new DataRequest();
        if (departure != null && !departure.isEmpty()) {
            // 将中文名称转换回校区编码
            req.add("departureCampus", getCampusCode(departure));
        }
        if (arrival != null && !arrival.isEmpty()) {
            req.add("arrivalCampus", getCampusCode(arrival));
        }

        DataResponse res = HttpRequestUtil.request("/api/schoolbus/getSchedulesByRoute", req);
        if (res != null && res.getCode() == 0) {
            scheduleList = (List<Map>) res.getData();
            updateTableView();
        } else {
            MessageDialog.showDialog("查询失败：" + (res != null ? res.getMsg() : "网络错误"));
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
            schedule.put("route", getCampusName(departureCampus) + " \u2192 " + getCampusName(arrivalCampus));

            Integer totalSeats = toInteger(schedule.get("totalSeats"));
            Integer reservedSeats = toInteger(schedule.get("reservedSeats"));
            if (totalSeats != null && reservedSeats != null) {
                schedule.put("availableSeats", (totalSeats - reservedSeats) + " \u5ea7");
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
        if (campusName == null || campusName.isEmpty()) return "";
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

        if (totalSeats != null && reservedSeats != null) {
            detailAvailableSeats.setText((totalSeats - reservedSeats) + " \u5ea7");
        }

        detailStatus.setText((String) schedule.get("statusText"));

        String remark = (String) schedule.get("remark");
        detailRemark.setText(remark != null ? remark : "");
    }

    /**
     * 刷新按钮
     */
    @FXML
    protected void onRefreshButtonClick() {
        loadActiveSchedules();
        clearDetail();
        departureCampusCombo.getSelectionModel().select(0);
        arrivalCampusCombo.getSelectionModel().select(0);
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
        detailAvailableSeats.setText("");
        detailStatus.setText("");
        detailRemark.setText("");
    }

    /**
     * 预约座位
     */
    @FXML
    protected void onReserveClick() {
        if (selectedSchedule == null) {
            MessageDialog.showDialog("请先选择一个班次");
            return;
        }

        // 显示预约确认对话框
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("预约班车");
        dialog.setHeaderText("预约班车座位");
        dialog.setContentText("备注信息（可选）:");

        dialog.showAndWait().ifPresent(remark -> {
            doReserve(remark);
        });
    }

    /**
     * 执行预约
     */
    private void doReserve(String remark) {
        // 确保 scheduleId 是整数类型
        Object scheduleIdObj = selectedSchedule.get("id");
        Integer scheduleId = toInteger(scheduleIdObj);
        
        if (scheduleId == null) {
            MessageDialog.showDialog("班次ID格式错误");
            return;
        }
        
        // 使用 DataRequest（后端已调整为接收 DataRequest 格式）
        DataRequest req = new DataRequest();
        req.add("scheduleId", scheduleId);
        if (remark != null && !remark.isEmpty()) {
            req.add("remark", remark);
        }

        System.out.println("预约请求参数: " + req.getData());
        DataResponse res = HttpRequestUtil.requestWithoutUsername("/api/schoolbus/reserveSeat", req);
        System.out.println("响应结果: code=" + (res != null ? res.getCode() : "null") + ", msg=" + (res != null ? res.getMsg() : "null"));
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("预约成功！\n\n班次：" + selectedSchedule.get("route"));
            loadActiveSchedules();
            clearDetail();
        } else {
            MessageDialog.showDialog("预约失败：" + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    /**
     * 刷新（实现doRefresh接口）
     */
    public void doRefresh() {
        loadActiveSchedules();
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
}
