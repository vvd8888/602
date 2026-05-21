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
 * MyReservationsController 我的预约控制类
 * 对应 my-reservations.fxml
 */
public class MyReservationsController {
    @FXML
    private TableView<Map> reservationTableView;
    @FXML
    private TableColumn<Map, String> routeColumn;
    @FXML
    private TableColumn<Map, String> departureTimeColumn;
    @FXML
    private TableColumn<Map, String> reservationTimeColumn;
    @FXML
    private TableColumn<Map, String> statusColumn;
    @FXML
    private TableColumn<Map, String> remarkColumn;

    @FXML
    private Label detailTitleLabel;
    @FXML
    private Label detailRoute;
    @FXML
    private Label detailDepartureCampus;
    @FXML
    private Label detailArrivalCampus;
    @FXML
    private Label detailDepartureTime;
    @FXML
    private Label detailArrivalTime;
    @FXML
    private Label detailReservationTime;
    @FXML
    private Label detailStatus;
    @FXML
    private Label detailCancelReason;
    @FXML
    private TextArea detailRemark;

    private List<Map> reservationList = new ArrayList<>();
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private Map selectedReservation = null;
    private List<Map> campusList = new ArrayList<>();

    @FXML
    public void initialize() {
        // 初始化表格列
        routeColumn.setCellValueFactory(new MapValueFactory<>("route"));
        departureTimeColumn.setCellValueFactory(new MapValueFactory<>("departureTime"));
        reservationTimeColumn.setCellValueFactory(new MapValueFactory<>("reservationTime"));
        statusColumn.setCellValueFactory(new MapValueFactory<>("statusText"));
        remarkColumn.setCellValueFactory(new MapValueFactory<>("remark"));

        // 监听表格行选择
        reservationTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedReservation = newVal;
                showReservationDetail(newVal);
            }
        });

        // 加载校区列表
        loadCampusList();
        // 加载预约列表
        loadReservationList();
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
     * 加载预约列表
     */
    private void loadReservationList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/schoolbus/getMyReservations", req);
        if (res != null && res.getCode() == 0) {
            reservationList = (List<Map>) res.getData();
            updateTableView();
        } else {
            MessageDialog.showDialog("加载预约列表失败：" + (res != null ? res.getMsg() : "网络错误"));
        }
    }

    /**
     * 更新表格数据
     */
    private void updateTableView() {
        observableList.clear();
        for (Map reservation : reservationList) {
            // 构建显示字段
            String departureCampus = (String) reservation.get("departureCampus");
            String arrivalCampus = (String) reservation.get("arrivalCampus");
            reservation.put("route", getCampusName(departureCampus) + " → " + getCampusName(arrivalCampus));

            observableList.add(reservation);
        }
        reservationTableView.setItems(observableList);
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
     * 显示预约详情
     */
    private void showReservationDetail(Map reservation) {
        detailTitleLabel.setText("预约 #" + reservation.get("id"));
        
        String departureCampus = (String) reservation.get("departureCampus");
        String arrivalCampus = (String) reservation.get("arrivalCampus");
        detailRoute.setText(getCampusName(departureCampus) + " → " + getCampusName(arrivalCampus));
        detailDepartureCampus.setText(getCampusName(departureCampus));
        detailArrivalCampus.setText(getCampusName(arrivalCampus));
        
        detailDepartureTime.setText((String) reservation.get("departureTime"));
        detailReservationTime.setText((String) reservation.get("reservationTime"));
        detailStatus.setText((String) reservation.get("statusText"));

        String cancelReason = (String) reservation.get("cancelReason");
        detailCancelReason.setText(cancelReason != null ? cancelReason : "");

        String remark = (String) reservation.get("remark");
        detailRemark.setText(remark != null ? remark : "");
    }

    /**
     * 清空详情面板
     */
    private void clearDetail() {
        selectedReservation = null;
        detailTitleLabel.setText("选择预约查看详情");
        detailRoute.setText("");
        detailDepartureCampus.setText("");
        detailArrivalCampus.setText("");
        detailDepartureTime.setText("");
        detailArrivalTime.setText("");
        detailReservationTime.setText("");
        detailStatus.setText("");
        detailCancelReason.setText("");
        detailRemark.setText("");
    }

    /**
     * 刷新按钮
     */
    @FXML
    protected void onRefreshButtonClick() {
        loadReservationList();
        clearDetail();
    }

    /**
     * 取消预约
     */
    @FXML
    protected void onCancelReservationClick() {
        if (selectedReservation == null) {
            MessageDialog.showDialog("请先选择一个预约记录");
            return;
        }

        // 使用 toInteger() 方法安全转换（Gson 解析的数字默认是 Double）
        Integer status = toInteger(selectedReservation.get("status"));
        if (status != null && (status == 2 || status == 3)) {
            MessageDialog.showDialog("该预约已取消或已完成，无法再次取消");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("取消预约");
        dialog.setHeaderText("请输入取消原因（可选）");
        dialog.setContentText("取消原因：");

        dialog.showAndWait().ifPresent(cancelReason -> {
            performCancelReservation(cancelReason);
        });
    }

    /**
     * 执行取消预约操作
     */
    private void performCancelReservation(String cancelReason) {
        // 确保 reservationId 是整数类型
        Object reservationIdObj = selectedReservation.get("id");
        Integer reservationId = toInteger(reservationIdObj);
        
        if (reservationId == null) {
            MessageDialog.showDialog("预约ID格式错误");
            return;
        }
        
        // 使用 DataRequest（后端使用 DataRequest.getInteger("reservationId")）
        DataRequest req = new DataRequest();
        req.add("reservationId", reservationId);
        if (cancelReason != null && !cancelReason.trim().isEmpty()) {
            req.add("cancelReason", cancelReason.trim());
        }

        System.out.println("取消预约请求参数: reservationId=" + reservationId + ", cancelReason=" + cancelReason);
        DataResponse res = HttpRequestUtil.request("/api/schoolbus/cancelReservation", req);
        System.out.println("取消预约响应结果: code=" + (res != null ? res.getCode() : "null") + ", msg=" + (res != null ? res.getMsg() : "null"));
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("取消预约成功");
            loadReservationList();
            clearDetail();
        } else {
            MessageDialog.showDialog("取消预约失败：" + (res != null ? res.getMsg() : "网络错误"));
        }
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
     * 刷新（实现doRefresh接口）
     */
    public void doRefresh() {
        loadReservationList();
    }
}
