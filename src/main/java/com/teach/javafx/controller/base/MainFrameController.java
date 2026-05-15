package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.MyTreeNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MainFrameController 登录交互控制类 对应 base/main-frame.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class MainFrameController {
    class ChangePanelHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            changeContent(actionEvent);
        }
    }
    private Map<String,Tab> tabMap = new HashMap<String,Tab>();
    private Map<String,Scene> sceneMap = new HashMap<String,Scene>();
    private Map<String,ToolController> controlMap =new HashMap<String,ToolController>();
    @FXML
    private MenuBar menuBar;
    @FXML
    private TreeView<MyTreeNode> menuTree;
    @FXML
    protected TabPane contentTabPane;
    @FXML
    private Label systemPrompt;

    private ChangePanelHandler handler= null;

    void addMenuItems(Menu parent, List<Map> mList) {
        String name, title;
        List sList;
        Map ms;
        Menu menu;
        MenuItem item;
        for ( Map m :mList) {
            sList = (List<Map>)m.get("sList");
            name = (String)m.get("name");
            title = (String)m.get("title");
            if(sList == null || sList.size()== 0) {
                item = new MenuItem();
                item.setId(name);
                item.setText(title);
                item.setOnAction(this::changeContent);
                parent.getItems().add(item);
            }else {
                menu = new Menu();
                menu.setText(title);
                addMenuItems(menu,sList);
                parent.getItems().add(menu);
            }
        }
    }

    /**
     * 页面加载对象创建完成初始话方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     * 系统初始时为没个角色增加了框架已经实现好了基础管理的功能，采用代码显示添加的方法加入，加入完缺省的功能菜单后，通过
     * HttpRequestUtil.request("/api/base/getMenuList",new DataRequest())加载用菜单管理功能，维护的菜单
     * 项目开发过程中，同学可以扩该方法，增肌自己设计的功能菜单，也可以通过菜单管理程序添加菜单，框架自动加载菜单管理维护的菜单，
     * 是新功能扩展
     */
    public void addMenuItem(Menu menu, String name, String title){
        MenuItem item;
        item = new MenuItem();
        item.setText(title);
        item.setId(name);
        item.setOnAction(this::changeContent);
        menu.getItems().add(item);
    }

    public void initMenuBar(List<Map> mList){
        Menu menu;
        Map m;
        int i;
        List<Map> sList;
        for(i = 0; i < mList.size();i++) {
            m = mList.get(i);
            sList = (List<Map>)m.get("sList");
            menu = new Menu();
            menu.setText((String)m.get("title"));
            if(sList != null && sList.size()> 0) {
                addMenuItems(menu,sList);
            }
            menuBar.getMenus().add(menu);
        }
    }

    void addMenuItems( TreeItem<MyTreeNode> parent, List<Map> mList) {
        List sList;
        TreeItem<MyTreeNode> menu;
        for ( Map m :mList) {
            sList = (List<Map>)m.get("sList");
            menu = new TreeItem<>(new MyTreeNode(null,(String)m.get("name") ,(String)m.get("title"),0));
            parent.getChildren().add(menu);
            if(sList !=  null && sList.size()> 0) {
                addMenuItems(menu, sList);
            }
        }
    }

    public void initMenuTree(List<Map> mList) {
        String role = AppStore.getJwt().getRole();
        MyTreeNode node = new MyTreeNode(null, null,"菜单",0);
        TreeItem<MyTreeNode> root = new TreeItem<>(node);
        TreeItem<MyTreeNode>  menu;
        int i,j;
        Map m;
        List<Map> sList;
        for(i = 0; i < mList.size();i++) {
            m = mList.get(i);
            sList = (List<Map>)m.get("sList");
            menu = new TreeItem<>(new MyTreeNode(null, (String)m.get("name"), (String)m.get("title"), (Integer)m.get("isLeft")));
            if(sList != null && sList.size()> 0) {
                addMenuItems(menu,sList);
            }
            root.getChildren().add(menu);
        }

        // 新增：手动添加老师开设课程菜单
        addTeacherCourseMenuToTree(root, role);

        menuTree.setRoot(root);
        menuTree.setShowRoot(false);
        menuTree.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>(){
            public void handle(MouseEvent event){
                Node node = event.getPickResult().getIntersectedNode();
                TreeItem<MyTreeNode> treeItem = menuTree.getSelectionModel().getSelectedItem();
                if(treeItem == null)
                    return;
                MyTreeNode menu = treeItem.getValue();
                if(menu == null)
                    return;
                String name = menu.getValue();
                if(name == null || name.length() == 0)
                    return ;
                if("logout".equals(name)) {
                    logout();
                }else if(name.endsWith("Command")){
                    try {
                        Method m = this.getClass().getMethod(name);
                        m.invoke(this);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    changeContent(name,menu.getLabel());
                }
            }
        });
    }

    /**
     * 手动添加老师开设课程菜单到菜单树
     */
    private void addTeacherCourseMenuToTree(TreeItem<MyTreeNode> root, String role) {
        System.out.println("=== 添加老师课程菜单到菜单树 ===");
        System.out.println("当前用户角色: " + role);

        // 只有老师角色（teacher 或 2）才显示
        if (!"teacher".equals(role) && !"2".equals(role)) {
            System.out.println("当前用户不是老师，不添加老师菜单");
            return;
        }

        System.out.println("当前用户是老师，添加老师菜单");

        // 查找是否已存在"老师功能"菜单
        TreeItem<MyTreeNode> teacherMenu = null;
        for (TreeItem<MyTreeNode> child : root.getChildren()) {
            if ("老师功能".equals(child.getValue().getLabel())) {
                teacherMenu = child;
                System.out.println("找到已存在的老师功能菜单");
                break;
            }
        }

        // 如果没有"老师功能"菜单，创建一个
        if (teacherMenu == null) {
            System.out.println("创建新的老师功能菜单");
            teacherMenu = new TreeItem<>(new MyTreeNode(null, "teacher-menu", "老师功能", 0));
            root.getChildren().add(teacherMenu);
        }

        // 添加"开设课程"子菜单
        boolean exists = false;
        for (TreeItem<MyTreeNode> child : teacherMenu.getChildren()) {
            if ("teacher-open-course".equals(child.getValue().getValue())) {
                exists = true;
                System.out.println("开设课程菜单已存在");
                break;
            }
        }

        if (!exists) {
            System.out.println("添加开设课程子菜单");
            TreeItem<MyTreeNode> openCourseItem = new TreeItem<>(
                    new MyTreeNode(null, "teacher-open-course", "开设课程", 0)
            );
            teacherMenu.getChildren().add(openCourseItem);
            teacherMenu.setExpanded(true);
        }
    }

    /**
     * 手动添加老师开设课程菜单到菜单栏
     */
    private void addTeacherCourseMenuToMenuBar() {
        System.out.println("=== 添加老师课程菜单到菜单栏 ===");

        try {
            String role = AppStore.getJwt().getRole();
            System.out.println("菜单栏 - 当前用户角色: " + role);

            if (!"teacher".equals(role) && !"2".equals(role)) {
                System.out.println("菜单栏 - 当前用户不是老师，不添加老师菜单");
                return;
            }

            System.out.println("菜单栏 - 当前用户是老师，添加老师菜单");

            // 查找是否已存在"老师功能"菜单
            Menu teacherMenu = null;
            for (Menu menu : menuBar.getMenus()) {
                if ("老师功能".equals(menu.getText())) {
                    teacherMenu = menu;
                    System.out.println("菜单栏 - 找到已存在的老师功能菜单");
                    break;
                }
            }

            // 如果没有"老师功能"菜单，创建一个
            if (teacherMenu == null) {
                System.out.println("菜单栏 - 创建新的老师功能菜单");
                teacherMenu = new Menu("老师功能");
                menuBar.getMenus().add(teacherMenu);
            }

            // 检查是否已存在"开设课程"子菜单
            boolean exists = false;
            for (MenuItem item : teacherMenu.getItems()) {
                if ("teacher-open-course".equals(item.getId())) {
                    exists = true;
                    System.out.println("菜单栏 - 开设课程菜单已存在");
                    break;
                }
            }

            if (!exists) {
                System.out.println("菜单栏 - 添加开设课程子菜单");
                MenuItem openCourseItem = new MenuItem("开设课程");
                openCourseItem.setId("teacher-open-course");
                openCourseItem.setOnAction(this::changeContent);
                teacherMenu.getItems().add(openCourseItem);
            }

        } catch (Exception e) {
            System.out.println("添加老师菜单到菜单栏失败: " + e.getMessage());
        }
    }

    /**
     * 强制添加测试菜单（无论如何都显示）
     */
    private void addForceTestMenus() {
        System.out.println("=== 强制添加测试菜单 ===");

        try {
            // 1. 强制添加测试菜单到顶部菜单栏
            addForceTestMenuToMenuBar();

            // 2. 强制添加测试菜单到左侧菜单树
            addForceTestMenuToTree();

            // 3. 打印当前用户信息
            printCurrentUserInfo();

        } catch (Exception e) {
            System.out.println("❌ 添加测试菜单时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 强制添加测试菜单到顶部菜单栏
     */
    private void addForceTestMenuToMenuBar() {
        System.out.println("正在添加测试菜单到顶部菜单栏...");

        // 创建一个独立的"测试菜单"，不依赖角色
        Menu testMenu = new Menu("🚀 测试菜单");
        MenuItem testItem1 = new MenuItem("测试老师课程");
        testItem1.setId("teacher-open-course");
        testItem1.setOnAction(this::changeContent);

        MenuItem testItem2 = new MenuItem("测试界面2");
        testItem2.setOnAction(e -> {
            System.out.println("测试菜单2被点击");
            showTestAlert("测试菜单2");
        });

        testMenu.getItems().addAll(testItem1, testItem2);

        // 添加到菜单栏
        menuBar.getMenus().add(testMenu);

        System.out.println("✅ 测试菜单已添加到顶部菜单栏");
    }

    /**
     * 强制添加测试菜单到左侧菜单树
     */
    private void addForceTestMenuToTree() {
        System.out.println("正在添加测试菜单到左侧菜单树...");

        // 获取菜单树的根节点
        TreeItem<MyTreeNode> root = menuTree.getRoot();
        if (root == null) {
            System.out.println("❌ 菜单树根节点为null");
            return;
        }

        // 创建一个"测试功能"节点
        MyTreeNode testNode = new MyTreeNode(null, "test-menu", "功能", 0);
        TreeItem<MyTreeNode> testTreeItem = new TreeItem<>(testNode);

        // 添加"测试老师课程"子节点
        MyTreeNode openCourseNode = new MyTreeNode(null, "teacher-open-course", "老师课程", 0);
        TreeItem<MyTreeNode> openCourseItem = new TreeItem<>(openCourseNode);
        testTreeItem.getChildren().add(openCourseItem);

        // 添加到根节点
        root.getChildren().add(testTreeItem);

        // 展开测试功能节点
        testTreeItem.setExpanded(true);

        System.out.println("✅ 测试菜单已添加到左侧菜单树");
    }

    /**
     * 打印当前用户信息
     */
    private void printCurrentUserInfo() {
        try {
            String role = AppStore.getJwt().getRole();
            String username = AppStore.getJwt().getUsername();
            System.out.println("=== 当前用户信息 ===");
            System.out.println("用户名: " + username);
            System.out.println("角色: " + role);
            System.out.println("是否是老师: " + ("teacher".equals(role) || "2".equals(role)));
        } catch (Exception e) {
            System.out.println("❌ 获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 显示测试弹窗
     */
    private void showTestAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("测试");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    public void initialize() {
        System.out.println("=== MainFrameController 初始化开始 ===");

        handler = new ChangePanelHandler();
        DataRequest req = new DataRequest();
        DataResponse res;

        res = HttpRequestUtil.request("/api/base/getDataBaseUserName", req);
        String userName = (String) res.getData();
        systemPrompt.setText("服务器：" + HttpRequestUtil.serverUrl + " 数据库：" + userName);

        res = HttpRequestUtil.request("/api/base/getMenuList", req);
        List<Map> mList = (List<Map>) res.getData();
        System.out.println("获取到的菜单列表大小: " + (mList != null ? mList.size() : 0));

        initMenuBar(mList);
        initMenuTree(mList);

        contentTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        contentTabPane.setStyle("-fx-background-image: url('shanda1.jpg'); -fx-background-repeat: no-repeat; -fx-background-size: cover;");

        // 新增：手动添加老师开设课程菜单到菜单栏
        addTeacherCourseMenuToMenuBar();

        // 新增：强制添加测试菜单（无论如何都显示）
        addForceTestMenus();

        System.out.println("✅ MainFrameController 初始化完成");
        System.out.println("菜单栏菜单数量: " + menuBar.getMenus().size());
    }

    /**
     * 点击菜单栏中的"退出"菜单，执行onLogoutMenuClick方法 加载登录页面，切换回登录界面
     * @param event
     */
    protected void onLogoutMenuClick(ActionEvent event){
        logout();
    }

    protected void logout(){
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/login-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 320, 240);
            MainApplication.loginStage("Login", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public  void changeContent(ActionEvent ae) {
        Object obj = ae.getSource();
        String name= null, title= null;
        if(obj instanceof MenuItem) {
            MenuItem item = (MenuItem)obj;
            name = item.getId();
            title = item.getText();
        }
        if(name == null)
            return;
        changeContent(name,title);
    }

    /**
     * 点击菜单栏中的菜单 执行changeContent 在主框架工作区增加和显示一个工作面板
     * @param name  菜单名 name.fxml 对应面板的配置文件
     * @param title 菜单标题 工作区中的TablePane的标题
     */
    public void changeContent(String name, String title) {
        // 调试输出
        System.out.println("=== changeContent 被调用 ===");
        System.out.println("name = " + name);
        System.out.println("title = " + title);
        System.out.println("尝试加载: " + name + ".fxml");

        // 尝试多种路径
        String[] possiblePaths = {
                name + ".fxml",                      // teacher-open-course.fxml
                "/" + name + ".fxml",               // /teacher-open-course.fxml
                "com/teach/javafx/view/" + name + ".fxml",  // 完整路径
                "/com/teach/javafx/view/" + name + ".fxml"  // 带斜杠的完整路径
        };

        for (String path : possiblePaths) {
            java.net.URL url = MainApplication.class.getResource(path);
            System.out.println("尝试路径: " + path + " -> " + (url != null ? "✅ 找到" : "❌ 未找到"));
        }

        if(name == null || name.length() == 0)
            return;

        if ("logout".equals(name)) {
            logout();
            return;
        }

        Tab tab = tabMap.get(name);
        Scene scene;
        Object c;
        if(tab == null) {
            scene = sceneMap.get(name);
            if(scene == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(name + ".fxml"));
                try {
                    scene = new Scene(fxmlLoader.load(), 1024, 768);
                    sceneMap.put(name, scene);
                    System.out.println("✅ 加载成功");
                } catch (IOException e) {
                    System.out.println("❌ 加载失败: " + e.getMessage());
                    e.printStackTrace();

                    // 如果找不到文件，尝试不加 .fxml 后缀
                    try {
                        fxmlLoader = new FXMLLoader(MainApplication.class.getResource(name));
                        scene = new Scene(fxmlLoader.load(), 1024, 768);
                        sceneMap.put(name, scene);
                        System.out.println("✅ 加载成功（不加.fxml后缀）");
                    } catch (IOException ex) {
                        ex.printStackTrace();

                        // 创建错误提示界面
                        Label errorLabel = new Label("无法加载界面: " + name + "\n" + ex.getMessage());
                        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-padding: 20;");
                        scene = new Scene(new javafx.scene.layout.StackPane(errorLabel), 400, 200);
                        sceneMap.put(name, scene);
                    }
                }
                c = fxmlLoader.getController();
                if(c instanceof ToolController) {
                    controlMap.put(name,(ToolController)c);
                }
            }
            tab = new Tab(title);
            tab.setId(name);
            tab.setOnSelectionChanged(this::tabSelectedChanged);
            tab.setOnClosed(this::tabOnClosed);
            tab.setContent(scene.getRoot());
            contentTabPane.getTabs().add(tab);
            tabMap.put(name, tab);
        }
        contentTabPane.getSelectionModel().select(tab);
    }

    public void tabSelectedChanged(Event e) {
        Tab tab = (Tab)e.getSource();
        String name = tab.getId();
        ToolController c = controlMap.get(name);
        if(c != null)
            c.doRefresh();
    }

    /**
     * 点击TablePane 标签页 的关闭图标 执行tabOnClosed方法
     * @param e
     */
    public void tabOnClosed(Event e) {
        Tab tab = (Tab)e.getSource();
        String name = tab.getId();
        contentTabPane.getTabs().remove(tab);
        tabMap.remove(name);
    }

    /**
     * ToolController getCurrentToolController() 获取当前显示的面板的控制对象， 如果面板响应编辑菜单中的编辑命名，交互控制需要继承 ToolController， 重写里面的方法
     * @return
     */
    public ToolController getCurrentToolController(){
        Iterator<String> iterator = controlMap.keySet().iterator();
        String name;
        Tab tab;
        while(iterator.hasNext()) {
            name = iterator.next();
            tab = tabMap.get(name);
            if(tab.isSelected()) {
                return controlMap.get(name);
            }
        }
        return null;
    }

    /**
     * 点击编辑菜单中的"新建"菜单，执行doNewCommand方法， 执行当前显示的面板对应的控制类中的doNew()方法
     */
    protected  void doNewCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doNew();
    }

    /**
     * 点击编辑菜单中的"保存"菜单，执行doSaveCommand方法， 执行当前显示的面板对应的控制类中的doSave()方法
     */
    protected  void doSaveCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doSave();
    }

    /**
     * 点击编辑菜单中的"删除"菜单，执行doDeleteCommand方法， 执行当前显示的面板对应的控制类中的doDelete()方法
     */
    protected  void doDeleteCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doDelete();
    }

    /**
     * 点击编辑菜单中的"打印"菜单，执行doPrintCommand方法， 执行当前显示的面板对应的控制类中的doPrint()方法
     */
    protected  void doPrintCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doPrint();
    }

    /**
     * 点击编辑菜单中的"导出"菜单，执行doExportCommand方法， 执行当前显示的面板对应的控制类中的doExport方法
     */
    protected  void doExportCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doExport();
    }

    /**
     * 点击编辑菜单中的"导入"菜单，执行doImportCommand方法， 执行当前显示的面板对应的控制类中的doImport()方法
     */
    protected  void doImportCommand(){
        ToolController c = getCurrentToolController();
        if(c == null)
            return;
        c.doImport();
    }

    /**
     * 点击编辑菜单中的"测试"菜单，执行doTestCommand方法， 执行当前显示的面板对应的控制类中的doImport()方法
     */
    protected  void doTestCommand(){
        ToolController c = getCurrentToolController();
        if(c == null) {
            c= new ToolController(){};
        }
        c.doTest();
    }

    public ToolController getToolController(String name){
        return  controlMap.get(name);
    }
}