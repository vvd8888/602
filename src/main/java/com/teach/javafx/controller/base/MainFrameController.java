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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MainFrameController 登录交互控制类
 */
public class MainFrameController {
    class ChangePanelHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            changeContent(actionEvent);
        }
    }
    private Map<String,Tab> tabMap = new HashMap<>();
    private Map<String,Scene> sceneMap = new HashMap<>();
    private Map<String,Object> controlMap = new HashMap<>();

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
        MenuItem item;
        for (Map m : mList) {
            sList = (List<Map>)m.get("sList");
            name = (String)m.get("name");
            title = (String)m.get("title");
            if(sList == null || sList.size()== 0) {
                item = new MenuItem();
                item.setId(name);
                item.setText(title);
                item.setOnAction(this::changeContent);
                parent.getItems().add(item);
            } else {
                Menu menu = new Menu();
                menu.setText(title);
                addMenuItems(menu, sList);
                parent.getItems().add(menu);
            }
        }
    }

    public void addMenuItem(Menu menu, String name, String title){
        MenuItem item = new MenuItem();
        item.setText(title);
        item.setId(name);
        item.setOnAction(this::changeContent);
        menu.getItems().add(item);
    }

    public void initMenuBar(List<Map> mList){
        for(int i = 0; i < mList.size(); i++) {
            Map m = mList.get(i);
            List<Map> sList = (List<Map>)m.get("sList");
            Menu menu = new Menu();
            menu.setText((String)m.get("title"));
            if(sList != null && sList.size() > 0) {
                addMenuItems(menu, sList);
            }
            menuBar.getMenus().add(menu);
        }
    }

    void addMenuItems(TreeItem<MyTreeNode> parent, List<Map> mList) {
        for (Map m : mList) {
            List sList = (List<Map>)m.get("sList");
            TreeItem<MyTreeNode> menu = new TreeItem<>(new MyTreeNode(null,(String)m.get("name"), (String)m.get("title"), 0));
            parent.getChildren().add(menu);
            if(sList != null && sList.size() > 0) {
                addMenuItems(menu, sList);
            }
        }
    }

    public void initMenuTree(List<Map> mList) {
        MyTreeNode node = new MyTreeNode(null, null, "菜单", 0);
        TreeItem<MyTreeNode> root = new TreeItem<>(node);

        for(int i = 0; i < mList.size(); i++) {
            Map m = mList.get(i);
            List<Map> sList = (List<Map>)m.get("sList");
            TreeItem<MyTreeNode> menu = new TreeItem<>(new MyTreeNode(null, (String)m.get("name"), (String)m.get("title"), (Integer)m.get("isLeft")));
            if(sList != null && sList.size() > 0) {
                addMenuItems(menu, sList);
            }
            root.getChildren().add(menu);
        }

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
                    return;
                if("logout".equals(name)) {
                    logout();
                } else if(name.endsWith("Command")){
                    try {
                        Method m = this.getClass().getMethod(name);
                        m.invoke(this);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    changeContent(name, menu.getLabel());
                }
            }
        });
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

        if (mList != null && !mList.isEmpty()) {
            initMenuBar(mList);
            initMenuTree(mList);
        } else {
            System.out.println("警告: 菜单列表为空");
            // 如果后端没有返回菜单，初始化一个基本的菜单树
            MyTreeNode node = new MyTreeNode(null, null, "菜单", 0);
            TreeItem<MyTreeNode> root = new TreeItem<>(node);
            menuTree.setRoot(root);
        }

        // 注释掉硬编码菜单，所有菜单都从数据库加载
        // addCustomMenus();

        contentTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        contentTabPane.setStyle("-fx-background-image: url('shanda1.jpg'); -fx-background-repeat: no-repeat; -fx-background-size: cover;");

        // 打印调试信息
        printUserInfo();

        System.out.println("✅ MainFrameController 初始化完成");
    }

    /**
     * 添加自定义菜单（已禁用，所有菜单从数据库加载）
     * 如需添加新菜单，请在MySQL的menu表中配置
     */
    private void addCustomMenus() {
        System.out.println("=== 添加自定义菜单（已禁用，使用数据库菜单） ===");

        // 获取当前用户角色
        String role = AppStore.getJwt() != null ? AppStore.getJwt().getRole() : "unknown";
        System.out.println("当前用户角色: " + role);

        // 1. 在菜单栏添加自定义菜单
        // addCustomMenusToMenuBar(role);

        // 2. 在菜单树添加自定义菜单
        // addCustomMenusToTree(role);
    }

    /**
     * 判断是否为管理员
     */
    private boolean isAdmin(String role) {
        return "admin".equals(role) || "administrator".equals(role) || "超级管理员".equals(role)
                || "管理员".equals(role) || "0".equals(role) || "ROLE_ADMIN".equals(role);
    }

    /**
     * 判断是否为老师
     */
    private boolean isTeacher(String role) {
        return "teacher".equals(role) || "老师".equals(role) || "教师".equals(role)
                || "2".equals(role) || "ROLE_TEACHER".equals(role);
    }

    /**
     * 判断是否为学生
     */
    private boolean isStudent(String role) {
        return "student".equals(role) || "学生".equals(role) || "1".equals(role)
                || "ROLE_STUDENT".equals(role);
    }

    /**
     * 在菜单栏添加自定义菜单
     */
    private void addCustomMenusToMenuBar(String role) {
        // 添加课程管理菜单（统一入口）
        Menu courseManageMenu = new Menu("课程管理");
        menuBar.getMenus().add(courseManageMenu);

        // 学生选课（给学生和管理员）
        if (isStudent(role) || isAdmin(role)) {
            MenuItem selectCourseItem = new MenuItem("学生选课");
            selectCourseItem.setId("student-select-course");
            selectCourseItem.setOnAction(this::changeContent);
            courseManageMenu.getItems().add(selectCourseItem);
        }

        // 开设课程（给老师和管理员）
        if (isTeacher(role) || isAdmin(role)) {
            MenuItem openCourseItem = new MenuItem("开设课程");
            openCourseItem.setId("teacher-open-course");
            openCourseItem.setOnAction(this::changeContent);
            courseManageMenu.getItems().add(openCourseItem);
        }

        System.out.println("✅ 课程管理菜单已添加到菜单栏");
    }

    /**
     * 在菜单树添加自定义菜单
     */
    private void addCustomMenusToTree(String role) {
        TreeItem<MyTreeNode> root = menuTree.getRoot();
        if (root == null) {
            MyTreeNode rootNode = new MyTreeNode(null, null, "菜单", 0);
            root = new TreeItem<>(rootNode);
            menuTree.setRoot(root);
        }

        // 添加课程管理菜单（统一入口）
        TreeItem<MyTreeNode> courseManageMenuItem = new TreeItem<>(new MyTreeNode(null, "course-manage-menu", "课程管理", 0));

        // 学生选课（给学生和管理员）
        if (isStudent(role) || isAdmin(role)) {
            TreeItem<MyTreeNode> selectCourseTreeItem = new TreeItem<>(
                    new MyTreeNode(null, "student-select-course", "学生选课", 0)
            );
            courseManageMenuItem.getChildren().add(selectCourseTreeItem);
        }

        // 开设课程（给老师和管理员）
        if (isTeacher(role) || isAdmin(role)) {
            TreeItem<MyTreeNode> openCourseTreeItem = new TreeItem<>(
                    new MyTreeNode(null, "teacher-open-course", "开设课程", 0)
            );
            courseManageMenuItem.getChildren().add(openCourseTreeItem);
        }

        root.getChildren().add(courseManageMenuItem);
        courseManageMenuItem.setExpanded(true);

        System.out.println("✅ 课程管理菜单已添加到菜单树");
    }

    /**
     * 打印用户信息
     */
    private void printUserInfo() {
        try {
            String role = AppStore.getJwt() != null ? AppStore.getJwt().getRole() : "unknown";
            String username = AppStore.getJwt() != null ? AppStore.getJwt().getUsername() : "unknown";
            System.out.println("=== 当前用户信息 ===");
            System.out.println("用户名: " + username);
            System.out.println("角色: " + role);
            System.out.println("是否是老师: " + isTeacher(role));
            System.out.println("是否是学生: " + isStudent(role));
            System.out.println("是否是管理员: " + isAdmin(role));
        } catch (Exception e) {
            System.out.println("获取用户信息失败: " + e.getMessage());
        }
    }

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

    public void changeContent(ActionEvent ae) {
        Object obj = ae.getSource();
        String name = null, title = null;
        if(obj instanceof MenuItem) {
            MenuItem item = (MenuItem)obj;
            name = item.getId();
            title = item.getText();
        }
        if(name == null)
            return;
        changeContent(name, title);
    }

    /**
     * 修改主工作区内容
     */
    public void changeContent(String name, String title) {
        System.out.println("=== changeContent 被调用 ===");
        System.out.println("name = " + name);
        System.out.println("title = " + title);

        // 获取当前用户角色
        String role = AppStore.getJwt() != null ? AppStore.getJwt().getRole() : "unknown";
        System.out.println("当前用户角色: " + role);

        // 如果是一级菜单（包含"-menu"后缀），不加载界面，只展开/折叠
        if (name != null && name.endsWith("-menu")) {
            System.out.println("📁 点击了一级菜单，不加载界面，只展开/折叠子菜单");
            // 查找对应的 TreeItem 并切换展开状态
            toggleTreeNode(name);
            return;
        }

        // 检查学生选课权限
        if ("student-select-course".equals(name)) {
            if (!isStudent(role) && !isAdmin(role)) {
                System.out.println("❌ 权限不足：只有学生和管理员可以访问学生功能");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("权限不足");
                alert.setHeaderText(null);
                alert.setContentText("您没有权限访问学生功能，只有学生和管理员可以访问");
                alert.showAndWait();
                return;
            }
        }

        // 检查老师功能权限
        if ("teacher-open-course".equals(name) || "teacher-course-manage".equals(name)) {
            if (!isTeacher(role) && !isAdmin(role)) {
                System.out.println("❌ 权限不足：只有老师和管理员可以访问老师功能");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("权限不足");
                alert.setHeaderText(null);
                alert.setContentText("您没有权限访问老师功能，只有老师和管理员可以访问");
                alert.showAndWait();
                return;
            }
        }

        // 检查请假管理权限
        if ("student-leave-panel".equals(name)) {
            // 学生请假申请：学生和管理员
            if (!isStudent(role) && !isAdmin(role)) {
                System.out.println("❌ 权限不足：只有学生和管理员可以访问请假申请");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("权限不足");
                alert.setHeaderText(null);
                alert.setContentText("您没有权限访问请假申请功能");
                alert.showAndWait();
                return;
            }
        }
        
        if ("admin-leave-approve".equals(name)) {
            // 管理员审批：仅管理员
            if (!isAdmin(role)) {
                System.out.println(" 权限不足：只有管理员可以访问管理员审批");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("权限不足");
                alert.setHeaderText(null);
                alert.setContentText("您没有权限访问管理员审批功能");
                alert.showAndWait();
                return;
            }
        }
        
        if ("teacher-leave-approve".equals(name)) {
            // 老师审批：老师和管理员
            if (!isTeacher(role) && !isAdmin(role)) {
                System.out.println("❌ 权限不足：只有老师和管理员可以访问老师审批");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("权限不足");
                alert.setHeaderText(null);
                alert.setContentText("您没有权限访问老师审批功能");
                alert.showAndWait();
                return;
            }
        }
        
        // 检查课件管理权限
        if ("courseware-panel".equals(name)) {
            // 课件管理：所有人
            if (!isStudent(role) && !isTeacher(role) && !isAdmin(role)) {
                System.out.println("❌ 权限不足：您没有权限访问课件管理");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("权限不足");
                alert.setHeaderText(null);
                alert.setContentText("您没有权限访问课件管理功能");
                alert.showAndWait();
                return;
            }
        }
        if ("shuttle-bus".equals(name)) {
            // 班次管理：仅管理员
            if (!isAdmin(role)) {
                System.out.println("❌ 权限不足：只有管理员可以访问班次管理");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("权限不足");
                alert.setHeaderText(null);
                alert.setContentText("您没有权限访问班次管理，只有管理员可以访问");
                alert.showAndWait();
                return;
            }
        }

        if ("shuttle-reserve".equals(name) || "my-reservations".equals(name)) {
            // 预约班车和我的预约：学生、老师、管理员
            if (!isStudent(role) && !isTeacher(role) && !isAdmin(role)) {
                System.out.println("❌ 权限不足：只有学生、老师和管理员可以访问校车预约功能");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("权限不足");
                alert.setHeaderText(null);
                alert.setContentText("您没有权限访问校车预约功能");
                alert.showAndWait();
                return;
            }
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
                // 尝试加载FXML文件 - 使用第一种方法（第一种版本的方法）
                try {
                    // 第一种尝试：使用第一种版本的加载方式
                    String fxmlPath = "com/teach/javafx/view/" + name + ".fxml";
                    System.out.println("尝试加载FXML: " + fxmlPath);

                    // 尝试从类路径加载
                    java.net.URL url = getClass().getClassLoader().getResource(fxmlPath);
                    if (url == null) {
                        // 第二种尝试：从MainApplication类加载
                        url = MainApplication.class.getResource(fxmlPath);
                    }

                    if (url == null) {
                        // 第三种尝试：从绝对路径加载
                        fxmlPath = "/com/teach/javafx/view/" + name + ".fxml";
                        url = MainApplication.class.getResource(fxmlPath);
                    }

                    if (url == null) {
                        // 第四种尝试：从resources目录直接加载
                        fxmlPath = name + ".fxml";
                        url = MainApplication.class.getResource(fxmlPath);
                    }

                    if (url != null) {
                        System.out.println("✅ 找到FXML文件: " + url);
                        FXMLLoader fxmlLoader = new FXMLLoader(url);
                        scene = new Scene(fxmlLoader.load(), 1024, 768);
                        sceneMap.put(name, scene);
                        c = fxmlLoader.getController();
                        if(c != null) {
                            controlMap.put(name, c);
                            System.out.println("✅ 成功加载控制器: " + c.getClass().getName());

                            // 如果控制器是老师开课控制器，尝试调用初始化方法
                            if (c.getClass().getSimpleName().contains("CourseEditController") ||
                                    c.getClass().getSimpleName().contains("TeacherOpenCourseController")) {
                                System.out.println("检测到老师开课控制器，尝试调用初始化方法");
                                try {
                                    // 尝试调用可能存在的初始化方法
                                    Method initMethod = c.getClass().getMethod("initialize");
                                    if (initMethod != null) {
                                        initMethod.invoke(c);
                                        System.out.println("✅ 成功调用控制器的initialize方法");
                                    }
                                } catch (NoSuchMethodException e) {
                                    System.out.println("控制器没有initialize方法，跳过");
                                } catch (Exception e) {
                                    System.out.println("调用initialize方法失败: " + e.getMessage());
                                }
                            }
                        } else {
                            System.out.println("⚠️ 控制器为null");
                        }
                    } else {
                        System.out.println("❌ 未找到FXML文件: " + name + ".fxml");
                        // 创建默认界面
                        Label label = new Label("功能界面: " + title + "\n\nFXML文件未找到: " + name + ".fxml\n\n请确保FXML文件位于正确的路径");
                        label.setStyle("-fx-font-size: 18px; -fx-font-weight: normal; -fx-text-fill: red;");
                        label.setWrapText(true);
                        label.setAlignment(javafx.geometry.Pos.CENTER);

                        StackPane stackPane = new StackPane();
                        stackPane.getChildren().add(label);
                        stackPane.setAlignment(Pos.CENTER);

                        scene = new Scene(stackPane, 600, 400);
                        sceneMap.put(name, scene);
                    }
                } catch (Exception e) {
                    System.out.println("❌ 加载FXML文件时出错: " + e.getMessage());
                    e.printStackTrace();

                    // 创建错误界面
                    Label label = new Label("加载界面时出错: " + e.getMessage());
                    label.setStyle("-fx-font-size: 16px; -fx-font-weight: normal; -fx-text-fill: red;");
                    label.setWrapText(true);
                    label.setAlignment(javafx.geometry.Pos.CENTER);

                    StackPane stackPane = new StackPane();
                    stackPane.getChildren().add(label);
                    stackPane.setAlignment(Pos.CENTER);

                    scene = new Scene(stackPane, 600, 400);
                    sceneMap.put(name, scene);
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
        Object c = controlMap.get(name);
        if(c != null) {
            try {
                // 尝试调用doRefresh方法
                Method method = c.getClass().getMethod("doRefresh");
                if (method != null) {
                    method.invoke(c);
                }
            } catch (Exception ex) {
                // 如果控制器没有doRefresh方法，忽略
                // System.out.println("⚠️ 控制器 " + c.getClass().getName() + " 没有doRefresh方法，跳过");
            }
        }
    }

    /**
     * 关闭标签页
     */
    public void tabOnClosed(Event e) {
        Tab tab = (Tab)e.getSource();
        String name = tab.getId();
        contentTabPane.getTabs().remove(tab);
        tabMap.remove(name);
        controlMap.remove(name);
    }

    /**
     * 切换树节点的展开/折叠状态
     */
    private void toggleTreeNode(String nodeName) {
        TreeItem<MyTreeNode> root = menuTree.getRoot();
        if (root == null) return;
        
        // 递归查找对应的 TreeItem
        TreeItem<MyTreeNode> targetNode = findTreeNode(root, nodeName);
        if (targetNode != null) {
            // 切换展开状态
            targetNode.setExpanded(!targetNode.isExpanded());
            System.out.println("🔄 已切换节点 " + nodeName + " 的展开状态: " + targetNode.isExpanded());
        } else {
            System.out.println("⚠️ 未找到节点: " + nodeName);
        }
    }
    
    /**
     * 递归查找树节点
     */
    private TreeItem<MyTreeNode> findTreeNode(TreeItem<MyTreeNode> parent, String nodeName) {
        if (parent.getValue() != null && nodeName.equals(parent.getValue().getValue())) {
            return parent;
        }
        
        for (TreeItem<MyTreeNode> child : parent.getChildren()) {
            TreeItem<MyTreeNode> found = findTreeNode(child, nodeName);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }

    /**
     * 获取当前显示的面板的控制对象
     */
    public Object getCurrentToolController(){
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
     * 新建命令
     */
    protected void doNewCommand(){
        Object c = getCurrentToolController();
        if(c == null)
            return;
        try {
            Method method = c.getClass().getMethod("doNew");
            if (method != null) {
                method.invoke(c);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 控制器没有doNew方法: " + e.getMessage());
        }
    }

    /**
     * 保存命令
     */
    protected void doSaveCommand(){
        Object c = getCurrentToolController();
        if(c == null)
            return;
        try {
            Method method = c.getClass().getMethod("doSave");
            if (method != null) {
                method.invoke(c);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 控制器没有doSave方法: " + e.getMessage());
        }
    }

    /**
     * 删除命令
     */
    protected void doDeleteCommand(){
        Object c = getCurrentToolController();
        if(c == null)
            return;
        try {
            Method method = c.getClass().getMethod("doDelete");
            if (method != null) {
                method.invoke(c);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 控制器没有doDelete方法: " + e.getMessage());
        }
    }

    /**
     * 打印命令
     */
    protected void doPrintCommand(){
        Object c = getCurrentToolController();
        if(c == null)
            return;
        try {
            Method method = c.getClass().getMethod("doPrint");
            if (method != null) {
                method.invoke(c);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 控制器没有doPrint方法: " + e.getMessage());
        }
    }

    /**
     * 导出命令
     */
    protected void doExportCommand(){
        Object c = getCurrentToolController();
        if(c == null)
            return;
        try {
            Method method = c.getClass().getMethod("doExport");
            if (method != null) {
                method.invoke(c);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 控制器没有doExport方法: " + e.getMessage());
        }
    }

    /**
     * 导入命令
     */
    protected void doImportCommand(){
        Object c = getCurrentToolController();
        if(c == null)
            return;
        try {
            Method method = c.getClass().getMethod("doImport");
            if (method != null) {
                method.invoke(c);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 控制器没有doImport方法: " + e.getMessage());
        }
    }

    /**
     * 测试命令
     */
    protected void doTestCommand(){
        Object c = getCurrentToolController();
        if(c == null) {
            System.out.println("当前没有选中的控制器");
            return;
        }
        try {
            Method method = c.getClass().getMethod("doTest");
            if (method != null) {
                method.invoke(c);
            }
        } catch (Exception e) {
            System.out.println("⚠️ 控制器没有doTest方法: " + e.getMessage());
        }
    }

    public Object getToolController(String name){
        return controlMap.get(name);
    }
}