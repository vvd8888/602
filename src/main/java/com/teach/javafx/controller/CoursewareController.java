package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CoursewareController extends ToolController {

    @FXML private TableView<Map> dataTableView;
    @FXML private TableColumn<Map, String> titleColumn;
    @FXML private TableColumn<Map, String> descColumn;
    @FXML private TableColumn<Map, String> fileNameColumn;
    @FXML private TableColumn<Map, String> fileSizeColumn;
    @FXML private TableColumn<Map, String> courseNameColumn;
    @FXML private TableColumn<Map, String> teacherColumn;
    @FXML private TableColumn<Map, String> downloadCountColumn;
    @FXML private TableColumn<Map, String> timeColumn;
    @FXML private TableColumn<Map, Void> actionColumn;

    @FXML private TextField titleField;
    @FXML private TextField descField;
    @FXML private TextField fileField;
    @FXML private ComboBox<OptionItem> courseComboBox;
    @FXML private VBox uploadArea;

    private File selectedFile;
    private String roleName;
    private ObservableList<Map> observableList = FXCollections.observableArrayList();

    private Integer getIntegerFromObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Double) return ((Double) obj).intValue();
        if (obj instanceof Long) return ((Long) obj).intValue();
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Long getLongFromObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        if (obj instanceof Double) return ((Double) obj).longValue();
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @FXML
    public void initialize() {
        roleName = AppStore.getJwt().getRole();

        if ("ROLE_TEACHER".equals(roleName) || "ROLE_ADMIN".equals(roleName)) {
            uploadArea.setVisible(true);
            loadCourseList();
        } else {
            uploadArea.setVisible(false);
        }

        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        descColumn.setCellValueFactory(new MapValueFactory<>("description"));
        fileNameColumn.setCellValueFactory(new MapValueFactory<>("fileName"));
        fileSizeColumn.setCellValueFactory(new MapValueFactory<>("fileSize"));
        courseNameColumn.setCellValueFactory(new MapValueFactory<>("courseName"));
        teacherColumn.setCellValueFactory(new MapValueFactory<>("teacherName"));
        downloadCountColumn.setCellValueFactory(new MapValueFactory<>("downloadCount"));
        timeColumn.setCellValueFactory(new MapValueFactory<>("createTime"));

        // 设置操作列，添加下载、查看和删除按钮
        actionColumn.setCellFactory(param -> new TableCell<Map, Void>() {
            private final Button downloadBtn = new Button("下载");
            private final Button viewBtn = new Button("查看");
            private final Button deleteBtn = new Button("删除");

            {
                // 设置按钮样式，使用更短的文本
                downloadBtn.setText("下载");
                downloadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-font-size: 12px;");
                downloadBtn.setMinWidth(50);
                
                viewBtn.setText("查看");
                viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-font-size: 12px;");
                viewBtn.setMinWidth(50);
                
                deleteBtn.setText("删除");
                deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-font-size: 12px;");
                deleteBtn.setMinWidth(50);

                downloadBtn.setOnAction(event -> {
                    Map item = getTableView().getItems().get(getIndex());
                    if (item != null) {
                        downloadCourseware(item);
                    }
                });

                viewBtn.setOnAction(event -> {
                    Map item = getTableView().getItems().get(getIndex());
                    if (item != null) {
                        viewCourseware(item);
                    }
                });

                deleteBtn.setOnAction(event -> {
                    Map item = getTableView().getItems().get(getIndex());
                    if (item != null) {
                        deleteCourseware(item);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // 只有教师和管理员显示删除按钮
                    if ("ROLE_TEACHER".equals(roleName) || "ROLE_ADMIN".equals(roleName)) {
                        HBox box = new HBox(5, downloadBtn, viewBtn, deleteBtn);
                        box.setStyle("-fx-alignment: center;");
                        setGraphic(box);
                    } else {
                        HBox box = new HBox(5, downloadBtn, viewBtn);
                        box.setStyle("-fx-alignment: center;");
                        setGraphic(box);
                    }
                }
            }
        });

        loadCoursewareList();
    }

    private void loadCourseList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/course/getCourseList", req);
        if (res != null && res.getCode() == 0) {
            List<Map> courses = (List<Map>) res.getData();
            courseComboBox.getItems().clear();

            OptionItem emptyItem = new OptionItem(-1, "", "请选择课程");
            courseComboBox.getItems().add(emptyItem);

            if (courses != null) {
                for (Map course : courses) {
                    Integer courseId = getIntegerFromObject(course.get("courseId"));
                    String courseName = (String) course.get("name");
                    if (courseId != null) {
                        OptionItem item = new OptionItem(courseId, String.valueOf(courseId), courseName);
                        courseComboBox.getItems().add(item);
                    }
                }
            }
        }
    }

    private void loadCoursewareList() {
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/courseware/getCoursewareList", req);

        if (res != null && res.getCode() == 0) {
            List<Map> list = (List<Map>) res.getData();
            observableList.clear();
            if (list != null && !list.isEmpty()) {
                for (Map item : list) {
                    // 安全获取并处理中文
                    Object titleObj = item.get("title");
                    String title = (titleObj != null) ? titleObj.toString() : "";
                    Object fileNameObj = item.get("fileName");
                    String fileName = (fileNameObj != null) ? fileNameObj.toString() : "";

                    Long size = getLongFromObject(item.get("fileSize"));
                    if (size != null) {
                        item.put("fileSize", String.format("%.2f", size / 1024.0));
                    } else {
                        item.put("fileSize", "0");
                    }
                    observableList.add(item);
                }
            }
            dataTableView.setItems(observableList);
            dataTableView.refresh();
        }
    }

    private void downloadCourseware(Map item) {
        Integer coursewareId = getIntegerFromObject(item.get("coursewareId"));
        String fileName = (String) item.get("fileName");

        if (coursewareId == null) {
            MessageDialog.showDialog("下载失败：课件ID无效");
            return;
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            MessageDialog.showDialog("下载失败：文件名为空");
            return;
        }

        System.out.println("开始下载课件，ID: " + coursewareId + ", 文件名: " + fileName);
        
        String url = "/api/courseware/download/" + coursewareId;
        byte[] data = HttpRequestUtil.downloadFile(url);

        if (data != null && data.length > 0) {
            System.out.println("下载成功，文件大小: " + data.length + " 字节");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(fileName);
            fileChooser.setTitle("保存课件文件");
            File saveFile = fileChooser.showSaveDialog(null);
            if (saveFile != null) {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(saveFile)) {
                    fos.write(data);
                    MessageDialog.showDialog("下载完成！");
                    loadCoursewareList();
                } catch (Exception e) {
                    MessageDialog.showDialog("保存文件失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.err.println("下载失败：文件数据为空");
            MessageDialog.showDialog("下载失败：文件不存在或数据为空，请检查后端服务是否正常");
        }
    }

    private void viewCourseware(Map item) {
        Integer coursewareId = getIntegerFromObject(item.get("coursewareId"));
        String fileName = (String) item.get("fileName");
        String title = (String) item.get("title");

        if (coursewareId == null) {
            MessageDialog.showDialog("查看失败：课件ID无效");
            return;
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            MessageDialog.showDialog("查看失败：文件名为空");
            return;
        }

        System.out.println("开始查看课件，ID: " + coursewareId + ", 文件名: " + fileName);
        
        // 先下载文件到临时目录
        String url = "/api/courseware/download/" + coursewareId;
        byte[] data = HttpRequestUtil.downloadFile(url);

        if (data != null && data.length > 0) {
            System.out.println("下载成功，文件大小: " + data.length + " 字节");
            try {
                // 创建临时文件
                String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
                File tempFile = File.createTempFile("courseware_", suffix);
                tempFile.deleteOnExit(); // JVM退出时删除

                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                    fos.write(data);
                }

                System.out.println("临时文件已创建: " + tempFile.getAbsolutePath());
                
                // 使用系统默认程序打开文件
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                        desktop.open(tempFile);
                    } else {
                        MessageDialog.showDialog("当前系统不支持打开此类型文件");
                    }
                } else {
                    MessageDialog.showDialog("当前系统不支持桌面操作");
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageDialog.showDialog("打开文件失败：" + e.getMessage());
            }
        } else {
            System.err.println("查看失败：文件数据为空");
            MessageDialog.showDialog("查看失败：文件不存在或数据为空，请检查后端服务是否正常");
        }
    }

    /**
     * 删除课件（仅教师和管理员可用）
     */
    private void deleteCourseware(Map item) {
        Integer coursewareId = getIntegerFromObject(item.get("coursewareId"));
        String title = (String) item.get("title");
        String fileName = (String) item.get("fileName");

        if (coursewareId == null) {
            MessageDialog.showDialog("删除失败：课件ID无效");
            return;
        }

        // 确认删除
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认删除");
        confirmDialog.setHeaderText("您确定要删除这个课件吗？");
        confirmDialog.setContentText("课件标题：" + title + "\n文件名：" + fileName);
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("开始删除课件，ID: " + coursewareId);
            
            // 发送删除请求
            DataRequest req = new DataRequest();
            req.add("coursewareId", coursewareId);
            
            // 使用POST请求调用删除接口
            DataResponse res = HttpRequestUtil.request("/api/courseware/delete", req);
            
            if (res != null && res.getCode() == 0) {
                System.out.println("删除成功");
                MessageDialog.showDialog("删除成功！");
                loadCoursewareList(); // 刷新列表
            } else {
                String errorMsg = (res != null) ? res.getMsg() : "删除失败：网络错误";
                System.err.println("删除失败: " + errorMsg);
                MessageDialog.showDialog("删除失败：" + errorMsg);
            }
        }
    }

    @FXML
    protected void onChooseFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择课件文件");
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            fileField.setText(selectedFile.getName());
        }
    }

    @FXML
    protected void onUploadClick() {
        String title = titleField.getText();
        String description = descField.getText();

        if (title.isEmpty()) {
            MessageDialog.showDialog("请输入课件标题");
            return;
        }
        if (selectedFile == null) {
            MessageDialog.showDialog("请选择文件");
            return;
        }

        OptionItem selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();
        String courseId = "";
        String courseName = "";
        if (selectedCourse != null && selectedCourse.getId() != -1) {
            courseId = selectedCourse.getValue();
            courseName = selectedCourse.getTitle();
        }

        final String finalTitle = title;
        final String finalDescription = description;
        final String finalCourseId = courseId;
        final String finalCourseName = courseName;
        final File finalFile = selectedFile;

        new Thread(() -> {
            try {
                String boundary = "----" + System.currentTimeMillis();
                String lineEnd = "\r\n";
                String twoHyphens = "--";

                java.net.URL url = new java.net.URL(HttpRequestUtil.serverUrl + "/api/courseware/upload");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setRequestProperty("Authorization", "Bearer " + AppStore.getJwt().getToken());

                try (java.io.OutputStream os = conn.getOutputStream();
                     java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(os, "UTF-8"), true)) {

                    writer.append(twoHyphens).append(boundary).append(lineEnd);
                    writer.append("Content-Disposition: form-data; name=\"title\"").append(lineEnd);
                    writer.append("Content-Type: text/plain; charset=UTF-8").append(lineEnd);
                    writer.append(lineEnd);
                    writer.append(finalTitle).append(lineEnd);

                    writer.append(twoHyphens).append(boundary).append(lineEnd);
                    writer.append("Content-Disposition: form-data; name=\"description\"").append(lineEnd);
                    writer.append("Content-Type: text/plain; charset=UTF-8").append(lineEnd);
                    writer.append(lineEnd);
                    writer.append(finalDescription).append(lineEnd);

                    writer.append(twoHyphens).append(boundary).append(lineEnd);
                    writer.append("Content-Disposition: form-data; name=\"courseId\"").append(lineEnd);
                    writer.append(lineEnd);
                    writer.append(finalCourseId).append(lineEnd);

                    writer.append(twoHyphens).append(boundary).append(lineEnd);
                    writer.append("Content-Disposition: form-data; name=\"courseName\"").append(lineEnd);
                    writer.append("Content-Type: text/plain; charset=UTF-8").append(lineEnd);
                    writer.append(lineEnd);
                    writer.append(finalCourseName).append(lineEnd);

                    writer.append(twoHyphens).append(boundary).append(lineEnd);
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + finalFile.getName() + "\"").append(lineEnd);
                    writer.append("Content-Type: application/octet-stream").append(lineEnd);
                    writer.append(lineEnd);
                    writer.flush();

                    try (java.io.FileInputStream fis = new java.io.FileInputStream(finalFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                    }

                    writer.append(lineEnd);
                    writer.append(twoHyphens).append(boundary).append(twoHyphens).append(lineEnd);
                    writer.flush();
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    Platform.runLater(() -> {
                        MessageDialog.showDialog("上传成功！");
                        titleField.clear();
                        descField.clear();
                        fileField.clear();
                        selectedFile = null;
                        courseComboBox.getSelectionModel().selectFirst();
                        loadCoursewareList();
                    });
                } else {
                    Platform.runLater(() -> MessageDialog.showDialog("上传失败: HTTP " + responseCode));
                }
                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> MessageDialog.showDialog("上传失败: " + e.getMessage()));
            }
        }).start();
    }

    @Override
    public void doRefresh() {
        loadCoursewareList();
    }
}
