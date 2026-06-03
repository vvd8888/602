package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.LocalDateStringConverter;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.*;
import javafx.scene.Scene;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.util.CommonMethod;
import com.teach.javafx.controller.base.MessageDialog;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

/**
 * StudentController 登录交互控制类 对应 student_panel.fxml  对应于学生管理的后台业务处理的控制器，主要获取数据和保存数据的方法不同
 *
 * @FXML 属性 对应fxml文件中的
 * @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class StudentController extends ToolController {
    private ImageView photoImageView;
    @FXML
    private TableView<Map> dataTableView;  //学生信息表
    @FXML
    private TableColumn<Map, String> numColumn;   //学生信息表 编号列
    @FXML
    private TableColumn<Map, String> nameColumn; //学生信息表 名称列
    @FXML
    private TableColumn<Map, String> deptColumn;  //学生信息表 院系列
    @FXML
    private TableColumn<Map, String> majorColumn; //学生信息表 专业列
    @FXML
    private TableColumn<Map, String> classNameColumn;
    @FXML
    private TableColumn<Map,String> gradeColumn;
    @FXML
    private TableColumn<Map, String> cardColumn; //学生信息表 证件号码列
    @FXML
    private TableColumn<Map, String> genderColumn; //学生信息表 性别列
    @FXML
    private TableColumn<Map,String > nationColumn;//xiugai
    @FXML
    private TableColumn<Map, String> birthdayColumn; //学生信息表 出生日期列
    @FXML
    private TableColumn<Map, String> emailColumn; //学生信息表 邮箱列
    @FXML
    private TableColumn<Map, String> phoneColumn; //学生信息表 电话列
    @FXML
    private TableColumn<Map, String> addressColumn;//学生信息表 地址列
    @FXML
    private Button photoButton;  //照片显示和上传按钮

    @FXML
    private TextField numField; //学生信息  学号输入域
    @FXML
    private TextField nameField;  //学生信息  名称输入域
    @FXML
    private TextField deptField; //学生信息  院系输入域
    @FXML
    private TextField majorField; //学生信息  专业输入域
    @FXML
    private TextField classNameField;
    @FXML
    private TextField gradeField;//学生信息  班级输入域
    @FXML
    private TextField cardField; //学生信息  证件号码输入域
    @FXML
    private ComboBox<OptionItem> genderComboBox;//学生信息  性别输入域
    @FXML
    private ComboBox<OptionItem> nationComboBox;//xiugai
    @FXML
    private DatePicker birthdayPick;  //学生信息  出生日期选择域
    @FXML
    private TextField emailField;  //学生信息  邮箱输入域
    @FXML
    private TextField phoneField;   //学生信息  电话输入域
    @FXML
    private TextField addressField;  //学生信息  地址输入域

    @FXML
    private TextField numNameTextField;  //查询 姓名学号输入域

    private Integer personId = null;  //当前编辑修改的学生的主键

    private ArrayList<Map> studentList = new ArrayList();  // 学生信息列表数据
    private List<OptionItem> genderList;   //性别选择列表数据
    private List<OptionItem> nationList;//xiugai
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表


    /**
     * 将学生数据集合设置到面板上显示
     */
    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < studentList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(studentList.get(j)));
        }
        dataTableView.setItems(observableList);
    }

    /**
     * 页面加载对象创建完成初始化方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     */

    @FXML
    public void initialize() {

        nationList = HttpRequestUtil.getDictionaryOptionItemList("MZ");
        if (nationList != null) {
            nationComboBox.getItems().addAll(nationList);
        }//xiugai

        photoImageView = new ImageView();
        photoImageView.setFitHeight(100);
        photoImageView.setFitWidth(100);
        photoButton.setGraphic(photoImageView);
        DataResponse res;
        DataRequest req = new DataRequest();
        req.add("numName", "");
        res = HttpRequestUtil.request("/api/student/getStudentList", req); //从后台获取所有学生信息列表集合
        if (res != null && res.getCode() == 0) {
            studentList = (ArrayList<Map>) res.getData();
        }
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));  //设置列值工程属性
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        deptColumn.setCellValueFactory(new MapValueFactory<>("dept"));
        majorColumn.setCellValueFactory(new MapValueFactory<>("major"));
        classNameColumn.setCellValueFactory(new MapValueFactory<>("className"));
        gradeColumn.setCellValueFactory(new MapValueFactory<>("grade"));
        cardColumn.setCellValueFactory(new MapValueFactory<>("card"));
        genderColumn.setCellValueFactory(new MapValueFactory<>("genderName"));
        nationColumn.setCellValueFactory(new MapValueFactory<>("nation"));//xiugai
        birthdayColumn.setCellValueFactory(new MapValueFactory<>("birthday"));
        emailColumn.setCellValueFactory(new MapValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new MapValueFactory<>("phone"));
        addressColumn.setCellValueFactory(new MapValueFactory<>("address"));
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        setTableViewData();
        genderList = HttpRequestUtil.getDictionaryOptionItemList("XBM");

        genderComboBox.getItems().addAll(genderList);
        birthdayPick.setConverter(new LocalDateStringConverter("yyyy-MM-dd"));
    }

    /**
     * 清除学生表单中输入信息
     */
    public void clearPanel() {
        personId = null;
        numField.setText("");
        nameField.setText("");
        deptField.setText("");
        majorField.setText("");
        classNameField.setText("");
        gradeField.setText("");
        cardField.setText("");
        genderComboBox.getSelectionModel().select(-1);
        birthdayPick.getEditor().setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
    }

    protected void changeStudentInfo() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentInfo", req);
        if (res.getCode() != 0) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        numField.setText(CommonMethod.getString(form, "num"));
        nameField.setText(CommonMethod.getString(form, "name"));
        deptField.setText(CommonMethod.getString(form, "dept"));
        majorField.setText(CommonMethod.getString(form, "major"));
        classNameField.setText(CommonMethod.getString(form, "className"));
        gradeField.setText(CommonMethod.getString(form, "grade"));
        cardField.setText(CommonMethod.getString(form, "card"));
        genderComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(genderList, CommonMethod.getString(form, "gender")));

        // 民族回显：按 title 匹配（因为保存时存的是名称）
        String nationName = CommonMethod.getString(form, "nation");
        if (nationName != null && !nationName.isEmpty()) {
            boolean found = false;
            for (int i = 0; i < nationList.size(); i++) {
                if (nationName.equals(nationList.get(i).getTitle())) {
                    nationComboBox.getSelectionModel().select(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                nationComboBox.getSelectionModel().select(-1);
            }
        } else {
            nationComboBox.getSelectionModel().select(-1);
        }

        birthdayPick.getEditor().setText(CommonMethod.getString(form, "birthday"));
        emailField.setText(CommonMethod.getString(form, "email"));
        phoneField.setText(CommonMethod.getString(form, "phone"));
        addressField.setText(CommonMethod.getString(form, "address"));
        displayPhoto();
    }

    /**
     * 点击学生列表的某一行，根据personId ,从后台查询学生的基本信息，切换学生的编辑信息
     */

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeStudentInfo();
    }

    /**
     * 点击查询按钮，从从后台根据输入的串，查询匹配的学生在学生列表中显示
     */
    @FXML
    protected void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentList", req);
        if (res != null && res.getCode() == 0) {
            studentList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }


    /**
     * 添加新学生， 清空输入信息， 输入相关信息，点击保存即可添加新的学生
     */
    @FXML
    protected void onAddButtonClick() {
        clearPanel();
    }

    /**
     * 点击删除按钮 删除当前编辑的学生的数据
     */
    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/student/studentDelete", req);
        if(res!= null) {
            if (res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
                onQueryButtonClick();
            } else {
                MessageDialog.showDialog(res.getMsg());
            }
        }
    }

    /**
     * 点击保存按钮，保存当前编辑的学生信息，如果是新添加的学生，后台添加学生
     */
    @FXML
    protected void onSaveButtonClick() {
        if (numField.getText().isEmpty()) {
            MessageDialog.showDialog("学号为空，不能修改");
            return;
        }
        Map<String,Object> form = new HashMap<>();
        form.put("num", numField.getText());
        form.put("name", nameField.getText());
        form.put("dept", deptField.getText());
        form.put("major", majorField.getText());
        form.put("className", classNameField.getText());
        form.put("grade",gradeField.getText());
        form.put("card", cardField.getText());
        if (genderComboBox.getSelectionModel() != null && genderComboBox.getSelectionModel().getSelectedItem() != null)
            form.put("gender", genderComboBox.getSelectionModel().getSelectedItem().getValue());
        form.put("birthday", birthdayPick.getEditor().getText());
        form.put("email", emailField.getText());
        form.put("phone", phoneField.getText());
        form.put("address", addressField.getText());

        OptionItem selectedNation = nationComboBox.getSelectionModel().getSelectedItem();
        String nation = (selectedNation != null) ? selectedNation.getTitle() : "";
        form.put("nation", nation);//xiugai

        DataRequest req = new DataRequest();
        req.add("personId", personId);
        req.add("form", form);
        DataResponse res = HttpRequestUtil.request("/api/student/studentEditSave", req);
        if (res.getCode() == 0) {
            personId = CommonMethod.getIntegerFromObject(res.getData());
            MessageDialog.showDialog("提交成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    /**
     * doNew() doSave() doDelete() 重写 ToolController 中的方法， 实现选择 新建，保存，删除 对学生的增，删，改操作
     */
    public void doNew() {
        clearPanel();
    }

    public void doSave() {
        onSaveButtonClick();
    }

    public void doDelete() {
        onDeleteButtonClick();
    }

    /**
     * 导出学生信息表的示例 重写ToolController 中的doExport 这里给出了一个导出学生基本信息到Excl表的示例， 后台生成Excl文件数据，传回前台，前台将文件保存到本地
     */
    public void doExport() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        byte[] bytes = HttpRequestUtil.requestByteData("/api/student/getStudentListExcl", req);
        if (bytes != null) {
            try {
                FileChooser fileDialog = new FileChooser();
                fileDialog.setTitle("前选择保存的文件");
                fileDialog.setInitialDirectory(new File("C:/"));
                fileDialog.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
                File file = fileDialog.showSaveDialog(null);
                if (file != null) {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(bytes);
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    protected void onImportButtonClick() {
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("前选择学生数据表");
        fileDialog.setInitialDirectory(new File("D:/"));
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
        File file = fileDialog.showOpenDialog(null);
        String paras = "";
        DataResponse res = HttpRequestUtil.importData("/api/term/importStudentData", file.getPath(), paras);
        if (res.getCode() == 0) {
            MessageDialog.showDialog("上传成功！");
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onFamilyButtonClick() {
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/student/getFamilyMemberList", req);
        if (res.getCode() != 0) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        List<Map> familyList = (List<Map>) res.getData();
        ObservableList<Map> oList = FXCollections.observableArrayList(familyList);
        Scene scene = null, pScene = null;
        Stage stage;
        stage = new Stage();
        TableView<Map> table = new TableView<>(oList);
        table.setEditable(true);
        TableColumn<Map, String> relationColumn = new TableColumn<>("关系");
        relationColumn.setCellValueFactory(new MapValueFactory("relation"));
        relationColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        relationColumn.setOnEditCommit(event -> {
            TableView tempTable = event.getTableView();
            Map tempEntity = (Map) tempTable.getItems().get(event.getTablePosition().getRow());
            tempEntity.put("relation",event.getNewValue());
        });
        table.getColumns().add(relationColumn);
        TableColumn<Map, String> nameColumn = new TableColumn<>("姓名");
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        table.getColumns().add(nameColumn);
        TableColumn<Map, String> genderColumn = new TableColumn<>("性别");
        genderColumn.setCellValueFactory(new MapValueFactory<>("gender"));
        genderColumn.setCellFactory(column -> new TableCell<Map, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    switch (item) {
                        case "1":
                            setText("男");
                            break;
                        case "2":
                            setText("女");
                            break;
                        default:
                            setText(item);
                    }
                }
            }
        });
        table.getColumns().add(genderColumn);

        TableColumn<Map, String> nationColumn = new TableColumn<>("民族");
        nationColumn.setCellValueFactory(new MapValueFactory<>("nation"));
        nationColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        table.getColumns().add(nationColumn);//xiugai

        TableColumn<Map, String> ageColumn = new TableColumn<>("年龄");
        ageColumn.setCellValueFactory(new MapValueFactory<>("age"));
        ageColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        table.getColumns().add(ageColumn);
        TableColumn<Map, String> unitColumn = new TableColumn<>("单位");
        unitColumn.setCellValueFactory(new MapValueFactory<>("unit"));
        unitColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        table.getColumns().add(unitColumn);
        BorderPane root = new BorderPane();
        FlowPane flowPane = new FlowPane();
        Button obButton = new Button("确定");
        obButton.setOnAction(event -> {
            for(Map map: table.getItems()) {
                System.out.println("map:"+map);
            }
            stage.close();
        });
        flowPane.getChildren().add(obButton);
        root.setCenter(table);
        root.setBottom(flowPane);
        scene = new Scene(root, 260, 140);
        stage.initOwner(MainApplication.getMainStage());
        stage.initModality(Modality.NONE);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setTitle("成绩录入对话框！");
        stage.setOnCloseRequest(event -> {
            MainApplication.setCanClose(true);
        });
        stage.showAndWait();
    }
    public void displayPhoto(){
        DataRequest req = new DataRequest();
//        req.add("fileName", "photo/" + personId + ".jpg");  //个人照片显示
//        byte[] bytes = HttpRequestUtil.requestByteData("/api/base/getFileByteData", req);  // 从后端服务器指定木下读取文件
        req.add("personId", personId+"");  //个人照片显示
        byte[] bytes = HttpRequestUtil.requestByteData("/api/base/getBlobByteData", req);  //从后端person表里读取图片
        if (bytes != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            Image img = new Image(in);
            photoImageView.setImage(img);
        }

    }

    @FXML
    public void onPhotoButtonClick(){
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("图片上传");
//        fileDialog.setInitialDirectory(new File("C:/"));
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG 文件", "*.jpg"));
        File file = fileDialog.showOpenDialog(null);
        if(file == null)
            return;
//        DataResponse res =HttpRequestUtil.uploadFile("/api/base/uploadPhoto",file.getPath(),"photo/" + personId + ".jpg");  //上传保存到服务器目录/photo/主键PersonId.jpg
        DataResponse res =HttpRequestUtil.uploadFile("/api/base/uploadPhotoBlob",file.getPath(), personId+"" );  //上传保存在主键为personId的Person记录的的Photo列中
        if(res.getCode() == 0) {
            MessageDialog.showDialog("上传成功！");
            displayPhoto();
        }
        else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
    @FXML
    public void onImportFeeButtonClick(){
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("前选择消费数据表");
        fileDialog.setInitialDirectory(new File("C:/"));
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
        File file = fileDialog.showOpenDialog(null);
        String paras = "personId="+personId;
        DataResponse res =HttpRequestUtil.importData("/api/student/importFeeData",file.getPath(),paras);
        if(res.getCode() == 0) {
            MessageDialog.showDialog("上传成功！");
        }
        else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    /**
     * 导出学生简历为PDF文件
     */
    @FXML
    protected void onExportResumeButtonClick() {
        if (personId == null) {
            MessageDialog.showDialog("请先选择一个学生！");
            return;
        }

        // 获取学生详细信息
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentInfo", req);
        
        if (res == null || res.getCode() != 0) {
            MessageDialog.showDialog("获取学生信息失败：" + (res != null ? res.getMsg() : "网络错误"));
            return;
        }

        Map<String, Object> studentInfo = (Map<String, Object>) res.getData();
        
        try {
            // 创建文件选择对话框
            FileChooser fileDialog = new FileChooser();
            fileDialog.setTitle("保存简历PDF文件");
            fileDialog.setInitialFileName(studentInfo.get("name") + "_简历.pdf");
            fileDialog.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF 文件", "*.pdf"));
            File file = fileDialog.showSaveDialog(null);
            
            if (file == null) {
                return; // 用户取消
            }

            // 生成PDF
            generateResumePDF(file, studentInfo);
            MessageDialog.showDialog("简历导出成功！");
            
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.showDialog("简历导出失败：" + e.getMessage());
        }
    }

    /**
     * 生成学生简历PDF
     */
    private void generateResumePDF(File file, Map<String, Object> studentInfo) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // 设置中文字体
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(bfChinese, 18, Font.BOLD);
        Font sectionFont = new Font(bfChinese, 14, Font.BOLD);
        Font contentFont = new Font(bfChinese, 12, Font.NORMAL);
        Font labelFont = new Font(bfChinese, 12, Font.BOLD);

        // 标题
        Paragraph title = new Paragraph("学生个人简历", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // 基本信息
        addSection(document, "基本信息", sectionFont);
        addField(document, "学号", CommonMethod.getString(studentInfo, "num"), labelFont, contentFont);
        addField(document, "姓名", CommonMethod.getString(studentInfo, "name"), labelFont, contentFont);
        addField(document, "性别", CommonMethod.getString(studentInfo, "genderName"), labelFont, contentFont);
        addField(document, "民族", CommonMethod.getString(studentInfo, "nation"), labelFont, contentFont);
        addField(document, "出生日期", CommonMethod.getString(studentInfo, "birthday"), labelFont, contentFont);
        addField(document, "证件号码", CommonMethod.getString(studentInfo, "card"), labelFont, contentFont);
        
        document.add(Chunk.NEWLINE);

        // 教育信息
        addSection(document, "教育信息", sectionFont);
        addField(document, "院系", CommonMethod.getString(studentInfo, "dept"), labelFont, contentFont);
        addField(document, "专业", CommonMethod.getString(studentInfo, "major"), labelFont, contentFont);
        addField(document, "班级", CommonMethod.getString(studentInfo, "className"), labelFont, contentFont);
        addField(document, "年级", CommonMethod.getString(studentInfo, "grade"), labelFont, contentFont);
        
        document.add(Chunk.NEWLINE);

        // 联系方式
        addSection(document, "联系方式", sectionFont);
        addField(document, "电话", CommonMethod.getString(studentInfo, "phone"), labelFont, contentFont);
        addField(document, "邮箱", CommonMethod.getString(studentInfo, "email"), labelFont, contentFont);
        addField(document, "地址", CommonMethod.getString(studentInfo, "address"), labelFont, contentFont);

        // 自我介绍（如果有）
        String introduce = CommonMethod.getString(studentInfo, "introduce");
        if (introduce != null && !introduce.isEmpty()) {
            document.add(Chunk.NEWLINE);
            addSection(document, "自我介绍", sectionFont);
            Paragraph introPara = new Paragraph(introduce, contentFont);
            introPara.setSpacingBefore(5);
            introPara.setSpacingAfter(10);
            document.add(introPara);
        }

        // 添加页脚
        Paragraph footer = new Paragraph("\n生成时间：" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 
                new Font(bfChinese, 10, Font.NORMAL));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
    }

    /**
     * 添加章节标题
     */
    private void addSection(Document document, String title, Font font) throws DocumentException {
        Paragraph section = new Paragraph(title, font);
        section.setSpacingBefore(10);
        section.setSpacingAfter(5);
        document.add(section);
    }

    /**
     * 添加字段行
     */
    private void addField(Document document, String label, String value, Font labelFont, Font contentFont) throws DocumentException {
        Paragraph para = new Paragraph();
        para.add(new Chunk(label + "：", labelFont));
        para.add(new Chunk(value != null ? value : "", contentFont));
        para.setSpacingAfter(3);
        document.add(para);
    }

}
