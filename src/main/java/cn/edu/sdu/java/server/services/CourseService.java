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

            // 3. 获取该学生已选的课程ID
            List<Score> studentScores = scoreRepository.findByStudentPersonId(student.getPersonId());
            Set<Integer> selectedCourseIds = studentScores.stream()
                    .map(score -> score.getCourse().getCourseId())
                    .collect(Collectors.toSet());

            // 4. 获取所有开放选课的课程
            List<Course> allCourses = courseRepository.findByStatus("OPEN");

            // 5. 过滤掉已选课程
            List<Course> availableCourses = allCourses.stream()
                    .filter(course -> !selectedCourseIds.contains(course.getCourseId()))
                    .collect(Collectors.toList());

            // 6. 转换为前端需要的格式
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

                // 前置课程信息
                Course preCourse = course.getPreCourse();
                if (preCourse != null) {
                    m.put("preCourseName", preCourse.getName());
                    m.put("preCourseId", preCourse.getCourseId());
                }

                dataList.add(m);
            }

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
            // 1. 获取当前登录用户
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 2. 获取课程ID
            Integer courseId = dataRequest.getInteger("courseId");
            if (courseId == null) {
                return CommonMethod.getReturnMessageError("课程ID不能为空");
            }

            // 3. 查询课程信息
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (!courseOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("课程不存在");
            }
            Course course = courseOpt.get();

            // 4. 检查课程是否开放选课
            if (!"OPEN".equals(course.getStatus())) {
                return CommonMethod.getReturnMessageError("该课程暂不开放选课");
            }

            // 5. 查询学生信息
            Optional<Person> personOpt = personRepository.findByNum(currentUsername);
            if (!personOpt.isPresent()) {
                return CommonMethod.getReturnMessageError("学生信息不存在");
            }
            Person student = personOpt.get();

            // 6. 检查是否已选过该课程
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

            // 7. 创建选课记录
            Score score = new Score();

            // 创建 Student 对象并设置关联
            Optional<Student> studentOpt = studentRepository.findById(student.getPersonId());
            if (studentOpt.isPresent()) {
                score.setStudent(studentOpt.get());
            } else {
                // 如果 Student 记录不存在，需要创建
                Student newStudent = new Student();
                newStudent.setPersonId(student.getPersonId());
                studentRepository.save(newStudent);
                score.setStudent(newStudent);
            }

            score.setCourse(course);
            score.setSelectionStatus("PENDING");  // 待审核
            score.setApplyTime(new Date());

            // 保存选课记录
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
     * 学生查看自己的选课记录
     */
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
            // 1. 获取当前登录用户
            String currentUsername = getCurrentUsername();
            if (currentUsername == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 2. 获取选课记录ID
            Integer scoreId = dataRequest.getInteger("scoreId");
            if (scoreId == null) {
                return CommonMethod.getReturnMessageError("选课记录ID不能为空");
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

            // 6. 批准选课
            score.setSelectionStatus("APPROVED");
            score.setApproveTime(new Date());
            score.setApproveBy(admin.getPersonId());
            score.setRejectReason(null);

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
        // 修正：判断是否为管理员（假设管理员类型为 0 或 1）
        if (person.getType() == null) {
            return false;
        }
        String type = person.getType().toString(); // 转为字符串比较
        return "0".equals(type) || "1".equals(type);
    }
}