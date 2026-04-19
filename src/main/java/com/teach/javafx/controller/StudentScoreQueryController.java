package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;

import java.util.List;
import java.util.Map;

public class StudentScoreQueryController {
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map,String> courseNumColumn;
    @FXML
    private TableColumn<Map,String> courseNameColumn;
    @FXML
    private TableColumn<Map,String> creditColumn;
    @FXML
    private TableColumn<Map,String> markColumn;

    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        courseNumColumn.setCellValueFactory(new MapValueFactory<>("courseNum"));
        courseNameColumn.setCellValueFactory(new MapValueFactory<>("courseName"));
        creditColumn.setCellValueFactory(new MapValueFactory<>("credit"));
        markColumn.setCellValueFactory(new MapValueFactory<>("mark"));

        loadScores();
    }

    private void loadScores() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/score/getScoreList", req);

        if(res != null && res.getCode() == 0) {
            List<Map> scoreList = (List<Map>)res.getData();
            observableList.clear();
            if (scoreList != null) {
                observableList.addAll(scoreList);
            }
            dataTableView.setItems(observableList);
        }
    }
}