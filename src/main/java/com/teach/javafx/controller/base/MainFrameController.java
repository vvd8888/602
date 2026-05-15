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

        initMenuBar(mList);
        initMenuTree(mList);

        // 添加自定义菜单
        addCustomMenus();

        contentTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        contentTabPane.setStyle("-fx-background-image: url('shanda1.jpg'); -fx-background-repeat: no-repeat; -fx-background-size: cover;");

        // 打印调试信息
        printUserInfo();

        System.out.println("✅ MainFrameController 初始化完成");
        System.out.println("菜单栏菜单数量: " + menuBar.getMenus().size());
    }

    /**
     * 添加自定义菜单
     */
    private void addCustomMenus() {
        System.out.println("=== 添加自定义菜单 ===");

        // 获取当前用户角色
        String role = AppStore.getJwt() != null ? AppStore.getJwt().getRole() : "unknown";
        System.out.println("当前用户角色: " + role);

        // 1. 添加老师功能菜单
        addTeacherMenu(role);

        // 2. 添加学生功能菜单
        addStudentMenu(role);

        // 3. 添加测试功能菜单
        addTestMenu();
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
     * 添加老师菜单
     */
    private void addTeacherMenu(String role) {
        // 老师和管理员都能看到老师功能
        if (!isTeacher(role) && !isAdmin(role)) {
            System.out.println("当前用户不是老师也不是管理员，不显示老师功能菜单");
            return;
        }

        // 在菜单栏中添加老师功能菜单
        Menu teacherMenu = new Menu("老师功能");
        menuBar.getMenus().add(teacherMenu);

        MenuItem openCourseItem = new MenuItem("开设课程");
        openCourseItem.setId("teacher-open-course");
        openCourseItem.setOnAction(this::changeContent);
        teacherMenu.getItems().add(openCourseItem);

        MenuItem courseManageItem = new MenuItem("课程管理");
        courseManageItem.setId("teacher-course-manage");
        courseManageItem.setOnAction(this::changeContent);
        teacherMenu.getItems().add(courseManageItem);

        // 在菜单树中添加老师功能菜单
        MyTreeNode rootNode = (MyTreeNode) menuTree.getRoot().getValue();
        if (rootNode == null) {
            rootNode = new MyTreeNode(null, null, "菜单", 0);
            TreeItem<MyTreeNode> root = new TreeItem<>(rootNode);
            menuTree.setRoot(root);
        }

        TreeItem<MyTreeNode> root = menuTree.getRoot();
        TreeItem<MyTreeNode> teacherMenuItem = new TreeItem<>(new MyTreeNode(null, "teacher-menu", "老师功能", 0));

        TreeItem<MyTreeNode> openCourseTreeItem = new TreeItem<>(
                new MyTreeNode(null, "teacher-open-course", "开设课程", 0)
        );
        teacherMenuItem.getChildren().add(openCourseTreeItem);

        TreeItem<MyTreeNode> courseManageTreeItem = new TreeItem<>(
                new MyTreeNode(null, "teacher-course-manage", "课程管理", 0)
        );
        teacherMenuItem.getChildren().add(courseManageTreeItem);

        root.getChildren().add(teacherMenuItem);

        System.out.println("✅ 老师功能菜单已添加");
    }

    /**
     * 添加学生菜单
     */
    private void addStudentMenu(String role) {
        // 学生和管理员都能看到学生功能
        if (!isStudent(role) && !isAdmin(role)) {
            System.out.println("当前用户不是学生也不是管理员，不显示学生功能菜单");
            return;
        }

        // 在菜单栏中添加学生功能菜单
        Menu studentMenu = new Menu("学生功能");
        menuBar.getMenus().add(studentMenu);

        MenuItem selectCourseItem = new MenuItem("选课");
        selectCourseItem.setId("student-select-course");
        selectCourseItem.setOnAction(this::changeContent);
        studentMenu.getItems().add(selectCourseItem);

        MenuItem myCourseItem = new MenuItem("我的课程");
        myCourseItem.setId("student-my-course");
        myCourseItem.setOnAction(this::changeContent);
        studentMenu.getItems().add(myCourseItem);

        // 在菜单树中添加学生功能菜单
        TreeItem<MyTreeNode> root = menuTree.getRoot();
        TreeItem<MyTreeNode> studentMenuItem = new TreeItem<>(new MyTreeNode(null, "student-menu", "学生功能", 0));

        TreeItem<MyTreeNode> selectCourseTreeItem = new TreeItem<>(
                new MyTreeNode(null, "student-select-course", "选课", 0)
        );
        studentMenuItem.getChildren().add(selectCourseTreeItem);

        TreeItem<MyTreeNode> myCourseTreeItem = new TreeItem<>(
                new MyTreeNode(null, "student-my-course", "我的课程", 0)
        );
        studentMenuItem.getChildren().add(myCourseTreeItem);

        root.getChildren().add(studentMenuItem);

        System.out.println("✅ 学生功能菜单已添加");
    }

    /**
     * 添加测试菜单
     */
    private void addTestMenu() {
        // 所有人都能看到测试功能
        Menu testMenu = new Menu("测试功能");
        menuBar.getMenus().add(testMenu);

        MenuItem teacherTestItem = new MenuItem("老师课程测试");
        teacherTestItem.setId("teacher-open-course");
        teacherTestItem.setOnAction(this::changeContent);
        testMenu.getItems().add(teacherTestItem);

        // 获取当前用户角色
        String role = AppStore.getJwt() != null ? AppStore.getJwt().getRole() : "unknown";
        if (isStudent(role) || isAdmin(role)) {
            MenuItem studentTestItem = new MenuItem("学生选课测试");
            studentTestItem.setId("student-select-course");
            studentTestItem.setOnAction(this::changeContent);
            testMenu.getItems().add(studentTestItem);
        }

        // 在菜单树中添加测试功能菜单
        TreeItem<MyTreeNode> root = menuTree.getRoot();
        TreeItem<MyTreeNode> testMenuItem = new TreeItem<>(new MyTreeNode(null, "test-menu", "测试功能", 0));

        TreeItem<MyTreeNode> teacherTestTreeItem = new TreeItem<>(
                new MyTreeNode(null, "teacher-open-course", "老师课程测试", 0)
        );
        testMenuItem.getChildren().add(teacherTestTreeItem);

        if (isStudent(role) || isAdmin(role)) {
            TreeItem<MyTreeNode> studentTestTreeItem = new TreeItem<>(
                    new MyTreeNode(null, "student-select-course", "学生选课测试", 0)
            );
            testMenuItem.getChildren().add(studentTestTreeItem);
        }

        root.getChildren().add(testMenuItem);

        System.out.println("✅ 测试功能菜单已添加");
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

        // 检查学生选课权限
        if ("student-select-course".equals(name) || "student-my-course".equals(name)) {
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
                // 尝试不同的路径
                String[] possiblePaths = {
                        "com/teach/javafx/view/" + name + ".fxml",  // 默认路径
                        "/com/teach/javafx/view/" + name + ".fxml",  // 绝对路径
                        name + ".fxml",  // 相对路径
                        "/" + name + ".fxml"  // 绝对路径
                };

                FXMLLoader fxmlLoader = null;
                boolean loaded = false;

                for (String path : possiblePaths) {
                    try {
                        System.out.println("尝试加载路径: " + path);
                        java.net.URL url = MainApplication.class.getResource(path);
                        if (url != null) {
                            System.out.println("找到资源: " + url);
                            fxmlLoader = new FXMLLoader(url);
                            scene = new Scene(fxmlLoader.load(), 1024, 768);
                            sceneMap.put(name, scene);
                            c = fxmlLoader.getController();
                            if(c != null) {
                                controlMap.put(name, c);
                            }
                            loaded = true;
                            System.out.println("✅ 成功加载: " + path);
                            break;
                        } else {
                            System.out.println("❌ 资源未找到: " + path);
                        }
                    } catch (Exception e) {
                        System.out.println("加载失败 (" + path + "): " + e.getMessage());
                    }
                }

                if (!loaded) {
                    System.out.println("❌ 所有路径都尝试失败，创建默认界面");
                    // 创建默认界面
                    Label label = new Label("功能界面: " + title);
                    label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

                    // 使用StackPane避免VBox导入问题
                    javafx.scene.layout.StackPane stackPane = new javafx.scene.layout.StackPane();
                    stackPane.getChildren().add(label);
                    stackPane.setAlignment(javafx.geometry.Pos.CENTER);

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