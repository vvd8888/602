package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * NoticeListController 学生端通知列表
 */
public class NoticeListController extends ToolController {

    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, String> noticeTypeColumn;
    @FXML private TableColumn<Map, String> creatorNameColumn;
    @FXML private TableColumn<Map, String> publishTimeColumn;
    @FXML private TableColumn<Map, String> isReadColumn;

    @FXML private ToggleButton allFilterBtn;
    @FXML private ToggleButton unreadFilterBtn;
    @FXML private ToggleButton readFilterBtn;
    @FXML private ComboBox<OptionItem> typeComboBox;

    @FXML private Label detailTitleLabel;
    @FXML private Label detailTypeLabel;
    @FXML private Label detailCreatorLabel;
    @FXML private Label detailTimeLabel;
    @FXML private Label detailReadInfoLabel;
    @FXML private TextArea detailContentArea;

    private ArrayList<Map> noticeList = new ArrayList<>();
    private ObservableList<Map> observableList = FXCollections.observableArrayList();
    private ToggleGroup filterGroup;
    private boolean refreshing = false;

    @FXML
    public void initialize() {
        // 表格列绑定
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        noticeTypeColumn.setCellValueFactory(new MapValueFactory<>("noticeType"));
        creatorNameColumn.setCellValueFactory(new MapValueFactory<>("creatorName"));
        publishTimeColumn.setCellValueFactory(new MapValueFactory<>("publishTime"));
        isReadColumn.setCellValueFactory(new MapValueFactory<>("isReadStatus"));

        // 行选择监听
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        tsm.getSelectedIndices().addListener(this::onTableRowSelect);

        // 筛选按钮组
        filterGroup = new ToggleGroup();
        allFilterBtn.setToggleGroup(filterGroup);
        unreadFilterBtn.setToggleGroup(filterGroup);
        readFilterBtn.setToggleGroup(filterGroup);

        // 类型下拉
        typeComboBox.getItems().add(new OptionItem(null, "", "全部类型"));
        typeComboBox.getItems().add(new OptionItem(null, "SYSTEM", "系统通知"));
        typeComboBox.getItems().add(new OptionItem(null, "COURSE", "课程通知"));
        typeComboBox.getItems().add(new OptionItem(null, "ACTIVITY", "活动通知"));
        typeComboBox.getSelectionModel().select(0);

        loadNotices();
    }

    private String getFilter() {
        ToggleButton selected = (ToggleButton) filterGroup.getSelectedToggle();
        if (selected == unreadFilterBtn) return "UNREAD";
        if (selected == readFilterBtn) return "READ";
        return "ALL";
    }

    @FXML
    protected void onFilterChanged() { loadNotices(); }

    @FXML
    protected void onRefreshButtonClick() { loadNotices(); }

    private void loadNotices() {
        refreshing = true;
        int selectedIndex = dataTableView.getSelectionModel().getSelectedIndex();

        DataRequest req = new DataRequest();
        req.add("filter", getFilter());
        OptionItem typeItem = typeComboBox.getSelectionModel().getSelectedItem();
        req.add("noticeType", typeItem != null ? typeItem.getValue() : "");

        DataResponse res = HttpRequestUtil.request("/api/notice/getNoticeList", req);
        if (res != null && res.getCode() == 0) {
            noticeList = (ArrayList<Map>) res.getData();
            if (noticeList == null) noticeList = new ArrayList<>();
            for (Map m : noticeList) {
                Boolean isRead = CommonMethod.getBoolean(m, "isRead");
                m.put("isReadStatus", isRead ? "已读" : "● 未读");
            }
            observableList.clear();
            observableList.addAll(noticeList);
            dataTableView.setItems(observableList);
        }

        if (selectedIndex >= 0 && selectedIndex < observableList.size()) {
            dataTableView.getSelectionModel().select(selectedIndex);
        }
        refreshing = false;
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        if (refreshing) return;
        Map<String, Object> row = dataTableView.getSelectionModel().getSelectedItem();
        if (row == null) {
            clearDetail();
            return;
        }
        Integer noticeId = CommonMethod.getInteger(row, "noticeId");
        loadDetail(noticeId);
    }

    private void loadDetail(Integer noticeId) {
        DataRequest req = new DataRequest();
        req.add("noticeId", noticeId);
        DataResponse res = HttpRequestUtil.request("/api/notice/getNoticeDetail", req);
        if (res != null && res.getCode() == 0) {
            Map<String, Object> data = (Map<String, Object>) res.getData();
            detailTitleLabel.setText(CommonMethod.getString(data, "title"));
            detailTypeLabel.setText("类型：" + CommonMethod.getString(data, "noticeType"));
            detailCreatorLabel.setText("发布者：" + CommonMethod.getString(data, "creatorName"));
            detailTimeLabel.setText("发布时间：" + CommonMethod.getString(data, "publishTime"));
            String readInfo = "已读 " + data.get("readCount") + " / " + data.get("targetCount")
                    + "（" + CommonMethod.getString(data, "readRate") + "）";
            detailReadInfoLabel.setText(readInfo);
            detailContentArea.setText(CommonMethod.getString(data, "content"));

            // 列表刷新以更新已读状态（延迟避免与选择事件冲突）
            javafx.application.Platform.runLater(this::loadNotices);
        }
    }

    private void clearDetail() {
        detailTitleLabel.setText("选择左侧通知查看详情");
        detailTypeLabel.setText("");
        detailCreatorLabel.setText("");
        detailTimeLabel.setText("");
        detailReadInfoLabel.setText("");
        detailContentArea.setText("");
    }

    @Override
    public void doRefresh() {
        loadNotices();
    }
}
