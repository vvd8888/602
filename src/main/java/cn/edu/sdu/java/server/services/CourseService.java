package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Score;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.ScoreRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final ScoreRepository scoreRepository;
    private final PersonRepository personRepository;
    private final StudentRepository studentRepository;

    public CourseService(CourseRepository courseRepository,
                         ScoreRepository scoreRepository,
                         PersonRepository personRepository,
                         StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.scoreRepository = scoreRepository;
        this.personRepository = personRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 获取当前登录的用户名
     */
    private String getCurrentUsername() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取课程列表
     */
    public DataResponse getCourseList(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        if (numName == null) {
            numName = "";
        }

        List<Course> cList = courseRepository.findCourseListByNumName(numName);
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> m;
        Course pc;

        for (Course c : cList) {
            m = new HashMap<>();
            m.put("courseId", c.getCourseId() + "");
            m.put("num", c.getNum());
            m.put("name", c.getName());
            m.put("credit", c.getCredit() + "");
            m.put("coursePath", c.getCoursePath());
            m.put("teacher", c.getTeacher());
            m.put("time", c.getTime());
            m.put("classroom", c.getClassroom());
            m.put("status", c.getStatus() != null ? c.getStatus() : "PENDING");

            pc = c.getPreCourse();
            if (pc != null) {
                m.put("preCourse", pc.getName());
                m.put("preCourseId", pc.getCourseId());
            }

            dataList.add(m);
        }

        return CommonMethod.getReturnData(dataList);
    }

    /**
     * 保存课程（新增或修改）
     */
    public DataResponse courseSave(DataRequest dataRequest) {
        try {
            Integer courseId = dataRequest.getInteger("courseId");
            String num = dataRequest.getString("num");
            String name = dataRequest.getString("name");
            String coursePath = dataRequest.getString("coursePath");
            Integer credit = dataRequest.getInteger("credit");
            Integer preCourseId = dataRequest.getInteger("preCourseId");
            String teacher = dataRequest.getString("teacher");
            String time = dataRequest.getString("time");
            String classroom = dataRequest.getString("classroom");
            String status = dataRequest.getString("status");

            Optional<Course> op;
            Course c = null;

            if (courseId != null) {
                op = courseRepository.findById(courseId);
                if (op.isPresent()) {
                    c = op.get();
                }
            }

            if (c == null) {
                c = new Course();
            }

            Course pc = null;
            if (preCourseId != null) {
                op = courseRepository.findById(preCourseId);
                if (op.isPresent()) {
                    pc = op.get();
                }
            }

            c.setNum(num);
            c.setName(name);
            c.setCredit(credit);
            c.setCoursePath(coursePath);
            c.setPreCourse(pc);
            c.setTeacher(teacher);
            c.setTime(time);
            c.setClassroom(classroom);

            if (status != null && !status.isEmpty()) {
                c.setStatus(status);
            } else if (c.getStatus() == null) {
                c.setStatus("PENDING");
            }

            courseRepository.save(c);
            return CommonMethod.getReturnMessageOK();

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除课程
     */
    public DataResponse courseDelete(DataRequest dataRequest) {
        try {
            Integer courseId = dataRequest.getInteger("courseId");

            if (courseId != null) {
                Optional<Course> op = courseRepository.findById(courseId);
                if (op.isPresent()) {
                    courseRepository.delete(op.get());
                }
            }

            return CommonMethod.getReturnMessageOK();

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("删除失败：" + e.getMessage());
        }
    }

    /**
     * 教师开设课程
     */
    public DataResponse openCourse(DataRequest dataRequest) {
        try {
            // 1. 获取当前登录的用户名（教师）
            String currentUsername = getCurrentUsername();
            if (currentUsername == null || currentUsername.isEmpty()) {
                return CommonMethod.getReturnMessageError("用户未登录或登录信息无效");
            }

            // 2. 获取请求中的课程信息
            String num = dataRequest.getString("num");
            String name = dataRequest.getString("name");
            String coursePath = dataRequest.getString("coursePath");
            Integer credit = dataRequest.getInteger("credit");
            Integer preCourseId = dataRequest.getInteger("preCourseId");
            String time = dataRequest.getString("time");
            String classroom = dataRequest.getString("classroom");
            String status = dataRequest.getString("status");

            // 3. 验证必填字段
            if (num == null || num.isEmpty()) {
                return CommonMethod.getReturnMessageError("课程编号不能为空");
            }
            if (name == null || name.isEmpty()) {
                return CommonMethod.getReturnMessageError("课程名称不能为空");
            }
            if (credit == null || credit <= 0) {
                return CommonMethod.getReturnMessageError("学分必须大于0");
            }

            // 4. 检查课程编号是否已存在
            Optional<Course> existingCourse = courseRepository.findByNum(num);
            if (existingCourse.isPresent()) {
                return CommonMethod.getReturnMessageError("课程编号已存在");
            }

            // 5. 创建新课程
            Course newCourse = new Course();
            newCourse.setNum(num);
            newCourse.setName(name);
            newCourse.setCredit(credit);
            newCourse.setCoursePath(coursePath != null ? coursePath : "");
            newCourse.setTeacher(currentUsername);
            newCourse.setTime(time != null ? time : "");
            newCourse.setClassroom(classroom != null ? classroom : "");

            if (status != null && !status.isEmpty()) {
                newCourse.setStatus(status);
            } else {
                newCourse.setStatus("PENDING");
            }

            // 6. 设置前置课程
            if (preCourseId != null) {
                Optional<Course> preCourse = courseRepository.findById(preCourseId);
                preCourse.ifPresent(newCourse::setPreCourse);
            }

            // 7. 保存课程
            courseRepository.save(newCourse);

            // 8. 返回成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("courseId", newCourse.getCourseId());
            result.put("message", "课程开设成功");
            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("开课失败：" + e.getMessage());
        }
    }

    /**
     * 更新课程状态（开放/关闭）
     */
    public DataResponse updateCourseStatus(DataRequest dataRequest) {
        try {
            System.out.println("🎯 开始更新课程状态");

            // 1. 获取参数
            Integer courseId = dataRequest.getInteger("courseId");
            String status = dataRequest.getString("status");

            System.out.println("请求参数 - courseId: " + courseId + ", status: " + status);

            if (courseId == null) {
                return CommonMethod.getReturnMessageError("课程ID不能为空");
            }

            if (status == null || (!"OPEN".equals(status) && !"CLOSED".equals(status) && !"PENDING".equals(status))) {
                return CommonMethod.getReturnMessageError("课程状态无效，必须是 OPEN、CLOSED 或 PENDING");
            }

            // 2. 查找课程
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (!courseOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("课程不存在，ID: " + courseId);
            }

            Course course = courseOpt.get();
            System.out.println("找到课程: " + course.getName() + "，当前状态: " + course.getStatus());

            // 3. 检查权限：只有课程的老师可以修改状态
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 获取当前用户的Person信息
            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("用户信息不存在");
            }

            Person currentUser = personOpt.get();

            // 检查是否是该课程的老师
            if (!currentUsername.equals(course.getTeacher()) && !isAdmin(currentUser)) {
                return CommonMethod.getReturnMessageError("您没有权限修改此课程的状态");
            }

            // 4. 更新课程状态
            course.setStatus(status);
            courseRepository.save(course);

            System.out.println("✅ 课程状态更新成功: " + course.getName() + " -> " + status);

            // 5. 返回成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("courseId", course.getCourseId());
            result.put("courseNum", course.getNum());
            result.put("courseName", course.getName());
            result.put("oldStatus", course.getStatus());
            result.put("newStatus", status);
            result.put("message", "课程状态更新成功");

            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("更新课程状态失败：" + e.getMessage());
        }
    }

    /**
     * 学生获取可选课程列表（排除已选课程）
     */
    public DataResponse getAvailableCourses(DataRequest dataRequest) {
        try {
            // 1. 获取当前登录用户
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 2. 通过用户名查询学生
            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("学生信息不存在");
            }
            Person student = personOpt.get();

            // 3. 获取所有开放选课的课程
            List<Course> openCourses = courseRepository.findByStatus("OPEN");
            
            // 4. 同步课程人数 - 从数据库重新计算，并确保maxCapacity有值
            for (Course course : openCourses) {
                // 从数据库计算实际选课人数
                long actualCount = scoreRepository.findByCourseId(course.getCourseId()).stream()
                        .filter(s -> "APPROVED".equals(s.getSelectionStatus()))
                        .count();
                
                course.setCurrentEnrolled((int) actualCount);
                
                // 如果maxCapacity为null或0，设置默认值为50
                if (course.getMaxCapacity() == null || course.getMaxCapacity() == 0) {
                    course.setMaxCapacity(50);
                }
                
                // 保存更新后的课程信息
                courseRepository.save(course);
                
                System.out.println("同步课程人数: " + course.getName() + 
                        " (已选:" + course.getCurrentEnrolled() + "/总容量:" + course.getMaxCapacity() + ")");
            }

            // 5. 获取该学生已选的课程ID
            List<Score> studentScores = scoreRepository.findByStudentPersonId(student.getPersonId());
            Set<Integer> selectedCourseIds = studentScores.stream()
                    .map(score -> score.getCourse().getCourseId())
                    .collect(Collectors.toSet());

            // 6. 过滤掉已选课程
            List<Course> availableCourses = openCourses.stream()
                    .filter(course -> !selectedCourseIds.contains(course.getCourseId()))
                    .collect(Collectors.toList());

            // 7. 转换为前端需要的格式
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Course course : availableCourses) {
                Map<String, Object> m = new HashMap<>();
                m.put("courseId", course.getCourseId());
                m.put("num", course.getNum());
                m.put("name", course.getName());
                m.put("credit", course.getCredit());
                m.put("teacher", course.getTeacher());
                m.put("time", course.getTime());
                m.put("classroom", course.getClassroom());
                m.put("status", course.getStatus());
                m.put("maxCapacity", course.getMaxCapacity());
                m.put("currentEnrolled", course.getCurrentEnrolled());
                m.put("availableSeats", course.getMaxCapacity() != null && course.getCurrentEnrolled() != null 
                        ? course.getMaxCapacity() - course.getCurrentEnrolled() : null);

                // 前置课程信息
                Course preCourse = course.getPreCourse();
                if (preCourse != null) {
                    m.put("preCourseName", preCourse.getName());
                    m.put("preCourseId", preCourse.getCourseId());
                }

                dataList.add(m);
            }

            System.out.println("✅ 返回可选课程数量: " + dataList.size());
            return CommonMethod.getReturnData(dataList);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("获取课程失败：" + e.getMessage());
        }
    }

    /**
     * 学生提交选课申请
     */
    public DataResponse applyForCourse(DataRequest dataRequest) {
        try {
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            Integer courseId = dataRequest.getInteger("courseId");
            if (courseId == null) {
                return CommonMethod.getReturnMessageError("课程ID不能为空");
            }

            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (!courseOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("课程不存在");
            }
            Course course = courseOpt.get();

            if (!"OPEN".equals(course.getStatus())) {
                return CommonMethod.getReturnMessageError("该课程暂不开放选课");
            }

            if (course.getMaxCapacity() != null && course.getCurrentEnrolled() != null 
                    && course.getCurrentEnrolled() >= course.getMaxCapacity()) {
                return CommonMethod.getReturnMessageError("该课程已满，无法选课");
            }

            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("学生信息不存在");
            }
            Person student = personOpt.get();

            Optional<Score> existingScore = scoreRepository.findByPersonIdAndCourseId(
                    student.getPersonId(), courseId);
            if (existingScore.isPresent()) {
                Score score = existingScore.get();
                String status = score.getSelectionStatus();
                if ("PENDING".equals(status)) {
                    return CommonMethod.getReturnMessageError("已提交选课申请，等待审核");
                } else if ("APPROVED".equals(status)) {
                    return CommonMethod.getReturnMessageError("已成功选修此课程");
                } else if ("REJECTED".equals(status)) {
                    return CommonMethod.getReturnMessageError("选课申请已被拒绝");
                }
            }

            if (!hasCompletedPreCourse(student.getPersonId(), course)) {
                Course preCourse = course.getPreCourse();
                String preCourseName = preCourse != null ? preCourse.getName() : "未知课程";
                return CommonMethod.getReturnMessageError("需要先完成前置课程：" + preCourseName);
            }

            List<Score> approvedCourses = scoreRepository.findByStudentPersonIdAndSelectionStatus(
                    student.getPersonId(), "APPROVED");
            
            if (hasTimeConflict(approvedCourses, course)) {
                return CommonMethod.getReturnMessageError("课程时间与已选课程冲突");
            }

            Score score = new Score();

            Optional<Student> studentOpt = studentRepository.findById(student.getPersonId());
            if (studentOpt.isPresent()) {
                score.setStudent(studentOpt.get());
            } else {
                Student newStudent = new Student();
                newStudent.setPersonId(student.getPersonId());
                studentRepository.save(newStudent);
                score.setStudent(newStudent);
            }

            score.setCourse(course);
            score.setSelectionStatus("PENDING");
            score.setApplyTime(new Date());

            scoreRepository.save(score);

            Map<String, Object> result = new HashMap<>();
            result.put("scoreId", score.getScoreId());
            result.put("message", "选课申请已提交，等待管理员审核");

            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("选课失败：" + e.getMessage());
        }
    }

    /**
     * 学生批量提交选课申请
     */
    public DataResponse submitSelections(DataRequest dataRequest) {
        try {
            System.out.println(" submitSelections 被调用");
            System.out.println("请求数据: " + dataRequest.getData());

            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("学生信息不存在");
            }
            Person student = personOpt.get();

            List<Integer> courseIds = new ArrayList<>();

            // 尝试获取courseIds列表
            List<?> courseIdsObj = dataRequest.getList("courseIds");
            System.out.println("courseIds列表: " + courseIdsObj);

            if (courseIdsObj != null && !courseIdsObj.isEmpty()) {
                for (Object obj : courseIdsObj) {
                    if (obj instanceof Integer) {
                        courseIds.add((Integer) obj);
                    } else if (obj instanceof String) {
                        try {
                            courseIds.add(Integer.parseInt((String) obj));
                        } catch (NumberFormatException e) {
                            System.err.println("无效的课程ID: " + obj);
                        }
                    } else if (obj instanceof Number) {
                        courseIds.add(((Number) obj).intValue());
                    }
                }
            }

            // 如果courseIds为空，尝试获取单个courseId
            if (courseIds.isEmpty()) {
                Integer courseId = dataRequest.getInteger("courseId");
                System.out.println("单个courseId: " + courseId);
                if (courseId != null) {
                    courseIds.add(courseId);
                }
            }

            // 如果还是空，尝试从data中直接获取所有可能的key
            if (courseIds.isEmpty()) {
                System.out.println("尝试从data中查找课程ID...");
                for (Map.Entry<String, Object> entry : dataRequest.getData().entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    System.out.println("  key: " + key + ", value: " + value + ", type: " + (value != null ? value.getClass().getName() : "null"));

                    if (key.toLowerCase().contains("course") && key.toLowerCase().contains("id")) {
                        if (value instanceof Integer) {
                            courseIds.add((Integer) value);
                        } else if (value instanceof String) {
                            try {
                                courseIds.add(Integer.parseInt((String) value));
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                    }
                }
            }

            System.out.println("最终解析到的courseIds: " + courseIds);

            if (courseIds.isEmpty()) {
                return CommonMethod.getReturnMessageError("课程ID不能为空，请检查前端传参格式");
            }

            // 检查是否有已APPROVED的课程（不能重复选）
            List<Score> allScores = scoreRepository.findByStudentPersonId(student.getPersonId());
            for (Integer courseId : courseIds) {
                Optional<Score> existingScore = scoreRepository.findByPersonIdAndCourseId(
                        student.getPersonId(), courseId);
                if (existingScore.isPresent()) {
                    Score score = existingScore.get();
                    if ("APPROVED".equals(score.getSelectionStatus())) {
                        return CommonMethod.getReturnMessageError("课程ID " + courseId + " 已成功选修，不能重复提交");
                    }
                }
            }

            // 删除该学生所有PENDING状态的选课记录（允许重新提交）
            List<Score> pendingScores = allScores.stream()
                    .filter(s -> "PENDING".equals(s.getSelectionStatus()))
                    .collect(Collectors.toList());

            if (!pendingScores.isEmpty()) {
                System.out.println("删除旧的PENDING选课记录: " + pendingScores.size() + " 条");
                scoreRepository.deleteAll(pendingScores);
            }

            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (Integer courseId : courseIds) {
                try {
                    System.out.println("处理课程ID: " + courseId);

                    // 为每个课程创建选课申请
                    Optional<Course> courseOpt = courseRepository.findById(courseId);
                    if (!courseOpt.isPresent()) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("courseId", courseId);
                        errorResult.put("error", "课程不存在");
                        results.add(errorResult);
                        failCount++;
                        continue;
                    }
                    Course course = courseOpt.get();

                    if (!"OPEN".equals(course.getStatus())) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("courseId", courseId);
                        errorResult.put("error", "该课程暂不开放选课");
                        results.add(errorResult);
                        failCount++;
                        continue;
                    }

                    if (course.getMaxCapacity() != null && course.getCurrentEnrolled() != null
                            && course.getCurrentEnrolled() >= course.getMaxCapacity()) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("courseId", courseId);
                        errorResult.put("error", "该课程已满");
                        results.add(errorResult);
                        failCount++;
                        continue;
                    }

                    // 检查前置课程
                    if (!hasCompletedPreCourse(student.getPersonId(), course)) {
                        Course preCourse = course.getPreCourse();
                        String preCourseName = preCourse != null ? preCourse.getName() : "未知课程";
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("courseId", courseId);
                        errorResult.put("error", "需要先完成前置课程：" + preCourseName);
                        results.add(errorResult);
                        failCount++;
                        continue;
                    }

                    // 检查时间冲突（只检查APPROVED的课程）
                    List<Score> approvedCourses = scoreRepository.findByStudentPersonIdAndSelectionStatus(
                            student.getPersonId(), "APPROVED");

                    if (hasTimeConflict(approvedCourses, course)) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("courseId", courseId);
                        errorResult.put("error", "课程时间冲突");
                        results.add(errorResult);
                        failCount++;
                        continue;
                    }

                    Score score = new Score();
                    Optional<Student> studentOpt = studentRepository.findById(student.getPersonId());
                    if (studentOpt.isPresent()) {
                        score.setStudent(studentOpt.get());
                    } else {
                        Student newStudent = new Student();
                        newStudent.setPersonId(student.getPersonId());
                        studentRepository.save(newStudent);
                        score.setStudent(newStudent);
                    }

                    score.setCourse(course);
                    score.setSelectionStatus("PENDING");
                    score.setApplyTime(new Date());

                    scoreRepository.save(score);

                    Map<String, Object> successResult = new HashMap<>();
                    successResult.put("courseId", courseId);
                    successResult.put("scoreId", score.getScoreId());
                    successResult.put("message", "选课申请已提交");
                    results.add(successResult);
                    successCount++;

                } catch (Exception e) {
                    e.printStackTrace();
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("courseId", courseId);
                    errorResult.put("error", "处理失败: " + e.getMessage());
                    results.add(errorResult);
                    failCount++;
                }
            }

            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("successCount", successCount);
            finalResult.put("failCount", failCount);
            finalResult.put("results", results);
            finalResult.put("message", String.format("提交完成：成功%d个，失败%d个", successCount, failCount));

            System.out.println("提交结果: " + finalResult);
            return CommonMethod.getReturnData(finalResult);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("批量选课失败：" + e.getMessage());
        }
    }

    public DataResponse getMySelections(DataRequest dataRequest) {
        try {
            // 1. 获取当前登录用户
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 2. 查询学生信息
            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("学生信息不存在");
            }
            Person student = personOpt.get();

            // 3. 查询该学生的选课记录
            List<Score> scores = scoreRepository.findByStudentPersonId(student.getPersonId());

            // 4. 转换为前端需要的格式
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Score score : scores) {
                Map<String, Object> m = new HashMap<>();
                m.put("scoreId", score.getScoreId());
                m.put("selectionStatus", score.getSelectionStatus());
                m.put("applyTime", score.getApplyTime());
                m.put("approveTime", score.getApproveTime());
                m.put("rejectReason", score.getRejectReason());
                m.put("mark", score.getMark());
                m.put("ranking", score.getRanking());

                // 课程信息
                Course course = score.getCourse();
                if (course != null) {
                    m.put("courseId", course.getCourseId());
                    m.put("courseNum", course.getNum());
                    m.put("courseName", course.getName());
                    m.put("courseCredit", course.getCredit());
                    m.put("courseTeacher", course.getTeacher());
                    m.put("courseTime", course.getTime());
                    m.put("courseClassroom", course.getClassroom());
                }

                dataList.add(m);
            }

            return CommonMethod.getReturnData(dataList);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("获取选课记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有学生的选课记录（用于教师统计课程人数）
     */
    public DataResponse getAllSelections(DataRequest dataRequest) {
        try {
            // 1. 查询所有选课记录
            List<Score> scores = scoreRepository.findAllSelections();

            // 2. 转换为前端需要的格式
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Score score : scores) {
                Map<String, Object> m = new HashMap<>();
                m.put("selectionId", score.getScoreId());
                m.put("selectionStatus", score.getSelectionStatus());
                m.put("applyTime", score.getApplyTime());
                m.put("approveTime", score.getApproveTime());
                m.put("rejectReason", score.getRejectReason());
                m.put("mark", score.getMark());
                m.put("ranking", score.getRanking());

                // 学生信息
                Student student = score.getStudent();
                if (student != null) {
                    m.put("personId", student.getPersonId());

                    // 从 Person 表获取学生姓名和学号
                    Optional<Person> personOpt = personRepository.findById(student.getPersonId());
                    if (personOpt.isPresent()) {
                        Person person = personOpt.get();
                        m.put("studentNum", person.getNum());
                        m.put("studentName", person.getName());
                    }
                }

                // 课程信息
                Course course = score.getCourse();
                if (course != null) {
                    m.put("courseId", course.getCourseId());
                    m.put("courseNum", course.getNum());
                    m.put("courseName", course.getName());
                    m.put("courseCredit", course.getCredit());
                    m.put("courseTeacher", course.getTeacher());
                    m.put("courseTime", course.getTime());
                    m.put("courseClassroom", course.getClassroom());
                }

                dataList.add(m);
            }

            return CommonMethod.getReturnData(dataList);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("获取所有选课记录失败：" + e.getMessage());
        }
    }

    /**
     * 教师更新学生成绩
     */
    public DataResponse updateSelectionMark(DataRequest dataRequest) {
        try {
            // 1. 获取当前登录用户
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 2. 获取请求参数
            Integer selectionId = dataRequest.getInteger("selectionId");
            Integer mark = dataRequest.getInteger("mark");

            // 3. 验证参数
            if (selectionId == null) {
                return CommonMethod.getReturnMessageError("选课记录ID不能为空");
            }
            if (mark == null) {
                return CommonMethod.getReturnMessageError("成绩不能为空");
            }
            if (mark < 0 || mark > 100) {
                return CommonMethod.getReturnMessageError("成绩范围必须在0-100之间");
            }

            // 4. 查询选课记录
            Optional<Score> scoreOpt = scoreRepository.findById(selectionId);
            if (!scoreOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("选课记录不存在");
            }
            Score score = scoreOpt.get();

            // 5. 更新成绩（移除状态限制，允许为任何状态的选课记录打分）
            score.setMark(mark);
            scoreRepository.save(score);

            // 6. 返回成功消息
            Map<String, Object> result = new HashMap<>();
            result.put("selectionId", selectionId);
            result.put("mark", mark);
            result.put("message", "成绩更新成功");

            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("成绩更新失败：" + e.getMessage());
        }
    }

    /**
     * 管理员获取待审核的选课申请列表
     */
    public DataResponse getPendingSelections(DataRequest dataRequest) {
        try {
            // 1. 获取当前登录用户
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 2. 验证是否为管理员
            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("用户信息不存在");
            }

            Person person = personOpt.get();
            if (!isAdmin(person)) {
                return CommonMethod.getReturnMessageError("需要管理员权限");
            }

            // 3. 查询所有待审核的选课记录
            List<Score> pendingScores = scoreRepository.findBySelectionStatus("PENDING");

            // 4. 转换为前端需要的格式
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Score score : pendingScores) {
                Map<String, Object> m = new HashMap<>();
                m.put("scoreId", score.getScoreId());
                m.put("applyTime", score.getApplyTime());
                m.put("selectionStatus", score.getSelectionStatus());

                // 学生信息
                Student student = score.getStudent();
                if (student != null) {
                    Optional<Person> studentPersonOpt = personRepository.findById(student.getPersonId());
                    if (studentPersonOpt.isPresent()) {
                        Person studentPerson = studentPersonOpt.get();
                        m.put("studentId", student.getPersonId());
                        m.put("studentNum", studentPerson.getNum());
                        m.put("studentName", studentPerson.getName());
                        m.put("className", student.getClassName());
                        m.put("major", student.getMajor());
                    }
                }

                // 课程信息
                Course course = score.getCourse();
                if (course != null) {
                    m.put("courseId", course.getCourseId());
                    m.put("courseNum", course.getNum());
                    m.put("courseName", course.getName());
                    m.put("courseCredit", course.getCredit());
                    m.put("courseTeacher", course.getTeacher());
                    m.put("courseTime", course.getTime());
                    m.put("courseClassroom", course.getClassroom());
                }

                dataList.add(m);
            }

            return CommonMethod.getReturnData(dataList);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("获取待审核列表失败：" + e.getMessage());
        }
    }

    /**
     * 管理员批准选课申请
     */
    public DataResponse approveSelection(DataRequest dataRequest) {
        try {
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            Integer scoreId = dataRequest.getInteger("scoreId");
            if (scoreId == null) {
                return CommonMethod.getReturnMessageError("选课记录ID不能为空");
            }

            Optional<Score> scoreOpt = scoreRepository.findById(scoreId);
            if (!scoreOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("选课记录不存在");
            }
            Score score = scoreOpt.get();

            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("用户信息不存在");
            }

            Person admin = personOpt.get();
            if (!isAdmin(admin)) {
                return CommonMethod.getReturnMessageError("需要管理员权限");
            }

            if (!"PENDING".equals(score.getSelectionStatus())) {
                return CommonMethod.getReturnMessageError("该选课申请状态不可操作");
            }

            Course course = score.getCourse();
            if (course.getMaxCapacity() != null && course.getCurrentEnrolled() != null 
                    && course.getCurrentEnrolled() >= course.getMaxCapacity()) {
                return CommonMethod.getReturnMessageError("该课程已满，无法批准");
            }

            score.setSelectionStatus("APPROVED");
            score.setApproveTime(new Date());
            score.setApproveBy(admin.getPersonId());
            score.setRejectReason(null);

            if (course.getCurrentEnrolled() == null) {
                course.setCurrentEnrolled(0);
            }
            course.setCurrentEnrolled(course.getCurrentEnrolled() + 1);
            courseRepository.save(course);

            scoreRepository.save(score);

            Map<String, Object> result = new HashMap<>();
            result.put("scoreId", score.getScoreId());
            result.put("message", "选课申请已批准");

            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("批准失败：" + e.getMessage());
        }
    }

    /**
     * 管理员拒绝选课申请
     */
    public DataResponse rejectSelection(DataRequest dataRequest) {
        try {
            // 1. 获取当前登录用户
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 2. 获取选课记录ID和拒绝理由
            Integer scoreId = dataRequest.getInteger("scoreId");
            String rejectReason = dataRequest.getString("rejectReason");

            if (scoreId == null) {
                return CommonMethod.getReturnMessageError("选课记录ID不能为空");
            }
            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                return CommonMethod.getReturnMessageError("拒绝理由不能为空");
            }

            // 3. 查询选课记录
            Optional<Score> scoreOpt = scoreRepository.findById(scoreId);
            if (!scoreOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("选课记录不存在");
            }
            Score score = scoreOpt.get();

            // 4. 验证是否为管理员
            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("用户信息不存在");
            }

            Person admin = personOpt.get();
            if (!isAdmin(admin)) {
                return CommonMethod.getReturnMessageError("需要管理员权限");
            }

            // 5. 验证选课状态是否为待审核
            if (!"PENDING".equals(score.getSelectionStatus())) {
                return CommonMethod.getReturnMessageError("该选课申请状态不可操作");
            }

            // 6. 拒绝选课
            score.setSelectionStatus("REJECTED");
            score.setApproveTime(new Date());
            score.setApproveBy(admin.getPersonId());
            score.setRejectReason(rejectReason.trim());

            scoreRepository.save(score);

            Map<String, Object> result = new HashMap<>();
            result.put("scoreId", score.getScoreId());
            result.put("message", "选课申请已拒绝");

            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("拒绝失败：" + e.getMessage());
        }
    }

    /**
     * 判断用户是否为管理员
     */
    private boolean isAdmin(Person person) {
        if (person.getType() == null) {
            return false;
        }
        String type = person.getType().toString();
        return "0".equals(type) || "1".equals(type);
    }

    /**
     * 学生退课
     */
    public DataResponse dropCourse(DataRequest dataRequest) {
        try {
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            Integer scoreId = dataRequest.getInteger("scoreId");
            if (scoreId == null) {
                return CommonMethod.getReturnMessageError("选课记录ID不能为空");
            }

            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("学生信息不存在");
            }
            Person student = personOpt.get();

            Optional<Score> scoreOpt = scoreRepository.findById(scoreId);
            if (!scoreOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("选课记录不存在");
            }

            Score score = scoreOpt.get();

            if (!score.getStudent().getPersonId().equals(student.getPersonId())) {
                return CommonMethod.getReturnMessageError("无权操作此选课记录");
            }

            if ("APPROVED".equals(score.getSelectionStatus())) {
                Course course = score.getCourse();
                if (course.getCurrentEnrolled() != null && course.getCurrentEnrolled() > 0) {
                    course.setCurrentEnrolled(course.getCurrentEnrolled() - 1);
                    courseRepository.save(course);
                }
            }

            scoreRepository.deleteByScoreIdAndStudentPersonId(scoreId, student.getPersonId());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "退课成功");
            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("退课失败：" + e.getMessage());
        }
    }

    /**
     * 取消选课（简化版退课，通过courseId）
     */
    public DataResponse cancelCourse(DataRequest dataRequest) {
        try {
            System.out.println("️ cancelCourse 被调用");
            System.out.println("请求数据: " + dataRequest.getData());

            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            Integer courseId = dataRequest.getInteger("courseId");
            if (courseId == null) {
                return CommonMethod.getReturnMessageError("课程ID不能为空");
            }

            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("学生信息不存在");
            }
            Person student = personOpt.get();

            Optional<Score> scoreOpt = scoreRepository.findByPersonIdAndCourseId(
                    student.getPersonId(), courseId);
            if (!scoreOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("未找到选课记录");
            }

            Score score = scoreOpt.get();

            if ("APPROVED".equals(score.getSelectionStatus())) {
                Course course = score.getCourse();
                if (course.getCurrentEnrolled() != null && course.getCurrentEnrolled() > 0) {
                    course.setCurrentEnrolled(course.getCurrentEnrolled() - 1);
                    courseRepository.save(course);
                    System.out.println(" 课程人数已减少: " + course.getName() + " -> " + course.getCurrentEnrolled());
                }
            }

            scoreRepository.delete(score);
            System.out.println("✅ 退课成功: courseId=" + courseId);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "退课成功");
            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("退课失败：" + e.getMessage());
        }
    }

    /**
     * 获取课程详情（包含选课人数等信息）
     */
    public DataResponse getCourseDetail(DataRequest dataRequest) {
        try {
            Integer courseId = dataRequest.getInteger("courseId");
            if (courseId == null) {
                return CommonMethod.getReturnMessageError("课程ID不能为空");
            }

            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (!courseOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("课程不存在");
            }

            Course course = courseOpt.get();
            Map<String, Object> result = new HashMap<>();
            result.put("courseId", course.getCourseId());
            result.put("num", course.getNum());
            result.put("name", course.getName());
            result.put("credit", course.getCredit());
            result.put("teacher", course.getTeacher());
            result.put("time", course.getTime());
            result.put("classroom", course.getClassroom());
            result.put("status", course.getStatus());
            result.put("maxCapacity", course.getMaxCapacity());
            result.put("currentEnrolled", course.getCurrentEnrolled());
            result.put("availableSeats", course.getMaxCapacity() != null && course.getCurrentEnrolled() != null 
                    ? course.getMaxCapacity() - course.getCurrentEnrolled() : null);

            Course preCourse = course.getPreCourse();
            if (preCourse != null) {
                result.put("preCourseId", preCourse.getCourseId());
                result.put("preCourseName", preCourse.getName());
            }

            return CommonMethod.getReturnData(result);

        } catch (Exception e) {
            e.printStackTrace();
            return CommonMethod.getReturnMessageError("获取课程详情失败：" + e.getMessage());
        }
    }

    /**
     * 检查课程时间冲突
     */
    private boolean hasTimeConflict(List<Score> approvedCourses, Course newCourse) {
        String newCourseTime = newCourse.getTime();
        if (newCourseTime == null || newCourseTime.isEmpty()) {
            return false;
        }

        for (Score score : approvedCourses) {
            Course existingCourse = score.getCourse();
            String existingTime = existingCourse.getTime();
            
            if (existingTime != null && !existingTime.isEmpty()) {
                if (isTimeOverlap(newCourseTime, existingTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断两个时间字符串是否有重叠
     */
    private boolean isTimeOverlap(String time1, String time2) {
        if (time1.equals(time2)) {
            return true;
        }
        
        String[] days1 = extractDays(time1);
        String[] days2 = extractDays(time2);
        
        for (String day1 : days1) {
            for (String day2 : days2) {
                if (day1.equals(day2)) {
                    String period1 = extractPeriod(time1);
                    String period2 = extractPeriod(time2);
                    if (period1 != null && period2 != null && period1.equals(period2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String[] extractDays(String time) {
        if (time.contains("星期")) {
            StringBuilder days = new StringBuilder();
            int index = 0;
            while ((index = time.indexOf("星期", index)) != -1) {
                if (index + 2 < time.length()) {
                    days.append(time.charAt(index + 2)).append(",");
                }
                index += 3;
            }
            if (days.length() > 0) {
                return days.toString().split(",");
            }
        }
        return new String[]{time};
    }

    private String extractPeriod(String time) {
        if (time.contains("第") && time.contains("节")) {
            int start = time.indexOf("第");
            int end = time.indexOf("节");
            if (start != -1 && end != -1 && end > start) {
                return time.substring(start, end + 1);
            }
        }
        return null;
    }

    /**
     * 检查前置课程是否完成
     */
    private boolean hasCompletedPreCourse(Integer personId, Course course) {
        Course preCourse = course.getPreCourse();
        if (preCourse == null) {
            return true;
        }

        Optional<Score> preCourseScore = scoreRepository.findApprovedByPersonIdAndCourseId(
                personId, preCourse.getCourseId());
        
        return preCourseScore.isPresent() && preCourseScore.get().getMark() != null 
                && preCourseScore.get().getMark() >= 60;
    }
}